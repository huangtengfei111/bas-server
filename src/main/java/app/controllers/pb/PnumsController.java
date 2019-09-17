package app.controllers.pb;

import static org.javalite.common.Collections.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.controllers.APIController;
import app.models.CallAttribution;
import app.models.pb.Pbill;
import app.models.search.Options;
import app.util.UniversalQueryHelper;

public class PnumsController extends APIController {

  /**
   * 1.短号时查找本案件中短号对应的可能存在的长号
   * 2.长号时返回对应的归属地
   * 
   * @throws IOException
   */
  @POST
  public void numInfo() throws IOException {
    Long caseId = Long.parseLong(param("case_id"));
    JSONObject jsonObject = JSON.parseObject(getRequestString());
    String num = jsonObject.getString("num");
    List<Pbill> pbills = new ArrayList<Pbill>();
    //@formatter:off
    String shortNumSql = "SELECT p.owner_num, p.owner_name " +
                         "FROM pbills AS p LEFT JOIN cases_pbills cp ON p.id = cp.pbill_id " +
                         "WHERE cp.case_id = ? AND owner_num LIKE ?";  
    //@formatter:on
    if (num != null) {
      int q = num.length();
      if (q > 5 && q < 11) {
        num = num.substring(q - 4);
        pbills = Pbill.findBySQL(shortNumSql, caseId, "%" + num);
        if (pbills != null) {
          for (Pbill pbill : pbills) {
            CallAttribution callAttribution = CallAttribution
                .findPhoneCA(pbill.getOwnerNum());
            if (callAttribution != null) {
              pbill.setCallAttribution(callAttribution.getCity());
            }
          }          
        }

      } else if (q > 10) {
        CallAttribution callAttribution = CallAttribution.findPhoneCA(num);
        if (callAttribution != null) {
          Pbill pbill = new Pbill();
          pbill.setCallAttribution(callAttribution.getCity());
          pbills.add(pbill);
        }
      }
    }
    setOkView("num for short num");
    view("pbills", pbills);
    render("/pb/pbills/index");
  }
  
  /**
   * 时间分割点统计
   * @throws Exception 
   * @throws IOException 
   */
  @POST
  public void commsOnBps() throws IOException, Exception {
    Long caseId = Long.parseLong(param("case_id"));
    Options options = UniversalQueryHelper.normalize(getRequestString(), "pr",
        map("cp.case_id", caseId.toString()));
    
    
    setOkView("commsOnBps");  
    render();   
  }
}
