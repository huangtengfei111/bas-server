package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import app.controllers.APIController;
import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import app.models.pb.PnumMeet;
import app.models.pb.PubServiceNum;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.services.pb.BackupNumsAnalyzer;
import app.services.pb.PbillService;
import app.services.pb.PbillStatService;
import app.util.DateTimeConvert;
import app.util.DeepCopy;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

public class PbillAnalyzeController extends APIController {

  @Inject
  private BackupNumsAnalyzer backupNumsAnalyzer;

  @Inject
  private PbillStatService pbillStatService;

  @Inject
  private PbillService pbillService;

  /**
   * 互相碰面查询
   * 
   * @throws Exception
   */
  @POST
  public void meets() throws Exception {

    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();

    PnumMeet meet = pbillStatService.meets(caseId, json);

    setOkView("meets analyzing");
    view("meet", meet);
    render("meet");
  }

  /**
   * 矩阵关系
   * 
   * @throws Exception
   */
  @POST
  public void matrix() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));

    // 获取号码集合
    LinkedHashMap<Object, List<Map>> lhm1 =
        new LinkedHashMap<Object, List<Map>>();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    List<String> xNums = (List) options.getAdhocParam("x_nums");// 横向号码
    List<String> yNums = (List) options.getAdhocParam("y_nums");// 纵向号码
    //@formatter:off
    String sql = "SELECT COUNT(1) AS inter_connect, MIN(started_day) AS first_day, MAX(started_day) AS last_day " +
                 "FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    //@formatter:on
    String pbillSql = " owner_num = ?";

    for (String xNum : xNums) {
      List<Map> lm1 = new ArrayList<Map>();
      for (String yNum : yNums) {
        if (!xNum.equals(yNum)) {
          long peerNumsCommonDegree = pbillStatService.peerNumsCommonDegree(json, xNum, yNum, caseId);
          Pbill xPbill = Pbill.findFirst(pbillSql, xNum);
          Pbill yPbill = Pbill.findFirst(pbillSql, yNum);
          options.addCriteria(new CriteriaTuple("pr.owner_num", xNum, CriteriaTuple.ONCE_QUERY));
          options.addCriteria(new CriteriaTuple("pr.peer_num", yNum, CriteriaTuple.ONCE_QUERY));
          List<Map> lm2 = doStat(options, sql);
          lm2.get(0).put("x_num", xNum);
          lm2.get(0).put("y_num", yNum);
          if (xPbill != null) {
            lm2.get(0).put("x_name", xPbill.getOwnerName());
          }
          if (yPbill != null) {
            lm2.get(0).put("y_name", yPbill.getOwnerName());
          }
          lm2.get(0).put("common_num_count", peerNumsCommonDegree);

          lm1.addAll(lm2);
        }
      }
      lhm1.put(xNum, lm1);
    }

    setOkView("matrix analyzing");

    view("valueType", "listMap");
    view("linkedHashMap", lhm1);
    render("/reports/linkedHashMap");

  }

  /**
   * 通话亲密关系
   * 
   * @throws Exception
   */
  @POST
  public void connections() throws Exception {
    String json = getRequestString();
    long caseId = Long.parseLong(param("case_id"));

    List<Map> results = pbillService.connections(json, caseId);

    setOkView();
    view("results", results);
    render();
  }

  /**
   * 号码碰撞 1.查出每个碰撞号码的对方号码,以话单中的本方号码为key存进集合 2.查出碰撞号码共同的对方号码,即,对方号码取交集
   * 
   * @throws Exception
   */
  @POST
  public void findCommons() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();

    LinkedHashMap<Object, List<Map>> lhm1 =
        new LinkedHashMap<Object, List<Map>>();

    // 获取碰撞号码集合
    // 规则
    int rule1NumCnt = 0;
    int rule2NumCnt = 0;
    int rule3NumCnt = 0;
    int offlineDays = 0;
    Timestamp rule1Ts = null;
    Timestamp rule2Ts = null;
    Timestamp rule3Ts = null;
    Timestamp minusDays = null;
    JSONObject criteria =
        JSONObject.parseObject(json).getJSONObject("criteria");
    JSONObject adhoc = JSONObject.parseObject(json).getJSONObject("adhoc");
    // 出现次数限定
    Integer targetInSets = adhoc.getInteger("target_in_sets");
    // 新出现时间限制
    if (adhoc.get("rule1_num_cnt") != null && adhoc.get("rule1_ts") != null) {
      rule1NumCnt = adhoc.getInteger("rule1_num_cnt");
      rule1Ts = adhoc.getTimestamp("rule1_ts");
    }
    // 消失时间限制
    if (adhoc.get("rule2_num_cnt") != null && adhoc.get("rule2_ts") != null) {
      rule2NumCnt = adhoc.getInteger("rule2_num_cnt");
      rule2Ts = adhoc.getTimestamp("rule2_ts");
    }
    // 没有联系的天数
    if (adhoc.get("rule3_num_cnt") != null && adhoc.get("rule3_ts") != null &&
        adhoc.get("offline_days") != null) {
      rule3NumCnt = adhoc.getInteger("rule3_num_cnt");
      rule3Ts = adhoc.getTimestamp("rule3_ts");
      LocalDateTime localDateTime =
          DateTimeConvert.dateToLocalDateTime(rule3Ts);
      offlineDays = adhoc.getInteger("offline_days");
      LocalDateTime minusDay = localDateTime.minusDays(offlineDays);
      minusDays = Timestamp.valueOf(minusDay);
    }

    JSONArray owner = criteria.getJSONArray("owner_num");
    logDebug("owner_num: " + owner);
    JSONArray ownerNumsJson = owner.getJSONArray(1);
    List<String> ownerNums = ownerNumsJson.toJavaList(String.class);

    //@formatter:off
    String sql = "SELECT pr.peer_num, pr.owner_num FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.peer_num, pr.owner_num";
    //@formatter:on
    List<Object> values = new ArrayList<>();
    List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
    if (pubServiceNums != null && pubServiceNums.size() > 0) {
      for (PubServiceNum psn : pubServiceNums) {
        values.add(psn.getNum());
      }
    }
    Options options3 = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    options3.addCriteria(new CriteriaTuple("pr.peer_num", Op.NOT_IN, values));
    options3.setViews(map("group-by", groupBy));
    List<Map> lm1 = doStat(options3, sql);

    HashMap<Object, List<Map>> ownerPeerNums = ListMap.reduce("owner_num", lm1);
    logDebug("peerNums size:" + ownerPeerNums.size());
    // 对方号码的交集
    Set<String> peerNumsSize = new HashSet<String>();
    int ownerNumSize = ownerNums.size();
    Set<String> temp = new HashSet<String>();
    Set<String> peerNums1 = new HashSet<String>();
    int count = 1;
    for (int i = 0; i < ownerNumSize; i++) {
      List<Map> _aPeerNums = ownerPeerNums.get(ownerNums.get(i));
      if (_aPeerNums == null) {
        continue;
      }
      Set aPeerNums = ListMap.valuesToSet(_aPeerNums);
      if (targetInSets == 1) {
        peerNumsSize.addAll(aPeerNums);
        continue;
      }
      for (int j = i + 1; j < ownerNumSize; j++) {
        Set<String> peerNums = null;
        List<Map> _oPeersNums = ownerPeerNums.get(ownerNums.get(j));
        if (_oPeersNums == null) {
          continue;
        }
        Set oPeersNums = ListMap.valuesToSet(_oPeersNums);
        peerNums = Sets.intersection(aPeerNums, oPeersNums);
        count++;
        if (count == 2) {
          temp.addAll(peerNums);
        } else if (count == 3) {
          peerNums1 = Sets.intersection(temp, peerNums);
        } else {
          peerNums1 = Sets.intersection(peerNums1, peerNums);
        }
        logDebug("peerNums : " + peerNums);
        if (targetInSets == 2) {
          peerNumsSize.addAll(peerNums);
        } else if (targetInSets == count) {
          peerNumsSize.addAll(peerNums1);
          count = 1;
          logDebug("nums is:" + peerNumsSize);
        }
        logDebug("temp : " + temp);
        logDebug("peerNums1 : " + peerNums1);
      }
    }

    logDebug("target nums " + peerNumsSize);

    // 找出所有对方号码的话单信息
    //@formatter:off
    sql = "SELECT pr.peer_num, pr.owner_num, pr.owner_cname, pr.peer_cname," +
                  "MIN(pr.started_at) as started_at_min, " +
                  "Max(pr.started_at) as started_at_max,pr.peer_num_attr, count(1) count " +
          "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    groupBy = "GROUP BY pr.peer_num,pr.owner_num";
    String offlineDaySql = "SELECT pr.owner_num " + 
                           "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String offlineDayGroupBy = "GROUP BY pr.owner_num ";
    List<Map> lm3 = new ArrayList<Map>();
    //@formatter:on
    if (peerNumsSize.size() > 0) {
      Options options = UniversalQueryHelper.normalize(json, "pr",
          map("cp.case_id", param("case_id")));
      Options options2 = (Options) DeepCopy.copy(options);
      options2.setViews(map("group-by", offlineDayGroupBy));

      List<Object> startedAtBetween = new ArrayList<>();
      startedAtBetween.add(minusDays);
      startedAtBetween.add(rule3Ts);
      options2.addCriteria(
          new CriteriaTuple("pr.started_at", Op.BETWEEN, startedAtBetween));
      for (String peerNum : peerNumsSize) {
        options.addCriteria(new CriteriaTuple("pr.peer_num", peerNum,
            CriteriaTuple.ONCE_QUERY));
        List<Map> lm2 = doStat(options, sql, groupBy, "", "");
        List<Map> lm4 = null;
        if (rule3Ts != null) {
          options2.addCriteria(new CriteriaTuple("pr.peer_num", peerNum,
              CriteriaTuple.ONCE_QUERY));

          lm4 = doStat(options2, offlineDaySql);
          logDebug("lm4.size : " + lm4.size());
        }
        int countBefore = 0;
        int countAfter = 0;
        int countOffline = 0;
        if (rule1NumCnt != 0 || rule2NumCnt != 0) {
          for (Map m : lm2) {
            logDebug("rule1Ts : " + rule1Ts + ", rule2Ts :　" + rule2Ts);
            if (rule1Ts != null) {
              boolean after = false;
              Timestamp minStartedAt = (Timestamp) m.get("started_at_min");
              after = minStartedAt.after(rule1Ts);
              if (after) {
                countAfter++;
              }
            }
            if (rule2Ts != null) {
              boolean before = true;
              Timestamp maxStartedAt = (Timestamp) m.get("started_at_max");
              before = maxStartedAt.before(rule2Ts);
              if (before) {
                countBefore++;
              }
            }
          }
          logDebug(
              "countAfter : " + countAfter + ",countBefote : " + countBefore);
          logDebug("rule1NumCnt : " + rule1NumCnt + ", rule2NumCnt : " +
                   rule2NumCnt);
        }
        if (lm4 != null) {
          int ownerSize = owner.size();
          int size = lm4.size();
          countOffline = ownerSize - size;
        }
        if (countAfter >= rule1NumCnt && countBefore >= rule2NumCnt &&
            countOffline >= rule3NumCnt) {
          lm3.addAll(lm2);
        }
        logDebug("targetNum pbills : " + lm3.size());
      }
    }
    setOkView("in commons");

    view("listMap", lm3);
    render("/reports/listMap");
  }

  /**
   * 伴随号码筛选
   * 
   * <pre>
   * {
  "case_id":5,
  "criteria":{
        "owner_num":["IN",["13506510163"]]
  },
  "view":{},
  "adhoc": {
        "rule": 2, 
        "startedAt": "2000-01-11",
        "endedAt": "2011-06-12",
        "interval": 50,
        "radius": 0.50
  }
  }
   * </pre>
   * 
   * @throws Exception
   */
  @POST
  public void backupNums() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    int pageSizeInDay = 5;

    //@formatter:off
    String alyzDaySql = "SELECT DISTINCT pr.alyz_day " +
                        "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String pbillRecordSql = "SELECT pr.* " +
                            "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    String pbillRecordOrderBy = "ORDER BY pr.started_at";
    String alyzDayOrderBy = "ORDER BY pr.alyz_day ASC";
    //@formatter:on
    List<Map> pub = Base.findAll("SELECT DISTINCT num FROM pub_service_nums");
    List pubServiceNums = ListMap.valuesToList(pub);

    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    options.addCriteria(
        new CriteriaTuple("pr.bill_type", Op.NOT_IN, Arrays.asList(2, 3)));
    options.addCriteria(
        new CriteriaTuple("pr.peer_num", Op.NOT_IN, pubServiceNums));
    Options optionsCopy = (Options) DeepCopy.copy(options);
    options.setOrderBy(pbillRecordOrderBy);
    optionsCopy.setOrderBy(alyzDayOrderBy);

    List<Map> alyzDayMap = doStat(optionsCopy, alyzDaySql);
    List<Date> alyzDays = ListMap.valuesToList(alyzDayMap);

    Date lastAlyzDay = null;
    int lastAlyzDayIndex = -1;

    if (options.getAdhocParam("last_alyz_day") != null) {
      lastAlyzDay      = Date.valueOf(options.getAdhocParam("last_alyz_day").toString());
      lastAlyzDayIndex = alyzDays.indexOf(lastAlyzDay);
    }

    Date alyzDayStart = null;
    Date alyzDayEnd = null;
    if (lastAlyzDayIndex == -1 && alyzDays.size() > 0) {
      alyzDayStart = alyzDays.get(0);
      if (alyzDays.size() > pageSizeInDay) {
        alyzDayEnd = alyzDays.get(pageSizeInDay - 1);
      } else {
        alyzDayEnd = alyzDays.get(alyzDays.size() - 1);
      }
    } else if (lastAlyzDayIndex + 1 < alyzDays.size()) {
      alyzDayStart = alyzDays.get(lastAlyzDayIndex + 1);
      int alyzDayEndIndex =
          lastAlyzDayIndex + pageSizeInDay > alyzDays.size() ?
              alyzDays.size() - 1 : (lastAlyzDayIndex + pageSizeInDay);
      alyzDayEnd = alyzDays.get(alyzDayEndIndex);
    }

    if(alyzDayStart != null && alyzDayEnd != null) {
      options.addCriteria(new CriteriaTuple("pr.alyz_day", Op.BETWEEN, Arrays.asList(alyzDayStart, alyzDayEnd)));
      List<PbillRecord> pbillRecords = findAll(PbillRecord.class, options, pbillRecordSql);

      Map<String, Collection<PbillRecord>> map = backupNumsAnalyzer.doAnalyze(options, pbillRecords);
      Collection<PbillRecord> result = map.get("pbillRecords");

      setOkView("analyze backup-nums");
      view("pbill_records", result);
      render();
    } else {
      setOkView("analyze backup-nums last page");
      render("/common/_blank");
    }
  }

  /**
   * 新号码全盘搜索
   * 
   * <pre>
   * ceriterias: IN...
   * view:
   * adhoc:
   * </pre>
   * 
   * @throws Exception
   */
  @POST
  public void findNew() throws Exception {

    // 1.接收前台数据
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    Object correlation = options.getAdhocParam("correlation");
    Object minStartedDay = options.getAdhocParam("min_started_day");
    // 排除特殊号码
    List<Object> values = new ArrayList<>();
    List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
    if (pubServiceNums != null && pubServiceNums.size() > 0) {
      for (PubServiceNum psn : pubServiceNums) {
        values.add(psn.getNum());
      }
      options.addCriteria(new CriteriaTuple("pr.peer_num", Op.NOT_IN, values));
    }

    // 2.向数据库查出对应数据
    // 对方号码,关联度,归属地,最早出现时间,最晚出现时间
    // 300 = 5分钟
    //@formatter:off
    String sql = "SELECT pr.peer_num, pr.peer_num_attr, MIN(started_day) AS first_start, MAX(started_day) AS last_start, " +
                         "COUNT(DISTINCT owner_num) AS correlation, COUNT(duration > 300 OR NULL) AS long_time_calls " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.peer_num";
    String having = "HAVING correlation >= ? AND first_start >= ?";
    //@formatter:on

    Map count = new HashMap<String, Integer>();
    List<Map> lm1 =
        doStat(options, sql, groupBy, "", having, correlation, minStartedDay);
    logDebug("peer_num count is {}" + lm1.size());
    // 获得新号码的总数
    int total = lm1.size();
    count.put("peer_num_total", total);
    List<Map> lm4 = new ArrayList<Map>();
    lm4.add(count);

    // 关联人员,总关联度
    //@formatter:off
    sql = "SELECT DISTINCT pr.owner_num " +
          "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
          "WHERE cp.case_id = ? AND pr.peer_num = ? GROUP BY pr.owner_num";
    //长通话= duration > 300
    String totalSql = "SELECT COUNT(pr.duration > 300 OR NULL) AS long_time_call_total " +
                      "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                      "WHERE cp.case_id = ? AND pr.peer_num = ?";
    //@formatter:on
    for (Map map : lm1) {
      if (map.get("peer_num") != null) {
        String peer_num = map.get("peer_num").toString();
        List<Map> lm2 = Base.findAll(sql, caseId, peer_num);
        List<String> ownerNum = ListMap.valuesToList(lm2);
        Map<String, List<String>> m = map("owner_nums", ownerNum);
        List<Map> lm3 = Base.findAll(totalSql, caseId, peer_num);
        m.putAll(lm3.get(0));
        m.putAll(map);
        lm4.add(m);
      }
    }

    setOkView("find new num");

    view("caseId", caseId);
    view("listMap", lm4);
    render();
  }

  /**
   * 关系判定
   * 
   * @throws Exception
   */
  @POST
  public void relCluster() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    CriteriaTuple criteria1 = options.getCriteria("pr.owner_num");
    String ownerNum = criteria1.getValues().get(0).toString();
    Object contactDownLimit = options.getAdhocParam("contact_down_limit");

    // 本方与多个对方号码的通话次数
    //@formatter:off
    String sql = "SELECT pr.peer_num, count(1) as count " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " ;         
    String groupSql = "GROUP BY pr.peer_num";
    String havingSql = "having count > ?";   
    
    List<Map> lm1 = doStat(options, sql, groupSql, "", havingSql, 0);
    //目标号码与共同联系人的最早，最晚，通话次数                   
    String sql2 = "SELECT pr.owner_num as t_num, count(1) as t_count, MIN(pr.started_day) as t_first_call_day, MAX(pr.started_day) as t_last_call_day, pr.peer_num as common_num " +
                  "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " ;
    groupSql = "GROUP BY pr.owner_num, pr.peer_num "; 
    String tHavingSql = "having t_count >= ?";
    //关系号码与共同联系人的最早，最晚，通话次数                   
    String sql3 = "SELECT pr.owner_num as r_num, count(1) as r_count, MIN(pr.started_day) as r_first_call_day, MAX(pr.started_day) as r_last_call_day, pr.peer_num as common_num " +
                  "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " ;
    String rHavingSql = "having r_count >= ?";
    //@formatter:on   

    // 排除掉与本方没有通话的对方号码得到关系号码，后获取共同联系人
    Map<String, LinkedHashMap<Object, Map>> detailMap = new HashMap<>();
    Options tOptions = (Options) DeepCopy.copy(options);
    for (Map map : lm1) {
      String peerNum = map.get("peer_num").toString();
      tOptions.delCriteria("pr.owner_num");
      tOptions.delCriteria("pr.peer_num");
      Options rOptions = (Options) DeepCopy.copy(tOptions);

      // 获取共同联系人
      List<Map> commonNums = Pbill.peerCommonNums(caseId, json, ownerNum,
          peerNum, true);
      if (commonNums.size() == 0) {
        continue;
      }
      List<Object> comNums = new ArrayList<>();
      for (Map m : commonNums) {
        comNums.add(m.get("peer_num"));
      }
//      List comNums = Pbill.peerNumsInCommon(caseId, ownerNum.toString(),
//          peerNum.toString());
      tOptions.addCriteria(new CriteriaTuple("pr.owner_num", ownerNum,
          CriteriaTuple.ONCE_QUERY));
      rOptions.addCriteria(
          new CriteriaTuple("pr.owner_num", peerNum, CriteriaTuple.ONCE_QUERY));

      tOptions.addCriteria(new CriteriaTuple("pr.peer_num", Op.IN, comNums,
          CriteriaTuple.ONCE_QUERY));
      rOptions.addCriteria(new CriteriaTuple("pr.peer_num", Op.IN, comNums,
          CriteriaTuple.ONCE_QUERY));

      // 目标号码与共同联系人的通话,关系号码与共同联系人的通话

      List<Map> tLm = doStat(tOptions, sql2, groupSql, "", tHavingSql,
          contactDownLimit);
      List<Map> rLm = doStat(rOptions, sql3, groupSql, "", rHavingSql,
          contactDownLimit);

      LinkedHashMap<Object, Map> lhm = ListMap.merge("common_num", rLm, tLm);
      detailMap.put(peerNum, lhm);
    }

    setOkView();
    view("summary", lm1);
    view("details", detailMap);
    render();
  }

  /**
   * 通话交叉查询 grx
   * 
   * @throws Exception
   */
  @POST
  public void overlap() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));

    String sql =
        "SELECT pr.bill_type, pr.owner_num_status, pr.owner_comm_loc, pr.owner_num, pr.owner_cname, pr.comm_direction, pr.peer_num, pr.peer_cname, pr.peer_comm_loc, " +
                 "pr.started_day, pr.started_time, pr.started_at, pr.ended_at, pr.started_time_l1_class, pr.weekday, pr.time_class, pr.duration_class, pr.owner_ct_code, pr.owner_lac " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    CriteriaTuple criteria = options.getCriteria("pr.peer_num");
    Object peerNum = criteria.getValues().get(0);
    options.delCriteria("pr.peer_num");
    List<Map> lm1 = doStat(options, sql);
    String sql2 =
        "SELECT pr.bill_type, pr.owner_num_status, pr.owner_comm_loc, pr.owner_num, pr.owner_cname, pr.comm_direction, pr.peer_num, pr.peer_cname, pr.peer_comm_loc, " +
                  "pr.started_day, pr.started_time, pr.started_at, pr.ended_at, pr.started_time_l1_class, pr.weekday, pr.time_class, pr.duration_class, pr.owner_ct_code, pr.owner_lac " +
                  "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    // select * from FROM pbill_records pr LEFT JOIN cases_pbills cp ON
    // pr.pbill_id = cp.pbill_id
    // where case_id = ? and (started_at >= startAt AND started_at <= endAt) or
    // (started_at <= startAt AND ended_at >= endAt)
    // or (ended_at >= startAt AND ended_at <= endAt))
    // 与某一条话单有重合的
    List<Map> lm3 = new ArrayList<Map>();
    for (Map map : lm1) {

      Object startedAt = map.get("started_at");
      Object endedAt = map.get("ended_at");
      options.delCriteria("pr.owner_num");
      options.addCriteria(
          new CriteriaTuple("pr.owner_num", peerNum, CriteriaTuple.ONCE_QUERY));
      options.addCriteria(new CriteriaTuple("pr.started_at", Op.LT_EQ,
          Arrays.asList(new Object[] { endedAt }), CriteriaTuple.ONCE_QUERY));
      options.addCriteria(new CriteriaTuple("pr.ended_at", Op.GT_EQ,
          Arrays.asList(new Object[] { startedAt }), CriteriaTuple.ONCE_QUERY));

      List<Map> lm2 = doStat(options, sql2);
      if (lm2.size() != 0) {
        for (Map map2 : lm2) {
          lm3.add(map);
          lm3.add(map2);
        }
      }
    }
    setOkView();
    view("listMap", lm3);
    render("/reports/listMap");
  }

  /**
   * 矩阵关系的通话详情
   * 
   * @throws Exception
   */
  @POST
  public void matrixDrilldownPbillRecords() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", caseId.toString()));

    //@formatter:off
    String sql = "SELECT pr.* " +
                 "FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";        
    //@formatter:on
    List<PbillRecord> pbillRecords = findAll(PbillRecord.class, options, sql);

    setOkView("matrix pbill records");
    view("pbill_records", pbillRecords);
    render("/pb/pbill_records/index");
  }

  /**
   * 矩阵关系的共同号码
   * 
   * @throws Exception
   */
  @POST
  public void matrixDrilldownCommonNums() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();   

    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    String xNum = options.getAdhocParam("x_num").toString();
    String yNum = options.getAdhocParam("y_num").toString();
    
    List<Map> commonNums = Pbill.peerCommonNums(caseId, json, xNum, yNum, true);
    options.addCriteria(new CriteriaTuple("pr.owner_num", Op.IN, Arrays.asList(xNum, yNum)));
    //@formatter:off
    String sql = "SELECT pr.owner_num, pr.peer_num as common_num , peer_cname as common_num_cname, COUNT(1) as count " + 
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id ";
    String groupBySql = " GROUP BY pr.owner_num,pr.peer_num";
    //@formatter:on 
    List<Map> result = new ArrayList<Map>();
    if (commonNums != null && commonNums.size() != 0) {
      List<Object> comNums = new ArrayList<>();
      for (Map map : commonNums) {
        comNums.add(map.get("peer_num"));
      }
      options.addCriteria(new CriteriaTuple("pr.peer_num", Op.IN, comNums));
      result = doStat(options, sql, groupBySql, "", "");
    }
    LinkedHashMap<Object, List<Map>> reduce = ListMap.reduce("owner_num", result);

    setOkView("matrix common nums");
    view("valueType", "listMap");
    view("useKey", true);
    view("linkedHashMap", reduce);
    render("/reports/linkedHashMap");
  }

  /**
   * 伴随号码统计分析
   * 
   * @throws Exception
   */
  @POST
  public void summaryBackupNums() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    options.addCriteria(
        new CriteriaTuple("pr.bill_type", Op.NOT_IN, Arrays.asList(2, 3)));
    List<Map> pub = Base.findAll("SELECT DISTINCT num FROM pub_service_nums");
    List pubServiceNums = ListMap.valuesToList(pub);
    options.addCriteria(
        new CriteriaTuple("pr.peer_num", Op.NOT_IN, pubServiceNums));
    //@formatter:off
    String sql = "SELECT pr.* FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id ";
    //@formatter:on
    String orderBy = "ORDER BY pr.started_at";
    options.setOrderBy(orderBy);

    List<PbillRecord> pbillRecords = findAll(PbillRecord.class, options, sql);

    Map<String, Collection<PbillRecord>> map =
        backupNumsAnalyzer.doAnalyze(options, pbillRecords);
    List<PbillRecord> backupNums = new ArrayList<>();
    map.get("backupNums").forEach(item -> {
      backupNums.add(item);
    });
    Map summaryBackupNums = new HashMap<>();
    if (backupNums != null && backupNums.size() > 0) {
      summaryBackupNums = backupNumsAnalyzer.summary(backupNums);
    }

    setOkView("summary backup-nums");
    view("summaryBackupNums", summaryBackupNums);
    render();
  }
}
