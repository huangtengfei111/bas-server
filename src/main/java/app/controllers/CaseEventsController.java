package app.controllers;

import app.util.JsonHelper;
import app.models.CaseEvent;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.RESTful;
import org.javalite.activejdbc.Paginator;
import org.javalite.common.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 
 */
@RESTful @SuppressWarnings("unchecked")
public class CaseEventsController extends APIController {

  public void index(){
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    
    Paginator<CaseEvent> p = new Paginator<CaseEvent>(CaseEvent.class, pageSize, 
                                "case_id  = ?", param("case_id")
                                ).orderBy("id desc");
    
    List<CaseEvent> caseEvents = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "case_events", caseEvents);
    render();    
  }

  public void create() throws IOException {
    
    Map payload = JsonHelper.toMap(getRequestString());
    CaseEvent ce = new CaseEvent();
    ce.fromMap(payload);
    ce.set("case_id", param("case_id"));
    ce.saveIt();
    
    setOkView("created");
    view("case_event", ce);
    render("_case_event");
  }

  public void show(){
  	/*
    RelNumber v = (RelNumber)RelNumber.findById(getId());
    if(v == null) {
      view("reason", "no such number", "code", 400);
      render("/common/error");
    } else {
      view("rel_number", v);
      render("_rel_number"); // unusual use of a partial - we are doing it for reuse
    }
    */
  }

  public void update() throws IOException {

    CaseEvent v = (CaseEvent)CaseEvent.findById(getId());
    if(v == null) {
    	setErrorView("no such case event", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMap(getRequestString());
      v.fromMap(payload);
      v.set("id", getId());
      v.set("case_id", param("case_id"));
      v.saveIt();
      
      setOkView("updated");
      view("case_event", v);
      render("_case_event");
    } 
  }

  public void destroy() {
    CaseEvent.delete("id = ?", getId());
    
    setOkView("deleted");
    view("id", getId());
    render("/common/ok");
  }

}