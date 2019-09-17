package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activeweb.annotations.GET;

import app.controllers.APIController;
import app.models.search.CriteriaTuple;
import app.models.search.Options;
import app.util.DeepCopy;
import app.util.StatColHeader;
import app.util.collections.ListMap;

public class PnumConnectionsController extends APIController {
  /**
   * 号码出现在其他话单中
   * @throws Exception 
   */
  @GET
  public void relatedPbills() throws Exception {
    long caseId = Long.parseLong(param("case_id"));
    String num = param("num");
    int page = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    
    Options options = new Options();
    options.addCriteria(new CriteriaTuple("cp.case_id", caseId, CriteriaTuple.ONCE_QUERY));
    if (num.length() >= 11) {
      options.addCriteria(
          new CriteriaTuple("pr.peer_num", num, CriteriaTuple.ONCE_QUERY));
      logDebug("peer_num : " + num);
    } else {
      options.addCriteria(new CriteriaTuple("pr.peer_short_num", num,
          CriteriaTuple.ONCE_QUERY));
      logDebug("peer_short_num : " + num);
    }
    Options options2 = (Options) DeepCopy.copy(options);
    
    //@formatter:off
    String sql = "SELECT pr.owner_num, pr.owner_cname, pr.peer_num, pr.peer_cname, " +
                        "pr.peer_short_num, pr.peer_num_isp, pr.peer_num_attr, " +
                        "COUNT(pr.bill_type = 1  or null) AS call_count, " +
                        "COUNT( DISTINCT pr.started_day) AS online_days, " +
                        "COUNT(pr.bill_type = 2 or null) AS sms_count, " +
                        "COUNT(time_class = 0 or null) AS private_time_count, " +
                        "COUNT(time_class = 1 or null ) AS work_time_count, " +
                        "COUNT(duration >= 300 or null) AS more_than_5_count, " +
                        "COUNT(started_hour_class >= 17 or null) AS after_21_count, " +
                        "SUM(duration) AS total_duration, " +
                        "MIN(pr.started_day) AS first_day, " +
                        "MAX(pr.started_day) AS last_day " +                
                 "FROM pbill_records as pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.owner_num";             
    String orderBy = "ORDER BY call_count DESC";
    //@formatter:on
    options.setViews(map("group-by", groupBy, "order-by", orderBy));
    List<Map> lm1 = doStat(options, sql);
    
  //@formatter:off
    sql = "SELECT pr.owner_num, pr.comm_direction AS cd, COUNT(1) AS count " +
          "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.owner_num,pr.comm_direction";
    //@formatter:on
    options2.setViews(map("group-by", groupBy));
    List<Map> lm2 = doStat(options2, sql);
    String[] mergeFields = { "cd", "count" };
    LinkedHashMap<Object, List<Map>> lhm2 = ListMap.reduceWithMergeFields("owner_num",
        lm2, mergeFields, (cd) -> StatColHeader.commDirection(cd.toString()),
        ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("owner_num", lm1, lhm2,List.class);        
    
    setOkView();
    view("valueType", "listMap");
    view("caseId", caseId);
    view("linkedHashMap", lhm3);
    render("/reports/numConnectionLinkedHashMap");
  }
 
}
