package app.controllers;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.javalite.activeweb.RequestUtils;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import app.exceptions.NoLicenseException;
import app.models.Account;
import app.models.Role;
import app.models.Setting;
import app.models.User;
import app.util.JsonHelper;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;

/**
 * @author
 */
public class LoginController extends APIController {

  @GET
  public void index() {
    // get the currently executing user:
    currentSession();
  }

  @POST
  public void login() throws Exception {
    Map<String, String> login = JsonHelper.toMap(getRequestString());

    String username = login.get("username");
    String password = login.get("password");
    logDebug("Try login > " + username + "/" + password);

    // get the currently executing user:
    Subject currentUser = SecurityUtils.getSubject();
    clearOtherSessionIfPossible(username, currentUser);

    if (!currentUser.isAuthenticated()) {
      // collect user principals and credentials in a gui specific manner
      // such as username/password html form, X509 certificate, OpenID,
      // etc.
      // We'll use the username/password example here since it is the most
      // common.
      UsernamePasswordToken token = new UsernamePasswordToken(username, password);
      // this is all you have to do to support 'remember me' (no config -
      // built in!):

      /* 记住我! */
      // token.setRememberMe();

      try {
        currentUser.login(token);
        if (currentUser.hasRole(Role.USER)) { // check license only on USER
          LicenseClientParam param = BASLicenseManager.loadClientParam();
          if (param != null) {
            BASLicenseManager licenseManager = new BASLicenseManager(param);
            licenseManager.verify();
          } else {
            throw new NoLicenseException();
          }
        }
        
        postLogin(currentUser);

      } catch (UnknownAccountException uae) {
        logError(uae);

        setErrorView("incorrect credentials", 401);
        render("/common/_blank");
      } catch (IncorrectCredentialsException ice) {
        logError(ice);

        setErrorView("incorrect credentials", 401);
        render("/common/_blank");
      } catch (LockedAccountException lae) {
        logError(lae);

        setErrorView("locked account", 401);
        render("/common/_blank");
      }
    } else {
      postLogin(currentUser);
    }

  }

  @GET
  public void logout() {
    Subject currentUser = SecurityUtils.getSubject();
    Account account = (Account) currentUser.getPrincipal();

    if (account != null) {
      currentUser.logout();

      view("account", account);
      setOkView("logged-out");
      render("_account");
    } else {
      setErrorView("no session", 502);
      render("/common/error");
    }
  }

  @GET
  public void currentSession() {
    Subject currentUser = SecurityUtils.getSubject();
    if (currentUser.isAuthenticated()) {
      Account account = (Account) currentUser.getPrincipal();
      String systemId = Setting.getSystemId();
      
      setOkView("logged-in session");
      view("account", account, "system_id", systemId);
      render("_account");
    } else {
      setErrorView("login required", 401);
      render("/common/error");
    }
  }

  private void clearOtherSessionIfPossible(String username, Subject currentUser) {
    if (currentUser != null) {
      Account account = (Account) currentUser.getPrincipal();
      if (account != null) {
        if (currentUser.isAuthenticated() &&
            !username.equals(account.getUsername())) {
          currentUser.logout();
        }
      }
    }
  }

  private void postLogin(Subject currentUser) {
    Account account = (Account) currentUser.getPrincipal();
    Session session = currentUser.getSession();
    String systemId = Setting.getSystemId();

    User user = account.getUser();

    String remoteAddr = RequestUtils.remoteAddress();
    user.logFootprint(remoteAddr);

    session.setAttribute("accountId", account.getLongId());
    session.setAttribute("userId", user.getLongId());
    session.setAttribute("userName", user.getName());

    setOkView("logged-in");
    view("account", account, "system_id", systemId);
    render("_account");
  }
}
