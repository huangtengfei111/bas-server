package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.scijava.parse.ExpressionParser;
import org.scijava.parse.Operator;
import org.scijava.parse.Operators;
import org.scijava.parse.Variable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import app.controllers.APIController;
import app.jobs.AppEventListener;
import app.jobs.RefreshCacheListener;
import app.jobs.UpdateCaseOverviewListener;
import app.jobs.events.PbillAddedEvent;
import app.models.Case;
import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import app.models.pb.RelNumber;
import app.models.pb.VenNumber;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.services.pb.PbillService;
import app.util.DeepCopy;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

public class PbillRecordsController extends APIController {

  @Inject
  private PbillService pbillService;

  /**
   * 其他案件话单导入
   * 
   * @throws Exception
   */
  @POST
  public void copy() throws Exception {
    // 从前台获取到案件id:case_id,话单id:pbills_id
    Long caseId = Long.parseLong(param("case_id"));
    JSONArray jsonPbId =
        JSONObject.parseObject(getRequestString()).getJSONArray("pbill_ids");
    List<String> ph = new ArrayList<>();
    List<Pbill> results = new ArrayList<Pbill>();
    List<String> ownerNums = new ArrayList<>();

    for (Object pbillId : jsonPbId) {
      ph.add("?");
    }
    //@formatter:off
    String pbillConds = "id IN (" + String.join(",", ph) + ")";
    String insertSql = "INSERT IGNORE INTO cases_pbills(case_id, pbill_id, created_at, updated_at)" +
                       "VALUES(?,?,NOW(),NOW())";
    //@formatter:on
    List<Pbill> pbills =
        Pbill.where(pbillConds, jsonPbId.stream().toArray(Object[]::new));
    Case c = Case.findById(caseId);
    Base.connection().setAutoCommit(false);
    Base.openTransaction();
    PreparedStatement insertCasePbill = Base.startBatch(insertSql);

    for (Pbill pbill : pbills) {
      Base.addBatch(insertCasePbill, c.getLongId(), pbill.getLongId());
      results.add(pbill);
      ownerNums.add(pbill.getOwnerNum());
    }
    Base.executeBatch(insertCasePbill);
    Base.commitTransaction();
    Base.connection().setAutoCommit(true);

    PbillAddedEvent pbillAddedEvent = new PbillAddedEvent(caseId, ownerNums);
    List<AppEventListener> listeners = new ArrayList<>();
    listeners.add(new UpdateCaseOverviewListener());
    listeners.add(new RefreshCacheListener());

    registerAndPost(pbillAddedEvent, listeners);

    setOkView("copy");
    
    view("items", results);
    render();
  }

