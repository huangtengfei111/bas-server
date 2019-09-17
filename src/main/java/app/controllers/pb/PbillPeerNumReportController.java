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
import app.models.search.Options;
import app.util.DeepCopy;
import app.util.StatColHeader;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

public class PbillPeerNumReportController extends ReportController {
  
  /**
   * 31-对方号码(未列出字段 号码级别,关联度) xlsx文件逻辑未实现
   * 
   * @throws Exception
   */
  @POST
  public void groupByPeerNum() throws Exception {
    String requestString = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    // 对方号码,人员信息,归属地,运营商,合计,私人时间,工作时间,联系天数,合计通话时间,五分钟以上,21时以后,语音数,首次时间,末次时间
    //@formatter:off
    String sql = "SELECT pr.peer_num, pr.peer_cname, pr.peer_num_attr, pr.peer_citizen_id, pr.peer_num_isp, COUNT(1) total, " +
                        "COUNT(time_class = 0 or null) AS private_time_count, " +
                        "COUNT(time_class = 1 or null) AS work_time_count, " +
                        "COUNT(DISTINCT pr.started_day) AS online_days, SUM(pr.duration) as total_duration, " +
                        "COUNT(duration >= 300 or null) AS more_than_5_count, " +
                        "COUNT(started_hour_class >= 17 or null) AS after_21_count, " +
                        "COUNT(pr.bill_type = 1  or null) AS call_count, " +
                        "COUNT(pr.bill_type = 2 or null) AS sms_count, " +
                        "MIN(pr.started_day) AS first_day, MAX(pr.ended_at) AS last_day " +
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.peer_num ";
    String orderBy = "ORDER BY call_count DESC";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", caseId.toString()));
    
    //@formatter:off
    sql = "SELECT pr.peer_num, pr.comm_direction AS cd, COUNT(1) AS count " +
          "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.peer_num,pr.comm_direction";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", caseId.toString()));
    String[] mergeFields = { "cd", "count" };
    LinkedHashMap<Object, List<Map>> lhm2 = ListMap.reduceWithMergeFields("peer_num",
        lm2, mergeFields, (cd) -> StatColHeader.commDirection(cd.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("peer_num", lm1, lhm2, List.class);

    setOkView("group-by-peernum");

    view("valueType", "listMap");
    view("caseId", caseId);
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {

    } else {
      render("/reports/numConnectionLinkedHashMap");
    }

  }
  
  /**
   * 32.对方号码(排除条件) 缺少字段:号码标注,标签,虚拟网名称,关联度, xlsx文件逻辑未实现
   * 
   * @throws Exception
   */
  @POST
  public void groupByPeerNumAndExclusionCondition() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", caseId.toString()));
    Options options2 = (Options) DeepCopy.copy(options);
    Object limit = options.getAdhocParam("limit");
    // 本方号码,对方号码,人员信息,对方短号,运营商,归属地,语音数,联系天数,短信(sms),私人时间,工作时间,五分钟以上,21时以后,通话时间,首次时间,末次时间
    //@formatter:off
    String sql = "SELECT pr.owner_num, pr.peer_num, pr.peer_cname, pr.peer_short_num, pr.peer_num_isp, pr.peer_num_attr, " +
                        "COUNT(pr.bill_type = 1  or null) AS call_count, COUNT( DISTINCT pr.started_day) AS online_days, " +
                        "COUNT(pr.bill_type = 2 or null) AS sms_count, COUNT(time_class = 0 or null) AS private_time_count, " +
                        "COUNT(time_class = 1 or null ) AS work_time_count, COUNT(duration >= 300 or null) AS more_than_5_count, " +
                        "COUNT(started_hour_class >= 17 or null) AS after_21_count, SUM(duration) AS total_duration, " +
                        "MIN(pr.started_day) AS first_day, MAX(pr.started_day) AS last_day " +
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.peer_num";
    String orderBy = "ORDER BY call_count DESC";
    //@formatter:on
    options.setViews(
        map("group-by", groupBy, "order-by", orderBy, "limit", limit));
    List<Map> lm1 = doStat(options, sql);

    //@formatter:off
    sql = "SELECT pr.peer_num, pr.comm_direction AS cd, COUNT(1) AS count " +
          "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.peer_num,pr.comm_direction";
    //@formatter:on
    options2.setViews(map("group-by", groupBy));
    List<Map> lm2 = doStat(options2, sql);
    String[] mergeFields = { "cd", "count" };
    LinkedHashMap<Object, List<Map>> lhm2 = ListMap.reduceWithMergeFields("peer_num",
        lm2, mergeFields, (cd) -> StatColHeader.commDirection(cd.toString()),
        ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("peer_num", lm1, lhm2,List.class);

    setOkView("overview/group-by-peernumexclusioncondition");

    view("valueType", "listMap");
    view("caseId", caseId);
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {

    } else {
      render("/reports/numConnectionLinkedHashMap");
    }
  }

  /**
   * 33-对方号码vs通话时长
   * 
   * @throws Exception
   */
  @POST
  public void groupByPeerNumAndDurationClass() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String sql ="SELECT pr.peer_num, pr.duration_class AS dc, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " + "ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.duration_class, pr.peer_num";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "dc", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 = ListMap.reduceWithMergeFields("peer_num",
        lm1, mergeFields, (dc) -> StatColHeader.durationClass(dc.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    
    //@formatter:off
    sql = "SELECT pr.peer_num, COUNT(1) AS total, pr.peer_cname " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.peer_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("peer_num", lm2, lhm1, List.class);
    
    setOkView("group-by-peernumanddurationclass");

    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("对方号码", "号码级别", "人员信息", "总计"));
      arrayList.addAll(3, StatColHeader.DURATION_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "对方号码vs通话时长");
    } else {
      render("/reports/linkedHashMap");
    }     
  }

  /**
   * 34-对方号码vs通话时段
   * 
   * @throws Exception
   */
  @POST
  public void groupByPeerNumAndStartedTimeL1Class() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String sql ="SELECT pr.peer_num, pr.started_time_l1_class AS stl1c, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.started_time_l1_class, pr.peer_num";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "stl1c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("peer_num", lm1, mergeFields,
            (stl1c) -> StatColHeader.daAndStartedTimeL1(stl1c.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    
    //@formatter:off
    sql = "SELECT pr.peer_num, COUNT(1) AS total, pr.peer_cname " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.peer_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 =
        ListMap.merge("peer_num", lm2, lhm1, List.class);
    
    setOkView("group-by-peernumandstartedtimel1class");

    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("对方号码", "号码级别", "人员信息", "总计"));
      arrayList.addAll(3, StatColHeader.DA_ST_L1_HEADERS_LIST);
      renderXlsx2(arrayList, "对方号码vs通话时段");
    } else {
      render("/reports/linkedHashMap");
    }     
  }
  
  /**
   * 35-对方号码vs通话时段(详细)
   *
   * @throws Exception
   */
  @POST
  public void groupByPeerNumAndStartedTimeL2Class() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String sql ="SELECT pr.peer_num, pr.started_time_l2_class AS stl2c, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.started_time_l2_class, pr.peer_num";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "stl2c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("peer_num", lm1, mergeFields,
            (stl2c) -> StatColHeader.daAndStartedTimeL2(stl2c.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    
    //@formatter:off
    sql ="SELECT pr.peer_num, COUNT(1) AS total, pr.peer_cname " +
         "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.peer_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("peer_num", lm2, lhm1, List.class);

    setOkView("group-by-peernumandstartedtimel2class");

    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = Collections.list("对方号码", "号码级别", "人员信息", "总计");
      arrayList.addAll(3, StatColHeader.DA_ST_L2_HEADERS_LIST);
      renderXlsx2(arrayList, "对方号码vs通话时段(详细)");
    } else {
      render("/reports/linkedHashMap");
    }     
  }
  
  /**
   * 36-对方号码vs通话时段(小时)
   * 
   * @throws Exception
   */
  @POST
  public void groupByPeerNumAndStartedHourClass() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String sql ="SELECT pr.peer_num, pr.started_hour_class AS shc, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.started_hour_class, pr.peer_num";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "shc", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 = ListMap.reduceWithMergeFields("peer_num",
        lm1, mergeFields, (shc) -> StatColHeader.startedHour(shc.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    
    //@formatter:off
    sql ="SELECT pr.peer_num, COUNT(1) AS total, pr.peer_cname " +
         "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.peer_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("peer_num", lm2, lhm1, List.class);
    
    setOkView("group-by-peernumandstartedhourclass");

    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("对方号码", "号码级别", "人员信息", "总计"));
      arrayList.addAll(3, StatColHeader.HOUR_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "对方号码vs通话时段(小时)");
    } else {
      render("/reports/linkedHashMap");
    }     
  }  
}