package app.models;

import org.javalite.activejdbc.Model;

/**
 *
 */
public class AuditLog extends Model {

  public static void setup(Long userId, String remoteAddr, Long caseId,
      String subject, String action, String params) {
    //@formatter:off
    AuditLog.createIt("user_id", userId, "subject", subject, 
                    "case_id", caseId, "remote_host", remoteAddr, 
                    "action", action, "params", params);
    //@formatter:on
  }
}