package app.controllers.pb;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.annotations.RESTful;

import app.controllers.APIController;
import app.models.pb.PubServiceNum;
import app.util.JsonHelper;

@RESTful
public class PubServiceNumsController extends APIController {

  private final String[] allowedFields = { "num", "memo", "grup" };

  public void index() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    Paginator p = new Paginator(PubServiceNum.class, pageSize, "1 = 1");
    
    List<PubServiceNum> psns = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "pub_service_nums", psns);
    render();

  }

  public void create() throws IOException {
    String json = getRequestString();
    Map payload = JsonHelper.toMapWithIncludes(json, allowedFields);

    PubServiceNum psn = new PubServiceNum();
    psn.fromMap(payload);
    psn.saveIt();

    setOkView("created");

    view("pubServiceNum", psn);
    render("_pubServiceNum");

  }

  public void update() throws IOException {
    PubServiceNum psn = PubServiceNum.findById(getId());
    if (psn == null) {
      setErrorView("no such searches", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMapWithIncludes(getRequestString(),allowedFields);
      psn.fromMap(payload);
      psn.set("id", getId());
      if (psn.saveIt()) {
        setOkView("updated");
        view("pubServiceNum", psn);
        render("_pubServiceNum");
      } else {
        setErrorView("update pubServiceNum failed", 400);
        render("/common/_blank");
      }
    }
  }

  public void destroy() {
    int d = PubServiceNum.delete("id = ?", getId());

    if (d == 0) {
      render("error");
    } else {
      setOkView("deleted");
      view("id", getId());
      render("/common/ok");
    }
  }
}
