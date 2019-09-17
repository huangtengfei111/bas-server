package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activeweb.annotations.POST;

import app.controllers.ReportController;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.util.StatColHeader;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

public class PbillCellTowerReportController extends ReportController {

  /**
   * 40-基站cellId(未查询出的字段: 类型,名称,地址)
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerCtCode() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id",caseId.toString()));
    List zero = Arrays.asList(0);
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));
    //@formatter:off
    String ownerCtCodeSql = "SELECT pr.owner_ct_code, pr.owner_lac, pr.owner_ci, COUNT(1) as contact_times, " + 
                                   "MIN(pr.started_at) as first_started_day, MAX(pr.ended_at) as ended_day, " + 
                                   "COUNT(DISTINCT pr.started_day) as online_days, " + 
                                   "(TIMESTAMPDIFF(DAY, MIN(pr.started_day), MAX(pr.ended_at)) - COUNT(DISTINCT pr.started_day)) as offline_days " +
                            "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.owner_ct_code";
    String orderBy = "ORDER BY contact_times DESC";
    //@formatter:on
    options.setGroupBy(groupBy);
    options.setOrderBy(orderBy);
    List<Map> ownerCtCodeTotal = doStat(options, ownerCtCodeSql);

    setOkView("group_by_owner_ct");
    view("listMap", ownerCtCodeTotal);

    if ("xlsx".equals(format())) {

    } else {
      render("/reports/listMap");
    }
  }

  /**
   * 41-基站vs通话时段(未查询出的字段: 类型,名称,地址)
   * 
   * @throws Exception
   */
  @POST
  public void groupByCodeAndStartedTimeL1Class() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    List zero = Arrays.asList(0);
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));
    //@formatter:off
    String sql = "SELECT pr.owner_ct_code, pr.started_time_l1_class AS stl1c, COUNT(1) AS count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_time_l1_class, pr.owner_ct_code";
    //@formatter:on

    List<Map> startedTimeL1Class = doStat(options, sql, groupBy, "", "");
    String[] mergeFields = { "stl1c", "count" };
    LinkedHashMap<Object, List<Map>> startedTimeClassReduce =
        ListMap.reduceWithMergeFields("owner_ct_code", startedTimeL1Class, mergeFields,
            (stl1c) -> StatColHeader.daAndStartedTimeL1(stl1c.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);
    //@formatter:off
    sql = "SELECT pr.owner_ct_code, COUNT(1) AS total, pr.owner_lac, pr.owner_ci " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.owner_ct_code";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> ownerCtCodeTotal = doStat(options, sql, groupBy, orderBy, "");
    LinkedHashMap<Object, List<Map>> mergeOwnerCtCodeTotal = ListMap.merge(
        "owner_ct_code", ownerCtCodeTotal, startedTimeClassReduce, List.class);

    setOkView("group_by_ct_code_and_started_time_l1_class");

    view("valueType", "listMap");
    view("linkedHashMap", mergeOwnerCtCodeTotal);
    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("基站代码", "总计"));
      arrayList.addAll(1, StatColHeader.DA_ST_L1_HEADERS_LIST);
      renderXlsx2(arrayList, "基站vs通话时段");
    } else {
      render("/reports/cellTowerLinkedHashMap");
    }
  }
  
  /**
   * 42-基站vs通话时段(详细)
   * 
   * @throws Exception
   */
  @POST
  public void groupByCodeAndStartedTimeL2Class() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    List zero = Arrays.asList(0);
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));
    //@formatter:off
    String sql = "SELECT pr.owner_ct_code, pr.started_time_l2_class AS stl2c, COUNT(1) AS count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_time_l2_class, pr.owner_ct_code";
    //@formatter:on

    List<Map> startedTimeL2Class = doStat(options, sql, groupBy, "", "");
    String[] mergeFields = { "stl2c", "count" };
    LinkedHashMap<Object, List<Map>> startedTimeL2ClassReduce = ListMap.reduceWithMergeFields(
        "owner_ct_code", startedTimeL2Class, mergeFields,
        (stl2c) -> StatColHeader.daAndStartedTimeL2(stl2c.toString()),
        ListMap.MERGE_WITH_REMOVE_POLICY);
    //@formatter:off
    sql = "SELECT pr.owner_ct_code, COUNT(1) AS total, pr.owner_lac, pr.owner_ci " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.owner_ct_code";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> ownerCtCodeTotal = doStat(options, sql, groupBy, orderBy, "");
    LinkedHashMap<Object, List<Map>> mergeOwnerCtCodeTotal = ListMap.merge("owner_ct_code", ownerCtCodeTotal,
        startedTimeL2ClassReduce, List.class);

    setOkView("group_by_ct_code_and_started_time_l2_class");

    view("valueType", "listMap");
    view("linkedHashMap", mergeOwnerCtCodeTotal);
    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(
          Arrays.asList("基站代码", "总计"));
      arrayList.addAll(1, StatColHeader.DA_ST_L2_HEADERS_LIST);
      renderXlsx2(arrayList, "基站vs通话时段（详细）");
    } else {
      render("/reports/cellTowerLinkedHashMap");
    }
  }

  /**
   * 43-基站vs通话时段(小时)(未查询出的字段: 类型,名称,代码属性,地图)
   * 
   * @throws Exception
   */
  @POST
  public void groupByCodeAndStartedHourClass() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", caseId.toString()));
    List<Object> zero = Arrays.asList(0);
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));
    // 字段:本方基站,通话时段(小时),通话时段(小时) 总数
    //@formatter:off
    String sql = "SELECT pr.owner_ct_code, pr.started_hour_class AS shc, COUNT(1) AS count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_hour_class, pr.owner_ct_code";
    //@formatter:on
    List<Map> startedHourClass = doStat(options, sql, groupBy, "", "");
    String[] mergeFields = { "shc", "count" };
    LinkedHashMap<Object, List<Map>> reduceStartedHourClass =
        ListMap.reduceWithMergeFields("owner_ct_code", startedHourClass, mergeFields,
            (shc) -> StatColHeader.startedHour(shc.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);

    // 字段:本方基站,天数,总计,本方lac,本方ci
    //@formatter:off
    sql = "SELECT pr.owner_ct_code, COUNT(DISTINCT started_day) online_days, COUNT(1) AS total, pr.owner_lac, pr.owner_ci " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.owner_ct_code";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> ownerCtCodeTotal = null;
    if (options.getAdhocParam("limit") != null) {
      int limit = Integer.parseInt(options.getAdhocParam("limit").toString());
      ownerCtCodeTotal =
          doStat(options, sql, groupBy, orderBy, "LIMIT ? ", limit);
    } else {
      ownerCtCodeTotal = doStat(options, sql, groupBy, orderBy, "");
    }

    LinkedHashMap<Object, List<Map>> mergeOwnerCtCodeTotal = ListMap.merge(
        "owner_ct_code", ownerCtCodeTotal, reduceStartedHourClass, List.class);

    setOkView("group by ct-code and started-time-l1-class");
    view("valueType", "listMap");
    view("linkedHashMap", mergeOwnerCtCodeTotal);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("基站代码", "总计"));
      arrayList.addAll(1, StatColHeader.HOUR_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "基站vs通话时段(小时)");
    } else {
      render("/reports/cellTowerLinkedHashMap");
    }
  }

  /**
   * 46-小区号lac
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerLac() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    options.addCriteria(
        new CriteriaTuple("pr.owner_lac", Op.GT, Arrays.asList(0)));
    //@formatter:off
    String sql = "SELECT pr.owner_lac, COUNT(1) AS count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.owner_lac";
    String orderBy = "ORDER BY count DESC";
    //@formatter:on
    List<Map> ownerLacTotal = doStat(options, sql, groupBy, orderBy, "");


    setOkView("group_by_owner_lac");

    view("listMap", ownerLacTotal);

    if ("xlsx".equals(format())) {

      // 将lm1中的key转换成表头对应的中文key
      List<Map> lm2 = new ArrayList<Map>();
      for (Map m : ownerLacTotal) {
        Set mkeys = m.keySet();
        Map<String, String> mapChange = new HashMap<String, String>();
        for (Object mkey : mkeys) {
          String mckey = StatColHeader.ownerNum(mkey.toString());
          mapChange.put(mckey, m.get(mkey).toString());
        }
        lm2.add(mapChange);
      }

      renderExcel(GROUP_BY_OWNER_LAC_HEADER, lm2, GROUP_BY_OWNER_LAC_FILE);
    } else {
      render();
      render("/reports/listMap");
    }
  }

  /**
   * 47-基站vs通话时长(未查询出的字段: 类型,名称,地址)
   * 
   * @throws Exception
   */
  @POST
  public void groupByCodeAndDurationClass() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    List<Object> zero = Arrays.asList(0);
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));
    //@formatter:off
    String sql = "SELECT pr.owner_ct_code, pr.duration_class AS dc, COUNT(1) AS count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.duration_class, pr.owner_ct_code";
    //@formatter:on
    List<Map> durationClass = doStat(options, sql, groupBy, "", "");
    String[] mergeFields = { "dc", "count" };
    LinkedHashMap<Object, List<Map>> reduceDurationClass =
        ListMap.reduceWithMergeFields("owner_ct_code", durationClass, mergeFields,
            (dc) -> StatColHeader.durationClass(dc.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);

    //@formatter:off
    sql = "SELECT pr.owner_ct_code, COUNT(1) AS total, pr.owner_lac, pr.owner_ci " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.owner_ct_code";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> ownerCtCodeTotal = doStat(options, sql, groupBy, orderBy, "");
    LinkedHashMap<Object, List<Map>> mergeOwnerCtCodeTotal = ListMap.merge(
        "owner_ct_code", ownerCtCodeTotal, reduceDurationClass, List.class);

    setOkView("group_by_code_and_durationClass");
    view("valueType", "listMap");
    view("linkedHashMap", mergeOwnerCtCodeTotal);
    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("基站代码", "总计"));
      arrayList.addAll(1, StatColHeader.DURATION_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "基站vs通话时段(小时)");
    } else {
      render("/reports/cellTowerLinkedHashMap");
    }
  }

  // 48-本方号码vs常用基站vs小时

  /**
   * 基站标注报表
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumAndStartedHourClass() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", caseId.toString()));

    // 查出字段:通话时段(小时)shc,各个通话时段的通话次数(count)
    //@formatter:off
    String sql = "SELECT pr.owner_num, pr.started_hour_class as shc, COUNT(1) as count " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.started_hour_class, pr.owner_num";
    //@formatter:on
    List<Map> startedHourClass = doStat(options, sql, groupBy, "", "");

    String[] mergeFields = { "shc", "count" };
    LinkedHashMap<Object, List<Map>> reduceStartedHourClass =
        ListMap.reduceWithMergeFields("owner_num", startedHourClass, mergeFields,
            (shc) -> StatColHeader.startedHour(shc.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);

    // 查出字段:话单号码, 联系次数, 在线天数, 首次通话时间, 末次通话时间
    //@formatter:off
    sql = "SELECT pr.owner_num, COUNT(1) AS total, " +
                 "COUNT(DISTINCT pr.started_day) as online_days, " +
                 "MIN(pr.started_day) as first_day, MAX(pr.started_day) as last_day " +
          "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.owner_num ";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> ownerNumTotal = null;
    if (options.getAdhocParam("limit") != null) {
      int limit = Integer.parseInt(options.getAdhocParam("limit").toString());
      ownerNumTotal = doStat(options, sql, groupBy, orderBy, "LIMIT ?", limit);
    } else {
      ownerNumTotal = doStat(options, sql, groupBy, orderBy, "");
    }

    LinkedHashMap<Object, List<Map>> mergeOwnerNumTotal =
        ListMap.merge("owner_num", ownerNumTotal, reduceStartedHourClass, List.class);

    setOkView("group by owner-num and started_hour_class");
    view("valueType", "listMap");
    view("linkedHashMap", mergeOwnerNumTotal);

    render("/reports/linkedHashMap");

  }

}