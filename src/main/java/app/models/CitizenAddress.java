package app.models;

/**
 *
 */
public class CitizenAddress extends CitizenAwareModel {
  
  static {
    validatePresenceOf("loc").message("Please provide citizenAddress loc");  
  }
  
	public CitizenAddress(){
	}

  public void setLoc(String loc) {
    set("loc", loc);
  }

  public void setMemo(String memo) {
    set("memo", memo);
  }

  public void setProvince(String province) {
    set("province", province);
  }

  public void setCity(String city) {
    set("city", city);
  }

  public void setTown(String town) {
    set("town", town);
  }

  public void setAreaCode(String areaCode) {
    set("area_code", areaCode);
  }

	public String getLoc() {
		return getString("loc");
	}

	public String getMemo() {
		return getString("memo");
	}	

  public String getProvince() {
    return getString("province");
  }

  public String getCity() {
    return getString("city");
  }

  public String getTown() {
    return getString("town");
  }

  public String getAreaCode() {
    return getString("area_code");
  }

  public void setCitizenBookid(Long citizenBookId) {
    set("citizen_book_id", citizenBookId);
  }

  public Long getCitizenBookid() {
    return getLong("citizen_book_id");
  }
}