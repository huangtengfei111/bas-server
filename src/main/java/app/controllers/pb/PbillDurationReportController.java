package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activeweb.annotations.POST;
import org.javalite.common.Collections;

import app.controllers.ReportController;
import app.util.StatColHeader;
import app.util.collections.ListMap;

/**
 * 5 8 11 12 13 14 15
 *
 */
public class PbillDurationReportController extends ReportController {

  /**
   * 5- 时间段
   * 
   * @throws Exception
   */
  @POST
  public void groupByDurationClass() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.duration_class as dc, COUNT(1) as count " +
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.duration_class";
    String orderBy = "ORDER BY dc";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    Map<String, Long> map = ListMap.toCountMap(lm1, "dc",
        (dc) -> StatColHeader.durationClass(dc));

    setOkView("stat: group by durationclass");
    view("oneMap", map);

    if ("xlsx".equals(format())) {
      renderXlsx2(Arrays.asList("时长", "总数"), "时长表");
    } else {
      render("/reports/map");
    }
  }

  /**
   * 8- 对方通话地
   * 
   * @throws Exception
   */
  @POST
  public void groupByPeerCommLoc() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.peer_comm_loc as pcl, COUNT(1) as count, COUNT(DISTINCT pr.peer_num) as peer_num_count " +
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.peer_comm_loc";
    String orderBy = "ORDER BY count DESC";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
//    Map<String, Long> map = ListMap.toCountMap(lm1, "pcl", null);

    setOkView("stat: group by peer_comm_loc");
    view("listMap", lm1);

    render("/reports/listMap");
  }

  /**
   * 11-通话时段
   * 
   * @throws Exception
   */
  @POST
  public void groupByStartedTimeL1Class() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.started_time_l1_class as stl1c, COUNT(1) as count " +
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_time_l1_class";
    String orderBy = "ORDER BY stl1c";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    Map<String, Long> map = ListMap.toCountMap(lm1, "stl1c",
        (stl1c) -> StatColHeader.startedTimeL1(stl1c));

    setOkView("stat: group by started_time_l1_class");
    view("oneMap", map);

    render("/reports/map");
  }

  /**
   * 12-通话时段(详细)
   * 
   * @throws Exception
   */
  @POST
  public void groupByStartedTimeL2Class() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.started_time_l2_class as stl2c, COUNT(1) as count " +
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_time_l2_class";
    String orderBy = "ORDER BY stl2c";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    Map<String, Long> map = ListMap.toCountMap(lm1, "stl2c",
        (stl2c) -> StatColHeader.daAndStartedTimeL2(stl2c));

    setOkView("stat: group by started_time_l2_class");
    view("oneMap", map);

    render("/reports/map");
  }

  /**
   * 13-通话时段(小时)
   * 
   * @throws Exception
   */
  @POST
  public void groupByStartedHourClass() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.started_hour_class as shc, COUNT(1) as count " +
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_hour_class";
    String orderBy = "ORDER BY shc";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, selectSql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    Map<String, Long> map = ListMap.toCountMap(lm1, "shc",
        (shc) -> StatColHeader.startedHour(shc));

    setOkView("stat: group by started_hour_class");
    view("oneMap", map);

    render("/reports/map");
  }

  /**
   * 14-通话时段vs通话时长
   * 
   * @throws Exception
   */
  @POST
  public void groupbyDurationClassAndStartedTimeL1Class() throws Exception {

    String json = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.duration_class as dc, started_time_l1_class as stl1c, count(1) as count " +
                        "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY started_time_l1_class, duration_class ";
    String orderBy = "ORDER BY dc, stl1c";
    //@formatter:on
    List<Map> lm = doStat(json, selectSql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "stl1c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("dc", lm,
                                  mergeFields, (dc) -> StatColHeader.durationClass(dc.toString()),
                                  (stl1c) -> StatColHeader.daAndStartedTimeL1(stl1c.toString()),
                                  ListMap.MERGE_WITH_REMOVE_POLICY);

    setOkView("stat: group by started_time_l1_class");
    view("useKey", true);
    view("valueType", "listMap");
    view("linkedHashMap", lhm1);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("时长类别"));
      arrayList.addAll(StatColHeader.DA_ST_L1_HEADERS_LIST);
      renderXlsx2(arrayList, "通话时段vs通话时长");
    } else {
      render("/reports/linkedHashMap");
    }

  }

  /**
   * 15-通话时段(详细)vs通话时长
   * 
   * @throws Exception
   */
  @POST
  public void groupbyDurationClassAndStartedTimeL2Class() throws Exception {

    String requestString = getRequestString();
    //@formatter:off
    String selectSql = "SELECT pr.duration_class as dc, started_time_l2_class as stl2c, count(1) as count " + 
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupSql = "GROUP BY started_time_l2_class, duration_class ";
    String orderSql = "ORDER BY dc, stl2c";
    //@formatter:on
    List<Map> lm = doStat(requestString, selectSql, groupSql, orderSql,
        "pr", map("cp.case_id", param("case_id")));

    String[] mergeFields = { "stl2c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("dc", lm,
                                  mergeFields, (dc) -> StatColHeader.durationClass(dc.toString()),
                                  (stl2c) -> StatColHeader.daAndStartedTimeL2(stl2c.toString()),
                                  ListMap.MERGE_WITH_REMOVE_POLICY);

    setOkView("stat: group by started_time_l2_class");
    
    view("useKey", true);
    view("valueType", "listMap");
    view("linkedHashMap", lhm1);
    if ("xlsx".equals(format())) {
      List<String> arrayList = Collections.list("时长类别");
      arrayList.addAll(StatColHeader.DA_ST_L2_HEADERS_LIST);
      renderXlsx2(arrayList, "通话时段(详细)vs通话时长");
    } else {
      render("/reports/linkedHashMap");
    }
  }
}