package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.annotations.POST;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import app.controllers.APIController;
import app.models.pb.PbillRecord;
import app.models.search.CriteriaTuple;
import app.models.search.Options;
import app.services.pb.PbillStatService;
import app.util.StatColHeader;
import app.util.UniversalQueryHelper;

public class PbillMutualController extends APIController {

  @Inject
  private PbillStatService pbillStatService;
  /**
   * 号码在对方网络里的关联度
   */
  public void closeDegree() {

  }

  /**
   * 按日期展示联系过程
   * 
   * @throws Exception
   */
  @POST
  public void dailyCount() throws Exception {
    String json = getRequestString();

    //@formatter:off
    String sql = "SELECT pr.started_day, COUNT(1) AS daily_count " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_day";   
    //@formatter:on
    
    List<Map> lm1 = doStat(json, sql, groupBy, "pr", map("cp.case_id", param("case_id")));
    
    setOkView("daily_count");
    view("listMap", lm1);
    render("/reports/listMap");

  }

  /**
   * 联系的时间分布(时间段分布)
   * 
   * @throws Exception
   */
  @POST
  public void hourDist() throws Exception {
    String json = getRequestString();

    //@formatter:off
    String sql = "SELECT pr.started_hour_class AS shc, COUNT(1) AS count  FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.started_hour_class";
    //@formatter:on
    
    List<Map> lm1 = doStat(json, sql, groupBy, "pr", map("cp.case_id", param("case_id")));

    for (Map m : lm1) {
      Object v1 = m.remove("shc");
      Object v2 = m.remove("count");
      String shc = StatColHeader.startedHour(v1.toString());
      m.put(shc, v2);
    }

    setOkView("hour_dist");
    view("listMap", lm1);
    render("/reports/listMap");

  }

  /**
   * 联系的时间分布(周几分布)
   * 
   * @throws Exception
   */
  @POST
  public void weekDist() throws Exception {
    String json = getRequestString();

    //@formatter:off
    String sql = "SELECT pr.weekday, COUNT(1) AS count  FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.weekday";
    //@formatter:on

    List<Map> lm1 = doStat(json, sql, groupBy, "pr", map("cp.case_id", param("case_id")));

    for (Map m : lm1) {
      Object v1 = m.remove("weekday");
      Object v2 = m.remove("count");
      String weekday = StatColHeader.week(v1.toString());
      m.put(weekday, v2);
    }
    
    setOkView("week_dist");
    view("listMap", lm1);
    render("/reports/listMap");
    
  }

  /**
   * 联系时长分布
   * 
   * @throws Exception
   */
  @POST
  public void duration() throws Exception {
    String json = getRequestString();

    //@formatter:off
    String sql = "SELECT pr.duration_class AS dc, COUNT(1) AS count FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY dc";
    //@formatter:on

    List<Map> lm1 =
        doStat(json, sql, groupBy, "pr", map("cp.case_id", param("case_id")));

    for (Map m : lm1) {
      Object v1 = m.remove("dc");
      Object v2 = m.remove("count");
      String weekday = StatColHeader.durationClass(v1.toString());
      m.put(weekday, v2);
    }

    setOkView("duration");
    view("listMap", lm1);
    render("/reports/listMap");
  }

  /**
   * 联系的主被叫对比
   * @throws Exception 
   */
  @POST
  public void calls() throws Exception {
    String json = getRequestString();
    
    //@formatter:off
    String sql = "SELECT pr.comm_direction AS cd, COUNT(1) AS count  " +
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.comm_direction";
    String having = "HAVING pr.comm_direction = 11 OR pr.comm_direction = 12";
    //@formatter:on
    
    List<Map> lm1 = doStat(json, sql, groupBy, having, "pr", map("cp.case_id", param("case_id")));

    for (Map m : lm1) {
      Object v1 = m.remove("cd");
      Object v2 = m.remove("count");
      String weekday = StatColHeader.commDirection(v1.toString());
      m.put(weekday, v2);
    }

    setOkView("calls");
    view("listMap", lm1);
    render("/reports/listMap");

  }

  /**
   * 碰面情况
   */
  public void meets() {

  }

  /**
   * 共同去外地情况
   * 
   * @throws Exception
   */
  @POST
  public void travel() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    String ownerNumA = options.getAdhocParam("numA").toString();
    String ownerNumB = options.getAdhocParam("numB").toString();

    List<Object> sqlAndVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    String timeSql = (String) sqlAndVals.get(0);
    List<Object> timeValues = (List) sqlAndVals.get(1);
    //@formatter:off
    String sql = "SELECT MIN(pr.started_at) AS min_started_at, MAX(pr.started_at) AS max_started_at " +
                 "FROM pbill_records AS pr LEFT JOIN pbills AS p ON pr.pbill_id = p.id " +
                 "LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                 "WHERE !LOCATE(pr.owner_comm_loc,p.call_attribution) " +
                 "AND pr.owner_num = ? AND " + timeSql;
    //@formatter:on
    List<Object> startedAtWithEndedAt = new ArrayList<>();
    startedAtWithEndedAt.add(ownerNumA);
    startedAtWithEndedAt.addAll(timeValues);
    List<Map> aOwnerNumTime =
        Base.findAll(sql, startedAtWithEndedAt.stream().toArray(Object[]::new));

