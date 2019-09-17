package app.models.pb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.javalite.activejdbc.ModelDelegate;

import app.models.CaseAwareModel;
import app.util.RegExpPattern;

/**
 *
 */
public class RelNumber extends CaseAwareModel {
  //1. 手工单条添加；2. 批量导入；3. 综合人员信息库中导入
  public static final int MAN_INPUT_SOURCE = 1;
  public static final int BATCH_INPUT_SOURCE = 2;
  
  static {
    validateRegexpOf("num", RegExpPattern.CN_MOBILE_NUM_RULE)
        .message("valid.relNumber num ");
    validateRegexpOf("short_num", RegExpPattern.CN_REL_NUM_RULE)
        .message("valid.short_num");
    // validatePresenceOf("num").message("Please provide relNumber num");
//    validatePresenceOf("short_num").message("Please provide relNumber short_num"); 
    validatePresenceOf("network").message("Please provide relNumber network"); 
  }

	public RelNumber() {
		
	}
	
  public RelNumber(Long caseId, int source) {
    setCaseId(caseId);
    setSource(source);

  }

  public RelNumber(Long caseId, String num, String shortNum, String network,
      String label, int source) {
		manageTime(false);
		setCaseId(caseId);
		setNum(shortNum);
		setShortNum(shortNum);
		setNetwork(network);
		setLabel(label);
		setSource(source);
		LocalDateTime dt = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String now = dt.format(formatter);
		set("created_at", now);
  	set("updated_at", now);
	}
	
  public void setCaseId(Long caseId) {
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

	public void setNetwork(String network) {
		set("network", network);
	}

	public String getNetwork() {
		return getString("network");
	}

	public void setLabel(String label) {
		set("label", label);
	}

	public String getLabel() {
		return getString("label");
	}

	public void setSource(int source) {
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
	
  public List<Pbill> getPbills() {
    // 找到本方号码对应的Pbill记录，并且确定本方号码的亲情网名称
    // 然后找到亲情网名称相同的号码，和前面的Pbill做关联
    List<RelNumber> sameNetwork = RelNumber
        .where("network = ? AND num != ?", getNetwork(), getNum());
    if (sameNetwork.size() > 0) {
      List<String> vals = new ArrayList<String>();
      List<String> params = new ArrayList<>();
      for (RelNumber relNumber : sameNetwork) {
        params.add("?");
        vals.add(relNumber.getNum());
      }
      // String sql = "SELECT * FROM pbills WHERE owner_num IN (?, ....)";
      // List<Pbill> pbills = ModelDelegate.findBySql(Pbill.class, sql, "");
      String paramStmt = String.join(",", params);
      String sql = "SELECT * FROM pbills WHERE owner_num IN (" + paramStmt + ")";
      Object[] _vals = vals.stream().toArray(Object[]::new);
      List<Pbill> pbills = ModelDelegate.findBySql(Pbill.class, sql, _vals);
      return pbills;
    }
    return null;
  }
}