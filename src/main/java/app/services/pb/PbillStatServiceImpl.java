package app.services.pb;

import static org.javalite.common.Collections.map;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Rectangle;

import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import app.models.pb.PnumMeet;
import app.models.pb.PubServiceNum;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

/**
 * @author
 */
public class PbillStatServiceImpl implements PbillStatService, PbillStatCapable, CellTowerAwareService {
  private static final Integer PAGE_SIZE = 5;

  //计算关联度的
  @Override
  public Set peerNumsInCommon(Long caseId, String num1, String num2) {
    return Pbill.peerNumsInCommon(caseId, num1, num2);
  }

  @Override
  public long peerNumsCommonDegree(Long caseId, String num1, String num2) {
    return Pbill.peerNumsCommonDegree(caseId, num1, num2);
  }

  @Override
  public long peerNumsCommonDegree(String json, String num1, String num2,
      Long caseId) throws Exception {
    List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
    List<Object> vals0 = new ArrayList<>();
    for (PubServiceNum psn : pubServiceNums) {
      vals0.add(psn.getNum());
    }
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    Options options2 = UniversalQueryHelper.normalize(json, "pr2",
        map("cp2.case_id", caseId.toString()));
    options.addCriteria(new CriteriaTuple("pr.peer_num", Op.NOT_IN, vals0));
    options.addCriteria(
        new CriteriaTuple("pr.owner_num", num1));
    options2.addCriteria(
        new CriteriaTuple("pr2.owner_num", num2));
    List<Object> sqlAndVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    List<Object> sqlAndVals2 =
        UniversalQueryHelper.getSqlAndBoundVals(options2, false);
    String sql = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    String sql2 = (String) sqlAndVals2.get(0);
    List vals2 = (List) sqlAndVals2.get(1);
    vals.addAll(vals2);

    //@formatter:off
    String commonDegreeSql = "SELECT count(distinct pr.peer_num) as degree " + 
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                 "WHERE  " + sql +
                      "AND peer_num IN ( SELECT distinct peer_num " + 
                                        "FROM pbill_records as pr2 LEFT JOIN cases_pbills as cp2 ON pr2.pbill_id = cp2.pbill_id " + 
                                        "WHERE " + sql2 + ")";
    //@formatter:on

    List<Map> lm =
        Base.findAll(commonDegreeSql, vals.stream().toArray(Object[]::new));
    if (lm == null || lm.size() == 0) {
      return 0l;
    } else {
      return Long.parseLong(lm.get(0).get("degree").toString());
    }
  }

  // 互相碰面
  @Override
  public PnumMeet meets(Long caseId, String json) throws Exception {
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    String locRule = options.getAdhocParam("loc_rule").toString();
    String numA = (String) options.getAdhocParam("numA");
    String numB = (String) options.getAdhocParam("numB");
    Date lastAlyzDay = null;
    if (options.getAdhocParam("last_alyz_day") != null) {
      lastAlyzDay =
          Date.valueOf(options.getAdhocParam("last_alyz_day").toString());
      options.addCriteria(
          new CriteriaTuple("pr.alyz_day", Op.GT, Arrays.asList(lastAlyzDay)));
    }
    List zero = Arrays.asList(0);
    Integer mutualCall = (Integer) options.getAdhocParam("mutual_call");

    options.addCriteria(
        new CriteriaTuple("pr.owner_num", numA, CriteriaTuple.ONCE_QUERY));
    options.addCriteria(new CriteriaTuple("pr.owner_lac", Op.GT, zero));
    options.addCriteria(new CriteriaTuple("pr.owner_ci", Op.GT, zero));

    //@formatter:off
    String alyzDaySql = "SELECT DISTINCT pr.alyz_day " +
                        "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String orderBy = "ORDER BY pr.alyz_day";
    //@formatter:on
    if (mutualCall == MUTUAL_CALL_TRUE) {
      options.addCriteria(
          new CriteriaTuple("pr.peer_num", numB, CriteriaTuple.ONCE_QUERY));
    }

    List<Map> alyzDayList = doStat(options, alyzDaySql, "", orderBy, "");
    if (alyzDayList == null || alyzDayList.size() == 0) {
      return null;
    }
    List<Date> alyzDays = ListMap.valuesToList(alyzDayList);
//    options.addCriteria(new CriteriaTuple("pr.alyz_day", Op.IN, alyzDays));

    if (SAME_CI.equals(locRule)) {
      return anlyzeBasedOnCI(options, json, caseId, numA, numB, alyzDays);
    } else if (SAME_LAC.equals(locRule)) {
      return anlyzeBasedOnLAC(options, json, caseId, numA, numB, alyzDays);
    } else if (SCOPE_CT.equals(locRule)) {
      return anlyzeBasedOnGeoDist(caseId, json, options, numA, numB,
          lastAlyzDay);
    }
    return null;
  }

