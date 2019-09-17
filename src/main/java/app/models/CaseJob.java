package app.models;

import java.util.Date;

public class CaseJob extends CaseAwareModel {
  public static final String JTYPE_CONNECT_CITIZEN = "conn_citizen_job";
  public static final String JTYPE_CONNECT_CELLTOWER = "conn_celltower_job";
  public static final String JTYPE_FIND_OUTLIER_NUM = "find_outlier_nums_job";
  public static final String JTYPE_MERGE_PBILLS = "merge_pbills_job";
  public static final String JTYPE_SYNC_CELLTOWE = "sync_celltower";

  public CaseJob(Long caseId, Long jid, String jtype, Date executedAt) {
    set("case_id", caseId);
    set("jid", jid);
    set("jtype", jtype);
    set("executed_at", executedAt);
  }

  public CaseJob() {
  }
}
