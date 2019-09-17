package app.controllers.admin;

import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.annotations.GET;

import app.controllers.APIController;


public class MissedCtRequestsController extends APIController{
  
  @GET
  public void index() {
    
    //@formatter:off
    String queryList = "SELECT code, count(*) as count ,min(created_at) as min_created_at, max(created_at) as max_created_at " + 
                       "FROM missed_ct_requests " + 
                       "GROUP BY code ORDER BY count desc;";
    //@formatter:on
    
//    String queryCodeCount="select count(*) count from (select distinct code from missed_ct_requests) as t";
    
//    List<Map> count = Base.findAll(queryCodeCount);
     
//    long currentPage = getCurrentPage(param("page"));
//    long pageSize = 10;
    
//    long codeCount = (long) count.get(0).get("count");
//    long pageTotal = codeCount/pageSize + (codeCount%pageSize == 0 ? 0 : 1);
    
    List<Map> missedCtReqs = Base.findAll(queryList);
    
    setOkView("missed_ct_requests list");
    view("missed_ct_requests", missedCtReqs);
    render();
  }

}
