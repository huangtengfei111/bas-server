package app.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javalite.activeweb.annotations.POST;

import app.exceptions.ErrorCodes;
import app.models.LabelGroup;
import app.models.pb.CtLabel;
import app.models.pb.PnumLabel;
import app.util.JsonHelper;

public class LabelGroupsController extends APIController {


  /**
   * 根据分类标签列表对应的号码
   * 
   * @throws IOException
   */
  @POST
  public void nums() throws IOException {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Map labelGroup = JsonHelper.toMap(json);

    LabelGroup labGroup =
        LabelGroup.findFirst("case_id = ? AND name = ? AND topic = ? ", caseId,
            labelGroup.get("label_group"), LabelGroup.NUM_TOPIC);
    if (labGroup != null) {
      List<PnumLabel> pnumLabels = labGroup.getAll(PnumLabel.class);
      List<String> nums = new ArrayList<String>();
      for (PnumLabel pnumLabel : pnumLabels) {
        String num = pnumLabel.getNum();
        nums.add(num);
      }

      setOkView("nums of specified label group");
      view("items", nums);
      render();
    } else {
      setErrorView("no such label group", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    }

  }

  /**
   * 根据分类标签列表对应的基站
   * 
   * @throws IOException
   */
  @POST
  public void cellTowers() throws IOException {
    Long caseId = Long.parseLong(param("case_id"));
    String json = getRequestString();
    Map labelGroup = JsonHelper.toMap(json);

    LabelGroup labGroup = LabelGroup.findFirst("case_id = ? AND name = ? AND topic = ?", caseId,
                                         labelGroup.get("label_group"), LabelGroup.CT_TOPIC);
    if (labGroup != null) {
      List<CtLabel> ctLabels = labGroup.getAll(CtLabel.class);
      List<String> codes = new ArrayList<String>();
      for (CtLabel ctLabel : ctLabels) {
        String ctCode = ctLabel.getCtCode();
        codes.add(ctCode);
      }

      setOkView("cell towers of specified label group");
      view("items", codes);
      render();
    } else {
      setErrorView("no such label group", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    }
  }
}
