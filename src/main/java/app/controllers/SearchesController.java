package app.controllers;

import app.util.JsonHelper;
import app.controllers.APIController;
import app.models.Search;

import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;
import org.javalite.common.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 
 */
@RESTful @SuppressWarnings("unchecked")
public class SearchesController extends APIController {
  
  public void index() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    
    Paginator p = new Paginator(Search.class, pageSize, "case_id  = ?", param("case_id")).orderBy("id desc");
    
    List<Search> searches = p.getPage(currentPage);
    setOkView();
    view("page_total", p.getCount(),
         "page_current", currentPage,
         "_search", searches);
    render();
  }
  
  public void create() throws IOException {
    Map payload = JsonHelper.toMap(getRequestString());
    Search s = new Search();
    s.fromMap(payload);
    s.set("case_id", param("case_id"));
    s.setAccountId("1");
    s.saveIt();
    
    setOkView("created");
    view("search", s);
    render("_search");
  }
  
  public void update() throws IOException {
    Search s = (Search)Search.findById(getId());
    if (s == null) {
      setErrorView("no such searches", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMap(getRequestString());
      s.fromMap(payload);
      s.set("id", getId());
      s.set("case_id", param("case_id"));
      s.saveIt();
      
      setOkView("updated");
      view("search", s);
      render("_search");
    }
  }
  
  public void destroy() {
    Search.delete("id = ?", getId());
    
    setOkView("deleted");
    view("id", getId());
    render("/common/ok");
  }
}