package app.controllers.filters;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class RequiredRoleFilter extends APIFilter {

  private String[] roles;
  public RequiredRoleFilter(String... roles) {
    this.roles = roles;
  }

  @Override
  public void before() {
    Subject currentUser = SecurityUtils.getSubject();
    boolean qualified = false;

    for (int i = 0; i < this.roles.length; i++) {
      if (currentUser.hasRole(this.roles[i])) {
        qualified = true;
        break;
      }
    }
    if (!qualified) {
      currentUser.checkRoles(this.roles);
    }
    super.before();
  }

  @Override
  public void after() {
    super.after();
  }

}
