package app.models;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;

import app.util.X500PrincipalHelper;

@BelongsTo(parent = License.class, foreignKeyName = "system_id")
public class ApiTrack extends Model {

  private final static Map<String, String> APIS = new HashMap<>();
  static {
    APIS.put("app.controllers.CasesController#summary:GET", "案件概览");
  }

  public ApiTrack() {
  }

  public ApiTrack(String systemId, String hostId) {
    setString("system_id", systemId);
    setString("host_id", hostId);
  }

  public void setSystemId(String systemId) {
    setString("system_id", systemId);
  }

  public void setController(String controller) {
    setString("controller", controller);
  }

  public void setHostId(String hostId) {
    setString("host_id", hostId);
  }

  public void setAction(String action) {
    setString("action", action);
  }

  public String getController() {
    return getString("controller");
  }

  public String getAction() {
    return getString("action");
  }

  public String getHolderName() {
    License license = this.parent(License.class);
    String holder = license.getString("holder");
    X500Principal principal = new X500Principal(holder);
    X500PrincipalHelper helper = new X500PrincipalHelper(principal);
    return helper.getCN();
  }

  public String getReadableApi() {
    String req = getController() + "#" + getAction();
    return APIS.get(req) == null ? req : APIS.get(req);
  }
}
