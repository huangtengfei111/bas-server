package app.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;

import app.exceptions.DBRecordExistsException;
import app.exceptions.ErrorCodes;
import app.jobs.AppEventListener;
import app.jobs.RefreshCacheListener;
import app.jobs.events.CaseDeletedEvent;
import app.models.Case;
import app.models.CaseBreakpoint;
import app.models.CaseEvent;
import app.models.pb.CtLabel;
import app.models.pb.Pbill;
import app.models.pb.PnumLabel;
import app.models.pb.RelNumber;
import app.models.pb.VenNumber;
import app.util.JsonHelper;
import app.util.UniversalQueryHelper;

/**
 * @author 
 */
@RESTful @SuppressWarnings("unchecked")
public class CasesController extends APIController {

  private final String[] allowedFields =
      { "name", "num", "started_at", "ended_at", "operator", "status", "memo" };

  public void index() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    long userId = userIdInSession();
    Paginator p = new Paginator(Case.class, pageSize, "created_by = ?", userId).orderBy("updated_at desc");
    
    List<Case> cases = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "cases", cases);
    render();
  }

  public void create() throws IOException, DBRecordExistsException {
    Map payload = JsonHelper.toMapWithIncludes(getRequestString(), allowedFields);
    Case c = new Case();
    c.fromMap(payload);
    c.setCreatedBy(userIdInSession());
    try {
      if (c.saveIt()) {
        setOkView("created");
        view("case", c);
        render("_case");
      }
    } catch (DBException e) {
      logError(e);
//      if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
//        throw new DBRecordExistsException();
//      } else {
        setErrorView(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
        render("/common/error");
//      }
    }
  }

  public void show() {
    Case c = (Case)Case.findById(getId());
    if(c == null) {
      setErrorView("no such case", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/_blank");
    } else {
      view("case", c);
      render();
    }
  }

  public void update() throws IOException, DBRecordExistsException {
    Case c = Case.findById(getId());
    if(c == null) {
      setErrorView("no such case", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMapWithIncludes(getRequestString(), allowedFields);
      c.fromMap(payload);
      c.setId(getId());
      // c.setAccountId(1);
      try {
        if (c.saveIt()) {
          setOkView("updated");
          view("case", c);
          render("_case");
        }
      } catch (DBException e) {
        logError(e);
//        if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
//          throw new DBRecordExistsException();
//        } else {
          setErrorView(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
          render("/common/error");
//        }
      }
    } 
  }

  @GET
  public void filter() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));    
    long userId = userIdInSession();
    int statusInt = Case.getStatusInt(param("status"));
    String query = "created_by = ? AND status = ?";
    Paginator p = new Paginator(Case.class, pageSize, query, userId, statusInt).orderBy("updated_at desc");
    
    List<Case> cases = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "cases", cases);
    render("index");
  }

  /**
   *
   */
  @POST
  public void search() throws Exception {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    long userId = userIdInSession();
    
    String json = getRequestString();
    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(json, "created_by", Long.toString(userId));
    
    String query = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    Object[] params = vals.stream().toArray(Object[]::new);
    
    Paginator p = new Paginator(Case.class, pageSize, query, params).orderBy("id desc");
    
    List<Case> cases = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "cases", cases);
    render("index");
  }

  
  @GET
  public void summary() {
    int caseId = Integer.parseInt(param("case_id"));

    Case.update("updated_at = ?", "id = ?", new Date(), caseId);
    Case c = Case.findById(caseId);
    setOkView();
    view("case", c);
    render("_case");
  }
  
  @DELETE
  public void destroy() {
    long id = Long.parseLong(getId());
    try {
      Base.openTransaction();      
      // 删除案件的话单
      //@formatter:off
      String getCasePbillsSql = "SELECT p.* " +
                                "FROM pbills p LEFT JOIN cases_pbills as cp ON p.id = cp.pbill_id " +
                                "WHERE cp.case_id = ? ";
      String otherCaseRefSql = "SELECT p.* " +
                               "FROM cases_pbills as cp LEFT JOIN pbills p ON p.id = cp.pbill_id " + 
                               "WHERE p.id = ? AND cp.case_id != ?";
     //@formatter:on
      List<Pbill> pbills = Pbill.findBySQL(getCasePbillsSql, id);

      for (Pbill pbill : pbills) {
        List<Pbill> pbs = Pbill.findBySQL(otherCaseRefSql, pbill.getId(), id);
        if (pbs.size() == 0) {
          Pbill.delete("id = ?", pbill.getId());
        }
      }

      getCasePbillsSql = "DELETE p FROM pbills AS p LEFT JOIN cases_pbills as cp ON p.id = cp.pbill_id "
          + "WHERE cp.case_id = ? AND HAVING()";

      // 号码标注
      PnumLabel.delete("case_id = ?", id);
      // 基站标注
      CtLabel.delete("case_id = ?", id);
      // 事件标注
      CaseEvent.delete("case_id = ?", id);
      // 虚拟网
      VenNumber.delete("case_id = ?", id);
      // 亲情网
      RelNumber.delete("case_id = ?", id);
      // 时间分割点
      CaseBreakpoint.delete("case_id = ?", id);
      //删除案件
      Case.delete("id = ?", id);
      
      Base.commitTransaction();

      List<AppEventListener> listeners = new ArrayList<>();
      listeners.add(new RefreshCacheListener());
      registerAndPost(new CaseDeletedEvent(id), listeners);

      setOkView("deleted case");
      view("id", id);
      render("/common/ok");
    } catch (Exception e) {
      Base.rollbackTransaction();
      e.printStackTrace();
      setErrorView("delete failed", ErrorCodes.INTERNAL_ERROR);
      render("/common/error");
    }
  }
}