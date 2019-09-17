package app.models;


import java.sql.Timestamp;
import java.util.Date;

import org.javalite.activejdbc.Model;

/**
 *
 */
public class User extends Model {
  static {
    validatePresenceOf("name").message("Please provide user name");
  }
  public String getName() {
    return getString("name");
  }
  
  public void setName(String name) {
    setString("name", name);
  }
  
  public String getAvatar() {
    return getString("avatar");
  }
  
  public void setAvatar(String avatar) {
    setString("avatar", avatar);
  } 
  
  public Timestamp getLastLoginAt() {
    return getTimestamp("last_login_at");
  }
  
  public String getLastRemoteHost() {
    return getString("last_remote_host");
  }

  public void logFootprint(String remoteHost) {
    User.update("last_login_at = ?, last_remote_host = ?", "id = ?", new Date(), remoteHost, getLongId());
  }
}