    startedAtWithEndedAt.clear();
    startedAtWithEndedAt.add(ownerNumB);
    startedAtWithEndedAt.addAll(timeValues);
    List<Map> bOwnerNumTime =
        Base.findAll(sql, startedAtWithEndedAt.stream().toArray(Object[]::new));

    Map aMap = aOwnerNumTime.get(0);
    Map bMap = bOwnerNumTime.get(0);
    List<PbillRecord> pbillRecords = new ArrayList<>();
    Map<String, List<Set<Timestamp>>> ext = new HashMap<>();
    if (aMap.get("min_started_at") != null &&
        aMap.get("max_started_at") != null &&
        bMap.get("min_started_at") != null &&
        bMap.get("max_started_at") != null) {
      Timestamp aStartedAt = (Timestamp) aMap.get("min_started_at");
      Timestamp aEndedAt = (Timestamp) aMap.get("max_started_at");
      logDebug("aStartedAt:" + aStartedAt + ", aEndedAt : " + aEndedAt);
      Timestamp bStartedAt = (Timestamp) bMap.get("min_started_at");
      Timestamp bEndedAt = (Timestamp) bMap.get("max_started_at");
      logDebug("bStartedAt:" + aStartedAt + ", bEndedAt : " + aEndedAt);
      if (bStartedAt.before(aEndedAt) && bEndedAt.after(aStartedAt)) {
        Timestamp startedAt = null;
        Timestamp endedAt = null;
        if (aStartedAt.before(bStartedAt)) {
          startedAt = bStartedAt;
        } else {
          startedAt = aStartedAt;
        }
        if (aEndedAt.before(bEndedAt)) {
          endedAt = aEndedAt;
        } else {
          endedAt = bEndedAt;
        }
        logDebug("startedAt:" + startedAt + ", endedAt : " + endedAt);
        //@formatter:off
        sql = "SELECT pr.started_at, pr.ended_at, pr.owner_comm_loc " +
              "FROM pbill_records pr LEFT JOIN pbills AS p ON pr.pbill_id = p.id " +
              "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
              "WHERE cp.case_id = ? AND pr.owner_num = ? AND (pr.started_at BETWEEN ? AND ?) " +
                    "AND !LOCATE(pr.owner_comm_loc,p.call_attribution) " +
              "ORDER BY pr.started_at ASC";
        //@formatter:on
        List<PbillRecord> prListA =
            PbillRecord.findBySQL(sql, caseId, ownerNumA, startedAt, endedAt);

        List<PbillRecord> prListB =
            PbillRecord.findBySQL(sql, caseId, ownerNumB, startedAt, endedAt);
        // 提取
        Multimap<String, Range<Long>> aLocWithTime = HashMultimap.create(); //
        String lastLoc = null;
        Range<Long> lastRange = null;
        for (int i = 0; i < prListA.size(); i++) {
          PbillRecord pr = prListA.get(i);
          String loc = pr.getOwnerCommLoc();
          Long startedAtInSec = pr.getStartedAt().getTime();
          Long endedAtInSec = pr.getEndedAt().getTime();
          if (lastLoc != null && lastLoc.equals(loc)) { // 同一个地点
            if (endedAtInSec < lastRange.upperEndpoint()) {
              lastRange = lastRange
                  .span(Range.closed(endedAtInSec, lastRange.upperEndpoint()));
            } else {
              lastRange = lastRange
                  .span(Range.closed(lastRange.upperEndpoint(), endedAtInSec));
            }
          } else { // 不是同一个地点
            if (lastLoc != null) {
              aLocWithTime.put(lastLoc, lastRange);
            }
            // 新开Range
            Range<Long> r = Range.closed(startedAtInSec, endedAtInSec);
//            aLocWithTime.put(loc, r);
            lastLoc = loc;
            lastRange = r;
          }
          if (i + 1 >= prListA.size()) {
            aLocWithTime.put(lastLoc, lastRange);
          }
        }
        Multimap<String, Range<Long>> bLocWithTime = HashMultimap.create();
        lastLoc = null;
        lastRange = null;
        for (int i = 0; i < prListB.size(); i++) {
          PbillRecord pr = prListB.get(i);
          String loc = pr.getOwnerCommLoc();
          Long startedAtInSec = pr.getStartedAt().getTime();
          Long endedAtInSec = pr.getEndedAt().getTime();
          if (lastLoc != null && lastLoc.equals(loc)) { // 同一个地点
            if (endedAtInSec < lastRange.upperEndpoint()) {
              lastRange = lastRange
                  .span(Range.closed(endedAtInSec, lastRange.upperEndpoint()));
            } else {
              lastRange = lastRange
                  .span(Range.closed(lastRange.upperEndpoint(), endedAtInSec));
            }
          } else { // 不是同一个地点
            if (lastLoc != null) {
              bLocWithTime.put(lastLoc, lastRange);
            }
            // 新开Range
            Range<Long> r = Range.closed(startedAtInSec, endedAtInSec);
//            bLocWithTime.put(loc, r);
            lastLoc = loc;
            lastRange = r;
          }
          if (i + 1 >= prListB.size()) {
            bLocWithTime.put(lastLoc, lastRange);
          }

        }
        logDebug("aLocWithTime : " + aLocWithTime);
        logDebug("bLocWithTime : " + bLocWithTime);
        Set<String> commLocs =
            Sets.intersection(aLocWithTime.keySet(), bLocWithTime.keySet());
        logDebug("commLocs : " + commLocs);
        List<Map> locWithTimeCommList = new ArrayList<>();
        Map locWithTime = new HashMap<>();
        //@formatter:off
        String pbillRecordsCommSql = "SELECT * " +
                                     "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                                     "WHERE cp.case_id = ? AND pr.owner_num IN (?, ?) " +
                                     "AND (pr.started_at BETWEEN ? AND ?) AND pr.owner_comm_loc = ? " +
                                     "ORDER BY pr.started_at ASC";
        //@formatter:on
        for (String locComm : commLocs) {
          List<Set<Timestamp>> timesList = new ArrayList<>();
          Collection<Range<Long>> aTimes = aLocWithTime.get(locComm);
          Collection<Range<Long>> bTimes = bLocWithTime.get(locComm);
          aTimes.forEach(aTime -> {
            bTimes.forEach(bTime -> {
              Set<Timestamp> times = new HashSet<>();
              if (aTime.upperEndpoint() >= bTime.lowerEndpoint() &&
                  aTime.lowerEndpoint() <= bTime.upperEndpoint()) {
                Range<Long> range = aTime.intersection(bTime);
                logDebug("range:" + range);
                if (!range.isEmpty()) {
                  Long startedAtInSec = range.lowerEndpoint();
                  Long endedAtInSec = range.upperEndpoint();
                  logDebug("startedAtInSec : " + startedAtInSec);
                  logDebug("endedAtInSec : " + endedAtInSec);
                  Timestamp startedAtComm = new Timestamp(startedAtInSec);
                  Timestamp endedAtComm = new Timestamp(endedAtInSec);
                  List<PbillRecord> pbillRecord = PbillRecord.findBySQL(
                      pbillRecordsCommSql, caseId, ownerNumA, ownerNumB,
                      startedAtComm, endedAtComm, locComm);
                  pbillRecords.addAll(pbillRecord);
                  times.add(startedAtComm);
                  times.add(endedAtComm);
                  timesList.add(times);
                }
              }
            });
          });
          if (timesList.size() > 0) {
            ext.put(locComm, timesList);
          }
        }
      }
    }
    setOkView("travel");
    view("ext", ext, "pbill_records", pbillRecords);
    render();
  }

  /**
   * 共同关系人
   * 
   * @throws Exception
   */
  @POST
  public void inCommons() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", param("case_id")));
    String numA = options.getAdhocParam("numA").toString();
    String numB = options.getAdhocParam("numB").toString();

    Set<String> peerNumSet = pbillStatService.peerNumsInCommon(caseId, numA, numB);
    List<Map> results = new ArrayList<>();
    if (peerNumSet != null) {
      peerNumSet.add(numA);
      peerNumSet.add(numB);
//      HashSet<String> peer = Sets.newHashSet(peerNums);
      // 找出所有对方号码的话单信息
      //@formatter:off
      String sql = "SELECT pr.owner_num, pr.peer_num, COUNT(1) as count, pr.owner_cname, pr.peer_cname, " +
                   "MIN(pr.started_at) AS min_started_at, MAX(pr.started_at) AS max_started_at " +
                   "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
      String having = "HAVING count > 1";
      //@formatter:on
      Object[] peerNums = peerNumSet.toArray();
      // 所有号码之间满足通话次数的结果
      for (int i = 0; i < peerNums.length; i++) {
        String num0 = peerNums[i].toString();
        for (int j = i + 1; j < peerNums.length; j++) {
          String num1 = peerNums[j].toString();
          options.addCriteria(new CriteriaTuple("pr.owner_num", num0, CriteriaTuple.ONCE_QUERY));
          options.addCriteria(new CriteriaTuple("pr.peer_num", num1, CriteriaTuple.ONCE_QUERY));
          List<Map> conn = doStat(options, sql, "", "", having);
          options.addCriteria(new CriteriaTuple("pr.owner_num", num1, CriteriaTuple.ONCE_QUERY));
          options.addCriteria(new CriteriaTuple("pr.peer_num", num0, CriteriaTuple.ONCE_QUERY));
          List<Map> conn1 = doStat(options, sql, "", "", having);

          results.addAll(conn);
          results.addAll(conn1);
        }
      }
      logDebug("peerNums : " + peerNums);
      logDebug("results : " + results);
    }

    setOkView("in commons");

    view("results", results);
    render("/pb/pbill_analyze/connections");

  }

}
