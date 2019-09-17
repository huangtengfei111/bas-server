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
import app.models.pb.RelNumber;
import app.util.ExcelFileHelper;
import app.util.JsonHelper;

/**
 * @author 
 */
@RESTful @SuppressWarnings("unchecked")
public class RelNumbersController extends APIController {
//  private static final String MAN_INPUT_SOURCE = "2";// 数据来源
  
  public void index(){
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    long caseId = Long.parseLong(param("case_id"));
    
    //@formatter:off
    String sql = "SELECT rn.* FROM rel_numbers as rn LEFT JOIN pnum_labels pl ON rn.num = pl.num AND pl.case_id = ? " +
                 "WHERE rn.case_id = ? " +
                 "ORDER BY pl.color_order IS NULL, pl.color_order ASC, rn.num ASC ";
    //@formatter:on

    Paginator p = new Paginator(RelNumber.class, pageSize, sql, caseId, caseId);
    List<RelNumber> relNumbers = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "rel_numbers", relNumbers);

    render();
  }

  public void create() throws IOException, DBRecordExistsException {
    Long caseId = Long.parseLong(param("case_id"));
    Map payload = JsonHelper.toMap(getRequestString());
    RelNumber rel = new RelNumber(caseId, RelNumber.MAN_INPUT_SOURCE);
    rel.fromMap(payload);
    try {
      if (rel.saveIt()) {
        setOkView("created");
        view("rel_number", rel);
        render("_rel_number");
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

  public void show(){
    RelNumber v = (RelNumber)RelNumber.findById(getId());
    if(v == null) {
      setErrorView("no such number", 400);
      render("/common/_blank");
    } else {
      view("rel_number", v);
      render("_rel_number"); // unusual use of a partial - we are doing it for reuse
    }
  }

  public void update() throws IOException, DBRecordExistsException {
    RelNumber rel = RelNumber.findById(getId());
    if (rel == null) {
      setErrorView("no such rel-number", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMap(getRequestString());
      rel.fromMap(payload);
      rel.setId(getId());
      rel.setCaseId(Long.parseLong(param("case_id")));
      try {
        if (rel.saveIt()) {
          setOkView("updated");
          view("rel_number", rel);
          render("_rel_number");
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
    RelNumber.delete("id = ?", getId());
    
    setOkView("deleted");
    view("id", getId());
    render("/common/ok");
  }

  @POST
  public void search() throws IOException {
  	int currentPage = getCurrentPage(param("page"));
  	int pageSize = getPageSize(param("pagesize"));
  	
  	ArrayList<String> conds = new ArrayList<String>();
  	ArrayList vals = new ArrayList();
  	
  	Map payload = JsonHelper.toMap(getRequestString());
  	
  	payload.put("case_id", param("case_id"));
  	payload.forEach((key,value) -> {
  		String keyStr = key.toString();
  		
  		if (value instanceof List) {
        List v = (List)value;
        String op = v.get(0).toString();
        Object val = v.get(1);
        switch (op) {
          case "FUZZY":
            conds.add(keyStr + " LIKE ?");
            vals.add("%" + val.toString() + "%");
            break;
          default:
            conds.add(keyStr + " = ?");
            vals.add(val.toString());
            break;
        }
      } else if (value instanceof String) {
        conds.add(keyStr + " = ?");
        vals.add(value.toString());
      }
  		
  	});
  	String query = String.join(" AND ", conds);
  	
  	Paginator p = new Paginator(RelNumber.class, pageSize, query, vals.stream().toArray(Object[]::new)).orderBy("id desc");
  	List<RelNumber> relNumbers = p.getPage(currentPage);
  	setOkView();
  	view("page_total", p.pageCount(),
  			 "page_current",currentPage,
  				"rel_numbers",relNumbers);
  	render("index");
  }
  
  @POST
  public void upload() throws Exception {
  	String caseId = param("case_id");
  	
  	List<FormItem> items = multipartFormItems();
  	ArrayList<RelNumber> relNumbers = new ArrayList<>();
  	
  	for (FormItem item : items) {
      if (item.isFile()) {
        InputStream is = item.getInputStream();
        Map<String, String> header = new HashMap<>();
        header.put("长号", "num");
        header.put("短号", "short_num");
        header.put("标注", "label");
        header.put("亲情网", "network");
        
        List<RelNumber> r = ExcelFileHelper.extract(header, is, RelNumber.class, 
                                                   (model) -> model.setSource(RelNumber.MAN_INPUT_SOURCE));
        relNumbers.addAll(r);
      }
    }
  	
  	session("upload.relNumbers", relNumbers);
  	setOkView("uploaded");
  	view("rel_numbers", relNumbers);
  	render("index");
  }
  
  @POST
  public void doImport() throws SQLException {
  	PreparedStatement ps = null;
  	String caseId = param("case_id");
  	try {
  	ArrayList<RelNumber> relNumbers = (ArrayList<RelNumber>)session("upload.relNumbers");
  	
  	if (relNumbers != null) {
			String sql = "INSERT IGNORE INTO rel_numbers SET case_id = ?, num = ?, short_num = ?, network = ?, label = ?, source = ?, created_at = ?, updated_at = ?";
			ps=Base.startBatch(sql);
			
			Base.openTransaction();
			for (RelNumber relNumber : relNumbers) {
				Base.addBatch(ps, caseId,
												 relNumber.getNum(),
												 relNumber.getShortNum(),
												 relNumber.getNetwork(),
												 relNumber.getLabel(),
												 relNumber.getSource(),
												 relNumber.getCreatedAt(),
												 relNumber.getUpdatedAt());
			}
			Base.executeBatch(ps);
			Base.commitTransaction();
			
			session().remove("upload.relNumbers");
			setOkView("doImported");
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
  
  @POST
  public void abortImport() {
  	session().remove("upload.relNumbers");
  	setOkView("aborted");
  	render("/common/_blank");
  }
  
  @GET
  public void networks() {
    Long caseId = Long.parseLong(param("case_id"));
    String fullQuery = "SELECT DISTINCT network FROM rel_numbers WHERE case_id = ?";
    List<RelNumber> relNums = RelNumber.findBySQL(fullQuery, caseId);

    setOkView();
    view("relNums", relNums); 
    render();
  }
}