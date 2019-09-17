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

public class PbillOwnerNumReportController extends ReportController {
  
  /**
   * 48-本方号码与常用基站与通话时段(每小时)
   * 缺少字段：出现频率，地图，基站标注，类型，名称
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumAndCtAndStartedHourClass() throws Exception {
    //@formatter:off
    String sql ="SELECT  pr.owner_num,pr.owner_ct_code, pr.started_hour_class AS shc, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.owner_ct_code, pr.owner_num, pr.started_hour_class";
    //@formatter:on

    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    List<Object> zero = Arrays.asList(0);
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));
    List<Map> startedHourClassList = doStat(options, sql, groupBy, "", "");

    String[] mergeFields = { "shc", "count" };
    List<String> comboKey =
        Arrays.asList(new String[] { "owner_ct_code", "owner_num" });
    LinkedHashMap<Object, List<Map>> startedHourClassMap =
        ListMap.reduceWithMergeFields(comboKey, startedHourClassList, mergeFields,
            (shc) -> StatColHeader.startedHour(shc.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);

    List<Object> ownerNums = options.getCriteria("pr.owner_num").getValues();
    options.delCriteria("pr.owner_num");
    //@formatter:off
    sql = "SELECT pr.owner_num, pr.owner_cname, pr.owner_ct_code, COUNT(DISTINCT pr.started_day) online_days, COUNT(1) AS total " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.owner_ct_code, pr.owner_num";
    String orderBy = "ORDER BY total DESC";
    String limitSql = "LIMIT 30";
    List<Map> allOwnerNumList = new ArrayList<>();
    //@formatter:on
    for (Object ownerNum : ownerNums) {
      options.addCriteria(
          new CriteriaTuple("pr.owner_num", ownerNum,
              CriteriaTuple.ONCE_QUERY));
      List<Map> oneOwnerNumList = doStat(options, sql, groupBy, orderBy, limitSql);
      allOwnerNumList.addAll(oneOwnerNumList);
    }


    LinkedHashMap<Object, List<Map>> mergeOwnerNumStartedHourClass =
        ListMap.merge(comboKey, allOwnerNumList, startedHourClassMap, List.class);


    setOkView("group-by-ownernumandctandstartedhourclass");
    view("valueType", "listMap");
    view("linkedHashMap", mergeOwnerNumStartedHourClass);

    if ("xlsx".equals(format())) {
      List<String> arrayList =
          new ArrayList<String>(Arrays.asList("本方号码", "号码级别", "总计"));
      arrayList.addAll(2, StatColHeader.HOUR_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "本方号码与通话时段(每小时)");
      // renderExcel(arrayList, lhm3, "");
    } else {
      render("/reports/groupByOwnerNumAndCtAndStartedHourClass");
    }
  }

  /**
   * 51-本方号码(未列出字段: 号码级别,关联度)
   */
  @POST
  public void groupByOwnerNum() throws Exception {
    String requestString = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    // 联系次数,联系人个数,通话时长,首次通话时间,末次通话时间,首末相距,使用天数,未使用天数,总通话时间s
    //@formatter:off
    String sql = "SELECT pr.owner_num, pr.owner_cname as owner_cname, COUNT(1) as count, COUNT(DISTINCT pr.peer_num) as peer_nums_cnt, " +
                         "SUM(pr.duration) as total_duration, MIN(pr.started_day) as first_day, MAX(pr.ended_at) as last_day, " +
                         "TIMESTAMPDIFF(DAY, MIN(pr.started_day), MAX(pr.started_day) + 1) as inter_days, " +
                         "COUNT(DISTINCT pr.started_day) as online_days, " +
                         "(TIMESTAMPDIFF(DAY, MIN(pr.started_day), MAX(pr.started_day) + 1) - COUNT(DISTINCT pr.started_day)) as offline_days " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " + "ON pr.pbill_id = cp.pbill_id  ";
    String groupBy = "GROUP BY pr.owner_num";
    String orderBy = "ORDER BY count DESC";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", caseId.toString()));

    // 将lm1中的key转换成表头对应的中文key
    List<Map> lm3 = new ArrayList<Map>();
    for (Map m : lm1) {
      Set mkeys = m.keySet();
      Map<String, String> mapChange = new HashMap<String, String>();
      mapChange.put("owner_num", m.get("owner_num").toString());
      for (Object mkey : mkeys) {
        if ("owner_cname".equals(mkey)) {
          continue;
        }
        String mckey = StatColHeader.ownerNum(mkey.toString());
        mapChange.put(mckey, m.get(mkey).toString());
      }
      lm3.add(mapChange);
    }



    //主叫,被叫次数
    //@formatter:off
    sql = "SELECT pr.owner_num as owner_num, pr.comm_direction as cd, COUNT(1) as total " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id  ";
    groupBy = "GROUP BY pr.owner_num, pr.comm_direction";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", caseId.toString()));

    String[] mergeFields = { "cd", "total" };
    LinkedHashMap<Object, List<Map>> lhm2 =
        ListMap.reduceWithMergeFields("owner_num", lm2, mergeFields,
            (cd) -> StatColHeader.commDirection(cd.toString()),
            ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, List<Map>> lhm3 =
        ListMap.merge("owner_num", lm1, lhm2, List.class);

    setOkView("stat: group by owner_num");
    view("valueType", "listMap");
    view("caseId", caseId);
    view("linkedHashMap", lhm3);
    



    if ("xlsx".equals(format())) {   

      LinkedHashMap<Object, List<Map>> lhm4 =
          ListMap.merge("owner_num", lm3, lhm2, List.class);
      GROUP_BY_OWNER_NUM_HEADER.addAll(5, StatColHeader.COMM_DIR_HEADERS_LIST);

      renderExcel(GROUP_BY_OWNER_NUM_HEADER, lhm4, GROUP_BY_OWNER_NUM_FILE);
    } else {
      render("/reports/numConnectionLinkedHashMap");
    }
  }
	
  /**
   * 52-本方号码vs通话时段
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumAndStartedTimeL1Class() throws Exception {
    // 查出 本方号码，时间分类1，单个分类总数
    //@formatter:off
    String sql = "SELECT pr.owner_num, pr.started_time_l1_class AS stl1c, COUNT(1) AS count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.owner_num, pr.started_time_l1_class";
    //@formatter:on
    String requestString = getRequestString();
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "stl1c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 = ListMap.reduceWithMergeFields("owner_num", lm1, mergeFields,
        (stl1c) -> StatColHeader.daAndStartedTimeL1(stl1c.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);

    // 查出 本方号码，所有分类的总和
    //@formatter:off
    sql = "SELECT pr.owner_num, COUNT(1) AS total, pr.owner_cname " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.owner_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("owner_num", lm2, lhm1, List.class);
   
    setOkView("group-by-ownernumandstartedtimel1class");
    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("本方号码", "号码级别", "总计"));
      arrayList.addAll(2, StatColHeader.DA_ST_L1_HEADERS_LIST);
      renderXlsx2(arrayList, "本方号码vs通话时段");
    } else {
      render("/reports/linkedHashMap");
    }  
  }
  
  /**
   * 53-本方号码vs通话时段(详细)
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumAndStartedTimeL2Class() throws Exception {
    // 本方号码,时间分类2,单个分类总数
    //@formatter:off
    String sql ="SELECT pr.owner_num, pr.started_time_l2_class AS stl2c, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.owner_num, pr.started_time_l2_class";
    //@formatter:on
    String requestString = getRequestString();
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "stl2c", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("owner_num", lm1, mergeFields,
            (stl2c) -> StatColHeader.daAndStartedTimeL2(stl2c.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);

    // 本方号码,所有分类总数
    //@formatter:off
    sql = "SELECT pr.owner_num, COUNT(1) AS total, pr.owner_cname " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.owner_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("owner_num", lm2, lhm1, List.class);
    
    setOkView("group-by-OwnerNum-StartedTimeL2Class");
    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("本方号码", "号码级别", "总计"));
      arrayList.addAll(2, StatColHeader.DA_ST_L2_HEADERS_LIST);
      renderXlsx2(arrayList, "本方号码vs通话时段(详细)");
    } else {
      render("/reports/linkedHashMap");
    }  
  }
  
  /**
   * 54-本方号码与通话时段(每小时)
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumAndStartedHourClass() throws Exception {
    // 本方号码,开始时间类型(小时),单个分类总数
    //@formatter:off
    String sql ="SELECT pr.owner_num, pr.started_hour_class AS shc, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.owner_num, pr.started_hour_class";
    //@formatter:on
    String requestString = getRequestString();
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "shc", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 =
        ListMap.reduceWithMergeFields("owner_num", lm1, mergeFields,
            (shc) -> StatColHeader.startedHour(shc.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    
    // 本方号码,所有分类总数
    //@formatter:off
    sql = "SELECT pr.owner_num, COUNT(1) AS total, pr.owner_cname " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.owner_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 = ListMap.merge("owner_num", lm2, lhm1, List.class);
    
    setOkView("overview/group-by-ownernumandstartedhourclass");
    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("本方号码", "号码级别", "总计"));
      arrayList.addAll(2, StatColHeader.HOUR_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "本方号码与通话时段(每小时)");
    } else {
      render("/reports/linkedHashMap");
    }  
  }
  
  /**
   * 本方号码vs通话时长
   * 
   * @throws Exception
   */
  @POST
  public void groupByOwnerNumAndDurationClass() throws Exception {
    String requestString = getRequestString();
    //@formatter:off
    String sql ="SELECT pr.owner_num, pr.duration_class AS dc, COUNT(1) AS count " +
                "FROM pbill_records as pr LEFT JOIN cases_pbills as cp " + "ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.duration_class, pr.owner_num";
    //@formatter:on
    List<Map> lm1 = doStat(requestString, sql, groupBy, "pr",
        map("cp.case_id", param("case_id")));
    String[] mergeFields = { "dc", "count" };
    LinkedHashMap<Object, List<Map>> lhm1 = ListMap.reduceWithMergeFields("owner_num",
        lm1, mergeFields, (dc) -> StatColHeader.durationClass(dc.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    
    //@formatter:off
    sql = "SELECT pr.owner_num, COUNT(1) AS total, pr.owner_cname " +
          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id";
    groupBy = "GROUP BY pr.owner_num";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on
    List<Map> lm2 = doStat(requestString, sql, groupBy, orderBy, "pr",
        map("cp.case_id", param("case_id")));
    LinkedHashMap<Object, List<Map>> lhm3 =
        ListMap.merge("owner_num", lm2, lhm1, List.class);
    
    setOkView("group-by-ownernumanddurationclass");

    view("valueType", "listMap");
    view("linkedHashMap", lhm3);

    if ("xlsx".equals(format())) {
      List<String> arrayList = new ArrayList<String>(Arrays.asList("本方号码", "号码级别", "人员信息", "总计"));
      arrayList.addAll(3, StatColHeader.DURATION_CLASS_HEADERS_LIST);
      renderXlsx2(arrayList, "本方号码vs通话时长");
    } else {
      render("/reports/linkedHashMap");
    }     
  }

  @POST
  public void freqCelltower() throws Exception {
    // 热力图细化
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));

    //@formatter:off
    String freqCelltowerSql = "SELECT pr.owner_num, pr.owner_ct_code, COUNT(1) AS total " +
                              "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY pr.owner_num, pr.owner_ct_code";
    String orderBy = "ORDER BY total DESC";
    //@formatter:on

    List<Map> lm1 = doStat(json, freqCelltowerSql, groupBy, orderBy, "pr",
        map("cp.case_id", caseId.toString()));

    setOkView("celltower freq of owner_num");
    view("listMap", lm1);
    render("/reports/freqCelltower");

  }

}