package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;

import app.controllers.ReportController;
import app.exceptions.ErrorCodes;
import app.models.Setting;
import app.models.pb.CellTower;
import app.models.pb.PbillRecord;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.services.pb.CellTowerService;
import app.util.JsonHelper;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;
import app.util.collections.ListUtils;

public class CellTowersController extends ReportController {

  @Inject
  private CellTowerService cellTowerService;

  /**
   * 返回根据时间查询的详细信息
   * 
   * @throws Exception
   */
  @POST
  public void daily() throws Exception {
    String json = getRequestString();
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", param("case_id")));
    options.addCriteria(
        new CriteriaTuple("pr.owner_lac", Op.GT, Arrays.asList(0)));
    boolean groupByNum = (boolean) options.getAdhocParam("group_by_num");
    logDebug("groupByNum: " + groupByNum);
    //@formatter:off
    String dailySql = "SELECT pr.id, pr.owner_num, pr.owner_ct_code, pr.peer_num, " +
                              "pr.started_at, pr.duration, pr.comm_direction, " +
                              "pr.owner_ct_lat, pr.owner_ct_lng, pr.owner_ct_dist, owner_ct_town " +
                       "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id";
    String orderBy = "ORDER BY pr.started_at ASC";
    //@formatter:on
    List<Map> dailyPbillRecords = doStat(options, dailySql, "", orderBy, "");

    SetMultimap<String, String> ownerCtCodes = HashMultimap.create();
    Set<String> ownerCtCodeRepeat = new HashSet<>();
    for (Map dailyPbillRecord : dailyPbillRecords) {
      String ownerCtCode = dailyPbillRecord.get("owner_ct_code").toString();
      String ownerNum = dailyPbillRecord.get("owner_num").toString();
      ownerCtCodes.put(ownerCtCode, ownerNum);
    }
    for (String ownerCtCode : ownerCtCodes.keySet()) {
      if (ownerCtCodes.get(ownerCtCode).size() > 1) {
        ownerCtCodeRepeat.add(ownerCtCode);
      }
    }
    
    //@formatter:off
    String ctCodeCountSql = "SELECT pr.owner_ct_code, count(*) AS count " +
                            "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "GROUP BY owner_ct_code";
    //@formatter:on
    List<Map> ctCodeCountList = doStat(options, ctCodeCountSql, groupBy, "", "");
    Map ownerCtCodeCount = new HashMap<>();
    ctCodeCountList.forEach(map -> {
      ownerCtCodeCount.put(map.get("owner_ct_code"), map.get("count"));
    });
    if (groupByNum) {
      LinkedHashMap<Object, List<Map>> ownerCtList =
          ListMap.reduce("owner_num", dailyPbillRecords);
      view("ownerCtLHM", ownerCtList);
    } else {
      view("ownerCtLM", dailyPbillRecords);
    }
    
    setOkView("daily celltowers");
    view("groupByNum", groupByNum);
    view("ownerCtCodeRepeat", ownerCtCodeRepeat);
    view("count", ownerCtCodeCount);
    render();
  }

  /**
   * Request:
   * 
   * <pre>
   * {"coord": "2", "fmt": 16, "ct_codes": [.....]}
   * </pre>
   * 
   * @throws Exception
   */
  @POST
  public void transGeoLoc() throws Exception {
    String jsonStr = getRequestString();
    JSONObject json = JSONObject.parseObject(jsonStr);
    String targetCoord = json.getString("coord");
    String _fmt = json.getString("fmt");
    int fmt = (_fmt == null) ? 10 : Integer.parseInt(_fmt);
    String ctCodes = json.getString("ct_codes");
    JSONArray _ctCodes = JSONArray.parseArray(ctCodes);
    List reqCtCodes = ListUtils.jsonArrayToList(_ctCodes);

    Map<String, List> ct2 = cellTowerService.smartLookup(reqCtCodes);
    List<CellTower> hittedCTs = ct2.get("hitted");
    List<String> missedCTCodes = ct2.get("missed");
    Map<String, List> ret = new HashMap<>();
    
    for (CellTower ct : hittedCTs) {
      List<Object> transformed = new ArrayList<>();

      double lng = 0, lat = 0;
      if (ct.getXLat() != null) {
        lng = ct.getXLng().doubleValue();
        lat = ct.getXLat().doubleValue();
//        double[] c = CellTowerWitch.detoxifyCoord(lng, lat);
//        lng = c[0];
//        lat = c[1];
      } else if (ct.getLac() != null) {
        lng = ct.getLng().doubleValue();
        lat = ct.getLat().doubleValue();
      }
//      if (GeoLocQueryAdapter.BMAPS_COORD.equals(targetCoord)) {
//        double[] c = CoordinateTransformUtil.gcj02tobd09(lng, lat);
      double[] c = { lng, lat };
      transformed.add(c);
      transformed.add(ct);
      ret.put(ct.getCode(), transformed);
//      }
    }
    
    for(String code: missedCTCodes) {
      List<Object> empty = new ArrayList<>();
      ret.put(code, empty);
    }

    setOkView("transformed to " + targetCoord + " geo loc");
    view("ctCoords", ret);
    render();
  }

