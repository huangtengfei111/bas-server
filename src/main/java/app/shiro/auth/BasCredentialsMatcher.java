package app.shiro.auth;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BasCredentialsMatcher extends HashedCredentialsMatcher {
  private static final Logger log = LoggerFactory.getLogger(BasCredentialsMatcher.class);

  private final String sensorHolderName = "sensorHolder";
  private final String blacklistHolderName = "blacklistHolder";
  private final int DEFAULT_FAILED_LIMIT = 5;

  private int failedLimit;
  private EhCacheManager shiroEhcacheManager;

  public BasCredentialsMatcher() {
    this.failedLimit = DEFAULT_FAILED_LIMIT;
  }

  public BasCredentialsMatcher(int failedLimit, EhCacheManager shiroEhcacheManager) {
    this.failedLimit         = failedLimit;
    this.shiroEhcacheManager = shiroEhcacheManager;
  }

  public void setFailedLimit(int failedLimit) {
    this.failedLimit = failedLimit;
  }

  public void setShiroEhcacheManager(EhCacheManager shiroEhcacheManager) {
    this.shiroEhcacheManager = shiroEhcacheManager;
  }

  @Override
  public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
    Cache<String, AtomicInteger> sensorHolder = shiroEhcacheManager.getCache(sensorHolderName);
    Cache<String, LocalDateTime> blacklistHolder = shiroEhcacheManager.getCache(blacklistHolderName);
    String login = (String) token.getPrincipal();

    LocalDateTime jailedAt = blacklistHolder.get(login);
    if (jailedAt == null) {
      // retry count + 1
      AtomicInteger retryCount = sensorHolder.get(login);
      if (null == retryCount) {
        retryCount = new AtomicInteger(0);
        sensorHolder.put(login, retryCount);
      }
      if (retryCount.incrementAndGet() > failedLimit) {
        blacklistHolder.put(login, LocalDateTime.now());
        log.warn("Login {} failed exceed max {}", login, failedLimit);
        throw new ExcessiveAttemptsException("acct.blocked");
      }
      boolean matches = super.doCredentialsMatch(token, info);
      if (matches) {
        sensorHolder.remove(login);
      }
      return matches;
    } else {
      log.warn("Login {} was blocked since {}", login, jailedAt);
      throw new ExcessiveAttemptsException("acct.blocked");
    }

  }
}

