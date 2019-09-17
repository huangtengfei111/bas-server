package app.models;

import org.javalite.activejdbc.Model;

public class CitizenBook extends Model {

  public static final String GOV_CB = "g"; // 公务员
  public static final String BIZ_CB = "b"; // 企业(老板)
  public static final String SOE_CB = "j"; // 国企员工
  public static final String GEN_CB = "z"; // 其他人员

  static {
    validatePresenceOf("name").message("Please provide book name");
    validatePresenceOf("version").message("Please provide book version");
  }

  public void setCategory(String category) {
    set("category", category);
  }

  public String getCategory() {
    return getString("category");
  }

  public void setVersion(String version) {
    set("category", version);
  }

  public String getVersion() {
    return getString("version");
  }

}
