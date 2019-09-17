package app.controllers.pb;

import static org.javalite.common.Collections.map;

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
 * 6 21 22 23 24
 *
 */
public class PbillDatetimeReportController extends ReportController {

  /**
   * 6-周几 /cases/{case_id}/pbills/overview/group-by-weekday
   * 
   * @throws Exception
   */
  @POST
  public void groupByWeekDay() throws Exception {
    //@formatter:off
    String sql = "SELECT pr.weekday as wd, COUNT(1) as count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " +
                 "ON pr.pbill_id = cp.pbill_id  ";
    String groupBy = "GROUP BY pr.weekday";
    //@formatter:on

    List<Map> statResult = doStat(getRequestString(), sql, groupBy, "pr", map("cp.case_id", param("case_id")));
    Map<String, Long> map = ListMap.toCountMap(statResult, "wd", (str) -> StatColHeader.week(str));

    setOkView("stat: group by weekday");
    view("oneMap", map);

    if ("xlsx".equals(format())) {
      renderXlsx2(Arrays.asList("周几", "联系次数"), "周几"); // TODO: resultMap -> oneMap
    } else {
      render("/reports/map");
    }

  }

  /**
   * 21-日期
   * 
   * @throws Exception
   */
  @POST
  public void groupbyStartedDay() throws Exception {
    //@formatter:off
    String sql = "SELECT pr.started_day as sd, COUNT(1) as count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " +
                 "ON pr.pbill_id = cp.pbill_id";
    String orderBy = "ORDER BY pr.started_day ASC";
    String groupBy = "GROUP BY pr.started_day";
    //@formatter:on

    List<Map> statResult = doStat(getRequestString(), sql, groupBy, orderBy, "pr", map("cp.case_id", param("case_id")));

    //LinkedHashMap<String, String> map = getResultMap(statResult, "sd", "n/a", "count");
    LinkedHashMap<String, Long> map = ListMap.toCountMap(statResult, "n/a", null);
    
    setOkView("stat: group by started_day");
    view("oneMap", map);

    if ("xlsx".equals(format())) {
      renderXlsx2(Arrays.asList("通话日期", "次数"), "日期");
    } else {
      render("/reports/map");
    }

  }

  /**
   * 22-日期vs通话时间段
   * 
   * @throws Exception
   */
  @POST
  public void groupByStartedDayAndStartedTimeL1Class() throws Exception {

    String requestString = getRequestString();
    //@formatter:off
    String sql = "SELECT pr.started_day, pr.started_time_l1_class as stl1c, count(1) as count " +
                         "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " +
                         "ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY started_time_l1_class, started_day ";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr", map("cp.case_id", param("case_id")));

    String[] mergeFields = {"stl1c", "count"};
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("started_day", lm1, mergeFields,
            (stl1c) -> StatColHeader.daAndStartedTimeL1(stl1c.toString()),ListMap.MERGE_WITH_REMOVE_POLICY);
    //@formatter:off
    sql = "SELECT pr.started_day, max(pr.started_at) as ended_started_at, " + 
                   "min(pr.started_at) as first_started_at, count(1) as total " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " +
                "ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY started_day";
    String orderBy = "ORDER BY pr.started_day ASC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr", map("cp.case_id", param("case_id")));

    LinkedHashMap<Object, List<Map>> lhm2 =
        ListMap.merge("started_day", lm2, lhm1, List.class);

    setOkView("stat: group by started_day and started_time_l1_class");
    view("valueType", "listMap");
    view("linkedHashMap", lhm2);

