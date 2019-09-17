package app.models.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class Options implements Serializable{
  public static final String AND_JOINT = "AND";
  public static final String OR_JOINT = "OR";

	private Set<CriteriaTuple> criterias;
	private Map<String, Object> views;
  private Map<String, Object> adhocParams;
  private List<String> notNullFields;
  private List<String> nullFields;
  private String condJoint;
	
	public Options(){
    this.criterias = Sets.newConcurrentHashSet();
    this.condJoint = AND_JOINT;
	}

	public Set<CriteriaTuple> getCriterias() {
		return this.criterias;
	}

  public CriteriaTuple getCriteria(String field) {
    for (CriteriaTuple cTuple : this.criterias) {
      if (cTuple.getField().equals(field)) {
        return cTuple;
      }
    }
    return null;
  }

	public void addCriteria(CriteriaTuple criteria) {
		criterias.add(criteria);
	}

  public void delCriteria(String field) {
    if (field == null)
      return;

    for (CriteriaTuple cTuple : this.criterias) {
      if (cTuple.getField().equals(field)) {
        this.criterias.remove(cTuple);
      }
    }
  }

  public Object getAdhocParam(String name) {
    if (this.adhocParams != null) {
      return this.adhocParams.get(name);
    }
    return null;
	}

  public void setNullFields(List<String> nullFields) {
    this.nullFields = nullFields;
  }

  public List<String> getNullFields() {
    return this.nullFields;
  }

  public void setNotNullFields(List<String> fields) {
    this.notNullFields = fields;
  }

  public List<String> getNotNullFields() {
    return this.notNullFields;
  }

	public Map<String, Object> getViews() {
		return this.views;
	}

	public void setViews(Map<String, Object> views) {
		this.views = views;
	}
	
  public void setAdhocParams(Map<String, Object> adhocs) {
    this.adhocParams = adhocs;
  }

  public long getLimit() {
    if (this.views != null && this.views.get("limit") != null) {
      return Long.parseLong(this.views.get("limit").toString());
    } else {
      return 0L;
    }
  }
  
  public void setOrderBy(String orderBy) {
    if (this.views == null) {
      this.views = new HashMap<>();
    }
    this.views.put("order-by", orderBy);
  }
  
  public String getOrderBy() {
    if (this.views != null && this.views.get("order-by") != null) {
      return this.views.get("order-by").toString();
    } else {
      return null;
    }
  }

  public String takeOrderBy() {
    if (this.views != null && this.views.get("order-by") != null) {
      return this.views.remove("order-by").toString();
    } else {
      return null;
    }
  }

  public String takeAndNormalizeOrderBy(String tableAlias) {
    String orderBy = takeOrderBy();
    if (orderBy != null) {
      List<String> l = new ArrayList<>();
      Iterable<String> items = Splitter.on(',').trimResults().omitEmptyStrings().split(orderBy);
      for (String item : items) {
        l.add(tableAlias + "." + item);
      }
      return Joiner.on(",").join(l);
    }
    return null;
  }

  public void setGroupBy(String groupBy) {
    if (this.views == null) {
      this.views = new HashMap<>();
    }
    this.views.put("group-by", groupBy);
  }
  
  public String getGroupBy() {
    if (this.views != null && this.views.get("group-by") != null) {
      return this.views.get("group-by").toString();
    } else {
      return null;
    }
  }
  
  public String getCondJoint() {
    return condJoint;
  }

  public void setCondJoint(String condJoint) {
    this.condJoint = condJoint;
  }

  @Override
	public String toString() {
		return "Options [ceriterias=" + criterias + ", views=" + views + "]";
	}
	
}