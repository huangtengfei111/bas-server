package app.controllers.pb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import app.controllers.APIController;
import app.exceptions.DBRecordExistsException;
import app.exceptions.ErrorCodes;
import app.models.LabelGroup;
import app.models.pb.CellTower;
import app.models.pb.CtLabel;
import app.util.ExcelFileHelper;
import app.util.JsonHelper;
import app.util.collections.ListUtils;

/**
 * 
 */
@RESTful
@SuppressWarnings("unchecked")
public class CtLabelsController extends APIController {
	
//	@Inject
//	private CtLabelService ctLabelService;
	
	public void index() {
		int currentPage = getCurrentPage(param("page"));
		int pageSize = getPageSize(param("pagesize"));

		Paginator p = new Paginator(CtLabel.class, pageSize, "case_id  = ?", param("case_id")).orderBy("id desc");
		List<CtLabel> ctLabels = p.getPage(currentPage);

		setOkView();
		view("page_total", p.pageCount(),
			   "page_current", currentPage,
			   "ct_labels", ctLabels);

		render();
	}
	
  public void create() throws IOException, DBRecordExistsException {
    String json = getRequestString();
    Map payload = JsonHelper.toMap(json);
    String ctCode = CellTower.normalizeCode(payload.get("ct_code").toString());

    JSONArray labelGroupNames =
        JSONObject.parseObject(json).getJSONArray(("label_group_names"));
    long caseId = Long.parseLong(param("case_id"));
    LabelGroup labelGroup = null;
    CtLabel ctLabel = null;

    try {
      Base.openTransaction();
      ctLabel = new CtLabel(caseId);
      ctLabel.fromMap(payload);
      ctLabel.setCtCode(ctCode);
      if (ctLabel.saveIt()) {
        if (labelGroupNames != null) {
          for (Object labelGroupName : labelGroupNames) {
            labelGroup = LabelGroup.findOrCreateIt("name", labelGroupName,
                "topic", LabelGroup.CT_TOPIC, "case_id", caseId);
            ctLabel.add(labelGroup);
          }
        }
      }
      Base.commitTransaction();

      setOkView("create");
      view("ct_label", ctLabel);
      render("_ct_label");
    } catch (DBException e) {
      Base.rollbackTransaction();
      logError(e);
//      if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
//        throw new DBRecordExistsException();
//      } else {
        setErrorView(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
        render("/common/error");
//      }
    }
	}
	
  public void update() throws IOException, DBRecordExistsException {
    CtLabel ctLabel = CtLabel.findById(getId());
    if (ctLabel == null) {
      setErrorView("no such label", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    } else {
      String json = getRequestString();
      long caseId = Long.parseLong(param("case_id"));
      Map payload = JsonHelper.toMap(json);
      String ctCode = CellTower.normalizeCode(payload.get("ct_code").toString());

      try {
        JSONArray labelGroupNames =
            JSONObject.parseObject(json).getJSONArray(("label_group_names"));
        boolean labelGroupChanged = false;
        List<String> lgArr = new ArrayList<>();

        if (labelGroupNames != null) {
          for (int i = 0; i < labelGroupNames.size(); i++) {
            lgArr.add(labelGroupNames.getString(i));
          }
        }
        if (!ListUtils.listEqualsIgnoreOrder(lgArr, ctLabel.getLabelGroupValues())) {
          labelGroupChanged = true;
        }

        ctLabel.fromMap(payload);
        ctLabel.setCtCode(ctCode);
        ctLabel.setId(getId());
        ctLabel.setCaseId(caseId);

        Base.openTransaction();
        if(ctLabel.saveIt()){
          if (labelGroupChanged) {
            ctLabel.deleteChildrenShallow(LabelGroup.class);

            if (labelGroupNames != null) {
              for (Object labelGroupName : labelGroupNames) {
                LabelGroup labelGroup =
                    LabelGroup.findOrCreateIt("name", labelGroupName,
                    "topic", LabelGroup.CT_TOPIC, "case_id", caseId);
                ctLabel.add(labelGroup);
              }
            }
          }
        }

        Base.commitTransaction();

        setOkView("update");
        view("ct_label", ctLabel);
        render("_ct_label");


      } catch (DBException e) {
        Base.rollbackTransaction();
        logError(e);
//        if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
//          throw new DBRecordExistsException();
//        } else {
          setErrorView(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
          render("/common/error");
//        }
      }

    }
	}
	
  public void destroy() {
    CtLabel cl = CtLabel.findById(getId());

    if (cl == null) {
      setErrorView("no such label", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    } else {
      try {
        Base.openTransaction();
        cl.deleteChildrenShallow(LabelGroup.class);
        cl.delete();
        Base.commitTransaction();
        
        setOkView("delete");
        view("id", getId());
        render("/common/ok");
      } catch (Exception e) {
        Base.rollbackTransaction();
        logError(e);
        setErrorView("deleted failed", 400);

        render("/common/error");
      }
    }
	}
	
	@POST
	public void upload() throws Exception {
		List<FormItem> items = multipartFormItems();
		ArrayList<CtLabel> ctLabels = new ArrayList<>();
		
		for (FormItem item : items) {
      if (item.isFile()) {
       InputStream is = item.getInputStream();
       Map<String, String> header = new HashMap<>();
       header.put("基站代码", "ct_code");
       header.put("标注", "label");
       header.put("备注", "memo");
       
       List<CtLabel> c = ExcelFileHelper.extract(header, is, CtLabel.class);
       ctLabels.addAll(c);
      }
    }
		
		session("upload.ct", ctLabels);
		setOkView("uploaded");
		view("ct_labels", ctLabels);
		render("index");
	}
	
	@POST
	public void doImport() throws SQLException {
		PreparedStatement ps = null;
    Long caseId = Long.parseLong(param("case_id"));
		try {
			ArrayList<CtLabel> ctLabels = (ArrayList<CtLabel>)session("upload.ct");
      logDebug("ctLabels :" + ctLabels);
			
			if (ctLabels != null) {
        String sql =
            "INSERT IGNORE INTO ct_labels SET case_id = ?, ct_code = ?, label = ?, memo = ?,created_at = ?,updated_at = ?";
				ps = Base.startBatch(sql);
				
				Base.openTransaction();
				for (CtLabel ctLabel : ctLabels) {
				  String ctCode = CellTower.normalizeCode(ctLabel.getCtCode().toString());
					Base.addBatch(ps, caseId,
													  ctCode,
													  ctLabel.getLabel(),
													  ctLabel.getMemo(),
													  ctLabel.getCreatedAt(),
													  ctLabel.getUpdatedAt());
				}
				Base.executeBatch(ps);
				Base.commitTransaction();
				
				session().remove("upload.ct");
				setOkView("doimport");
			} else {
				setErrorView("no data in session", 405);
			}
		} catch (Exception e) {
			Base.rollbackTransaction();
      logError(e);
      setErrorView("rollback transaction", 405);
		}finally {
			if (ps != null) {
				ps.close();
			}
			render("/common/_blank");
		}
	}
	
	@POST
	public void abortImport() {
		session().remove("upload.ct");
		setOkView("abortImport");
		render("/common/_blank");
	}
	
  /**
   * 基站标注(分类标签:列表使用)
   */
	@GET
	public void labelGroup() {
	  List<LabelGroup> lg = LabelGroup.find("case_id = ? AND topic = ? ORDER BY id", param("case_id"), LabelGroup.CT_TOPIC);

    setOkView("list label group");
    view("label_groups", lg);
    render("/label_groups/index");
	}
	
  /**
   * 基站标注:
   * 
   * @throws Exception
   */
  @POST
  public void createLabelsOnScope() throws Exception {
    String json = getRequestString();
    Long caseId = Long.parseLong(param("case_id"));
    Map payload = JsonHelper.toMap(json);
    String addr = (String) payload.get("address");
    Double radius = Double.parseDouble(payload.get("radius").toString());
    // String labelGroupName = String.valueOf(payload.get("label_group_name"));
    JSONArray labelGroupNames =
        JSONObject.parseObject(json).getJSONArray(("label_group_names"));

    LabelGroup labelGroup = null;
    List<CtLabel> ctLabels = new ArrayList<CtLabel>();
    CtLabel ctLabel = null;
    CellTower centerCellTower = CellTower.findFirst("addr = ?", addr);

    List<CellTower> cellTowers = null;
    if (centerCellTower != null) {
      Double lat = Double.parseDouble(centerCellTower.get("lat").toString());
      Double lng = Double.parseDouble(centerCellTower.get("lng").toString());

      cellTowers = CellTower.nearby(lat, lng, radius);
    }
    // labelGroup =
    // LabelGroup.findFirst("name = ? AND topic = ? AND case_id = ?",
    // labelGroupName, LabelGroup.CT_TOPIC, caseId);
    // if (labelGroup == null) {
    // labelGroup =
    // new LabelGroup(caseId, labelGroupName, LabelGroup.CT_TOPIC);
    // labelGroup.saveIt();
    // }
    if (cellTowers != null && cellTowers.size() > 0) {
      for (CellTower cellTower : cellTowers) {
        String ctCode = cellTower.getCode();
        // Long ci = cellTower.getCi();
        // Long mnc = cellTower.getMnc();
        // Long lac = cellTower.getLac();
        ctLabel = new CtLabel(caseId, ctCode, CtLabel.CENTER_FALSE);
        ctLabel.fromMap(payload);
        if (ctLabel.saveIt()) {
          if (labelGroupNames != null && labelGroupNames.size() > 0) {
            for (Object labelGroupName : labelGroupNames) {
              labelGroup =
                  LabelGroup.findFirst("name = ? AND topic = ? AND case_id = ?",
                      labelGroupName, LabelGroup.NUM_TOPIC, caseId);
              if (labelGroup == null) {
                labelGroup = new LabelGroup(caseId, labelGroupName.toString(),
                    LabelGroup.NUM_TOPIC);
              }
              ctLabel.add(labelGroup);
            }
          }
        } else {
          setErrorView("create labels-on-scope failed", ErrorCodes.CT_ERROR);
          render("/common/error");
          break;
        }
        ctLabels.add(ctLabel);
      }
      setOkView("create labels-on-scope");
      view("ct_labels", ctLabels);
      render("index");
    }else {
      setErrorView("create labels-on-scope failed", ErrorCodes.CT_ERROR);
      render("/common/error");
    }

  }

  /**
   * 根据基站编码显示对应基站编码的标注信息
   */
  @GET
  public void show() {
    Long caseId = Long.parseLong(param("case_id"));
    String ctCode = param("ct_code");
    
    CtLabel ctLabel =
        CtLabel.findFirst("case_id = ? AND ct_code = ?", caseId, ctCode);

    if (ctLabel == null) {
      setErrorView("no such ctLabel", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/_blank");
    } else {
      setOkView();
      view("ct_label", ctLabel);
      render("_ct_label");
    }

  }
  
  @POST
  public void search() throws IOException {
    String json = getRequestString();
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));
    
    List<String> conds = new ArrayList<String>();
    List vals = new ArrayList();
    //将前端的json数据转换成Map
    Map payload = JsonHelper.toMap(json);
    
    payload.put("case_id", param("case_id"));
    payload.forEach((key, value) -> {
      String keyStr = key.toString();
      
      if (value instanceof List ) {
        List v = (List)value;
        String op = v.get(0).toString();
        Object val = v.get(1);
        switch (op) {
          case "FUZZY":
            conds.add(keyStr + " LIKE ?");
            vals.add("%" + val.toString() + "%");
            break;
          default:
            conds.add(keyStr + " = ?");
            vals.add(val.toString());
            break;
        }
      } else if (value instanceof String) {
        conds.add(keyStr + " = ?");
        vals.add(value.toString());
      }      
    });
    String query = String.join(" AND ", conds);
    
    Paginator p = new Paginator(CtLabel.class, pageSize, query, vals.stream().toArray(Object[]::new)).orderBy("color_order IS NULL, color_order ASC");
  
    List<CtLabel> ctLabels = p.getPage(currentPage);
    setOkView();
    view("page_total", p.pageCount(),
         "page_current",currentPage,
         "ct_labels", ctLabels);
    render("index");
  }
}