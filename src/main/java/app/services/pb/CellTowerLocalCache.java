package app.services.pb;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import app.exceptions.NoLocalCellTowerException;
import app.models.pb.CellTower;
import io.vavr.Tuple;
import io.vavr.Tuple3;

public class CellTowerLocalCache {
  private static final Logger log = LoggerFactory.getLogger(CellTowerLocalCache.class);

  private static CellTowerLocalCache instance = null;

  private static LoadingCache<Tuple3<Long, Long, Long>, CellTower> cache = null;

  private CellTowerLocalCache() {
  }

  public static CellTowerLocalCache getInstance() {
    if (instance == null) {
      synchronized (CellTowerLocalCache.class) {
        if (instance == null) {
          cache    = CacheBuilder.newBuilder().maximumSize(50000).expireAfterAccess(3600, TimeUnit.MINUTES)
              .build(new CacheLoader<Tuple3<Long, Long, Long>, CellTower>() {
                         @Override
                         public CellTower load(Tuple3<Long, Long, Long> key) throws Exception {
                           CellTower ct = CellTower.findOne(key);
                           if (ct == null) {
                             throw new NoLocalCellTowerException(key.toString());
                           }
                           return ct;
                         }

                       });
          instance = new CellTowerLocalCache();
        }
      }
    }
    return instance;
  }

  public CellTower get(String code) {
    Tuple3<Long, Long, Long> key = normalizeKey(code);
    if (key == null)
      return null;

    try {
      return cache.get(key);
    } catch (ExecutionException e) {
      // log.error(e.getMessage(), e);
      return null;
    }
  }

  public CellTower get(Long mnc, Long lac, Long ci) {
    try {
      return cache.get(Tuple.of(mnc, lac, ci));
    } catch (ExecutionException e) {
      // log.error(e.getMessage(), e);
      return null;
    }
  }

  public Tuple3<Long, Long, Long> normalizeKey(String code) {
    List l = CellTower.normalize(code, 16);
    if (l != null) {
      Long mnc = (Long) l.get(0);
      Long lac = (Long) l.get(1);
      Long ci = (Long) l.get(2);
      return Tuple.of(mnc, lac, ci);
    }
    return null;
  }
}
