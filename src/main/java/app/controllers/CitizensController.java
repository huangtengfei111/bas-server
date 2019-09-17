package app.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;

import app.exceptions.ErrorCodes;
import app.models.Citizen;
import app.models.CitizenBook;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.services.CitizenService;
import app.util.StatColHeader;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

@SuppressWarnings("unchecked")
public class CitizensController extends APIController {

  @Inject
  private CitizenService citizenService;

  public void index() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));

    Paginator p = new Paginator(Citizen.class, pageSize, "1 = 1").orderBy("id desc");

    List<Citizen> citizens = p.getPage(currentPage);
    setOkView();
    view("page_total", p.pageCount(), "page_current", currentPage, "citizens", citizens);

    render();
  }

  @POST
  public void upload() throws IOException {
    List<FormItem> items = multipartFormItems();

    FormItem fileItem = null;
    List<Map> importResults = new ArrayList<Map>();
    Map<String, Object> map = new HashMap<String, Object>();
    CitizenBook cbook = new CitizenBook();
    
    for (FormItem item : items) {
      if (item.isFile()) {
        fileItem = item;
        map.put("fileName", item.getFileName());
        cbook.set("filename", item.getFileName());
      } else {
        String field = item.getFieldName();
        String fValue = item.getStreamAsString();
        cbook.set(field, fValue);
      }
    }

    if (fileItem != null) {
      cbook.saveIt();
      int total = citizenService.upload(fileItem.getInputStream(), cbook);
      map.put("total", total);

      importResults.add(map);
      logDebug("imported: " + importResults);

      setOkView();

      view("results", importResults);
      render("upload");

    } else {
      setErrorView("bad input", ErrorCodes.INVALID_FILE);
      render("/common/error");
    }
  }

  @POST
  public void maninput() throws IOException {
    JSONObject citizenJson = JSONObject.parseObject(getRequestString());
    Long id = citizenService.maninput(citizenJson);

    if (id != -1) {
      setOkView("man input successed");
      view("id", id);
      render("/common/ok");
    } else {
      setErrorView("man input failed", ErrorCodes.INTERNAL_ERROR);
      render("/common/error");
    }
  }

  /**
   * ListMap.merge()
   * 
   * <pre>
   * data1:
   * 
   * book_name, citizens.id, ...
   * ---------------------------
   * 
   * data2:
   * citizens.id, cp.num, cp.memo, 
   * ---------------------------
   * 
   * data3:
   * citizens.id, ca.loc, ca.memo
   * ---------------------------
   * 
   * </pre>
   * 
   * @throws IOException
   */
  @POST
  public void search() throws Exception {
    // 获取前台搜索条件
    String json = values().get("reqString").toString();
    Options options = UniversalQueryHelper.normalize(json, "c", null);
    CriteriaTuple cTuple = options.getCriteria("c.name");
//    String q = cTuple.getValues().get(0).toString().trim();
//    int len = q.length();
//    if (len < 12) { // 短号则取出末4位相同的长号号码通讯录
//      if (len > 4) {
//        q = q.substring(len - 4);
//      }
//      options.addCriteria(new CriteriaTuple("cp.num", Op.END_WITH, Arrays.asList(new String[] { q })));
//    } else {
//      options.addCriteria(new CriteriaTuple("cp.num", Op.FUZZY, cTuple.getValues()));
//    }
    options.addCriteria(new CriteriaTuple("cp.num", Op.FUZZY, cTuple.getValues()));
    options.setCondJoint(Options.OR_JOINT);
    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(options, true);

    String query = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    Object[] params = vals.stream().toArray(Object[]::new);

    //@formatter:off
    String sql = "SELECT c.id AS cid, c.name AS cname, c.position, c.social_no, cb.id AS cb_id, cb.name AS book_name " +
                 "FROM citizens AS c LEFT JOIN citizen_books AS cb ON c.citizen_book_id = cb.id " +
                 "LEFT JOIN citizen_phones AS cp ON c.id = cp.citizen_id " + query;
    //@formatter:on

    List<Map> lm1 = Base.findAll(sql, params);

    //@formatter:off
    sql = "SELECT c.id AS cid, cp.num, cp.memo AS num_memo, ca.loc, ca.memo AS loc_memo " +
          "FROM citizens AS c LEFT JOIN citizen_phones AS cp ON c.id = cp.citizen_id " +
             "LEFT JOIN citizen_addresses AS ca ON c.id = ca.citizen_id " + query;
    //@formatter:on

    List<Map> lm2 = Base.findAll(sql, params);
    String[] memoFields = { "num", "loc" };

//    LinkedHashMap<Object, List<Map>> lhm1 = ListMap.reduceWithMergeFields("cid", lm2, mergePhoneFields,
//        (phone_memo) -> StatColHeader.phoneMemo(phone_memo.toString()), ListMap.MERGE_WITH_REMOVE_POLICY);
    LinkedHashMap<Object, Map> lhm1 =
        ListMap.reduceWithMergeMemoFields("cid", lm2, memoFields,
                                          (args) -> StatColHeader.citizenMemo((List) args),
                                          ListMap.MERGE_WITH_REMOVE_POLICY);

    LinkedHashMap<Object, Map> lhm2 = ListMap.merge("cid", lm1, lhm1, Map.class);

    setOkView("search cititzens");

    view("valueType", "map");
    view("linkedHashMap", lhm2);
    render("/reports/linkedHashMap");
  }

  @GET
  public void position() {
    Long citizenId = Long.parseLong(param("id"));

    Citizen citizen = Citizen.findById(citizenId);
    String position = null;
    if (citizen != null) {
      position = citizen.getPosition();
    }

    setOkView();
    view("position", position);
    render();
  }
}