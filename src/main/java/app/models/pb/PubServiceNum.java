package app.models.pb;

import org.javalite.activejdbc.Model;

public class PubServiceNum extends Model {
  public static final int BANK_NUMS_GROUP = 0;
  public static final int INSURANCE_NUMS_GROUP = 1;

  public String getNum() {
    return getString("num");
  }

}
