package app.models.pb;

import org.javalite.activejdbc.Model;

public class OutlierNum extends Model {

  public static final Integer LESS_PEER_NUM_FLAW_TYPE = 1;
  public static final Integer CONTINUOUS_NO_CALL_FLAW_TYPE = 2;
  public static final Integer TOTAL_UNUSEED_DAYS_FLAW_TYPE = 3;
  public static final Integer AVERAGE_DAILY_CALLS_FLAW_TYPE = 4;
  
  static {
    validatePresenceOf("num").message("Please provide outlierNum num"); 
    validatePresenceOf("flaw_type").message("Please provide outlierNum flaw_type"); 
  }

  public String getNum() {
    return getString("num");
  }

}
