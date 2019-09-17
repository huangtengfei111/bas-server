package app.models;

import org.javalite.activejdbc.Model;

public class Role extends Model {
  public static final String SUPER = "super";
  public static final String ADMIN = "admin";
  public static final String USER = "user";

  static {
    validatePresenceOf("name").message("Please provide role name");
    validatePresenceOf("value").message("Please provide role value");
  }
  
  public String getValue() {
    return getString("value");
  }

  public boolean isSuper() {
    return SUPER.equals(getValue());
  }

  public boolean isAdmin() {
    return ADMIN.equals(getValue());
  }

  public boolean isUser() {
    return USER.equals(getValue());
  }
}
