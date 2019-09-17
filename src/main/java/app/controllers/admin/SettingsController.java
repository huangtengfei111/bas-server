package app.controllers.admin;

import app.models.Setting;
import app.util.JsonHelper;
import app.controllers.APIController;

import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.RESTful;
import org.javalite.activeweb.annotations.GET;
import org.javalite.common.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 
 */
@RESTful @SuppressWarnings("unchecked")
public class SettingsController extends APIController {
	
	private String[] reserved = {"id", "account_id"};
	
  public void index() {
  }

  /**
   * Global settings
   */
  @GET
  public void global() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    
    Paginator p = new Paginator(Setting.class, pageSize, "account_id = 0").orderBy("id desc");
    List<Setting> settings = p.getPage(currentPage);
    
    setOkView();
    view("page_total", p.pageCount(),
         "page_current",currentPage,
         "settings",settings);
    render("index");
  }

  /**
   *
   */
  @GET
  public void account() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    
    Paginator p = new Paginator(Setting.class, pageSize, "account_id > 0").orderBy("id desc");
    List<Setting> settings = p.getPage(currentPage);
    
    setOkView();
    view("page_total", p.pageCount(),
         "page_current",currentPage,
         "settings",settings);
    render("index");
  }

  public void create() throws IOException {
  	Map payload = JsonHelper.toMap(getRequestString());
  	Setting s = new Setting();
  	s.fromMap(payload);
  	s.set("id", getId());
  	s.saveIt();
  	
  	setOkView("created");
  	view("setting", s);
  	render("_setting");
  }

  public void update() throws IOException {
  	Setting s = (Setting)Setting.findById(getId());
  	if (s == null) {
			view("no such setting", 400);
			render("/common/_blank");
		} else {
			Map payload = JsonHelper.toMapWithIgnores(getRequestString(),reserved);
			s.fromMap(payload);
			s.saveIt();
			
			setOkView("updated");
			view("setting", s);
			render("_setting");
		}
  }

  public void destroy() {
  	Setting.delete("id = ?", getId());
  	setOkView("deleted");
  	view("id", getId());
    render("/common/ok");
  }

}