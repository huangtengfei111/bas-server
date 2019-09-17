package app.controllers.filters;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

import app.models.AuditLog;

public class LogActionFilter extends HttpSupportFilter {

  @Override
  public void before() {
    String action = getRoute().getController().getClass().getName() + "#" + getRoute().getActionName() + ":" + method();
    Long caseId = Long.parseLong(param("case_id"));
    try {
      String reqString = getRequestString();
      assign("reqString", reqString);
      // String params = params().toString(); // getRequestString();

      Subject currentUser = SecurityUtils.getSubject();
      Session session = currentUser.getSession();
      if (session != null) {
        Long uId = (Long) session.getAttribute("userId");
        String uName = (String) session.getAttribute("userName");
        AuditLog.setup(uId, remoteAddress(), caseId, uName, action,
            reqString);
      }
    } catch (Exception e) {
      logError(e);
    }
  }

}
