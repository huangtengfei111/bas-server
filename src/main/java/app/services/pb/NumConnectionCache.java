package app.services.pb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vavr.Tuple;
import io.vavr.Tuple2;

public class NumConnectionCache {
  private static final Logger log = LoggerFactory.getLogger(NumConnectionCache.class);

  private static NumConnectionCache instance = null;
  private static LoadingCache<Tuple2<Long, String>, Long> cache = null;

  private NumConnectionCache() {
  }

  public static NumConnectionCache getInstance() {
    if (instance == null) {
      synchronized (NumConnectionCache.class) {
        if (instance == null) {
          cache    = CacheBuilder.newBuilder().maximumSize(50000).expireAfterAccess(3600, TimeUnit.MINUTES)
              .build(new CacheLoader<Tuple2<Long, String>, Long>() {
                @Override
                public Long load(Tuple2<Long, String> key) throws Exception {
                  Long caseId = key._1();
                  String pnum = key._2();

                  //@formatter:off
                  String pnumBasedConnSql = "SELECT COUNT(DISTINCT owner_num) as c FROM pbill_records as pr " + 
                                            "LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                                            "WHERE cp.case_id = ? AND pr.peer_num = ?";
                  //@formatter:on
                  List<Map> lm2 = Base.findAll(pnumBasedConnSql, caseId, pnum);
                  Long c = Long.parseLong(lm2.get(0).get("c").toString());
                  log.debug("peer_num : {} , case_id : {}, count : {}", pnum,
                      caseId, c);
                  if (c > 0) {
                    return c;
                  }
                  return 0l;
                }
              });
          instance = new NumConnectionCache();
        }
      }
    }
    return instance;
  }

  public Long get(Long caseId, String pnum) throws ExecutionException {
    Tuple2<Long, String> key = Tuple.of(caseId, pnum);
    return cache.get(key);
  }

  public void refresh(Long caseId, String pnum) {
    Tuple2<Long, String> key = Tuple.of(caseId, pnum);
    cache.refresh(key);
  }

  public void invalidate(Long caseId, String pnum) {
    Tuple2<Long, String> key = Tuple.of(caseId, pnum);
    cache.invalidate(key);
  }

  /**
   * 更新涉及到的owner_num和peer_num对应的关联度
   * 
   * @param caseId
   */
  public void update(Long caseId) {

  }

  public void loadAll() throws ExecutionException {
    // load all active cases and owner numbers
    //@formatter:off
    String sql = "SELECT p.owner_num, cp.case_id FROM pbills as p " + 
                 "LEFT JOIN cases_pbills as cp ON p.id = cp.pbill_id LEFT JOIN cases as c ON c.id = cp.case_id " + 
                 "WHERE c.status = 1;";
    //@formatter:on
    List<Map> caseNums = Base.findAll(sql);
    log.info("Load {} <case, num> pairs into memory", caseNums.size());
    for (Map map : caseNums) {
      String ownerNum = map.get("owner_num").toString();
      Long caseId = Long.parseLong(map.get("case_id").toString());
      get(caseId, ownerNum);
    }
  }
}
