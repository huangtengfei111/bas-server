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
public class VenNumber extends CaseAwareModel {
  // 1. 手工单条添加；2. 批量导入；3. 综合人员信息库中导入
  public static final int MAN_INPUT_SOURCE = 1;
  public static final int BATCH_INPUT_SOURCE = 2;

  static {
//    validatePresenceOf("num").message("Please provide venNumber num"); 
//    validateNumericalityOf("short_num").greaterThan(100000).lessThan(1000000).onlyInteger().message("valid.shortNum");
    validateRegexpOf("num", RegExpPattern.CN_MOBILE_NUM_RULE)
        .message("valid.venNumber num ");
    validateRegexpOf("short_num", RegExpPattern.CN_VEN_NUM_RULE)
        .message("valid.shortNum");
    validatePresenceOf("network").message("required.venNetwork");
  }
  
	public VenNumber() {
	}

  public VenNumber(Long caseId, int source) {
    setCaseId(caseId);
    setSource(source);
  }

  public VenNumber(Long caseId, String num, String shortNum, String network,
      String label, int source) {
		manageTime(false);
		setCaseId(caseId);
		setNum(num);
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
    // 找到本方号码对应的Pbill记录，并且确定本方号码的虚拟网名称
    // 然后找到虚拟网名称相同的号码，和前面的Pbill做关联
    List<VenNumber> sameNetwork = VenNumber
        .where("network = ? AND num != ?", getNetwork(), getNum());
    if(sameNetwork.size() > 0) {
      List<String> vals = new ArrayList<String>();
      List<String> params = new ArrayList<>();
      for (VenNumber venNumber : sameNetwork) {
        params.add("?");
        vals.add(venNumber.getNum());
      }
      String paramStmt = String.join(",", params);
//      String pnum = "owner_num IN (" + String.join(",", nums) + ") ";
      String sql = "SELECT * FROM pbills WHERE owner_num IN (" + paramStmt + ")";
      Object[] _vals = vals.stream().toArray(Object[]::new);
      List<Pbill> pbills = ModelDelegate.findBySql(Pbill.class, sql, _vals);
      return pbills;
    }
    return null;
  }
}