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
import app.exceptions.InvalidFieldValueException;
import app.models.Citizen;
import app.models.CitizenPhone;
import app.models.LabelGroup;
import app.models.pb.PnumLabel;
import app.models.pb.VenNumber;
import app.util.ExcelFileHelper;
import app.util.JsonHelper;
import app.util.collections.ListUtils;

/**
 * 号码标注
 */
@RESTful
@SuppressWarnings("unchecked")
public class PnumLabelsController extends APIController {

	private String[] reserved = {"id","case_id"};
	
	public void index() {
		int currentPage = getCurrentPage(param("page"));
		int pageSize = getPageSize(param("pagesize"));

    Paginator p = new Paginator(PnumLabel.class, pageSize, "case_id  = ?", param("case_id"))
        .orderBy("color_order is null, color_order asc"); // null last

		List<PnumLabel> pnumLabels = p.getPage(currentPage);

		setOkView();
		view("page_total", p.pageCount(),
			   "page_current", currentPage, 
			   "pnum_labels", pnumLabels);
		
		render();
  }
	
  public void create()
      throws IOException, DBRecordExistsException, InvalidFieldValueException {
    long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Map payload = JsonHelper.toMap(json);
    JSONArray labelGroupNames =
        JSONObject.parseObject(json).getJSONArray(("label_group_names"));

    try {
      Base.openTransaction();
      PnumLabel pl = new PnumLabel(caseId);
      pl.fromMap(payload);
      if (pl.saveIt()) {
        if (labelGroupNames != null) {
          for (Object labelGroupName : labelGroupNames) {
            LabelGroup labelGroup =
                LabelGroup.findOrCreateIt("name", labelGroupName, "topic", LabelGroup.NUM_TOPIC, "case_id", caseId);
            pl.add(labelGroup);
          }
        }
      }
      Base.commitTransaction();
      
      setOkView("created");
      view("pnum_label", pl);
      render("_pnum_label");
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
    PnumLabel pl = PnumLabel.findById(getId());
    if (pl == null) {
      setErrorView("no such label", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
		} else {
      String json = getRequestString();
      Long caseId = Long.parseLong(param("case_id"));
      Map payload = JsonHelper.toMap(json);

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
        if (!ListUtils.listEqualsIgnoreOrder(lgArr, pl.getLabelGroupValues())) {
          labelGroupChanged = true;
        }

        pl.fromMap(payload);
        pl.set("id", getId());
        pl.set("case_id", caseId);

        Base.openTransaction();
        if (pl.saveIt()) {
          if (labelGroupChanged) {
            pl.deleteChildrenShallow(LabelGroup.class);

            if (labelGroupNames != null) {
              for (Object labelGroupName : labelGroupNames) {
                LabelGroup labelGroup =
                    LabelGroup.findOrCreateIt("name", labelGroupName, "topic", LabelGroup.NUM_TOPIC, "case_id", caseId);
                pl.add(labelGroup);
              }
            }
          }
        }
        Base.commitTransaction();
        
        setOkView("updated");
        view("pnum_label", pl);
        render("_pnum_label");
      } catch (DBException e) {
        Base.rollbackTransaction();
        logError(e);
//        if (e
//            .getCause() instanceof MySQLIntegrityConstraintViolationException) {
//          throw new DBRecordExistsException();
//        } else {
          setErrorView(e.getMessage(), ErrorCodes.INTERNAL_ERROR);
          render("/common/error");
//        }
      }
		}
	}
	
	public void destroy() {
    PnumLabel pl = PnumLabel.findById(getId());

    if (pl == null) {
      setErrorView("no such label", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    } else {
      try {
        Base.openTransaction();
        pl.deleteChildrenShallow(LabelGroup.class);
        pl.delete();
        Base.commitTransaction();
        
        setOkView("deleted");
        view("id", getId());
        render("/common/ok");
      } catch (Exception e) {
        Base.rollbackTransaction();
        logError(e);
        setErrorView("deleted_failed", 400);

        render("/common/error");
      }
    }
	}
	
  public void labelGroup() {
    List<LabelGroup> lg = LabelGroup.find("case_id = ? AND topic = ? ORDER BY id", param("case_id"), LabelGroup.NUM_TOPIC);

    setOkView("list label group");
    view("label_groups", lg);
    render("/label_groups/index");
  }

	@POST
	public void search() throws IOException{
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
		
		Paginator p = new Paginator(PnumLabel.class, pageSize, query, vals.stream().toArray(Object[]::new)).orderBy("color_order IS NULL, color_order ASC");
	
		List<PnumLabel> pnumLabels = p.getPage(currentPage);
		setOkView();
		view("page_total", p.pageCount(),
				 "page_current",currentPage,
				 "pnum_labels", pnumLabels);
		render("index");
	}
	
  @POST
	public void upload() throws Exception{
		String caseId = param("case_id");
		
		List<FormItem> items = multipartFormItems();
		ArrayList<PnumLabel> pnumLabels = new ArrayList<>();
		
		for (FormItem item : items) {
      if (item.isFile()) {
        InputStream is = item.getInputStream();
        Map<String, String> header = new HashMap<>();
        header.put("长号", "num");
        header.put("短号", "short_num");
        header.put("标注", "label");
        header.put("备注", "memo");
        
        List<PnumLabel> p = ExcelFileHelper.extract(header, is, PnumLabel.class, 
                                                    (model) -> model.setSource(PnumLabel.MAN_INPUT_SOURCE));
        pnumLabels.addAll(p);
      }
    }
		
		session("upload.pnum", pnumLabels);
		setOkView("uploaded");
		view("pnum_labels", pnumLabels);
		render("index");
	}
	
	@POST
	public void doImport() throws SQLException {
		PreparedStatement ps = null;
		String caseId = param("case_id");
		try{
			ArrayList<PnumLabel> pnumLabels = (ArrayList<PnumLabel>) session("upload.pnum");
		
			if (pnumLabels != null) {
        String sql =
            "INSERT IGNORE INTO pnum_labels SET case_id = ?, num = ?, short_num = ?, label = ?, memo = ?, source = ?, created_at = ?, updated_at = ?";
				ps = Base.startBatch(sql);
				
				Base.openTransaction();
				for (PnumLabel pnumLabel : pnumLabels) {
					
					Base.addBatch(ps, caseId,
													pnumLabel.getNum(),
													pnumLabel.getShortNum(),
													pnumLabel.getLabel(),
													pnumLabel.getMemo(),
													pnumLabel.getSource(),
													pnumLabel.getCreatedAt(),
													pnumLabel.getUpdatedAt());
				}
				Base.executeBatch(ps);
				Base.commitTransaction();
				
				session().remove("upload.pnum");
				setOkView("imported");
			} else {
				setErrorView("no data in session", 405);
			}
		} catch(Exception e){
			//e.printStackTrace();
			Base.rollbackTransaction();
		} finally{
			if (ps != null) {
				ps.close();
			}
			render("/common/_blank");
		}
	}
	
	@POST
	public void abortImport() {
		session().remove("upload.pnum");
		setOkView("aborted");
		render("/common/_blank");
	}

  /**
   * 根据号码显示对应号码的标注信息
   */
  @GET
  public void show() {
    Long caseId = Long.parseLong(param("case_id"));
    String num = param("num");

    PnumLabel pnumLabel =
        PnumLabel.findFirst("case_id = ? AND num = ?", caseId, num);

    if (pnumLabel == null) {
      setErrorView("no such pnum_label", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/_blank");
    } else {
      setOkView();
      view("pnum_label", pnumLabel);
      render("_pnum_label");
    }
  }

  /**
   * 根据长号，短号和虚拟网进行智能检索
   * 
   * @throws IOException
   */
  @POST
  public void smartLookup() throws IOException {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Map<String,String> payload = JsonHelper.toMap(json);

    String shortNum = payload.get("short_num");
    String ownerNum = payload.get("owner_num");
    String venNetWork = payload.get("ven_network");
    PnumLabel pnumLabel = null;
    VenNumber venNum = null;

    //@formatter:off
    String venNetWorkSql = " case_id = ? AND num = ? ";
    String venNuSql = " case_id = ? AND network = ? AND short_num = ?";
    String pnumSql = " case_id = ? AND num = ? ";
    //@formatter:on

    if (shortNum != null && ownerNum != null) { // 短号和通话的号码
      VenNumber vn = VenNumber.findFirst(venNetWorkSql, caseId, ownerNum);
      if (vn != null) {
        venNum = VenNumber.findFirst(venNuSql, caseId, vn.getNetwork(), shortNum);
        if (venNum != null) {
          pnumLabel = PnumLabel.findFirst(pnumSql, caseId, venNum.getNum());
        }
      }

      if (pnumLabel == null) {
        pnumLabel = new PnumLabel();
        pnumLabel.setVenShortNum(shortNum.toString());
      }
    } else if (shortNum != null && venNetWork != null) { // 短号和虚拟网名称
      venNum = VenNumber.findFirst(venNuSql, caseId, venNetWork, shortNum);
      if (venNum != null) {
        pnumLabel = PnumLabel.findFirst(pnumSql, caseId, venNum.getNum());
      }

      if (pnumLabel == null) {
        pnumLabel = new PnumLabel();
        pnumLabel.setVenShortNum(shortNum.toString());
      }
    } else if (ownerNum != null) { // 长号
      venNum = VenNumber.findFirst(venNetWorkSql, caseId, ownerNum);
      pnumLabel = PnumLabel.findFirst(pnumSql, caseId, ownerNum);
      if (pnumLabel == null) {
        pnumLabel = new PnumLabel();
        pnumLabel.setNum(ownerNum.toString());
      }
    }

    if (venNum != null) {
      pnumLabel.setNum(venNum.getNum());
      pnumLabel.setVenShortNum(venNum.getShortNum());
      pnumLabel.setVenNetwork(venNum.getNetwork());
    }
    // 有长号就根据长号优先来找
    String num = pnumLabel.getNum() == null ? pnumLabel.getShortNum() : pnumLabel.getNum();
    //@formatter:off
    String citizenSql = "SELECT c.company, c.position FROM citizens c LEFT JOIN citizen_phones cp ON c.id = cp.citizen_id " + 
                        "WHERE cp.num = ? ORDER BY c.version DESC";
    //@formatter:on
    List<Citizen> citizens = Citizen.findBySQL(citizenSql, num);
    if (citizens != null && citizens.size() > 0) {
      Citizen citizen = citizens.get(0);
      pnumLabel.setCompany(citizen.getCompany());
      pnumLabel.setPosition(citizen.getPosition());
    }

    setOkView("smart lookup");
    view("pnum_label", pnumLabel);
    render();
  }
}