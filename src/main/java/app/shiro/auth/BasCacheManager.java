package app.shiro.auth;

import org.apache.shiro.cache.ehcache.EhCacheManager;

import net.sf.ehcache.CacheManager;

public class BasCacheManager extends EhCacheManager {

  public BasCacheManager() {
    setCacheManager(CacheManager.create());
  }
}