  /**
   * Input: {codes: ["lac:ci:mnc", ....], fmt: 16}
   * 
   * @throws Exception
   */
  @POST
  public void multiGeoLoc() throws Exception {
    Map payload = JsonHelper.toMap(getRequestString());
    if (payload != null) {
      List<String> codes = (List<String>) payload.get("codes");
      String appId = (String) payload.get("app_id");
      Object fmtVal = payload.get("fmt");
      int fmt = (fmtVal == null) ? 10 : Integer.parseInt(fmtVal.toString());

      Map<String, List> ct2 = cellTowerService.smartLookup(codes);
      List<CellTower> hittedCTs = ct2.get("hitted");
      List<String> missedCTCodes = ct2.get("missed");
      logInfo("missedCTCodes = " + missedCTCodes.get(0));
      // 记录保存在中心服务器
      if (Setting.isSuperNode()) {
        logInfo("Setting.SystemId()" + Setting.getSystemId());
        PreparedStatement ps = null;
        try {
          String sql =
              "INSERT INTO missed_ct_requests SET app_id = ?, code = ?, created_at = NOW()";
          ps = Base.startBatch(sql);
          Base.openTransaction();
          for (String code : missedCTCodes) {
            Base.addBatch(ps, appId, code);
          }
          Base.executeBatch(ps);
          Base.commitTransaction();
        } catch (Exception e) {
          Base.rollbackTransaction();
          logError(e.getMessage(), e);
        } finally {
          if (ps != null) {
            ps.close();
          }
        }
      }
      setOkView("multi geo loc query");
      view("cell_towers", hittedCTs);
      render("index");
    }
  }

  /**
   * 号码基站统计信息
   * 
   * @throws Exception
   */
  @POST
  public void topFreq() throws Exception {
    String json = getRequestString();
    //@formatter:off
    String sql = "SELECT pr.owner_ct_code AS ocd,COUNT(1) AS count FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = "AND pr.owner_ct_code != '' AND pr.owner_ct_code IS NOT NULL GROUP BY pr.owner_ct_code";
    String orderBy = "ORDER BY count DESC LIMIT 10";
    //@formatter:on
    List<Map> lm1 = doStat(json, sql, groupBy, orderBy, "pr", map("cp.case_id", param("case_id")));

    Object v1 = null;
    Object v2 = null;
    for (Map m : lm1) {
      v1 = m.remove("ocd");
      v2 = m.remove("count");
      m.put(v1, v2);
    }


    setOkView("top freq celltower");
    view("listMap", lm1);

    render("/reports/listMap");
  }

  /**
   * 话单轨迹热力图
   * 
   * @throws Exception
   */
  @POST
  public void topHots() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT pr.owner_ct_lat AS lat, pr.owner_ct_lng AS lng, COUNT(1) AS count FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String groupBy = " GROUP BY pr.owner_ct_code ";
    String orderBy = " ORDER BY count DESC ";
    //@formatter:on         

    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", param("case_id")));
    options.setNotNullFields(Arrays.asList(new String[] { "pr.owner_ct_code", "pr.owner_ct_lat", "pr.owner_ct_lng" }));
    options.setGroupBy(groupBy);
    options.setOrderBy(orderBy);
    List<Map> lm = doStat(options, sql);

    setOkView("top hots celltower");
    view("listMap", lm);
    render();
  }

  @GET
  public void malformed() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    Long caseId = Long.parseLong(param("case_id"));

    //@formatter:off
    String sql = "SELECT DISTINCT pr.owner_ct_code, pr.owner_comm_loc, pr.owner_ci "+
                 "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                 "WHERE cp.case_id = ?  AND pr.owner_ci > 0 AND pr.owner_lac = 0 ";
    String countSql = "count(DISTINCT pr.owner_ct_code)";
    //@formatter:on
    
//    List<MalformedCellTower> mcts =
//        ModelDelegate.findBySql(MalformedCellTower.class, sql, caseId);
//    List<PbillRecord> pbillRecords = PbillRecord.findBySQL(sql, caseId);
    Paginator<PbillRecord> p =
        new Paginator<>(PbillRecord.class, pageSize, true, sql, countSql, caseId).orderBy("owner_ci ASC");
    List<PbillRecord> pbillRecords = p.getPage(currentPage);
    
    setOkView("malformed2 list");
