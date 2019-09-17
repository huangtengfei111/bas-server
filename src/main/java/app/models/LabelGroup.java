package app.models;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.ModelDelegate;

import app.models.pb.CtLabel;
import app.models.pb.PnumLabel;

public class LabelGroup extends CaseAwareModel {
  public static final int NUM_TOPIC = 1;
  public static final int EVENT_TOPIC = 2;
  public static final int CT_TOPIC = 3;

  static {
    validatePresenceOf("name").message("Please provide labelGroup name");  
  }
  
  public static LazyList<PnumLabel> getPnumLabels(Long caseId, String groupName) {
    //@formatter:off
    String sql = "SELECT pl.* FROM pnum_labels as pl LEFT JOIN label_groups as lg ON pl.label_group_id = lg.id " + 
                 "WHERE pl.case_id = ? AND lg.name = ?";
    //@formatter:on
    return ModelDelegate.findBySql(PnumLabel.class, sql, caseId, groupName);
  }

  public static LazyList<CtLabel> getCtLabels(Long caseId, String groupName) {
    //@formatter:off
    String sql = "SELECT pl.* FROM ct_labels as cl LEFT JOIN label_groups as lg ON cl.label_group_id = lg.id " + 
                 "WHERE pl.case_id = ? AND lg.name = ?";
    //@formatter:on
    return ModelDelegate.findBySql(CtLabel.class, sql, caseId, groupName);
  }

  public LabelGroup() {
  }

  public LabelGroup(Long caseId, String name, int topic) {
    set("case_id", caseId, "name", name, "topic", topic);
  }

  public String getName() {
    return getString("name");
  }

}
