package app.models.pb;

import org.javalite.activejdbc.Model;

public class MissedCtRequest extends Model {
  
  public void setCode(String code) {
    set("code", code);
  }

  public void setAppId(String appId) {
    set("app_id", appId);
  }
}
