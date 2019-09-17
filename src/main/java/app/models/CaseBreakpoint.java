package app.models;

import java.sql.Date;
import java.util.List;

/**
 *
 */
public class CaseBreakpoint extends CaseAwareModel {
  
  static {
    validatePresenceOf("name").message("Please provide caseBreakpoint name"); 
    validatePresenceOf("started_at").message("Please provide caseBreakpoint started_at"); 
  }
  
  public static List<CaseBreakpoint> findByStartedAt(long caseId, Date start, Date end){
    String sql = "SELECT * FROM case_breakpoints WHERE case_id = ? AND started_at BETWEEN ? AND ?";
    List<CaseBreakpoint> caseBreakpoints = CaseBreakpoint.findBySQL(sql, caseId, start, end);
    return caseBreakpoints;
  }

  public Date getStartedAt() {
    return getDate("started_at");
  }
}