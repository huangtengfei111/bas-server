package app.controllers.admin;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.cache.ehcache.EhCache;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;

import com.google.common.reflect.ClassPath;

import app.controllers.APIController;
import app.exceptions.ErrorCodes;
import app.exceptions.InvalidLicenseException;
import app.exceptions.NoLicenseException;
import app.models.Account;
import app.models.Role;
import app.models.User;
import app.util.JsonHelper;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;
import app.util.license.client.LockServerParam;
import de.schlichtherle.license.LicenseContent;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

@SuppressWarnings("unchecked")
@RESTful
public class AccountsController extends APIController {

  private String[] reserved = { "id", "salt", "username", "password" };

  public void index() {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));

//  	Paginator p = new Paginator(Account.class, pageSize, "deleted_at IS NULL").orderBy("id desc");
    //@formatter:off
    String sql = "SELECT a.* FROM accounts a LEFT JOIN users u ON a.user_id = u.id " + 
                 "WHERE a.deleted_at IS NULL";
    //@formatter:on
    Paginator p = new Paginator(Account.class, pageSize, sql).orderBy("u.last_login_at DESC");
    List<Account> accounts = p.getPage(currentPage);

    setOkView();
    view("page_total", p.pageCount(), "page_current", currentPage, "accounts", accounts);

    render();
  }

  @GET
  public void locked() {
    URL uri = ClassPath.class.getClassLoader().getResource("shiro-ehcache.xml");
    logDebug("uri:" + uri);
    CacheManager cacheManager = CacheManager.create(uri);
    Ehcache cache = cacheManager.getCache("blacklistHolder");

    logDebug("cache : " + cache);
    if (cache != null) {
      Map<String, LocalDateTime> blackMap = new HashMap<>();
      EhCache<String, LocalDateTime> blackCache = new EhCache<>(cache);
      Set<String> keys = blackCache.keys();
      for (String login : keys) {
        logDebug("login:" + login);
        if (blackCache.get(login) != null) {
          LocalDateTime blockedAt = blackCache.get(login);
          logDebug("blockedAt : " + blockedAt);
          blackMap.put(login, blockedAt);
        }
      }
      setOkView("locked accounts");
      view("locked", blackMap);
      render("/admin/accounts/locked");
    } else {
      setErrorView("no cache", ErrorCodes.INTERNAL_ERROR);
      render("/common/error");
    }
  }

  /**
   * @throws NoLicenseException
   * @throws InvalidLicenseException
   *
   */
  public void create() throws Exception {

    String[] protectedFields = { "id" };
    Map map = JsonHelper.toMap(getRequestString());

    Map _account = (Map) map.get("account");
    Map _user = (Map) map.get("user");

    if (_account == null && _user == null) {
      setErrorView("Accounts,users is null", 400);
      render("/common/error");
    } else {
      Account acc = Account.findFirst("username = ?", _account.get("username"));
      if (acc != null) {
        setErrorView("username is exist", 400);
        render("/common/error");
      } else {
        LicenseClientParam param = BASLicenseManager.loadClientParam();
        if (param != null) {
          BASLicenseManager licenseManager = new BASLicenseManager(param);
          try {
            LicenseContent content = licenseManager.verify();
            LockServerParam lsParam = (LockServerParam) content.getExtra();
            Long acctLimit = lsParam.getAcctLimit();
            Long count = Account.count();
            if (acctLimit <= count) {
              setErrorView("License acctLimit insufficient", 400);
              render("/common/error");
            } else {
              Account account = new Account();
              account.fromMap(_account);
              account.validate();

              User user = new User();
              user.fromMap(_user);
              user.validate();

              Map<String, String> acctErr = account.errors();
              Map<String, String> usrErr = user.errors();
              if (acctErr.size() > 0 || usrErr.size() > 0) {
                setErrorView("create user/account error", 400);
                render("/common/error");
              } else {
                String _role = _account.get("role").toString();
                Role role = Role.findFirst("value = ?", _role);
                if (role == null) {
                  setErrorView("role is null", 400);
                  render("/common/error");
                } else {
                  if (!_account.get("password").equals(_account.get("confirmed_password"))) {
                    setErrorView("inconsistent password", 400);
                    render("/common/error");
                  } else {
                    try {
                      Base.openTransaction();

                      user.saveIt();

                      RandomNumberGenerator rng = new SecureRandomNumberGenerator();
                      Object salt = rng.nextBytes();

                      account.setEncryptedPassword(account.getPassword(), salt);
                      account.setRoleId(role.getLongId());
                      account.setParent(user);

                      account.saveIt();
                      Base.commitTransaction();

                      setOkView("account created");
                      view("account", account);
                      render("_account");

                    } catch (Exception e) {
                      logError(e);
                      Base.rollbackTransaction();
                    }
                  }
                }
              }
            }
          } catch (Exception e) {
            logError(e.getMessage(), e);
            throw new InvalidLicenseException(e);
          }

        } else {
          throw new NoLicenseException();
        }
      }
    }
  }

  /**
   *
   */
  public void update() throws IOException {
    Account a = (Account) Account.findById(getId());
    if (a == null) {
      setErrorView("no such account", 400);
      render("/common/_blank");
    } else {
      Map payload = JsonHelper.toMapWithIgnores(getRequestString(), reserved);
      a.fromMap(payload);
      a.set("id", getId());
      a.saveIt();

      setOkView("updated");
      view("account", a);
      render("_account");
    }
  }

  public void destroy() {
    Account account = Account.findFirst("id = ?", getId());
    if (!account.isBuiltIn()) {
      // Account.delete("id = ?", getId());
      account.delete();

      setOkView("account deleted");
      view("id", getId());
      render("/common/ok");
    } else {
      setErrorView("account.builtIn", ErrorCodes.ACCT_BUILT_IN);
      render("/common/error");
    }
  }

  /**
   * 冻结账号
   */
  @POST
  public void revoke() {
    Account account = Account.findFirst("id = ?", getId());
    
    //TODO 要考虑管理员的账号是否可以冻结，以下按不可冻结进行。
    
    if (!account.isBuiltIn()) {

      if (!account.isRevoked()) {
        account.set("deleted_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        account.saveIt();
      }

      setOkView("account revoked!");
      view("result", "revoke success");
      render("/common/ok");
    } else {
      setErrorView("account.builtIn", ErrorCodes.ACCT_BUILT_IN);
      render("/common/error");
    }
  }

  @POST
  public void resetPasswd() throws IOException {
    Map payload = JsonHelper.toMap(getRequestString());

    Account a = Account.findById(payload.get("id"));
    if (a == null) {
      setErrorView("no such account", 400);
      render("/common/_blank");
    } else {
      String password = payload.get("password").toString();
      String repeatPassword = payload.get("repeat_password").toString();
      if (!password.equals(repeatPassword)) {
        setErrorView("inconsistent password", 400);
        render("/common/_blank");
      } else {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        a.setEncryptedPassword(password, salt);
        a.saveIt();

        setOkView("account resetPasswd");
        view("account", a);
        render("_account");
      }
    }
  }

  @POST
  public void search() throws IOException {
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));

    List<String> conds = new ArrayList<String>();
    List vals = new ArrayList();

    Map payload = JsonHelper.toMap(getRequestString());

    payload.forEach((key, valus) -> {
      String keyStr = key.toString();

      if (valus instanceof List) {
        List v = (List) valus;
        String op = v.get(0).toString();
        Object val = v.get(1);

        switch (op) {
        case "FUZZY":
          conds.add(keyStr + " LIKE ?");
          vals.add("%" + val.toString() + "%");
          break;
        default:
          conds.add(keyStr + " = ?");
          vals.add(val.toString());
          break;
        }
      } else if (valus instanceof String) {
        conds.add(keyStr + " = ?");
        vals.add(valus);
      }
    });
    String query = String.join(" AND ", conds);

    Paginator p =
        new Paginator(Account.class, pageSize, query, vals.stream().toArray(Object[]::new)).orderBy("id desc");
    List<Account> accounts = p.getPage(currentPage);
    setOkView();
    view("page_total", p.getCount(), "page_current", currentPage, "accounts", accounts);
    render("index");
  }

}