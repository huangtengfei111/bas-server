package app.controllers.pb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.ModelDelegate;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import app.controllers.APIController;
import app.exceptions.ErrorCodes;
import app.jobs.AppEventListener;
import app.jobs.RefreshCacheListener;
import app.jobs.UpdateCaseOverviewListener;
import app.jobs.events.PbillDeletedEvent;
import app.models.Case;
import app.models.CasesPbills;
import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import app.models.pb.PnumLabel;
import app.models.pb.RelNumber;
import app.models.pb.VenNumber;
import app.services.pb.PbillService;
import app.util.StatColHeader;
import app.util.collections.ListMap;

/**
 */
@SuppressWarnings("unchecked")
public class PbillsController extends APIController {

  @Inject
  private PbillService pbillService;

  @GET
  public void index() {
    int page = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT p.* FROM pbills AS p LEFT JOIN cases_pbills cp ON p.id = cp.pbill_id "  + 
                     "LEFT JOIN pnum_labels as pl ON p.owner_num = pl.num AND pl.case_id = ? " +
                 "WHERE cp.case_id = ? " +
                 "ORDER BY pl.color_order IS NULL, pl.color_order ASC, p.owner_name IS NULL, p.owner_name, p.owner_num ASC"; 
    //@formatter:on 

    Paginator<Pbill> paginator = new Paginator<Pbill>(Pbill.class, pageSize, sql, caseId, caseId);
    List<Pbill> pbills = paginator.getPage(page);
    
    setOkView("list pbills");
    view("page_total", paginator.pageCount(), "page_current", page,
         "case_id", caseId,
         "pbills", pbills);
    render();
  }

