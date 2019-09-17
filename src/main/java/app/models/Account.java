package app.models;

import java.util.List;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.javalite.activejdbc.Model;

/**
 *
 */
public class Account extends Model {

  static {
//    validatePresenceOf("username", "password");
    validatePresenceOf("username").message("Please provide account username");
  }

  public Account() {
  }

  public void setEncryptedPassword(String password, Object salt) {
    String s = salt.toString();
    String hashedPasswordBase64 = new Sha256Hash(password, s, 1024).toBase64();
    set("password", hashedPasswordBase64);
    set("salt", s);
  }

  public void setUsername(String username) {
    set("username", username);
  }

  public String getUsername() {
    return getString("username");
  }

  public String getPassword() {
    return getString("password");
  }

  public String getSalt() {
    return getString("salt");
  }

  public List<Setting> getSettings() {
    return getAll(Setting.class);
  }

  public User getUser() {
    return this.parent(User.class);
  }

  public Role getRole() {
    return this.parent(Role.class);
  }

  public void setRoleId(Long longId) {
    set("role_id", longId);
  }

  public void setUserId(Long longId) {
    set("user_id", longId);
  }

  public boolean isAdmin() {
    return false;
  }

  public boolean isSuper() {
    return false;
  }

  public boolean isBuiltIn() {
    return getBoolean("built_in");
  }

  // 检测账号是否处于冻结状态
  public boolean isRevoked() {
    return null != getString("deleted_at");
  }

  public boolean getBuiltIn() {
    return getBoolean("built_in");
  }

  public Object getUserId() {
    return getInteger("user_id");
  }

}