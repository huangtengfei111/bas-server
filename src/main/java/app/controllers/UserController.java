package app.controllers;

import java.io.IOException;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;

import app.models.Account;
import app.models.User;
import app.util.JsonHelper;

public class UserController extends APIController {
  
  @GET
  public void profile() {
    Session session = SecurityUtils.getSubject().getSession();
    int accountId = Integer
        .parseInt(session.getAttribute("accountId").toString());
    Account account = Account.findById(accountId);

    setOkView();
    view("account", account);
    render();
  }
  
  @PUT
  public void updateAcct() throws IOException {
    Map map = JsonHelper.toMap(getRequestString());
    Session session = SecurityUtils.getSubject().getSession();
    int accountId = Integer
        .parseInt(session.getAttribute("accountId").toString());
    Account account = Account.findById(accountId);
    User user = account.getUser();
    user.setName(map.get("name").toString());
    user.setAvatar(map.get("avatar").toString());
    if (user.saveIt()) {
      setOkView();
      view("account", account);
      render("/user/profile");
    } else {
      setErrorView("updateAcct failure", 400);
      render("/common/error");
    }
  }

  @POST
  public void updatePassword() throws IOException {
    Map map = JsonHelper.toMap(getRequestString());
    String oldPassword = map.get("old_password").toString();
    String password = map.get("password").toString();
    String confirmedPassword = map.get("confirmed_password").toString();
    Session session = SecurityUtils.getSubject().getSession();
    int accountId = Integer
        .parseInt(session.getAttribute("accountId").toString());

    Account account = Account.findById(accountId);
    String salt = account.getSalt();
    Account ac = new Account();
    ac.setEncryptedPassword(oldPassword, salt);
    if (!ac.getPassword().equals(account.getPassword())) {
      setErrorView("old_password is not correct", 400);
      render("/common/error");
    } else {
      if (!password.equals(confirmedPassword)) {
        setErrorView("inconsistent password", 400);
        render("/common/error");
      } else {
        account.setEncryptedPassword(password, salt);
        if (account.saveIt()) {
          setOkView("password change success");
          view("account", account);
          render("/admin/accounts/_account");
        } else {
          setErrorView("password change failed", 400);
          render("/common/error");
        }
      }
    }
  }
}
