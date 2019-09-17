package app.models.search;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class CriteriaTuple implements Serializable { 
  public static final int ONCE_QUERY = 1;
  public static final int ALWAYS_QUERY = 2;

	private String field;
	private String op;
  private List<Object> values;
  private int lifeCycle;
	
	public CriteriaTuple() {
	}
	
  public CriteriaTuple(String field, Object value) {
    this.field  = field;
    this.op        = Op.EQ;
    this.values = Arrays.asList(new Object[] { value });
    this.lifeCycle = ALWAYS_QUERY;
  }

  public CriteriaTuple(String field, Object value, int lifeCycle) {
    this.field     = field;
    this.op        = Op.EQ;
    this.values    = Arrays.asList(new Object[] { value });
    this.lifeCycle = lifeCycle;
  }

  public CriteriaTuple(String field, String op, List<Object> values) {
    this.field     = field;
    this.op        = op;
    this.values    = values;
    this.lifeCycle = ALWAYS_QUERY;
  }

  public CriteriaTuple(String field, String op, List<Object> values, int lifeCycle) {
	  this.field = field;
	  this.op = op;
	  this.values = values;
    this.lifeCycle = lifeCycle;
	}

  public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

  public List<Object> getValues() {
		return this.values;
	}

	public void setValues(Object values) {
		if(values instanceof List) {
			this.values = (List)values;
    } else {
      this.values = Arrays.asList(new Object[] { values });
    }
	}

  public boolean isOnceQuery() {
    return (this.lifeCycle == ONCE_QUERY);
  }

	@Override
	public String toString() {
		return "CriteriaTuple [field=" + field + ", op=" + op + ", values=" + values + "]";
	}
	
}