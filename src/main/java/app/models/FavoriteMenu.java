package app.models;

import org.javalite.activejdbc.Model;

public class FavoriteMenu extends Model {
  
  static {
    validatePresenceOf("mkey").message("Please provide menu key");
  }

  public FavoriteMenu() {
  }


  public void setUserId(long userId) {
    set("user_id", userId);
  }

  public void setMkey(String mkey) {
    set("mkey", mkey);
  }

}
