package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.util.List;
import java.util.Map;

import org.javalite.activeweb.annotations.POST;

import app.controllers.ReportController;
import app.util.StatColHeader;
import app.util.collections.ListMap;

/**
 * 1 2 3 4
 *
 */
public class PbillOverviewReportController extends ReportController {

	public void index() {
    if("xml".equals(format())){
      render().noLayout().contentType("text/xml");
    }

    if("json".equals(format())){
      render().noLayout().contentType("application/json");
    }
	}


  /**
   * 1# 计费类型
   * 
   * @throws Exception
   */
  @POST
  public void groupByBillType() throws Exception {
  	String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.bill_type as bt, COUNT(1) as count " + 
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.bill_type";
    String orderBy = "ORDER BY count desc";
    //@formatter:on

    List<Map> lm1 = doStat(requestString, selectSql, groupBy, orderBy, "pr", map("cp.case_id", param("case_id")));
    Map<String, Long> map = ListMap.toCountMap(lm1, "bt", (bt) -> StatColHeader.billType(bt));
    
    setOkView("stat: group by billtype");
    view("oneMap", map);


    if("xlsx".equals(format())){

      view("xlsxHeader", GROUP_BY_BILL_TYPE_HEADER);
      renderExcel(GROUP_BY_BILL_TYPE_HEADER, map, GROUP_BY_BILL_TYPE_FILE);
//      renderExcel(GROUP_BY_BILL_TYPE_FILE);
    } else {
      render("/reports/map");
    }
  }	

  /**
   * 2# 联系类型
   * 
   * @throws Exception
   */
  @POST
  public void groupByCommDirection() throws Exception {
  	String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.comm_direction as cd, COUNT(1) as count " + 
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id  ";
    String groupBy = "ORDER BY count desc";
    String orderBy = "GROUP BY pr.comm_direction";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, orderBy, "pr", map("cp.case_id", param("case_id")));
   
    Map<String, Long> map = ListMap.toCountMap(lm1, "cd", (cd) -> StatColHeader.commDirection(cd));

    setOkView("stat: group by comm_direction");
    view("oneMap", map);

    render("/reports/map");
  }

  /**
   * 3# 通话状态
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumStatus() throws Exception {
  	String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.owner_num_status as ons, COUNT(1) as count " + 
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id  ";
    String groupBy = "ORDER BY count DESC";
    String orderBy = "GROUP BY pr.owner_num_status";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, orderBy, groupBy, "pr", map("cp.case_id", param("case_id")));

    Map<String, Long> map = ListMap.toCountMap(lm1, "ons", (ons) -> StatColHeader.ownerNumStatus(ons));

    setOkView("stat: group by owner_num_status");
    view("oneMap", map);

    render("/reports/map");
  }

  /**
   * 4# 本方通话地
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerCommLoc() throws Exception {
    
    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.owner_comm_loc as ocl, COUNT(1) as count, COUNT(DISTINCT pr.started_day) as day_count "  + 
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "ORDER BY count desc";
    String orderBy = "GROUP BY pr.owner_comm_loc";
    
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, orderBy, groupBy, "pr", map("cp.case_id", param("case_id")));
    
//    Map<String, Long> map = ListMap.toCountMap(lm1, "ocl", null);

    setOkView("stat: group by owner_comm_loc");
//    view("oneMap", map);
    view("listMap", lm1);
    render("/reports/listMap");
  }  
}