    if ("xlsx".equals(format())) {
      // TODO 可重复利用的表头已经定义在StatColHeader中,可使用一下方式进行拼接
      // 注:Arrays.asList 返回结果为List 未实现 addAll 方法,建议使用Collections.list("对方号码", "号码级别");
      List<String> arrayList = Collections.list("日期", "总计", "首次时间", "末次时间");
      arrayList.addAll(1, StatColHeader.DA_ST_L1_HEADERS_LIST);
      renderXlsx2(arrayList, "日期vs通话时长");
    } else {
      render("/reports/linkedHashMap");
    }

  }

  /**
   * 23-日期v通话时常(详细)
   * 
   * @throws Exception
   */
  @POST
  public void groupByStartedDayAndStartedTimeL2Class() throws Exception {

    String requestString = getRequestString();
    //@formatter:off
    String sql = "SELECT pr.started_day, pr.started_time_l2_class as stl2c, count(1) as count " +
                       "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY started_time_l2_class, started_day ";
    //@formatter:on

    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr", map("cp.case_id", param("case_id")));
    String[] mergeFields = { "stl2c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("started_day", lm1, mergeFields,
            (stl2c) -> StatColHeader.daAndStartedTimeL2(stl2c.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);
    //@formatter:off
    sql = "SELECT pr.started_day, max(pr.started_at) as ended_started_at, " + 
                       "min(pr.started_at) as first_started_at, count(1) as total " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " +
                "ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY started_day";
    String orderBy = "ORDER BY pr.started_day ASC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr", map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lmh2 =
        ListMap.merge("started_day", lm2, lhm1, List.class);

    setOkView("stat: group by started-day and started_time_l2_class");
    view("valueType", "listMap");
    view("linkedHashMap", lmh2);

    if ("xlsx".equals(format())) {
      List<String> arrayList = Collections.list("日期", "总计", "首次时间", "末次时间");
      arrayList.addAll(1, StatColHeader.DA_ST_L2_HEADERS_LIST);
      renderXlsx2(arrayList, "日期vs通话时长(详细)");
    } else {
      render("/reports/linkedHashMap");
    }
  }

  /**
   * 24-日期vs对方号码
   * 
   * @throws Exception
   */
  @POST
  public void groupbyStartedDayAndPeerNum() throws Exception {
    // 1.获取前台传来的数据
    String json = getRequestString();

    // 查出除备注外的所有数据
    //@formatter:off
    String sql = "SELECT pr.started_day, pr.peer_num, pr.owner_num, pr.peer_cname, COUNT(1) AS contact_times " +
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.started_day,pr.peer_num,pr.owner_num";
    String orderBy = "ORDER BY pr.started_day ASC,contact_times DESC";
    //@formatter:on
    List<Map> lm1 = doStat(json, sql, groupBy, orderBy, "pr", map("case_id", param("case_id")));


    // 查出备注中的数据
    //@formatter:off
    sql = "SELECT pr.started_day, pr.peer_num,pr.owner_num, pr.started_time, " +
                 "pr.comm_direction AS cd, pr.duration AS dr " +
          "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.started_at, pr.peer_num, pr.comm_direction, pr.owner_num";
    //@formatter:on
    List<Map> lm2 = doStat(json, sql, groupBy, "pr", map("case_id", param("case_id")));
    String[] mergeFields = { "cd", "dr" };

    List<String> comboKey =
        Arrays.asList(new String[] { "started_day", "peer_num", "owner_num" }); // 组合键
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields(comboKey, lm2, mergeFields,
            (cd) -> StatColHeader.commDirection(cd.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, List<Map>> lhm2 = ListMap.merge(comboKey, lm1, lhm1, List.class);

    setOkView("group by started day and peer num");

    view("valueType", "listMap");
    view("comboKey", comboKey);
    view("linkedHashMap", lhm2);
    if ("xlsx".equals(format())) {

    } else {
      render("/reports/groupByStartedDay");
    }
  }

  /**
   * 25-日期vs基站 ct_code
   * 
   * @throws Exception
   */
  @POST
  public void groupbyStartedDayAndCtCode() throws Exception {
    String json = getRequestString();
    // 查出除备注外的所有数据
    //@formatter:off
    String sql = "SELECT pr.started_day, pr.owner_num, pr.owner_ct_code, COUNT(1) AS contact_times " +
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_day, pr.owner_num, pr.owner_ct_code";
    String orderBy = "ORDER BY pr.started_day ASC, contact_times DESC";
    //@formatter:on   
    List<Map> lm1 = doStat(json, sql, groupBy, orderBy, "pr",
        map("case_id", param("case_id")));

    //@formatter:off
    sql = "SELECT pr.started_day, pr.owner_num, pr.owner_ct_code, pr.started_time, " +
                 "pr.comm_direction AS cd, pr.duration AS dr  " +
          "FROM pbill_records AS pr LEFT JOIN cases_pbills As cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.started_at, pr.owner_ct_code, pr.comm_direction, pr.owner_num";
    //@formatter:on 
    List<Map> lm2 =
        doStat(json, sql, groupBy, "pr", map("case_id", param("case_id")));

    String[] mergeFields = { "cd", "dr" };
    List<String> comboKey =
        Arrays.asList(
            new String[] { "started_day", "owner_ct_code", "owner_num" }); // 组合键

    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields(comboKey, lm2, mergeFields,
            (cd) -> StatColHeader.commDirection(cd.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, List<Map>> lhm2 =
        ListMap.merge(comboKey, lm1, lhm1, List.class);

    setOkView("group by started day and ct code");

    view("valueType", "listMap");
    view("comboKey", comboKey);
    view("linkedHashMap", lhm2);
    if ("xlsx".equals(format())) {

    } else {
      render("/reports/groupByStartedDay");
    }
  }

}