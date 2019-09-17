package app.models;

/**
 *
 */
public class CitizenPhone extends CitizenAwareModel {
  
  static {
    validatePresenceOf("num").message("Please provide citizenPhone num");  
  }

  public CitizenPhone() {
  }

  public CitizenPhone(String num, String memo, String venName) {
    setMemo(memo);
    setNum(num);
    setVenName(venName);
  }

  public boolean isVenNum() {
    return (getVenName() != null) && (!"".equals(getVenName()));
  }

  public String getVenName() {
    return getString("ven_name");
  }

  public String getNum() {
		return getString("num");
	}

	public String getMemo() {
		return getString("memo");
	}
	
	public void setNum(String num) {
    set("num", num);
  }
	
  public void setMemo(String memo) {
    set("memo", memo);
  }

  public void setVenName(String venName) {
    set("ven_name", venName);
  }

  public void setCitizenBookid(Long citizenBookId) {
    set("citizen_book_id", citizenBookId);
  }

  public Long getCitizenBookid() {
    return getLong("citizen_book_id");
  }

}