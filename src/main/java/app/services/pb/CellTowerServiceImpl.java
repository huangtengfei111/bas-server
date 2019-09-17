package app.services.pb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import app.models.Setting;
import app.models.pb.CellTower;
import app.util.ct.BasGeoLocQueryAdapter;
import app.util.ct.GeoLocQueryAdapter;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;
import app.util.license.client.LockServerParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;

public class CellTowerServiceImpl implements CellTowerService {

  private static CellTowerLocalCache cellTowerLocalCache = CellTowerLocalCache.getInstance();
  private static final Logger log =
      LoggerFactory.getLogger(CellTowerServiceImpl.class);
  
  @Override
  public CellTower smartLookup(String code, boolean localOnly) throws Exception {
    CellTower ct = cellTowerLocalCache.get(code);
    if (ct == null && !localOnly) {
      List<String> codes = new ArrayList<>();
      codes.add(code);
      Map<String, List> result = syncFromRemote(codes, "16");
      List hitted = result.get("hitted");
      if(hitted != null && hitted.size() > 0) {
        return (CellTower) hitted.get(0);
      } else {
        return null;
      }
    }
    return ct;
  }

  /**
   * 智能化查找基站信息
   * <ol>
   * <li>区分本地数据和请求远程数据</li>
   * <li>分批请求远程数据</li>
   * </ol>
   */
  @Override
  public Map<String, List> smartLookup(List<String> codes) throws Exception {
    if (codes == null || codes.size() == 0) {
      return null;
    }

    List<CellTower> cellTowers = new ArrayList<>();
    // 本地数据库不存在
    List<String> reqCtCodes = new ArrayList<String>();

    for (String ctCode : codes) {
      CellTower ct = cellTowerLocalCache.get(ctCode);
      if (ct != null) {
        cellTowers.add(ct);
      } else {
        reqCtCodes.add(ctCode);
      }
    }

    Map<String, List> results = syncFromRemote(reqCtCodes, "16");
    List<CellTower> remoteHittedCTs = results.get("hitted");
    List<String> remoteMissedCTs = results.get("missed");
    
    try {
      Base.openTransaction();

      for (CellTower cellTower : remoteHittedCTs) {
        if (cellTower.getMcc() == null) {
          log.debug("mcc {}, code {}", cellTower.getMcc(), cellTower.getCode());
        }
        cellTower.saveIt();
        cellTowers.add(cellTower);
      }
      Base.commitTransaction();
    } catch (Exception e) {
      Base.rollbackTransaction();
      log.error(e.getMessage(), e);
    }

    log.debug("Requests: {}, and hit: {}, missed: {}", codes.size(), cellTowers.size(), remoteHittedCTs.size());
    results.put("hitted", cellTowers);
    return results;
  }

  public Map<String, List> syncFromRemote(List<String> codes, String fmt) throws Exception {
    Set<String> s = Sets.newHashSet(codes);
    GeoLocQueryAdapter adapter = null;

    Map ret = new HashMap();
    Set<String> hits = new HashSet<>();
    List<CellTower> hitCTs = new ArrayList<>();

    if (Setting.isSuperNode()) {
      adapter = null; // new GpsspgGeoLocQueryAdapter();
    } else {
      LicenseClientParam param = BASLicenseManager.loadClientParam();
      if (param != null) {
        LicenseManager manager = new BASLicenseManager(param);
        LicenseContent licenseContent = manager.verify();
        if (licenseContent != null) {
          LockServerParam lsp = ((LockServerParam) licenseContent.getExtra());
          String endpoint = Setting.centerBasHostPort();
          String appId = lsp.getSystemId().toString();
          String appKey = lsp.getHostId();
          adapter = new BasGeoLocQueryAdapter(endpoint, appId, appKey);
        }
      }
    }
    if (adapter != null) {
      List<Map> lm = adapter.doQuery(codes, fmt, GeoLocQueryAdapter.GMAPS_COORD);

      for (Map m : lm) {
        hits.add(m.get("code").toString());
        CellTower ct = new CellTower();
        ct.fromMap(m);
        hitCTs.add(ct);
      }
    }

    ret.put("hitted", hitCTs);
    ret.put("missed", Lists.newArrayList(Sets.difference(s, hits)));
    return ret;
  }

}