  /**
  *
  */
  @POST
  public void search() throws Exception {
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id",param("case_id")));
    Object dailyRec = options.getAdhocParam("daily_rec");
    Object limitObject = options.getAdhocParam("limit");
    Object pageObject = options.getAdhocParam("page");
    Map<String, Object> views = options.getViews();
    if (views.containsKey("order-by")) {
      Object orderBy = views.get("order-by");
      orderBy = "ORDER BY " + orderBy;
      views.put("order-by", orderBy);
    }

    int limit = 0;
    int page = 0;
    int offSet = 0;

    String limitSql = " ";
    String offSetSql = " ";
    if (limitObject != null) {
      limitSql = " LIMIT ?";
      limit = Integer.parseInt(limitObject.toString());
    }
    if (pageObject != null) {
      offSetSql = " OFFSET ?";
      page = Integer.parseInt(pageObject.toString());
      offSet = (page - 1) * limit;
    }

    List<Object> sqlAndVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, true);
    String sqlCond = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);

    //@formatter:off
    String sql = "SELECT pr.* FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + 
                sqlCond + limitSql + offSetSql;
    //@formatter:on

    if (dailyRec != null && !"all".equals(dailyRec)) {
      options = UniversalQueryHelper.normalize(json, "pr",
          map("cp.case_id", param("case_id")));
      String orderBy = options.takeAndNormalizeOrderBy("pr2");
      if ("null".equals(orderBy)) {
        orderBy = " ";
      } else {
        orderBy = "ORDER BY " + orderBy;
      }
      sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(options, true);
      sqlCond = (String) sqlAndVals.get(0);
      vals = (List) sqlAndVals.get(1);
    if ("min".equals(dailyRec)) {
      //@formatter:off
      String minSubQuery = "SELECT pr.owner_num AS owner_num, MIN(pr.started_at) AS started_at " +
                           "FROM pbill_records AS pr " +
                           "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond + 
                           "GROUP BY pr.owner_num, pr.started_day";
      sql = "SELECT pr2.* FROM ( " + minSubQuery + " ) AS spr " +
            "INNER JOIN pbill_records pr2 ON pr2.started_at = spr.started_at AND pr2.owner_num = spr.owner_num " +
            orderBy + limitSql + offSetSql ;
      //@formatter:on
    } else if ("max".equals(dailyRec)) {
      //@formatter:off
      String maxSubQuery = "SELECT pr.owner_num AS owner_num, MAX(pr.started_at) AS started_at " +
                           "FROM pbill_records AS pr " +
                           "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond + 
                           "GROUP BY pr.owner_num, pr.started_day";
      sql = "SELECT pr2.* FROM ( "+ maxSubQuery + " ) AS spr " +
            "INNER JOIN pbill_records pr2 ON pr2.started_at = spr.started_at AND pr2.owner_num = spr.owner_num " +
            orderBy + limitSql + offSetSql;
      //@formatter:on
    } else if ("min_max".equals(dailyRec)) {
    //@formatter:off
      String minMaxSubQuery = "(SELECT pr.owner_num AS owner_num, MIN(pr.started_at) AS started_at " +
                              "FROM pbill_records AS pr " +
                              "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond + 
                              "GROUP BY pr.owner_num, pr.started_day )" +
                              "UNION " +
                              "(SELECT pr.owner_num AS owner_num, MAX(pr.started_at) AS started_at " +
                              "FROM pbill_records AS pr " +
                              "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond +
                              "GROUP BY pr.owner_num, pr.started_day)";
      sql = "SELECT pr2.* FROM (" + minMaxSubQuery + ") AS spr " +
            "INNER JOIN pbill_records pr2 ON pr2.started_at = spr.started_at AND pr2.owner_num = spr.owner_num " +
            orderBy + limitSql + offSetSql;
      //@formatter:on
      vals.addAll(vals);
    }
    }
    if (limitObject != null) {
      vals.add(limit);
    }
    if (pageObject != null) {
      vals.add(offSet);
    }
    Object[] params = vals.stream().toArray(Object[]::new);

    List<PbillRecord> pbillRecords = PbillRecord.findBySQL(sql, params);

    setOkView("Search pbill records");
    view("page_current", page, "page_total", 0);
    view("pbill_records", pbillRecords);
    render("index");
  }

  /**
   * 根据所选字段显示符合条件的数量
   * 
   */
  @POST
  public void countBy() throws Exception {
    String json = getRequestString();
    List<Object> sqlAndVals =
        UniversalQueryHelper.getSqlAndBoundVals(json, "pr", true,
            map("cp.case_id", param("case_id")));

    String sqlCond = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    Object[] params = vals.stream().toArray(Object[]::new);

    //@formatter:off
    String sql = "SELECT COUNT(1) as count " + 
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " + 
                 sqlCond;
    //@formatter:on      
    List<Map> resultMap = Base.findAll(sql, params);

    if (resultMap != null && resultMap.size() > 0) {
      Map m = resultMap.get(0);
      if (m.get("count") != null) {
        long count = Long.parseLong(m.get("count").toString());

        setOkView("Count pbill records by field");
        view("count", count);
        render("/common/count");
        return;
      }
    }

    setOkView("Count pbill records and no result");
    view("count", 0);
    render("/common/count");
  }

  /**
   * 
   * @throws Exception
   */
  @POST
  public void countByDist() throws Exception {
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    Object dailyRec = options.getAdhocParam("daily_rec");
    Map<String, Object> views = options.getViews();
    if (views.containsKey("order-by")) {
      options.takeOrderBy();
    }
    List<Object> sqlAndVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, true);
    String sqlCond = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);

      //@formatter:off
      String sql = "SELECT pr.started_day, count(1) as count " +
                   "FROM pbill_records AS pr " +
                   "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond +
                   "GROUP BY pr.started_day ORDER BY pr.started_day";
      //@formatter:on

    if ("min".equals(dailyRec)) {
      //@formatter:off
        String minSubQuery = "SELECT pr.owner_num AS owner_num, MIN(pr.started_at) AS started_at " +
                             "FROM pbill_records AS pr " +
                             "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond + 
                             "GROUP BY pr.owner_num, pr.started_day";
        sql = "SELECT pr2.started_day, count(1) as count FROM ( " + minSubQuery + " ) AS spr " +
              "INNER JOIN pbill_records pr2 ON pr2.started_at = spr.started_at AND pr2.owner_num = spr.owner_num " +
              "GROUP BY pr2.started_day ORDER BY pr2.started_day ";
        //@formatter:on
    } else if ("max".equals(dailyRec)) {
      //@formatter:off
        String maxSubQuery = "SELECT pr.owner_num AS owner_num, MAX(pr.started_at) AS started_at " +
                             "FROM pbill_records AS pr " +
                             "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond + 
                             "GROUP BY pr.owner_num, pr.started_day";
        sql = "SELECT pr2.started_day, count(1) as count FROM ( "+ maxSubQuery + " ) AS spr " +
              "INNER JOIN pbill_records pr2 ON pr2.started_at = spr.started_at AND pr2.owner_num = spr.owner_num " +
              "GROUP BY pr2.started_day ORDER BY pr2.started_day ";
        //@formatter:on
    } else if ("min_max".equals(dailyRec)) {
      //@formatter:off
        String minMaxSubQuery = "(SELECT pr.owner_num AS owner_num, MIN(pr.started_at) AS started_at " +
                                "FROM pbill_records AS pr " +
                                "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond + 
                                "GROUP BY pr.owner_num, pr.started_day )" +
                                "UNION " +
                                "(SELECT pr.owner_num AS owner_num, MAX(pr.started_at) AS started_at " +
                                "FROM pbill_records AS pr " +
                                "LEFT JOIN cases_pbills AS cp ON cp.pbill_id = pr.pbill_id " + sqlCond +
                                "GROUP BY pr.owner_num, pr.started_day)";
        sql = "SELECT pr2.started_day, count(1) as count FROM (" + minMaxSubQuery + ") AS spr " +
              "INNER JOIN pbill_records pr2 ON pr2.started_at = spr.started_at AND pr2.owner_num = spr.owner_num " +
              "GROUP BY pr2.started_day ORDER BY pr2.started_day " ;
        //@formatter:on
      vals.addAll(vals);
    }

    Object[] params = vals.stream().toArray(Object[]::new);

    List<Map> lm1 = Base.findAll(sql, params);

    view("listMap", lm1);
    setOkView("dist of count-by");
    render("/reports/listMap");

  }

  @POST
  public void countByHourInWeekday() throws Exception {
    String json = getRequestString();
    //@formatter:off
    String selectSql = "SELECT weekday, started_hour_class, count(1) as count " + 
                       "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY weekday, started_hour_class";
    //@formatter:on    

    List<Map> lm1 = doStat(json, selectSql, groupBy, "", "pr", map("cp.case_id", param("case_id")));
    view("listMap", lm1);
    setOkView("dist of hour in weekday");
    render("/reports/listMap");
  }

  /**
   * 每天通话数量
   * 
   * @throws Exception
   */
  @POST
  public void dailyCount() throws Exception {
    //@formatter:off
    String sql = "SELECT COUNT(1) as count, pr.started_day " +
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id ";
    String orderBy = "ORDER BY pr.started_day ASC";
    String groupBy = "GROUP BY pr.started_day";
    //@formatter:on
    List<Map> lm1 = doStat(getRequestString(), sql, groupBy, orderBy, "pr", map("cp.case_id", param("case_id")));

    setOkView();
    view("listMap", lm1);
    render("/reports/listMap");
  }

  /**
   * 虚拟网短号转换
   */
  @GET
  public void convertVenNums() {
    Long caseId = Long.parseLong(param("case_id"));
    List<Pbill> pbills = Pbill.pbillsInCase(caseId);
    int effected = 0;
    int venNumCnt = 0;
    
    for (Pbill pbill : pbills) {
      List<VenNumber> venNums = pbill.venNums(caseId);
      if (venNums != null) {
        for (VenNumber venNum : venNums) {
          venNumCnt++;
          String num = venNum.getNum();
          String shortNum = venNum.getShortNum();
          int updated = PbillRecord.update("peer_num = ?", "peer_short_num = ?", num, shortNum);
          effected = effected + updated;
        }
      }
    }
    setOkView();
    view("venNumCnt", venNumCnt, "effected", effected);

    render();
  }

  /**
   * 亲情网短号转换
   */
  @GET
  public void convertRelNums() {
    Long caseId = Long.parseLong(param("case_id"));
    List<Pbill> pbills = Pbill.pbillsInCase(caseId);
    int effected = 0;
    int relNumCnt = 0;

    for (Pbill pbill : pbills) {
      List<RelNumber> relNums = pbill.relNums(caseId);
      if (relNums != null) {
        for (RelNumber relNum : relNums) {
          relNumCnt++;
          String num = relNum.getNum();
          String shortNum = relNum.getShortNum();
          int updated = PbillRecord.update("peer_num = ?", "peer_short_num = ?",
              num, shortNum);
          effected = effected + updated;
        }
      }
    }
    setOkView();
    view("relNumCnt", relNumCnt, "effected", effected);

    render();
  }
  
  /**
   * 短号转换
   */
  @GET
  public void covertShortNums() {
    Long caseId = Long.parseLong(param("case_id"));
    List<Pbill> pbills = Pbill.pbillsInCase(caseId);

    int venNumCnt = 0;
    int venNumEffected = 0;
    int relNumCnt = 0;
    int relNumEffected = 0;

    for (Pbill pbill : pbills) {
      List<VenNumber> venNums = pbill.venNums(caseId);
      if (venNums != null) {
        for (VenNumber venNum : venNums) {
          venNumCnt++;
          String num = venNum.getNum();
          String shortNum = venNum.getShortNum();
          int updated = PbillRecord.update("peer_num = ?", "peer_short_num = ?",
              num, shortNum);
          venNumEffected = venNumEffected + updated;
        }
      }
      List<RelNumber> relNums = pbill.relNums(caseId);
      if (relNums != null) {
        for (RelNumber relNum : relNums) {
          relNumCnt++;
          String num = relNum.getNum();
          String shortNum = relNum.getShortNum();
          int updated = PbillRecord.update("peer_num = ?", "peer_short_num = ?",
              num, shortNum);
          relNumEffected = relNumEffected + updated;
        }
      }
    }
    setOkView();
    view("venNumCnt", venNumCnt, "venNumEffected", venNumEffected, "relNumCnt",
        relNumCnt, "relNumEffected", relNumEffected);

    render();

  }
  
  /**
   * 集合运算
   * @throws Exception 
   */
  @POST
  public void calcOnSets() throws Exception {
    Long caseId = Long.parseLong(param("case_id"));
    
    JSONObject jsonOperation = JSON.parseObject(getRequestString());
    String t1Json = jsonOperation.getString("t1");
    Options t1Options = UniversalQueryHelper.normalize(t1Json, "pr",
        map("cp.case_id", caseId.toString()));
    String expression = jsonOperation.getString("expression");
    String meter = jsonOperation.getString("meter");
    String meter2 = "";
    if ("peer_num".equals(meter)) {
      meter = "pr.peer_num";
      meter2 = "peer_num";
    }else if ("celltower".equals(meter)) {
      meter = "pr.owner_ct_code";
      meter2 = "owner_ct_code";
    }
    //@formatter:off
    String sql = "SELECT count(1), " + meter + " " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " ;
    String groupSql = "group by " + meter;
    //@formatter:off
    List<Map> lm1 = doStat(t1Options, sql, groupSql, "", "");
    
    String t2Json = jsonOperation.getString("t2");
    Options t2Options = UniversalQueryHelper.normalize(t2Json, "pr",
        map("cp.case_id", caseId.toString()));    
    List<Map> lm2 = doStat(t2Options, sql, groupSql, "", "");
    
    Set t1Set = ListMap.valuesToSet(lm1);
    Set t2Set = ListMap.valuesToSet(lm2);  
    Map<String, Set> map = new HashMap<String, Set>();
    map.put("t1", t1Set);
    map.put("t2", t2Set);
    //集合逻辑的运算
    LinkedList<Object> queue = new ExpressionParser().parsePostfix(expression);
    LinkedList<Set> stack = new LinkedList<>();
    for (Object obj : queue) {
      if (obj instanceof Variable) {
        Variable var = (Variable) obj;
        Set s = map.get(var.getToken());        
        stack.push(s);
      }
      if (obj instanceof Operator) {
        Operator op = (Operator) obj;
        if (op.equals(Operators.ADD)) {
          Set v2 = stack.pop();
          Set v1 = stack.pop();
          Set v = Sets.union(v1, v2);
          stack.push(v);
        } else if (op.equals(Operators.SUB)) {
          Set v2 = stack.pop();
          Set v1 = stack.pop();
          Set v = Sets.difference(v1, v2);
          stack.push(v);
        } else if (op.equals(Operators.BITWISE_AND)) {
          Set v2 = stack.pop();
          Set v1 = stack.pop();
          Set v = Sets.intersection(v1, v2);
          stack.push(v);
        }
      }
    }
    Set set = stack.pop();   
    
    //所有话单的结果
    //@formatter:off
    sql = "SELECT count(1) as count, MIN(started_day) as all_first_day, MAX(started_day) as all_last_day " +
          "FROM pbill_records";
    //满足t1条件的结果
    String sql2 = "SELECT " + meter + ", count(1) as t1_count, MIN(pr.started_at) as t1_first_call, MAX(pr.started_at) as t1_last_call " +
                  "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " ;
    //@formatter:off
    Options options = new Options();
    List<Map> lm = new ArrayList<Map>();
    for (Object object : set) {
      options.addCriteria(new CriteriaTuple(meter2, object, CriteriaTuple.ONCE_QUERY));
      List<Map> lm3 = doStat(options, sql);
      
      t1Options.addCriteria(new CriteriaTuple(meter, object, CriteriaTuple.ONCE_QUERY));
      List<Map> lm4 = doStat(t1Options, sql2);
      //两条结果的合并
      if (Integer.parseInt(lm3.get(0).get("count").toString()) != 0) {
        for (int i = 0; i < lm3.size(); i++) {
          lm3.get(i).putAll(lm4.get(i));
        }
        lm.addAll(lm3);
      }     
    }
    
    setOkView();
    view("listMap", lm);
    render("/reports/listMap");    
  }
  
  /**
   * 通话一致性查询
   * @throws Exception 
   */
  @POST
  public void sameCalls() throws Exception {

    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    
    Options options2 = (Options)DeepCopy.copy(options);
    
    List<Object> ownerNums = options.getCriteria("pr.owner_num").getValues();
    List<Object> peerNums = options.getCriteria("pr.peer_num").getValues();
    
    //号码集合
    List<Object> ons = new  ArrayList<Object>();
    ons.addAll(ownerNums);
    ons.addAll(peerNums);   

    options2.delCriteria("pr.owner_num");
    options2.delCriteria("pr.peer_num");
    options2.addCriteria(new CriteriaTuple("pr.owner_num", Op.IN, ons, CriteriaTuple.ONCE_QUERY));
    options2.addCriteria(new CriteriaTuple("pr.peer_num", Op.IN, ons, CriteriaTuple.ONCE_QUERY));
    
    // 列出有merge_pbr_id的记录
    //@formatter:off
    String sql = "SELECT * " + 
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON cp.pbill_id = pr.pbill_id ";
    options2.setNotNullFields(Arrays.asList(new String[]{"pr.merged_pbr_id"}));
    
    List<Map> lm1 = doStat(options2, sql);
    
    //对没有merge_pbr_id的记录进行筛选
    sql = "SELECT pr.bill_type, pr.owner_num_status, pr.owner_comm_loc, pr.owner_num, pr.owner_cname, pr.comm_direction, pr.peer_num, pr.peer_cname, pr.peer_comm_loc, " + 
          "pr.started_day, pr.started_time, pr.started_at, pr.ended_at, pr.started_time_l1_class, pr.weekday, pr.time_class, pr.duration_class, pr.owner_ct_code, pr.owner_lac " +
          "FROM pbill_records pr LEFT JOIN cases_pbills cp ON cp.pbill_id = pr.pbill_id ";
    options.delCriteria("pr.peer_num"); 
    //@formatter:on
    options.setNullFields(Arrays.asList(new String[] { "pr.merged_pbr_id" }));
    List<Map> lm2 = doStat(options, sql);
    if (lm2 != null) {
      Date startedAt1;
      Date startedAt2;
      options.delCriteria("pr.owner_num");
      options.delCriteria("pr.peer_num");

      for (Map map : lm2) {
        Date startedAt = (Date) map.get("started_at");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startedAt);
        calendar.add(Calendar.SECOND, -10);
        startedAt1 = calendar.getTime();
        calendar.add(Calendar.SECOND, 20);
        startedAt2 = calendar.getTime();
        options.addCriteria(new CriteriaTuple("pr.started_at", Op.BETWEEN,
            Arrays.asList(new Object[] { startedAt1, startedAt2 }),
            CriteriaTuple.ONCE_QUERY));

        Object peerNum = map.get("peer_num");
        Object ownerNum = map.get("owner_num");
        options.addCriteria(new CriteriaTuple("pr.owner_num", peerNum,
            CriteriaTuple.ONCE_QUERY));
        options.addCriteria(new CriteriaTuple("pr.peer_num", ownerNum,
            CriteriaTuple.ONCE_QUERY));
        List<Map> lm3 = doStat(options, sql);

        if (lm3.size() != 0) {
          lm1.add(map);
          lm1.addAll(lm3);
        }

      }
    }

    setOkView();
    view("listMap", lm1);
    render("/reports/listMap");
  }
}
