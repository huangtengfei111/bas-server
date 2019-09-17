package app.models;

import org.javalite.activejdbc.Model;

/**
 *
 */
public class Setting extends Model {
  //@formatter:off
  public static final String K_SYS_ID            = "system.id";
  public static final String K_SYS_VERSION       = "system.version";
  public static final String K_SYS_LOGIN_MODE    = "system.login.mode"; // 登录模式
  public static final String K_LICENSE_HOSTID    = "license.hostid";    // 服务器许可证
  public static final String K_PBCONVERTER_PW    = "pbconverter.pw";   // 转换器密码
  public static final String K_CBAS_HP           = "center.bas.hp";
     
  public static final String BMAPS_APP_KEY     = "bmaps.appkey";
  public static final String GMAPS_APP_KEY     = "gmaps.appkey";

  public static final String BAS_GEOLOC_KEY    = "bas.geoloc.key";
  public static final String BAS_GEOLOC_QLIMIT = "bas.geoloc.qlimit";

  public static final String GPSSPG_APP_ID     = "gpsspg.appid";
  public static final String GPSSPG_APP_KEY    = "gpsspg.appkey";

  public static final String V_BAS_HOST        = "bas.shulan.com"; 
  public static final String SUPER_NODE_ID     = "0";
  public static final String SYS_LOGIN_LICENSE = "desktop";          // license-only
  public static final String SYS_LOGIN_WEB     = "web";
  public static final String SYS_LOGIN_MIXED   = "mixed";
  // @formatter:on

  static {
    validatePresenceOf("k").message("Please provide setting key");
  }
  
  public static String getSystemId() {
    Setting s = Setting.findFirst("k = ?", K_SYS_ID);
    return s.getString("v");
  }

  public static boolean isSuperNode() {
    return SUPER_NODE_ID.equals(getSystemId());
  }

  public static String centerBasHostPort() {
    if (!isSuperNode()) {
      Setting s = Setting.findFirst("k = ?", K_CBAS_HP);
      if (s != null) {
        return s.getString("v");
      }
    }
    return null;
  }

  public String getValue(String key) {
    return getString(key);
  }
  
  public String getV() {
    return getString("v");
  }

  public static void setSystemId(Long systemId) {
    Setting setting = Setting.findFirst("k = ?", Setting.K_SYS_ID);
    if (!"0".equals(setting.getV())) {
      Setting.update("v = ?", "k = ?", systemId, Setting.K_SYS_ID);
    }
  }
}