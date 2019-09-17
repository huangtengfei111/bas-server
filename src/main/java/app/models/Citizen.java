package app.models;

import java.util.List;

import org.javalite.activejdbc.Model;

/**
 *
 */
public class Citizen extends Model {
	private List<CitizenPhone> _phones;
	private List<CitizenAddress> _addresses;

  static {
    validatePresenceOf("name").message("Please provide citizen name");
  }
	
	public Citizen(){
	}

	public Citizen(String socialNo, String name, String phone, String mobile, String company, String address) {
		manageTime(false);
		setSocialNo(socialNo);
		setName(name);
		setAddress(address);
		setPhone(phone);
		setMobile(mobile);
		setCompany(company);
	}
	
	public Citizen(String socialNo, String name, String address, String phone, String mobile, String company, String org, String position) {
		manageTime(false);
		setSocialNo(socialNo);
		setName(name);
		setAddress(address);
		setPhone(phone);
		setMobile(mobile);
		setCompany(company);
		setOrg(org);
		setPosition(position);
	}
	
	public void setSocialNo(String socialNo) {
		set("social_no", socialNo);
	}
	
	public String getSocialNo() {
		return getString("social_no");
	}
	
	public void setName(String name) {
		set("name", name);
	}
	
	public String getName() {
		return getString("name");
	}
	
	public void setAddress(String address) {
		set("address", address);
	}
	
	public String getAddress() {
		return getString("address");
	}
	
	public void setPhone(String phone) {
		set("phone", phone);
	}
	
	public String getPhone() {
		return getString("phone");
	}
	
	public void setMobile(String mobile) {
		set("mobile", mobile);
	}
	
	public String getMobile() {
		return getString("mobile");
	}
	
	public void setCompany(String company) {
		set("company", company);
	}
	
	public String getCompany() {
		return getString("company");
	}
	
	public void setOrg(String org) {
		set("org", org);
	}
	
	public String getOrg() {
		return getString("org");
	}
	
	public void setPosition(String position) {
		set("position", position);
	}
	
	public String getPosition() {
		return getString("position");
	}
	
	public List<CitizenPhone> getCitizenPhones(){
		return getAll(CitizenPhone.class);
	}

	public List<CitizenPhone> _getPhones() {
		return this._phones;
	}

	public void _setPhones(List<CitizenPhone> phones) {
		this._phones = phones;
	}

	public List<CitizenAddress> _getAddresses() {
		return this._addresses;
	}

	public void _setAddresses(List<CitizenAddress> addresses) {
		this._addresses = addresses;
	}

  public void setCategory(String category) {
    set("category", category);
	}

  public String getCategory() {
    return getString("category");
  }

  public void setVersion(String version) {
    set("version", version);
  }

  public String getVersion() {
    return getString("version");
  }

  public void setCitizenBookId(Long citizenBookId) {
    set("citizen_book_id", citizenBookId);
  }

  public Long getCitizenBookid() {
    return getLong("citizen_book_id");
  }
}