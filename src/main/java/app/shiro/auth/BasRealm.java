package app.shiro.auth;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.exceptions.AccountRevokedException;
import app.models.Account;
import app.models.Role;

public class BasRealm extends AuthorizingRealm {

	private static final Logger log = LoggerFactory.getLogger(BasRealm.class);

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    // null usernames are invalid
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    Account principal = (Account) principals.getPrimaryPrincipal();

    Role role = principal.getRole();
    if (role != null) {
      info.addRole(role.getValue());
    }

    // User user = (User) getAvailablePrincipal(principals);

    // SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    // info.setRoles(user.getRoles());
    // info.setStringPermissions(user.getPerms());
    return info;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    // identify account to log to
    UsernamePasswordToken userPassToken = (UsernamePasswordToken) token;
    String username = userPassToken.getUsername();

    if (username == null) {
      log.error("Username is null.");
      return null;
    }
    log.info("Login {}", username);

    // read password hash and salt from db
    try {
      Account account = Account.findFirst("username = ?", username);
      log.info("Account info {}", account);

      if (account == null) {
        log.error("No account found for user [{}]", username);
        return null;
      }

      if (account.isRevoked()) { // <- 代码插入的位置
        throw new AccountRevokedException();
      } else {

        SimpleAuthenticationInfo info =
            new SimpleAuthenticationInfo(account, account.getPassword(), account.getUsername());

        if (account.getSalt() != null)
          info.setCredentialsSalt(ByteSource.Util.bytes(account.getSalt()));

        return info;
      }
    } catch (Exception e) {
      log.error("Authentication error: {}", e);
      return null;
    }

  }
}