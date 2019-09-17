package app.controllers;

import app.util.JsonHelper;
import app.controllers.APIController;
import org.javalite.activeweb.AppController;
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
public class RestfulController extends APIController {

  public void index(){
    // view("relNumbers", RelNumber.where("case_id = ?", param("case_id")).orderBy("id"));
    render().contentType("application/json");
  }

  public void create() throws IOException {
    /*
    Map payload = JsonHelper.toMap(getRequestString());
    RelNumber v = new RelNumber();
    v.fromMap(payload);
    v.set("case_id", param("case_id"));
    v.saveIt();
    */
    // view("message", "created", "id", v.get("id"));
    render("/common/ok");
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
    /*
    RelNumber v = (RelNumber)RelNumber.findById(getId());
    if(v == null) {
      view("reason", "no such number", "code", 400);
      render("/common/error");
    } else {
      Map payload = JsonHelper.toMap(getRequestString());
      v.fromMap(payload);
      v.set("id", getId());
      v.set("case_id", param("case_id"));
      v.saveIt();

      view("message", "updated", "id", getId());
      render("/common/ok");
    } 
    */
  }

  public void destroy() {
    // RelNumber.delete("id = ?", getId());
    // view("message", "deleted", "id", getId());
    render("ok");
  }

}