  /**
   * 查询
   * @throws Exception
   */
  @POST
  public void search() throws Exception {

    Long caseId = Long.parseLong(param("case_id"));
    JSONObject jsonObject = JSON.parseObject(getRequestString());
    String query = jsonObject.getString("query");
    JSONArray jsonArray = jsonObject.getJSONArray("label_groups");
    List<Pbill> pbills = new ArrayList<Pbill>();
    Set<String> set1 = new HashSet<String>();
    Set<String> set2 = new HashSet<String>();
    Set<String> set3 = new HashSet<String>();
    List<String> labelGroups = new ArrayList<String>();
    String pbillConds = " 1=1 ";
    List<String> ph = new ArrayList<>();
    List<Object> values = new ArrayList<>();
    // 标注的模糊匹配
    //@formatter:off
    String sql1 = "SELECT num FROM pnum_labels WHERE case_id = ? AND label Like ? ";
    // 话单的模糊匹配
    String sql2 = "SELECT p.owner_num " + 
                  "FROM pbills AS p LEFT JOIN cases_pbills cp ON p.id = cp.pbill_id "  +
                  "WHERE cp.case_id = ? AND p.owner_num Like ? ";    
    //@formatter:on
    if (query != null && jsonArray == null) {
      List<Map> lm2 = Base.findAll(sql1, caseId, "%" + query + "%");
      set2 = ListMap.valuesToSet(lm2);
      List<Map> lm3 = Base.findAll(sql2, caseId, "%" + query + "%");
      set3 = ListMap.valuesToSet(lm3);
      set1 = Sets.union(set2, set3);
    } else if (query == null && jsonArray != null) {
      labelGroups = jsonArray.toJavaList(String.class);
      values.add(caseId);
      values.add(caseId);
      for (String labelGroup : labelGroups) {
        ph.add("?");
        values.add(labelGroup);
      }
      pbillConds = " lg.name IN (" + String.join(",", ph) + ")";
      // 分类标签条件查询
      //@formatter:off
      String sql3 = "SELECT pl.num " + 
                    "FROM pnum_labels as pl LEFT JOIN pnum_labels_label_groups as pllg ON pllg.pnum_label_id = pl.id " + 
                    "LEFT JOIN label_groups as lg ON lg.id = pllg.label_group_id " + 
                    "WHERE pl.case_id = ? AND lg.case_id = ? AND " + pbillConds;
      //@formatter:on
      List<Map> lm1 = Base.findAll(sql3,
          values.stream().toArray(Object[]::new));
      set1 = ListMap.valuesToSet(lm1);
    } else {
      List<Map> lm2 = Base.findAll(sql1, caseId, "%" + query + "%");
      set2 = ListMap.valuesToSet(lm2);
      List<Map> lm3 = Base.findAll(sql2, caseId, "%" + query + "%");
      set3 = ListMap.valuesToSet(lm3);
      set2 = Sets.union(set2, set3);

      labelGroups = jsonArray.toJavaList(String.class);
      values.add(caseId);
      values.add(caseId);
      for (String labelGroup : labelGroups) {
        ph.add("?");
        values.add(labelGroup);
      }
      pbillConds = " lg.name IN (" + String.join(",", ph) + ")";
      //@formatter:off
      String sql3 = "SELECT pl.num " + 
                    "FROM pnum_labels as pl LEFT JOIN pnum_labels_label_groups as pllg ON pllg.pnum_label_id = pl.id " + 
                    "LEFT JOIN label_groups as lg ON lg.id = pllg.label_group_id " + 
                    "WHERE pl.case_id = ? AND lg.case_id = ? AND " + pbillConds;
      //@formatter:on
      List<Map> lm1 = Base.findAll(sql3,
          values.stream().toArray(Object[]::new));
      set1 = ListMap.valuesToSet(lm1);
      set1 = Sets.intersection(set1, set2);
    }

    // 查询pbill表
    List<Object> valus = new ArrayList<Object>();
    List<String> pb = new ArrayList<String>();
    valus.add(caseId);
    valus.add(caseId);
    if (set1.size() > 0) {
      for (String num : set1) {
        pb.add("?");
        valus.add(num);
      }
      pbillConds = " p.owner_num IN (" + String.join(",", pb) + ")";
      //@formatter:off     
      String sql = "SELECT p.* FROM pbills AS p LEFT JOIN cases_pbills cp ON p.id = cp.pbill_id "  + 
                   "LEFT JOIN pnum_labels as pl ON p.owner_num = pl.num AND pl.case_id = ? " +
                   "WHERE cp.case_id = ? AND " + pbillConds +
                   "ORDER BY pl.color_order IS NULL, pl.color_order ASC, p.owner_name IS NULL, p.owner_name, p.owner_num ASC"; 
      //@formatter:on
      pbills = Pbill.findBySQL(sql, valus.stream().toArray(Object[]::new));
    }

    setOkView("search pbills");
    view("pbills", pbills);
    view("case_id", caseId);
    render("index");
  }

  @POST
  public void setVen() throws IOException {
    int caseId = Integer.parseInt(param("case_id"));
    JSONObject jsonVen = JSON.parseObject(getRequestString());
    List<VenNumber> venNums = JSON.parseArray(jsonVen.get("values").toString(), VenNumber.class);
    List<VenNumber> venNumbers = new ArrayList<VenNumber>();
    for (VenNumber venNumber : venNums) {
      VenNumber vn = VenNumber.findFirst("num = ? and case_id = ?", venNumber.getNum(), caseId);
      if (vn == null) {
        vn = new VenNumber();       
        vn.setCaseId(caseId);
      }
      vn.set("num", venNumber.getNum(), "network", venNumber.getNetwork(), "short_num", venNumber.getShortNum());
      
      vn.saveIt();
      venNumbers.add(vn);
    }
    
    setOkView("setVen");
    view("ven_numbers", venNumbers);
    render("../ven_numbers/index");
  }

  @POST
  public void setRelNetwork() throws IOException {
    Long caseId = Long.parseLong(param("case_id"));
    JSONObject jsonRelNetWork = JSON.parseObject(getRequestString());
    List<RelNumber> relNums = JSON.parseArray(jsonRelNetWork.get("values").toString(), RelNumber.class);
    List<RelNumber> relNumbers = new ArrayList<RelNumber>();
    for (RelNumber relNumber : relNums) {
      RelNumber rn = RelNumber.findFirst("num = ? and case_id = ?", relNumber.getNum(), caseId);
      if (rn == null) {
        rn = new RelNumber();
        rn.setCaseId(caseId);
        rn.setSource(RelNumber.MAN_INPUT_SOURCE);
      }
      rn.set("num", relNumber.getNum(), "network", relNumber.getNetwork(), "short_num", relNumber.getShortNum());
      
      rn.saveIt();
      relNumbers.add(rn);
    }
    
    setOkView("setRelNetwork");
    view("rel_numbers", relNumbers);
    render("../rel_numbers/index");    
  }
  
