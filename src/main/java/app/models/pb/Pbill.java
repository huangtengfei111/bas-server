package app.models.pb;


import static org.javalite.common.Collections.map;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.ModelDelegate;

import app.models.CallAttribution;
import app.models.Case;
import app.models.LabelGroup;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.util.UniversalQueryHelper;
import app.util.collections.ListMap;

/**
 * Phone Bills
 */
public class Pbill extends Model {
  
  public Pbill() {
  }

  public static Pbill findOrCreate(String ownerNum) {    
    Pbill pbill = Pbill.findFirst("owner_num = ?", ownerNum);    
    CallAttribution callAttribution = CallAttribution.findPhoneCA(ownerNum);
    if (pbill == null) {
      pbill = new Pbill();
    }
    pbill.setOwnerNum(ownerNum);
    if (callAttribution != null) {
      pbill.setCallAttribution(callAttribution.getCity());
    }

    pbill.saveIt();
    return pbill;
  }

  public static List<Pbill> pbillsInCase(Long caseId) {
    String fullQuery = "SELECT p.* FROM pbills as p LEFT JOIN cases_pbills as cp ON p.id = cp.pbill_id WHERE cp.case_id = ?";
    return ModelDelegate.findBySql(Pbill.class, fullQuery, caseId);
  }

  public static Set peerNumsInCommon(Long caseId, String num1, String num2) {
    return peerNumsInCommon(caseId, num1, num2, true);
  }

  public static Set peerNumsInCommon(Long caseId, String num1, String num2,
      boolean excludePubServiceNums) {

    List<Object> vals0 = new ArrayList<>();
    List<String> ph = new ArrayList<>();
    String pbillConds = " 1=1 ";
    vals0.add(caseId.toString());
    vals0.add(num1);
    if (excludePubServiceNums) {
      List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
      if (pubServiceNums != null && pubServiceNums.size() > 0) {
        for (PubServiceNum psn : pubServiceNums) {
          ph.add("?");
          vals0.add(psn.getNum());
        }
        pbillConds = "pr.peer_num NOT IN (" + String.join(",", ph) + ") ";
      }
      vals0.add(caseId.toString());
      vals0.add(num2);
    }
    Set<String> peerNumsInComm = new HashSet<>();
    //@formatter:off
    String sql = "SELECT distinct pr.peer_num " + 
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                 "WHERE cp.case_id = ? AND pr.owner_num = ? AND " + pbillConds +
                      "AND peer_num IN ( SELECT distinct peer_num " + 
                                        "FROM pbill_records as pr2 LEFT JOIN cases_pbills as cp2 ON pr2.pbill_id = cp2.pbill_id " + 
                                        "WHERE cp2.case_id = ? AND pr2.owner_num = ?)";
    //@formatter:on
    List<Map> lm = Base.findAll(sql, vals0.stream().toArray(Object[]::new));
    peerNumsInComm = ListMap.valuesToSet(lm);

    return peerNumsInComm;
  }
  
  public static List<Map> peerCommonNums(Long caseId, String json, String num1,
      String num2, boolean excludePubServiceNums) throws Exception {

    Options options = UniversalQueryHelper.normalize(json, "pr",
        map("cp.case_id", caseId.toString()));
//    String num1 = options.getAdhocParam("x_num").toString();
//    String num2 = options.getAdhocParam("y_num").toString();

    Options options2 = UniversalQueryHelper.normalize(json, "pr2",
        map("cp2.case_id", caseId.toString()));
    options.delCriteria("pr.owner_num");
    options.delCriteria("pr.peer_num");
    options2.delCriteria("pr2.owner_num");
    options2.delCriteria("pr2.peer_num");

    options.addCriteria(new CriteriaTuple("pr.owner_num", num1));
    options2.addCriteria(new CriteriaTuple("pr2.owner_num", num2));

    List<Object> vals0 = new ArrayList<>();
    List<String> ph = new ArrayList<>();
    if (excludePubServiceNums) {
      List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
      if (pubServiceNums != null && pubServiceNums.size() > 0) {
        for (PubServiceNum psn : pubServiceNums) {
          vals0.add(psn.getNum());
        }
      }
    }
    options.addCriteria(new CriteriaTuple("pr.peer_num", Op.NOT_IN, vals0));
    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(options,
        false);
    List<Object> sqlAndVals2 = UniversalQueryHelper.getSqlAndBoundVals(options2,
        false);
    String sql1 = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    String sql2 = (String) sqlAndVals2.get(0);
    List vals2 = (List) sqlAndVals2.get(1);
    vals.addAll(vals2);

    //@formatter:off
    String sql = "SELECT distinct pr.peer_num, peer_cname " + 
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                 "WHERE " + sql1 +
                      "AND peer_num IN ( SELECT distinct peer_num " + 
                                        "FROM pbill_records as pr2 LEFT JOIN cases_pbills as cp2 ON pr2.pbill_id = cp2.pbill_id " + 
                                        "WHERE " + sql2 + ")";
    //@formatter:on
    List<Map> lm = Base.findAll(sql, vals.stream().toArray(Object[]::new));
    return lm;
  }
  
  public static long peerNumsCommonDegree(Long caseId, String num1, String num2) {
    return peerNumsCommonDegree(caseId, num1, num2, true);
  }
  
