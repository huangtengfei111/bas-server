package app.util.ct;

import static org.javalite.common.Collections.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import app.models.pb.CellTower;
import app.util.ChineseAddressParser;
import app.util.HttpClient;
import app.util.JsonHelper;
// import javassist.NotFoundException;

public class JuheGeoLocQueryAdapter {

  public final static String GSM = "1";
  public final static String CDMA = "2";
  public static final String SRC = "juhe";


  /**
   * 聚合查询经纬度信息<br>
   * LAC CI MNC -> 对应CDMA的 NID BID SID
   * 
   * @param lac  ->nid
   * @param ci   ->cell
   * @param mnc  ->sid
   * @return 查询不到时候返回null;<br>
   *         查询成功返回Map,MapKey如下<br>
   *         联通和移动Key:MCC MNC LAC CELL LNG LAT O_LNG O_LAT PRECISION ADDRESS<br>
   *         电信Key: sid nid bid lat lon o_lat o_lon address raggio
   *
   */
  public static Map<String, String> getGeolocWithJuHe(String lac, String ci, String mnc) {

    String type = CDMA;
    try {
      int mncNum = Integer.parseInt(mnc);
      if (mncNum == 0 || mncNum == 1)
        type = GSM;
    } catch (Exception e) {
      throw new NumberFormatException("用户输入的mnc格式不正确");
    }

    String dianXinKey = getDYKey();
    String yiDongAndLianTongKey = getYLKey();

    Map<String, String> result = null;
    String info, url = null;
    switch (type) {
    case GSM:
      url = "http://v.juhe.cn/cell/get?mnc=" + mnc + "&cell=" + ci + "&lac=" + lac + "&hex=10&key="
          + yiDongAndLianTongKey;
       System.out.println("查询:"+url);
      info = HttpClient.doGet(url);
       System.out.println("结果:"+info);
      result = parseInfo(info, GSM);
      break;
    case CDMA:
      url = "http://v.juhe.cn/cdma/?sid=" + mnc + "&cellid=" + ci + "&nid=" + lac + "&hex=10&key=" + dianXinKey;
      info = HttpClient.doGet(url);
      result = parseInfo(info, CDMA);
      break;
    default:
      break;
    }
    
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> parseInfo(String info, String type) {
    Map<String, Object> parentsMap = JsonHelper.toMap(info);

    Map<String, String> result = new HashMap<>();

    if (!"200".equals(parentsMap.get("resultcode")))
      return map("code",parentsMap.get("resultcode"),"error_code",parentsMap.get("error_code"),"reason",parentsMap.get("reason"));

    switch (type) {
    case GSM:
      Map<String, Object> childMap = (Map<String, Object>) parentsMap.get("result");
      ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) childMap.get("data");
      result = list.get(0);
      result.put("type", GSM);
      break;
    case CDMA:
      result = (Map<String, String>) parentsMap.get("result");
      result.put("type", CDMA);
      break;
    default:
      break;
    }
    
    result.put("code",(String) parentsMap.get("resultcode"));
    return result;
  }

  /**
   * 根据mmc lac 和 ci 查聚合的基站
   * 
   * @param mnc
   * @param lac
   * @param ci
   * @return CellTower
   * @throws NotFoundException
   */

  public  CellTower doQuery(String mnc, Integer lac, Long ci) throws Exception {
    CellTower ct = new CellTower();
    ct.set("lac", mnc).set("ci", ci).set("mnc", mnc);
   
    Map<String, String> result = new CaseInsensitiveMap<String, String>(
        getGeolocWithJuHe(lac.toString(), ci.toString(), mnc));
    /*
    if(result.get("code")!="200")
      throw new NotFoundException(result.get("reason"));
    */      
    if (result != null) {
      if (result.get("type").equals(GSM)) {
        //GSM
        ct.set("lng", result.get("lng"))
          .set("lat", result.get("lat"))
          .set("glng", result.get("o_lng"))
          .set("glat", result.get("o_lat"))
          .set("radius", result.get("PRECISION"))
          .set("addr", result.get("address"));
      } else {
        // CDMA
        ct.set("lng", result.get("lon"))
          .set("lat", result.get("lat"))
          .set("glng", result.get("o_lon"))
          .set("glat", result.get("o_lat"))
          .set("radius", result.get("raggio"))
          .set("addr", result.get("address"));
      }
    }
    
    List<Map<String, String>> list = ChineseAddressParser.parseMultiLines(result.get("address"));

    if (list.size() > 0) {
      Map<String, String> addressMap = list.get(0);
      ct.set("province",addressMap.get("province"))
      .set("city",addressMap.get("city"))
      .set("district",addressMap.get("county"))
      .set("town",addressMap.get("town"));
    }
    
    return ct;
  }

  // 得到移动和联通的key
  private static String getYLKey() {
    return "5acaeb506f460f01e2a660c68e2ed8da";
  }

  // 得到电信key
  private static String getDYKey() {
    return "b8225651b770e61acdd8e737f8678072";
  }
}