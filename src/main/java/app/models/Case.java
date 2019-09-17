package app.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Model;

import app.models.pb.Pbill;

/**
 *
 */
public class Case extends Model {

  static {
    validatePresenceOf("name").message("Please provide case name");
    validatePresenceOf("num").message("Please provide case num");
  }

  private static final Map<String, Integer> STATUS_INT_MAP = new HashMap<String, Integer>() {
    {
        put("archived", 0);
        put("active",   1);
  		}};

  public Case(){
	}

  public void setId(long Id) {
		set("id", Id);
	}

  public void setCreatedBy(long userId) {
    set("created_by", userId);
	}
	
	public List<CaseEvent> getCaseEvents(){
		return getAll(CaseEvent.class);
	}

	public static int getStatusInt(String status) {
		return STATUS_INT_MAP.get(status);
	}
	
  public String getName() {
    return getString("name");
  }

  public String getNum() {
    return getString("num");
  }

  public String getPbCity() {
    return getString("pb_city");
  }

  public Long getCreateBy() {
    return getLong("created_by");
  }

	public List<CaseJob> getCaseJobs(){
    return getAll(CaseJob.class);
  }

  public List<Pbill> getPbills() {
    return getAll(Pbill.class);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getLongId() == null) ? 0 : getLongId().hashCode());
    result = prime * result + ((getNum() == null) ? 0 : getNum().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Case other = (Case) obj;
    if (getLongId() == null) {
      if (other.getLongId() != null)
        return false;
    } else if (!getLongId().equals(other.getLongId()))
      return false;
    if (getNum() == null) {
      if (other.getNum() != null)
        return false;
    } else if (!getNum().equals(other.getNum()))
      return false;
    return true;
  }

}