  public PnumMeet anlyzeBasedOnCI(Options options, String query, Long caseId,
      String numA, String numB, List<Date> alyzDays) throws Exception {
    PnumMeet meet = new PnumMeet();
    meet.setRule("lacOrCi");
    Options options2 = UniversalQueryHelper.normalize(query, "pr2",
        map("cp2.case_id", caseId.toString()));
    options.addCriteria(new CriteriaTuple("pr.owner_num", numB));
    options2.addCriteria(new CriteriaTuple("pr2.owner_num", numA));
    List<Object> numASqlAndBoundVals =
        UniversalQueryHelper.getSqlAndBoundVals(options2, false);
    String numASql = (String) numASqlAndBoundVals.get(0);
    List numAVals = (List) numASqlAndBoundVals.get(1);
    List<Object> numBSqlAndBoundVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    String numBSql = (String) numBSqlAndBoundVals.get(0);
    List numBvals = (List) numBSqlAndBoundVals.get(1);

    //@formatter:off
    String numAOwnerCiSql = "SELECT DISTINCT pr2.owner_ci " +
                            "FROM pbill_records AS pr2 LEFT JOIN cases_pbills AS cp2 ON pr2.pbill_id = cp2.pbill_id " +
                            "WHERE " + numASql + 
                            "AND pr2.alyz_day = ?";
    String ownerCiNumANumBSql = "SELECT DISTINCT pr.owner_ci FROM pbill_records AS pr " +
                                "LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                                "WHERE " + numBSql + 
                                "AND pr.owner_ci IN (" + numAOwnerCiSql + ") AND pr.alyz_day = ?";
    //@formatter:on
    HashMap<Date, Set<String>> ciMap = new HashMap<>();
    List alyzDayPageSize = new ArrayList<>();
    int pageSize = 0;
    for (Date alyzDay : alyzDays) {
      List ownerCiNumANumBVals = new ArrayList<>();
      ownerCiNumANumBVals.addAll(numBvals);
      ownerCiNumANumBVals.addAll(numAVals);
      ownerCiNumANumBVals.add(alyzDay);
      ownerCiNumANumBVals.add(alyzDay);
      List<Map> ownerCiNumANumBList = Base.findAll(ownerCiNumANumBSql,
          ownerCiNumANumBVals.stream().toArray(Object[]::new));
      if (ownerCiNumANumBList != null && ownerCiNumANumBList.size() > 0) {
        Set<String> ciSet = new HashSet<>();
        ownerCiNumANumBList.forEach(ownerCiNumANumB -> {
          Long ownerCi =
              Long.parseLong(ownerCiNumANumB.get("owner_ci").toString());
          ciSet.add(Long.toHexString(ownerCi).toUpperCase());
        });
        ciMap.put(alyzDay, ciSet);
        alyzDayPageSize.add(alyzDay);
        pageSize++;
      }
      if (pageSize == PAGE_SIZE) {
        break;
      }
    }

    List<PbillRecord> pbillRecords = null;
    if (alyzDayPageSize != null && alyzDayPageSize.size() > 0) {
      pbillRecords =
          pbillRecordsOnSameCISameLAC(query, numA, numB, alyzDayPageSize);
      meet.setPbillRecords(pbillRecords);
      meet.setLacOrCiMap(ciMap);
      return meet;
    }
    return null;
  }

