package app.controllers;

import app.util.JsonHelper;
import app.models.CaseBreakpoint;
import app.models.CaseEvent;

import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.javalite.activeweb.annotations.RESTful;
import org.javalite.activejdbc.Paginator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 
 */
@RESTful @SuppressWarnings("unchecked")
public class CaseBreakpointsController extends APIController {
	
	@GET
	public void index() {
		int page = getCurrentPage(param("page"));
		int pageSize = getPageSize(param("pageSize"));
		Paginator<CaseBreakpoint> paginator = new Paginator<CaseBreakpoint>(CaseBreakpoint.class, 
									  pageSize, "case_id = ?", param("case_id")).orderBy("id desc");
		List<CaseBreakpoint> breakpoints = paginator.getPage(page);
		setOkView();
		view(
		   "page_total",paginator.pageCount(),
			 "page_current",page,			 
			 "case_breakpoints",breakpoints
			);
		render();
	
	}
	@POST
	public void create() throws IOException {
		Map payload = JsonHelper.toMap(getRequestString());
		CaseBreakpoint breakpoint = new CaseBreakpoint();
		breakpoint.fromMap(payload);
		breakpoint.set("case_id", param("case_id"));
		breakpoint.saveIt();
		
		setOkView("created");
	  view("case_breakpoint", breakpoint);
	  render("_case_breakpoint");

	}
	
	@PUT
	public void update() throws IOException {
		CaseBreakpoint breakpoint = CaseBreakpoint.findById(getId());
		if (breakpoint == null) {
		  setErrorView("no such case event", 400);
      render("/common/_blank");
		}else {
			Map map = JsonHelper.toMap(getRequestString());
			breakpoint.fromMap(map);
			breakpoint.set("case_id", param("case_id"));
			breakpoint.setId(getId());
			breakpoint.saveIt();
			
			setOkView("updated");
      view("case_breakpoint", breakpoint);
      render("_case_breakpoint");
		}
	}
	
	@DELETE
	public void destroy() {
	  CaseBreakpoint.delete("id = ?", getId());
    
    setOkView("deleted");
    view("id", getId());
    render("/common/ok");
	}
}