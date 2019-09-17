package app.models.pb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import app.models.CaseAwareModel;
import app.models.LabelGroup;
import app.util.RegExpPattern;

public class CtLabel extends CaseAwareModel{

  public static final int CENTER_TRUE = 1;
  public static final int CENTER_FALSE = 0;
  
  static {
    validateRegexpOf("ct_code", RegExpPattern.CN_CT_CODE_RULE)
        .message("valid.ctCode");
    validatePresenceOf("label").message("required.ctLabel");
  }

	public CtLabel () {
		
	}
	
  public CtLabel(Long caseId) {
    set("case_id", caseId);
  }

  public CtLabel(Long caseId, String ctCode, Long lac, Long ci,
      String label, String memo) {
		manageTime(false);
		setCaseId(caseId);
		setCtCode(ctCode);
		setLac(lac);
		setCi(ci);
		setLabel(label);
		setMemo(memo);
		LocalDateTime dt = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String now = dt.format(formatter);
		set("created_at", now);
		set("updated_at", now);
	}
	
  public CtLabel(Long caseId, String ctCode, int center) {
    set("case_id", caseId);
    setCtCode(ctCode);
    set("center", center);
  }
	
  public void setCaseId(Long caseId) {
		set("case_id", caseId);
	}
	
	public void setCtCode(String ctCode) {
		set("ct_code", ctCode);
	}
	
	public String getCtCode() {
		return getString("ct_code");
	}
	
  public void setLac(Long lac) {
		set("lac", lac);
	}
	
  public Long getLac() {
    return getLong("lac");
	}
	
  public void setCi(Long ci) {
		set("ci", ci);
	}
	
  public Long getCi() {
    return getLong("ci");
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

}