  @POST
  public void setResidence() throws IOException {
    JSONObject jsonRelNetWork = JSON.parseObject(getRequestString());
    List<Pbill> pbills = JSON.parseArray(jsonRelNetWork.get("values").toString(), Pbill.class);
    for (Pbill pbill : pbills) {
      Pbill.update("residence = ?", "id = ?", pbill.getResidence(), pbill.getId());
    }
    
    setOkView("setResidence");
    view("pbills", pbills);
    render("index");
  }
  
  /*
   * 删除对应号码话单
   */
  @POST
  public void destroy() throws Exception {
    // 获取前台数据#170
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));

    JSONArray ownerNums =
        JSONObject.parseObject(json).getJSONArray("owner_num");

    //@formatter:off
    String sql = "SELECT p.id " +
                 "FROM pbills p LEFT JOIN cases_pbills cp ON p.id = cp.pbill_id " +
                 "WHERE owner_num = ? AND cp.case_id != ?";
    //@formatter:on

    try {
      Map<String, Pbill> m = new HashMap<String, Pbill>();
      Base.openTransaction();

      for (Object ownerNum : ownerNums) {
        List<Pbill> pbills = Pbill.findBySQL(sql, ownerNum, caseId);
        Pbill p = Pbill.findFirst("owner_num = ?", ownerNum);
        if (pbills.size() == 0) {
          PbillRecord.delete("owner_num = ?", ownerNum);
        }
        int deleteCasePbill = CasesPbills.delete("pbill_id = ? AND case_id = ?",
            p.getId(), caseId);
        if (deleteCasePbill > 0) {
          PbillDeletedEvent pbillDeletedEvent =
              new PbillDeletedEvent(caseId, p);
          List<AppEventListener> listeners = new ArrayList<>();
          listeners.add(new RefreshCacheListener());
          listeners.add(new UpdateCaseOverviewListener());

          registerAndPost(pbillDeletedEvent, listeners);
        }
      }

      Base.commitTransaction();

      setOkView("destroy pbills");
      render("/common/_blank");
    } catch (Exception e) {
      Base.rollbackTransaction();
      logError(e);
      setErrorView("destroy pbills failed", ErrorCodes.INTERNAL_ERROR);
      render("/common/error");
    }
  }

  /**
   *
   */
  @GET
  public void ownerNums() {
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT distinct p.owner_num, p.owner_name " + 
                 "FROM pbills as p LEFT JOIN cases_pbills as cp ON p.id = cp.pbill_id " + 
                      "LEFT JOIN pnum_labels as pl ON p.owner_num = pl.num AND pl.case_id = ? " + 
                 "WHERE cp.case_id = ? " + 
                 "ORDER BY pl.color_order IS NULL, pl.color_order ASC, p.owner_name IS NULL, p.owner_name, p.owner_num ASC"; // null last
    //@formatter:on
    List<Map> result = Base.findAll(sql, caseId, caseId);

    setOkView("list owner nums");
    view("listMap", result);
    render("/reports/listMap");
  }

  /**
   * 根据号码或者号码标注给出本方号码建议
   * @throws IOException 
   */
  @POST
  public void suggestOwnerNums() throws IOException {
    JSONObject jsonInput = JSON.parseObject(getRequestString());
    String input = jsonInput.getString("input");
    List<String> ownerNums = new ArrayList<String>();
    
    String sql = "SELECT owner_num FROM pbills WHERE owner_num LIKE ?";
    List<Pbill> pbills = Pbill.findBySQL(sql, "%" + input + "%");
    for (Pbill pbill : pbills) {
      ownerNums.add(pbill.getOwnerNum());
    }
    
    sql = "SELECT num FROM pnum_labels WHERE label LIKE ?";
    List<PnumLabel> pnumLabels = PnumLabel.findBySQL(sql, "%" + input + "%");
    for (PnumLabel pnumLabel : pnumLabels) {
      ownerNums.add(pnumLabel.getNum());
    }
    
    setOkView();
    view("ownerNums", ownerNums);
    render();
  }

  /**
   * 话单导入
   */
  @POST
  public void upload() {
    Long caseId = Long.parseLong(param("case_id"));

    Iterator<FormItem> items = uploadedFiles();
    String pbillImportId = null;
    List<Map> importResults = new ArrayList<>();

    while(items.hasNext()) {
      FormItem item = items.next();
      if (item.isFile()) { // handle file
        logDebug("Upload file: " + item.getFileName() + ", import_id = " + pbillImportId);

        if (pbillImportId != null) {
          List<Map> l = pbillService.doImport(pbillImportId, caseId, item.getFileName(), item.getInputStream());
          importResults.addAll(l);
        }
      } else { // handle other field
        String fName = item.getFieldName();
        String fValue = item.getStreamAsString();
        logDebug("Field: " + fName + ", value = " + fValue);

        if("import_id".equals(fName)) {
          pbillImportId = fValue;
        }
      }
    }

    boolean importFailed = false;
    if (importResults.size() == 1) {
      Map m = importResults.get(0);
      if (m.get("error") != null) {
        importFailed = true;
      }
    }
    if (importFailed) {
      setErrorView("Import pbill", 400);
    } else {
      setOkView("pbill records uploaded");
    }
    view("importResults", importResults);
    render();
  }

  @GET
  public void days() {
    String sql = "SELECT pb_started_at, pb_ended_at " + 
                 "FROM cases WHERE id = ?";
    Long caseId = Long.parseLong(param("case_id"));
    List<Map> list = Base.findAll(sql, caseId);
    setOkView();
    view("results", list);
    render();
  }
  
  @GET
  public void alyzDays() {
    String sql = "SELECT pb_alyz_day_start, pb_alyz_day_end "+
                 "FROM cases WHERE id = ?";
    List<Map> list = Base.findAll(sql, Integer.parseInt(param("case_id")));
    
    setOkView();
    view("results", list);
    render("days");
  }
  
  /**
   * 案件概览中的地区分布
   */
  @GET
  public void geoDistOverview() {
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT COUNT(1) AS count, pr.peer_comm_loc " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON cp.pbill_id = pr.pbill_id " + 
                 "WHERE cp.case_id = ? " + //AND pr.peer_comm_loc NOT IN ('', '0') " + 
                 "GROUP BY pr.peer_comm_loc ORDER BY count DESC";
    //@formatter:on 
    List<Map> result = Base.findAll(sql, caseId);
    LinkedHashMap<Object, Object> countMap = new LinkedHashMap<Object, Object>();
    for (Map map : result) {
      Object key = map.get("peer_comm_loc");
      Object value = map.get("count");
      countMap.put(key, value);
    }

    setOkView("geo dist");
    view("countMap", countMap);
    render();
  }

  public void geoDist() {
    String num = param("num");
    long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
       String sql = "SELECT COUNT(1) AS count, pr.peer_comm_loc AS peer_comm_loc, pr.owner_num AS owner_num " +
                     "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON cp.pbill_id = pr.pbill_id " + 
                    "WHERE cp.case_id = ? AND pr.owner_num = ? GROUP BY pr.peer_comm_loc";
    //@formatter:on 
    List<Map> listMap = Base.findAll(sql, caseId, num);
    
    setOkView();
    view("listMap", listMap);
    render("/reports/listMap");
  }
  
  @GET
  public void dailyCount() {
    String num = param("num");
    long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT COUNT(1) as count, pr.started_day, pr.owner_num AS owner_num " +
                 "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                 "WHERE cp.case_id = ? AND pr.owner_num = ? " + 
                 "GROUP BY pr.started_day ORDER BY pr.started_day ASC";
    //@formatter:on 
    List<Map> list = Base.findAll(sql, caseId, num);

    setOkView();
    view("listMap", list);
    render("/reports/listMap");
  }

  /**
   * 每个时间段的通话次数
   */
  @GET
  public void hourlyCount() {
    String ownerNum = param("num");
    long caseId = Long.parseLong(param("case_id"));

    //@formatter:off
    String sql = "SELECT pr.started_hour_class, COUNT(1) AS count FROM pbill_records pr " +
                 "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                 "WHERE cp.case_id = ? AND pr.owner_num = ? AND pr.owner_ct_code != '' " +
                 "AND pr.owner_ct_code IS NOT NULL GROUP BY pr.started_hour_class;";
    //@formatter:on

    List<Map> lm1 = Base.findAll(sql, caseId, ownerNum);

    for (Map m : lm1) {
      Object v1 = m.get("started_hour_class");
      String v2 = StatColHeader.startedHour(v1.toString());
      Object v3 = m.get("count");
      m.put(v2, v3);
    }

    setOkView("hourly count");
    view("listMap", lm1);
    render("/reports/listMap");

  }

  /**
   * 话单列表`仅显示异常话单`
   */
  @GET
  public void onlyOutliers() {
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT p.* " +
                 "FROM pbills AS p LEFT JOIN cases_pbills cp ON p.id = cp.pbill_id " +
                 "LEFT JOIN pnum_labels AS pl ON p.owner_num = pl.num AND pl.case_id = ? " +
                 "WHERE cp.case_id = ? AND p.owner_num IN ( " +
                     "SELECT DISTINCT otn.num FROM outlier_nums AS otn " +
                     "LEFT JOIN cases_pbills cp ON otn.pbill_id = cp.pbill_id " +
                     "WHERE cp.case_id = ? " + ") " +
                 "ORDER BY pl.color_order IS NULL, pl.color_order ASC, p.owner_name IS NULL, p.owner_name ASC, p.owner_num";
    //@formatter:on

    List<Pbill> pbills =
        ModelDelegate.findBySql(Pbill.class, sql, caseId, caseId, caseId);
    setOkView("list pbills with outliers");
    view("case_id", caseId, "pbills", pbills);
    render("index");

  }


  /**
   * 他案导入的搜索
   * 
   * @throws IOException
   */
  @POST
  public void searchWithCases() throws IOException {
    Long caseId = Long.parseLong(param("case_id"));
    JSONObject jsonObject = JSON.parseObject(getRequestString());
    String query = jsonObject.getString("query");
    Map<Case, List<Pbill>> map = new HashMap<Case, List<Pbill>>();
    if (query != null) {
      // 模糊匹配标注
//      String sql = "SELECT num FROM pnum_labels WHERE label LIKE ? ";
//      List<Map> lm1 = Base.findAll(sql, "%" + query + "%");
      // 模糊匹配号码
      String sql = "SELECT owner_num FROM pbills WHERE owner_num LIKE ?";
      List<Map> lm2 = Base.findAll(sql, "%" + query + "%");
//      Set<String> set1 = ListMap.valuesToSet(lm1);
      Set<String> set2 = ListMap.valuesToSet(lm2);
//      set1 = Sets.union(set1, set2);

      List<Object> valus = new ArrayList<>();
      List<String> ph = new ArrayList<>();
      if (set2 != null && set2.size() > 0) {
        for (String num : set2) {
          ph.add("?");
          valus.add(num);
        }
        String pbillConds = "owner_num IN (" + String.join(",", ph) + ")";
        List<Pbill> pbills = Pbill.where(pbillConds, valus.stream().toArray(Object[]::new));

        // 返回案件及该案件对应的pbill
        for (Pbill pbill : pbills) {
          for (Case c : pbill.getCase(caseId)) {
            List<Pbill> pbList = map.get(c);
            if (pbList == null) {
              pbList = new ArrayList<>();
              pbList.add(pbill);
              List<Pbill> put = map.put(c, pbList);
            } else {
              pbList.add(pbill);
            }
          }
        }
      }
    }
    setOkView();
    view("map", map);
    render();
  }
  
}

