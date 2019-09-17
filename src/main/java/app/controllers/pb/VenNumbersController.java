package app.controllers.pb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;

import app.controllers.APIController;
import app.exceptions.DBRecordExistsException;
import app.exceptions.ErrorCodes;
import app.models.pb.VenNumber;
import app.util.ExcelFileHelper;
import app.util.JsonHelper;
import app.util.UniversalQueryHelper;

/**
 * @author 
 */
@RESTful
@SuppressWarnings("unchecked")
public class VenNumbersController extends APIController {
  private final String[] includeFields = {"num", "short_num", "network"};
  
  public void index(){
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    long caseId = Long.parseLong(param("case_id"));

    // @formatter:off
    String sql = "SELECT vn.* FROM ven_numbers as vn LEFT JOIN pnum_labels pl ON vn.num = pl.num AND pl.case_id = ? " +
                 "WHERE vn.case_id = ? " +
                 "ORDER BY pl.color_order IS NULL, pl.color_order ASC, vn.num ASC ";
    // @formatter:on
    
    Paginator p = new Paginator(VenNumber.class, pageSize, sql, caseId, caseId);
    List<VenNumber> venNumbers = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "ven_numbers", venNumbers);
    render();
  }

  public void create() throws IOException, DBRecordExistsException {
    Map payload = JsonHelper.toMapWithIncludes(getRequestString(), includeFields);
    long caseId = Long.parseLong(param("case_id"));

    VenNumber venNumber = new VenNumber(caseId, VenNumber.MAN_INPUT_SOURCE);
    venNumber.fromMap(payload);
    try {
      if (venNumber.saveIt()) {
        setOkView("created");
        view("ven_number", venNumber);
        render("_ven_number");
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
    VenNumber v = (VenNumber)VenNumber.findById(getId());
    if(v == null) {
      view("reason", "no such ven-number", "code", 400);
      render("/common/error");
    } else {
      setOkView("show");
      view("ven_number", v);
      render("_ven_number"); // unusual use of a partial - we are doing it for reuse
    }
  }

  public void update() throws IOException, DBRecordExistsException {
    VenNumber v = VenNumber.findById(getId());
    if(v == null) {
      setErrorView("no such ven-number", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMap(getRequestString());
      v.fromMap(payload);
      v.setId(getId());
      v.setCaseId(Long.parseLong(param("case_id")));
      try {
        if (v.saveIt()) {
          setOkView("updated");
          view("ven_number", v);
          render("_ven_number");
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

  public void destroy() {
    VenNumber.delete("id = ?", getId());

    setOkView("deleted");
    view("id", getId());
    render("/common/ok");
  }
  
  @POST
  public void search() throws Exception{
  	int currentPage = getCurrentPage(param("page"));
  	int pageSize = getPageSize(param("pagesize"));
  	
    // Map filter = JsonHelper.toMap(getRequestString());
    // filter.put("case_id", param("case_id"));
    String json = getRequestString();
    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(json,
        "case_id", param("case_id"));

    String sql = (String)sqlAndVals.get(0);
    List vals = (List)sqlAndVals.get(1);

  	Paginator p = new Paginator(VenNumber.class, pageSize, sql, vals.stream().toArray(Object[]::new))
                     .orderBy("id desc");
  	
  	List<VenNumber> venNumbers = p.getPage(currentPage);
  	setOkView();
  	view("page_total", p.pageCount(),
  			 "page_current",currentPage,
  			 "ven_numbers",venNumbers);
  	render("index");		 
  }
  
  @POST
  public void upload() throws Exception {
    String caseId = param("case_id");
    
    List<FormItem> items = multipartFormItems();
    ArrayList<VenNumber> venNumbers = new ArrayList<>();
    
    for (FormItem item : items) {
      if (item.isFile()) {
        InputStream is = item.getInputStream();
        Map<String, String> header = new HashMap<>();
        header.put("长号", "num");
        header.put("短号", "short_num");
        header.put("虚拟网", "network");
        header.put("标注", "label");

        List<VenNumber> r = ExcelFileHelper.extract(header, is, VenNumber.class, 
                                                    (model) -> model.setSource(VenNumber.BATCH_INPUT_SOURCE));
        venNumbers.addAll(r);
      }
    }
    
    session("upload.vennum", venNumbers);
    setOkView("uploaded");
    view("ven_numbers", venNumbers);    
    render("index");
  }

  @POST
  public void doImport() throws SQLException{
    PreparedStatement ps = null;
    String caseId = param("case_id");
    try {
      ArrayList<VenNumber> venNums = (ArrayList<VenNumber>)session("upload.vennum");
      
      if(venNums != null) {
        String sql = "INSERT IGNORE INTO ven_numbers SET case_id = ?, num = ?, short_num = ?, network = ?, label = ?, source = ?, created_at = ?, updated_at = ?";
        ps = Base.startBatch(sql);
        
        Base.openTransaction();
        for(VenNumber venNum : venNums) {
          Base.addBatch(ps, caseId, 
                           venNum.getNum(), 
                           venNum.getShortNum(), 
                           venNum.getNetwork(),
                           venNum.getLabel(),
                           venNum.getSource(),
                           venNum.getCreatedAt(),
                           venNum.getUpdatedAt());
        }
        Base.executeBatch(ps);
        Base.commitTransaction();

        session().remove("upload.vennum");
        setOkView("imported");
      } else {
        setErrorView("no data in session", 405);
      }
    } catch(Exception e){
      //e.printStackTrace();
      Base.rollbackTransaction();
    } finally{
      if(ps != null) {
        ps.close();
      }
      render("/common/_blank");
    }
  }

  @GET
  public void networks() {
    Long caseId = Long.parseLong(param("case_id"));
    String fullQuery = "SELECT DISTINCT network FROM ven_numbers WHERE case_id = ?";
    List<VenNumber> venNums = VenNumber.findBySQL(fullQuery, caseId);

    setOkView();
    view("venNums", venNums); 
    render();
  }

  @POST
  public void abortImport() {
    session().remove("upload.vennum");
    setOkView("aborted");
    render("/common/_blank");
  }
}