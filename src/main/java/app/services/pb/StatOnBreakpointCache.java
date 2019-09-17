package app.services.pb;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import app.models.Case;
import app.models.CaseBreakpoint;
import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import io.vavr.Tuple;
import io.vavr.Tuple4;

/**
 * 根据时间分割点计算对应的通话统计
 *
 */
public class StatOnBreakpointCache {
  private static final Logger log = LoggerFactory.getLogger(StatOnBreakpointCache.class);

  private static StatOnBreakpointCache instance = null;
  private static LoadingCache<Tuple4<Long, String, Date, Date>, Long> cache = null;

  private StatOnBreakpointCache() {
  }

  public static StatOnBreakpointCache getInstance() {
    if (instance == null) {
      cache    =
          CacheBuilder.newBuilder().maximumSize(50000)
          .build(new CacheLoader<Tuple4<Long, String, Date, Date>, Long>() {
                     @Override
                     public Long load(Tuple4<Long, String, Date, Date> key) throws Exception {
                       return PbillRecord.countByBreakpoint(key);
                     }

                   });
      instance = new StatOnBreakpointCache();
    }
    return instance;
  }

  public Long get(Long caseId, String num, Date startedAt, Date endedAt) {
    Tuple4<Long, String, Date, Date> key = Tuple.of(caseId, num, startedAt, endedAt);
    if (key == null)
      return null;

    try {
      return cache.get(key);
    } catch (ExecutionException e) {
      // log.error(e.getMessage(), e);
      return 0l;
    }
  }

  // 载入所有时间分割点统计
  public void loadAll() throws ExecutionException {
    List<Case> cases = Case.findAll();
    for (Case c : cases) {
      long caseId = Long.parseLong(c.getId().toString());
      List<Pbill> pbills = c.getPbills();
      for (Pbill pbill : pbills) {
        String ownerNum = pbill.getOwnerNum();
        Date startedAt = pbill.getStartedAt();
        Date endedAt = pbill.getEndedAt();
        List<CaseBreakpoint> caseBreakpoints = CaseBreakpoint
            .findByStartedAt(caseId, startedAt, endedAt);
        cache.get(new Tuple4<>(caseId, ownerNum, startedAt,
            caseBreakpoints.get(0).getStartedAt()));
        for (int i = 0; i < caseBreakpoints.size(); i++) {
          cache.get(new Tuple4<>(caseId, ownerNum,
              caseBreakpoints.get(i).getStartedAt(),
              caseBreakpoints.get(i + 1).getStartedAt()));
        }
        cache.get(new Tuple4<>(caseId, ownerNum,
            caseBreakpoints.get(caseBreakpoints.size() - 1).getStartedAt(),
            endedAt));
      }
    }
  }
}