  public PnumMeet anlyzeBasedOnLAC(Options options, String query, Long caseId,
      String numA, String numB, List<Date> alyzDays) throws Exception {
    PnumMeet meet = new PnumMeet();
    meet.setRule("lacOrCi");
    Options options2 = UniversalQueryHelper.normalize(query, "pr2",
        map("cp2.case_id", caseId.toString()));
    options.addCriteria(new CriteriaTuple("pr.owner_num", numB));
    options2.addCriteria(new CriteriaTuple("pr2.owner_num", numA));
    List<Object> numASqlAndBoundVals =
        UniversalQueryHelper.getSqlAndBoundVals(options2, false);
    String numASql = (String) numASqlAndBoundVals.get(0);
    List numAVals = (List) numASqlAndBoundVals.get(1);
    List<Object> numBSqlAndBoundVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    String numBSql = (String) numBSqlAndBoundVals.get(0);
    List numBvals = (List) numBSqlAndBoundVals.get(1);

    //@formatter:off
    String numAOwnerLacSql = "SELECT DISTINCT pr2.owner_lac " +
                            "FROM pbill_records AS pr2 LEFT JOIN cases_pbills AS cp2 ON pr2.pbill_id = cp2.pbill_id " +
                            "WHERE " + numASql + 
                            "AND pr2.alyz_day = ?";
    String ownerLacNumANumBSql = "SELECT DISTINCT pr.owner_lac FROM pbill_records AS pr " +
                                "LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                                "WHERE " + numBSql + 
                                "AND pr.owner_lac IN (" + numAOwnerLacSql + ") AND pr.alyz_day = ?";
    //@formatter:on
    HashMap<Date, Set<String>> lacMap = new HashMap<>();
    List alyzDayPageSize = new ArrayList<>();
    int pageSize = 0;
    for (Date alyzDay : alyzDays) {
      List ownerLacNumANumBVals = new ArrayList<>();
      ownerLacNumANumBVals.addAll(numBvals);
      ownerLacNumANumBVals.addAll(numAVals);
      ownerLacNumANumBVals.add(alyzDay);
      ownerLacNumANumBVals.add(alyzDay);
      List<Map> ownerLacNumANumBList = Base.findAll(ownerLacNumANumBSql,
          ownerLacNumANumBVals.stream().toArray(Object[]::new));
      if (ownerLacNumANumBList != null && ownerLacNumANumBList.size() > 0) {
        Set<String> lacSet = new HashSet<>();
        ownerLacNumANumBList.forEach(ownerLacNumANumB -> {
          Long ownerLac =
              Long.parseLong(ownerLacNumANumB.get("owner_lac").toString());
          lacSet.add(Long.toHexString(ownerLac).toUpperCase());
        });
        lacMap.put(alyzDay, lacSet);
        alyzDayPageSize.add(alyzDay);
        pageSize++;
      }
      if (pageSize == PAGE_SIZE) {
        break;
      }
    }

    List<PbillRecord> pbillRecords = null;
    if (alyzDayPageSize != null && alyzDayPageSize.size() > 0) {
      pbillRecords =
          pbillRecordsOnSameCISameLAC(query, numA, numB, alyzDayPageSize);
      meet.setPbillRecords(pbillRecords);
      meet.setLacOrCiMap(lacMap);
      return meet;
    }
    return null;
  }

  public PnumMeet anlyzeBasedOnGeoDist(Long caseId, String query,
      Options options, String numA, String numB, Date lastAlyzDay)
      throws Exception {
    // FIXME: check options value
    PnumMeet meet = new PnumMeet();
    meet.setRule("dist");
    Double radius = 0.0D;
    if (options.getAdhocParam("radius") != null) {
      radius = Double.parseDouble(options.getAdhocParam("radius").toString());
    }
    radius = radius / 1000.0D;

    List<PbillRecord> result = new ArrayList<>();
    //@formatter:off
    String sql = "SELECT pr.owner_ct_lng, pr.owner_ct_lat, owner_ct_code, pr.alyz_day FROM pbill_records AS pr " +
                 "LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY pr.owner_ct_lng, pr.owner_ct_lat, pr.alyz_day";
    String orderBy = "ORDER BY pr.started_at";
    //@formatter:on
    options.addCriteria(new CriteriaTuple("pr.owner_num", numA));
    options.setGroupBy(groupBy);
    options.setOrderBy(orderBy);

    List<PbillRecord> pbillRecords = findAll(PbillRecord.class, options, sql);
    Date temp = null;
    int countCondition = 1;
    HashMap<Date, List<Set<String>>> closedCellTowers =
        new HashMap<>();

    for (PbillRecord pbillRecord : pbillRecords) {
      Date alyzDay = pbillRecord.getAlyzDay();
      if (lastAlyzDay != null &&
          (lastAlyzDay.equals(alyzDay) || lastAlyzDay.after(alyzDay))) {
        continue;
      }
      PnumMeet pm =
          pbillRecordsOnGeoDist(caseId, pbillRecord, radius, numA, numB, query);
      if (pm != null) {
        Set<String> codes = pm.getCellTowers();
        List<PbillRecord> pb = pm.getPbillRecords();
        if (closedCellTowers.containsKey(pbillRecord.getAlyzDay())) {
          closedCellTowers.get(pbillRecord.getAlyzDay()).add(codes);
        } else {
          List<Set<String>> items = new ArrayList<>();
          items.add(codes);
          closedCellTowers.put(pbillRecord.getAlyzDay(), items);
          result.addAll(pb);
        }
        if (temp == null) {
          temp = alyzDay;
        }
        if (!temp.equals(alyzDay)) {
          countCondition++;
          temp = alyzDay;
        }
      }
      if (countCondition == PAGE_SIZE) {
        break;
      }
    }

    meet.setClosedCellTowers(closedCellTowers);
    meet.setPbillRecords(result);
    return meet;
  }