//    view("pbill_records", mcts, "case_id", caseId);
    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "pbill_records", pbillRecords, 
         "case_id", caseId);
    render("malformed2");
  }

  @POST
  public void fixMalformed() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    JSONObject jsonObject = JSONObject.parseObject(json);
    JSONArray jsonArray = jsonObject.getJSONArray("fixed");
    List<Map> lm1 = new ArrayList<Map>();

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject fixed = jsonArray.getJSONObject(i);
      String ctCode = String.valueOf(fixed.get("ct_code"));
      Object commLoc = fixed.get("comm_loc");
      if (ctCode != null) {
        List<Object> normalize = CellTower.normalize(ctCode, 16);
        if (normalize != null) {
          Map m = new HashMap();
          Long mnc = (Long) normalize.get(0);
          Long lac = (Long) normalize.get(1);
          Long ci = (Long) normalize.get(2);
          logDebug("mnc : " + mnc + ", ci : " + ci + ", lac : " + lac);
          if (commLoc == null) {
            // TODO: 补全sql有问题,两表关联后,无法同步更新lac和owner_ct_code
            //@formatter:off
            String updateOwnerSql = "UPDATE pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                                    "SET pr.owner_lac = ?, pr.owner_ct_code = concat(hex(?), ':', hex(owner_ci), ':', hex(owner_mnc)) " +
                                    "WHERE cp.case_id = ? AND pr.owner_ci = ? AND pr.owner_lac = 0 ";
            String updatePeerSql = "UPDATE pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                                   "SET pr.peer_lac = ?, pr.peer_ct_code = concat(hex(?), ':', hex(peer_ci), ':', hex(peer_mnc)) " +
                                   "WHERE cp.case_id = ? AND pr.peer_ci = ? AND pr.peer_lac = 0 ";
            //@formatter:on
            int ownerUpdated = 0;
            int peerUpdated = 0;
            try {
              Base.openTransaction();
              ownerUpdated = Base.exec(updateOwnerSql, lac, lac, caseId, ci);
              peerUpdated  = Base.exec(updatePeerSql, lac, lac, caseId, ci);
              Base.commitTransaction();
              int updated = ownerUpdated + peerUpdated;
              m.put("ct_code", ctCode);
              m.put("updated", updated);
              lm1.add(m);
            } catch (Exception e) {
              Base.rollbackTransaction();
              logError(e);
            }
          } else {
          //@formatter:off
            String updateOwnerSql = "UPDATE pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                                    "SET pr.owner_lac = ?, pr.owner_ct_code = concat(hex(?), ':', hex(owner_ci), ':', hex(owner_mnc)) " +
                                    "WHERE cp.case_id = ? AND pr.owner_ci = ? AND pr.owner_comm_loc = ? AND pr.owner_lac = 0 ";
            String updatePeerSql = "UPDATE pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                                   "SET pr.peer_lac = ?, pr.peer_ct_code = concat(hex(?), ':', hex(peer_ci), ':', hex(peer_mnc)) " +
                                   "WHERE cp.case_id = ? AND pr.peer_ci = ? AND pr.peer_comm_loc = ? AND pr.peer_lac = 0 ";
            //@formatter:on
            int ownerUpdated = 0;
            int peerUpdated = 0;
            try {
              Base.openTransaction();
              ownerUpdated = Base.exec(updateOwnerSql, lac, lac, caseId, ci, commLoc);
              peerUpdated  = Base.exec(updatePeerSql, lac, lac, caseId, ci, commLoc);
              Base.commitTransaction();
              int updated = ownerUpdated + peerUpdated;
              m.put("ct_code", ctCode);
              m.put("updated", updated);
              lm1.add(m);
            } catch (Exception e) {
              Base.rollbackTransaction();
              logError(e);
            }
          }
        }
      }
    }
    if (lm1.size() == 0) {
      setErrorView("update failed", ErrorCodes.CT_ERROR);
      render("/common/_blank");
    } else {
      setOkView("fixMalformed");
      view("listMap", lm1);
      render("/reports/listMap");
    }

  }

  @POST
  public void pbillRecords() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String pbillRecordSql = "SELECT pr.* " +
                            "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id";
    String orderBy = "ORDER BY started_at ASC";
    //@formatter:on 
    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
    options.setOrderBy(orderBy);
    List<PbillRecord> pbillRecords =
        findAll(PbillRecord.class, options, pbillRecordSql);

    setOkView("daily pbillRecords");
    view("pbill_records", pbillRecords);
    render("/pb/pbill_records/index");


  }

  @POST
  public void locCityCi() throws IOException {
    String getRequest = getRequestString();
    JSONObject json = JSONObject.parseObject(getRequest);
    String city = json.getString("city");
    Long ci = json.getLong("ci");
    Long mnc = json.getLong("mnc");

    CellTower cellTower =
        CellTower.findFirst("ci = ? AND mnc = ? AND city = ?", ci, mnc, city);

    setOkView("loc city ci");
    view("cell_tower", cellTower);
    render();

  }
}
