package app.models;

import org.javalite.activejdbc.Model;

/**
 *
 */
public class CaseAwareModel extends Model {

  public Long getCaseId() {
    return getLong("case_id");
  }

  public void setCaseId(int caseId) {
		set("case_id", caseId);
	}
}