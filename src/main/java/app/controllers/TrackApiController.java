package app.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.annotations.RESTful;

import app.exceptions.ErrorCodes;
import app.models.ApiTrack;
import app.models.Setting;
import app.util.JsonHelper;

@RESTful
public class TrackApiController extends APIController {

  public void index() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    Paginator p = new Paginator(ApiTrack.class, pageSize, "1 = 1")
        .orderBy("created_at DESC");

    List<ApiTrack> apiTracks = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), 
         "page_current", currentPage, 
         "apiTracks", apiTracks);
    render();

  }

  public void create() throws IOException, SQLException {
    Map payload = JsonHelper.toMap(getRequestString());
    if (payload != null) {
      String systemId = (String) payload.get("app_id");
      String hostId = (String) payload.get("app_key");
      // 记录保存在中心服务器
      if (Setting.isSuperNode()) {
        ApiTrack apiTrack = new ApiTrack();
        apiTrack.fromMap(payload);
        apiTrack.setSystemId(systemId);
        apiTrack.setHostId(hostId);
        if (apiTrack.saveIt()) {
          setOkView("created api_tracks");
          view("apiTrack", apiTrack);
          render("_apiTrack");
        } else {
          setErrorView("created api_tracks failed", ErrorCodes.INTERNAL_ERROR);
          render("/common/error");
        }
      }
    }
  }
  public void topActive() {
    String sql = "SELECT l.holder AS holder ,count(l.holder) AS count " + 
                 "FROM api_tracks AS a LEFT JOIN licenses AS l " + 
                 "ON a.host_id = l.host_id " + 
                 "WHERE DATE_SUB(CURDATE(), INTERVAL 30 DAY) <= DATE(a.created_at) " + 
                 "GROUP BY holder " + 
                 "ORDER BY count DESC " + 
                 "LIMIT 10";
    
    List<Map> topActive = Base.findAll(sql);
    
    setOkView("top active customers");
    view("top_active_customers", topActive);
    render();
  }
  public void topInactive() {
    String sql = "SELECT l.holder AS holder ,count(l.holder) AS count " + 
        "FROM api_tracks AS a LEFT JOIN licenses AS l " + 
        "ON a.host_id = l.host_id " + 
        "WHERE DATE_SUB(CURDATE(), INTERVAL 30 DAY) <= DATE(a.created_at) " + 
        "GROUP BY holder " + 
        "ORDER BY count " + 
        "LIMIT 10";
    
    List<Map> topInactive = Base.findAll(sql);
    
    setOkView("top inactive customers");
    view("top_inactive_customers", topInactive);
    render();
  }
}
