package app.models.pb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import app.models.CaseAwareModel;
import app.models.LabelGroup;
import app.util.RegExpPattern;

public class PnumLabel extends CaseAwareModel {
  public static final String MAN_INPUT_SOURCE = "1";// 数据来源
  private String venShortNum;
  private String venNetwork;
  private String position;
  private String company;

  static {
    validateRegexpOf("num", RegExpPattern.CN_ANY_NUM_RULE)
        .message("valid.pnum");
    validatePresenceOf("label").message("required.pnumLabel");
  }
  
	public PnumLabel() {
    setSource(MAN_INPUT_SOURCE);
	}
	
	public PnumLabel(String caseId, String num, String shortNum, String label, String memo, String source) {
		manageTime(false);
		setCaseId(caseId);
		setNum(num);
		setShortNum(shortNum);
		setLabel(label);
		setMemo(memo);
		setSource(source);
		LocalDateTime dt = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String now = dt.format(formatter);
		set("created_at", now);
		set("updated_at", now);
	}

  public PnumLabel(Long caseId) {
    set("case_id", caseId);
    setSource(MAN_INPUT_SOURCE);
  }

	public void setCaseId(String caseId) {
		set("case_id", caseId);
	}
	
	public void setNum(String num) {
		set("num", num);
	}
	
	public String getNum() {
		return getString("num");
	}
	
	public void setShortNum(String shortNum) {
		set("short_num", shortNum);
	}
	
	public String getShortNum() {
		return getString("short_num");
	}
	
	public void setLabel(String label) {
		set("label", label);
	}
	
	public String getLabel() {
		return getString("label");
	}
	
	public void setMemo(String memo) {
		set("memo", memo);
	}
	
	public String getMemo() {
		return getString("memo");
	}
	
  public void setSource(String source) {
    set("source", source);
  }
  
  public int getSource() {
    return getInteger("source");
  }
  
  public String getCreatedAt() {
		return getString("created_at");
	}

	public String getUpdatedAt() {
		return getString("updated_at");
	}

  public List<LabelGroup> getLabelGroups() {
    return getAll(LabelGroup.class);
  }

  public List<String> getLabelGroupValues() {
    List<LabelGroup> lgs = getAll(LabelGroup.class);
    List<String> vals = new ArrayList<>();
    for (LabelGroup lg : lgs) {
      vals.add(lg.getName());
    }
    return vals;
  }

  public void setVenShortNum(String venShortNum) {
    this.venShortNum = venShortNum;
  }

  public String getVenShortNum() {
    return this.venShortNum;
  }

  public void setVenNetwork(String venNetwork) {
    this.venNetwork = venNetwork;
  }

  public String getVenNetwork() {
    return this.venNetwork;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getPosition() {
    return this.position;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getCompany() {
    return this.company;
  }

}
