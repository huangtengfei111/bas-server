package app.services.pb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.models.Case;
import app.models.pb.CommPair;
import app.models.pb.Pbill;

public class NumRelCache {
  private static final Logger log = LoggerFactory.getLogger(NumRelCache.class);

  private static NumRelCache instance = null;
  private static Map<CommPair, Long> inCommonsCache = null;
  private static Map<String, Long> correlationCache = null;
  private NumRelCache() {
  }

  public static NumRelCache getInstance() {
    if (instance == null) {
      synchronized (NumRelCache.class) {
        if (instance == null) {
          inCommonsCache   = new ConcurrentHashMap<>();
          correlationCache = new ConcurrentHashMap<>();
          instance         = new NumRelCache();
        }
      }
    }
    return instance;
  }

  public void loadAll() {
     List<Case> cases = Case.findAll();

    for (Case c : cases) {
      Long caseId = c.getLongId();
      List<Pbill> pbills = c.getPbills();

      for (int i = 0; i < pbills.size(); i++) {
        String num1 = pbills.get(i).getOwnerNum();
        for (int j = i + 1; j < pbills.size(); j++) {
          String num2 = pbills.get(j).getOwnerNum();
          CommPair cp = new CommPair(caseId, num1, num2);
          long d = Pbill.peerNumsCommonDegree(caseId, num1, num2);

          inCommonsCache.put(cp, d);
        }
      }
    }
    log.info("Init in-commons cache ({})", inCommonsCache.size());


  }

  public Long getCommonsDegree(CommPair commPair) {
    Long d = inCommonsCache.get(commPair);
    if (d == null) {
      d = inCommonsCache.get(commPair.shadow());
    }
    return d;
  }

  public Long getCommonsDegree(Long caseId, String peerNumA, String peerNumB) {
    return getCommonsDegree(new CommPair(caseId, peerNumA, peerNumB));
  }

  public void refresh(Long caseId) {

  }

  private List<String> peerNums() {
    return null;
  }
}

