package app.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.javalite.activejdbc.Model;

import app.util.X500PrincipalHelper;

public class License extends Model {

//  static {
//    validateNumericalityOf("acct_limit").greaterThan(0).onlyInteger().message("acctLimit.invalid");
//    // validate expired at
//  }

  public String getHostId() {
    return getString("host_id");
  }

  public String getHolder() {
    return getString("holder");
  }
  
  public Map<String, String> getHolderDetails() {
    Map<String, String> detail = new HashMap<>();
    if (getHolder() != null) {
      X500Principal p = new X500Principal(getHolder());
      X500PrincipalHelper helper = new X500PrincipalHelper(p);
      detail.put("name", helper.getCN());
      detail.put("city", helper.getL());
      detail.put("state", helper.getST());
      detail.put("country", helper.getC());
    }
    return detail;
  }

  public String getStorePass() {
    return getString("store_pass");
  }
  
  public String getPublicAlias() {
    return getString("public_alias");
  }
  
  public String getSystemSN() {
    return getString("system_sn");
  }
  
  public String getBaseboardInfo() {
    return getString("baseboard_info");
  }
  
  public String getProcessorInfo() {
    return getString("processor_info");
  }
  
  public List<String> getMacAddress() {
    List<String> l = new ArrayList<>();
    for(String s : getString("mac_address").split(",")) {
      l.add(s);
    }
    return l;
  }
  
  public List<String> getIpAddress() {
    List<String> l = new ArrayList<>();
    for(String s : getString("ip_address").split(",")) {
      l.add(s);
    }
    return l;
  }
  
  public String getPlan() {
    return getString("plan");
  }
  
  public String getPrivateAlias() {
    return getString("private_alias");
  }
  
  public String getKeyPass() {
    return getString("key_pass");
  }
  
  public Date getExpiredAt() {
    return getTimestamp("expired_at");
  }
  
  public Date getIssuedAt() {
    return getTimestamp("issued_at");
  }

  public long getAcctLimit() {
    return getLong("acct_limit");
  }

  public String getIssuedBy() {
    Long userId = getLong("issued_by");
    User user = User.findById(userId);
    if (user != null) {
      return user.getName();
    }
    return null;
  }

  public String getPath() {
    return getString("path");
  }

  public void setSalt(String salt) {
    setString("salt", salt);
  }

  public void setKeyPass(String keypass) {
    setString("key_pass", keypass);
  }

  public void setExpiredAt(Date expiredAt) {
    setTimestamp("expired_at", expiredAt);
  }
  
  public void setIssuedAt(Date issuedAt) {
    setTimestamp("issued_at", issuedAt);
  }

  public void setIssuedBy(long issuedBy) {
    setLong("issued_by", issuedBy);
  }

  public void setAcctLimit(long acctLimit) {
    setLong("acct_limit", acctLimit);
  }

  public void setDeletedAt(Date date) {
    setTimestamp("deleted_at", date);
  }

  public void setPath(String path) {
    setString("path", path);
  }

  public void setPlan(String plan) {
    setString("plan", plan);   
  }
}