  public List<PbillRecord> pbillRecordsOnSameCISameLAC(String query,
      String numA, String numB, List alyzDayPageSize) throws Exception {
    List pubServiceNumList = new ArrayList<>();
    List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
    if (pubServiceNums != null && pubServiceNums.size() > 0) {
      for (PubServiceNum psn : pubServiceNums) {
        pubServiceNumList.add(psn.getNum());
      }
    }
    Options options = UniversalQueryHelper.normalize(query, "", map());
    options.addCriteria(
        new CriteriaTuple("peer_num", Op.NOT_IN, pubServiceNumList));
    options.addCriteria(new CriteriaTuple("alyz_day", Op.IN, alyzDayPageSize));
    options.addCriteria(
        new CriteriaTuple("owner_num", Op.IN, Arrays.asList(numA, numB)));
    // TODO 获取高级搜索中的条件参数
    options.delCriteria("started_at");
    options.delCriteria("started_hour_class");
    options.delCriteria("started_time");
    options.delCriteria("started_time_l1_class");
    options.delCriteria("started_time_l2_class");
    options.delCriteria("time_class");
    List<Object> sqlAndBoundVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    String allPbillRecords = (String) sqlAndBoundVals.get(0);
    List pbillRecordsVals = (List) sqlAndBoundVals.get(1);
    List<PbillRecord> pbillRecords = PbillRecord
        .where(allPbillRecords, pbillRecordsVals.stream().toArray(Object[]::new))
        .orderBy("started_at ASC");
    return pbillRecords;
  }

  public PnumMeet pbillRecordsOnGeoDist(Long caseId, PbillRecord pbillRecord,
      Double radius, String numA, String numB, String query)
      throws Exception {
    PnumMeet pm = new PnumMeet();
    Date alyzDay = pbillRecord.getAlyzDay();
    Double lat = pbillRecord.getOwnerCtLat();
    Double lng = pbillRecord.getOwnerCtLng();
    if (lat == null || lng == null || radius == null) {
      return null;
    }
    List<Object> vals0 = new ArrayList<>();
    List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
    if (pubServiceNums != null && pubServiceNums.size() > 0) {
      for (PubServiceNum psn : pubServiceNums) {
        vals0.add(psn.getNum());
      }
    }
    // TODO 获取高级搜索中的条件参数
    Options options = UniversalQueryHelper.normalize(query, "",
        map("alyz_day", alyzDay.toString()));
    options.addCriteria(new CriteriaTuple("peer_num", Op.NOT_IN, vals0));
    List<Object> sqlAndBoundValsB =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    String sqlUniversqlB = (String) sqlAndBoundValsB.get(0);
    List valsB = (List) sqlAndBoundValsB.get(1);
    options.delCriteria("started_at");
    options.delCriteria("started_hour_class");
    options.delCriteria("started_time");
    options.delCriteria("started_time_l1_class");
    options.delCriteria("started_time_l2_class");
    options.delCriteria("time_class");
    List<Object> sqlAndBoundVals =
        UniversalQueryHelper.getSqlAndBoundVals(options, false);
    String sqlUniversal = (String) sqlAndBoundVals.get(0);
    List vals = (List) sqlAndBoundVals.get(1);

    SpatialContext geo = SpatialContext.GEO;
    Rectangle rect = geo.getDistCalc().calcBoxByDistFromPt(
        geo.makePoint(lng, lat), radius * DistanceUtils.KM_TO_DEG, geo, null);

    valsB.add(numB);
    valsB.add(rect.getMinY());
    valsB.add(rect.getMaxY());
    valsB.add(rect.getMinX());
    valsB.add(rect.getMaxX());
    vals.add(numA);
    vals.add(numB);

    //@formatter:off
    String sql = "SELECT DISTINCT owner_ct_code FROM pbill_records WHERE " +
                 sqlUniversqlB  + "AND owner_num = ? " +
                 "AND (owner_ct_lat BETWEEN ? AND ?) AND (owner_ct_lng BETWEEN ? AND ?) " +
                 "AND owner_lac > 0 AND owner_ci > 0 ORDER BY started_at " ;
    String allRecordSql = sqlUniversal + " AND (owner_num = ? OR owner_num = ?) " +
                          "AND owner_lac > 0 AND owner_ci > 0 ORDER BY started_at " ;
    //@formatter:on
    List<Map> pbillRecordsB =
        Base.findAll(sql, valsB.stream().toArray(Object[]::new));

    if (pbillRecordsB.size() == 0) {
      return null;
    }
    Set cellTowers = ListMap.valuesToSet(pbillRecordsB);
    cellTowers.add(pbillRecord.getOwnerCtCode());
    List<PbillRecord> pbillRecords =
        PbillRecord.where(allRecordSql, vals.stream().toArray(Object[]::new));

    pm.setCellTowers(cellTowers);
    pm.setPbillRecords(pbillRecords);
    
    return pm;

  }

}
