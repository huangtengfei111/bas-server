package app.models.pb;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PnumMeet {

  private String rule;
  private HashMap<Date, Set<String>> lacOrCiMap;
//  private HashMap<Date, Set<String>> ciSet;
  private HashMap<Date, List<Set<String>>> closedCellTowers;
  private Set<String> cellTowers;
  private List<PbillRecord> pbillRecords;

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public HashMap<Date, Set<String>> getLacOrCiMap() {
    return lacOrCiMap;
  }

  public void setLacOrCiMap(HashMap<Date, Set<String>> lacOrCiMap) {
    this.lacOrCiMap = lacOrCiMap;
  }

//  public HashMap<Date, Set<String>> getCiSet() {
//    return ciSet;
//  }

//  public void setCiSet(HashMap<Date, Set<String>> ciSet) {
//    this.ciSet = ciSet;
//  }

  public HashMap<Date, List<Set<String>>> getClosedCellTowers() {
    return closedCellTowers;
  }

  public void setClosedCellTowers(
      HashMap<Date, List<Set<String>>> closedCellTowers) {
    this.closedCellTowers = closedCellTowers;
  }

  public Set<String> getCellTowers() {
    return cellTowers;
  }

  public void setCellTowers(Set<String> cellTowers) {
    this.cellTowers = cellTowers;
  }

  public List<PbillRecord> getPbillRecords() {
    return pbillRecords;
  }

  public void setPbillRecords(List<PbillRecord> pbillRecords) {
    this.pbillRecords = pbillRecords;
  }

}