  public static long peerNumsCommonDegree(Long caseId, String num1, String num2, boolean excludePubServiceNums) {

    List<Object> vals0 = new ArrayList<>();
    List<String> ph = new ArrayList<>();
    String pbillConds = " 1=1 ";
    vals0.add(caseId.toString());
    vals0.add(num1);
    if (excludePubServiceNums) {
      List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();
      if (pubServiceNums != null && pubServiceNums.size() > 0) {
        for (PubServiceNum psn : pubServiceNums) {
          ph.add("?");
          vals0.add(psn.getNum());
        }
        pbillConds = "pr.peer_num NOT IN (" + String.join(",", ph) + ") ";
      }
      vals0.add(caseId.toString());
      vals0.add(num2);
    }
    //@formatter:off
    String sql = "SELECT count(distinct pr.peer_num) as degree " + 
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                 "WHERE cp.case_id = ? AND pr.owner_num = ? AND " + pbillConds + 
                      "AND peer_num IN ( SELECT distinct peer_num " + 
                                        "FROM pbill_records as pr2 LEFT JOIN cases_pbills as cp2 ON pr2.pbill_id = cp2.pbill_id " + 
                                        "WHERE cp2.case_id = ? AND pr2.owner_num = ?)";
    //@formatter:on
    List<Map> lm = Base.findAll(sql, vals0.stream().toArray(Object[]::new));

    if (lm == null || lm.size() == 0) {
      return 0l;
    } else {
      return Long.parseLong(lm.get(0).get("degree").toString());
    }
  }

  public VenNumber getVenNumber(Long caseId) {
    return VenNumber.findFirst("num = ? AND case_id = ?", getOwnerNum(), caseId);
  }

  public RelNumber getRelNumber(Long caseId) {
    return RelNumber.findFirst("num = ? AND case_id = ?", getOwnerNum(), caseId);
  }

  public List<VenNumber> venNums(Long caseId) {
    List<VenNumber> venNums = VenNumber.where("num = ? AND case_id = ?", getOwnerNum(), caseId);
    if (venNums.size() == 0) {
      return null;
    } else {
      List<Object> vals = new ArrayList<>();
      List<String> params = new ArrayList<>();
      vals.add(caseId);
      for (VenNumber venNum : venNums) {
        params.add("?");
        vals.add(venNum.getNetwork());
      }
      String paramStmt = String.join(",", params);
      String sql = "case_id = ? AND network IN (" + paramStmt + ")";
      Object[] _vals = vals.stream().toArray(Object[]::new);
      return VenNumber.where(sql, _vals);
    }
  }

  public List<RelNumber> relNums(Long caseId) {
    List<RelNumber> relNums = RelNumber.where("num = ? AND case_id = ?", getOwnerNum(), caseId);
    if (relNums.size() == 0) {
      return null;
    } else {
      List<Object> vals = new ArrayList<>();
      List<String> params = new ArrayList<>();
      vals.add(caseId);
      for (RelNumber relNum : relNums) {
        params.add("?");
        vals.add(relNum.getNetwork());
      }
      String paramStmt = String.join(",", params);
//    String sql = "SELECT * FROM rel_numbers WHERE case_id = ? AND network IN (" + paramStmt + ")";
      String sql = "case_id = ? AND network IN (" + paramStmt + ")";
//    String[] vals = networks.stream().toArray(String[]::new);
      Object[] _vals = vals.stream().toArray(Object[]::new);
      return RelNumber.where(sql, _vals);
    }
  }
  
  public List<LabelGroup> getLabelGroups(Long caseId) {
    String sql = "SELECT lg.* FROM label_groups as lg LEFT JOIN pnum_labels_label_groups as pllg ON lg.id = pllg.label_group_id " +
                 "LEFT JOIN pnum_labels as pl ON pllg.pnum_label_id = pl.id " +
                 "WHERE lg.case_id = ? AND pl.case_id = ? AND pl.num = ?";
    return ModelDelegate.findBySql(LabelGroup.class, sql, caseId, caseId, getOwnerNum());
  }
  
  public List<OutlierNum> getOutliers() {
    String fullQuery = "SELECT * FROM outlier_nums WHERE pbill_id = ? AND num = ?";
    return ModelDelegate.findBySql(OutlierNum.class, fullQuery, getLongId(), getOwnerNum());
  }

  public String getOwnerNum() {
    return getString("owner_num");
  }

  public void setOwnerNum(String ownerNum) {
    set("owner_num", ownerNum);
  }
  
  public String getOwnerName() {
    return getString("owner_name");
  }

  public void setOwnerName(String ownerName) {
    set("owner_name", ownerName);
  }
  
  public String getResidence() {
    return getString("residence");
  }
  
  public void setResidence(String residence) {   
    set("residence", residence);
  }
  
  public Date getStartedAt() {
    return getDate("started_at");
  }
  
  public Date getEndedAt() {
    return getDate("ended_at");
  }
  
  public void setCallAttribution(String callAttribution) {   
    set("call_attribution", callAttribution);
  }

  public List<Case> getCase(long caseId) {
    String sql = "SELECT c.* " +
                 "FROM cases as c LEFT JOIN cases_pbills as cp ON c.id = cp.case_id LEFT JOIN pbills as p ON cp.pbill_id = p.id " +
                 "WHERE p.id = ? AND c.id != ? ";
    List<Case> cases = Case.findBySQL(sql, getId(), caseId);
    return cases;
  } 
}