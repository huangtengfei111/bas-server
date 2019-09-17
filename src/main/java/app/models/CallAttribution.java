package app.models;

import org.javalite.activejdbc.Model;

public class CallAttribution extends Model {
  
  public static CallAttribution findPhoneCA(String phoneNum) {
    CallAttribution  callAttribution = CallAttribution.findFirst("num = ?", phoneNum.substring(0,7));        
    return callAttribution;
  }
  
  public String getCity() {
    return getString("city");
  }  
}
