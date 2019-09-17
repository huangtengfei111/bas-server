package app.services.pb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Rectangle;

import app.models.pb.BackupNum;
import app.models.pb.PbillRecord;
import app.models.search.Options;
import app.util.collections.SortedMapUtils;
import io.vavr.Tuple;
import io.vavr.Tuple2;

public class BackupNumsAnalyzerImpl implements BackupNumsAnalyzer, CellTowerAwareService, PbillStatCapable {
  /**
   * PbillRecord list should order by started_at asc
   * 
   * @param interval
   * @param locRule
   * @param pbillRecords
   * @return
   */
  @Override
  public Map<String,Collection<PbillRecord>> doAnalyze(Options options,
      List<PbillRecord> pbillRecords) {
    Map<String,Collection<PbillRecord>> m = new HashMap<>();
    String locRule = options.getAdhocParam("loc_rule").toString();
    int interval = Integer.parseInt(options.getAdhocParam("interval").toString());
    Double radius = 0.0D;
    if (options.getAdhocParam("radius") != null) {
      radius = Double.parseDouble(options.getAdhocParam("radius").toString());
    }
    radius = radius / 1000.0D;

    TreeMap<Tuple2<String, Timestamp>, PbillRecord> map = new TreeMap<>();
    TreeMap<Tuple2<String, Timestamp>, PbillRecord> backupNumsMap =
        new TreeMap<>();
    for (PbillRecord pbRec : pbillRecords) {
      Tuple2<String, Timestamp> key = Tuple.of(pbRec.getOwnerNumber(), pbRec.getStartedAt());
      map.put(key, pbRec);
      if (pbRec.getOwnerCi() > 0 && pbRec.getOwnerLac() > 0) {
        List<PbillRecord> backups = null;
        if (SAME_LAC.equals(locRule)) {
          backups = pbillRecordsOnSameLAC(pbRec, interval);
        } else if (SAME_CI.equals(locRule)) {
          backups = pbillRecordsOnSameCI(pbRec, interval);
        } else if (SCOPE_CT.equals(locRule)) {
          backups = pbillRecordsNearby(pbRec, interval, radius);
        }
        if (backups != null) {
          for (PbillRecord pr : backups) {
            Tuple2<String, Timestamp> bpKey = Tuple.of(pr.getOwnerNumber(), pr.getStartedAt());
            pr.setHighlight(true);
            map.put(bpKey, pr);
            backupNumsMap.put(bpKey, pr);
          }
        }
      }
    }
    Map<Tuple2<String, Timestamp>, PbillRecord> sorted = SortedMapUtils.sortByValues(map);
    m.put("backupNums", backupNumsMap.values());
    m.put("pbillRecords", sorted.values());
    return m;
  }


  @Override
  public Map summary(List<PbillRecord> pbillRecords) {
    return BackupNum.generateSummary(pbillRecords);
  }

  /**
   * <pre>
   * 
   *    |-----|o---------------o|-----|
   *  t1-N   t1 本方号码通话时间段  t2   t2+N
   *  
   *  满足是该本方号码的伴随号码的条件是 通话开始时间在t2与t2+N之间 或者 通话结束时间在t1-N与t1之间
   *  
   * </pre>
   * @param ownerPbRec
   * @param interval
   * @param lacOrCi
   * @return
   */
  private List<PbillRecord> pbillRecordsOnSameLAC(PbillRecord ownerPbRec,
      int interval) {
    List<PbillRecord> result = new ArrayList<>();

    Timestamp oprStart = ownerPbRec.getStartedAt();
    Timestamp oprEnd = ownerPbRec.getEndedAt();

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(oprStart.getTime());
    calendar.add(Calendar.MINUTE, -1 * interval);
    Timestamp tsStartedAt = new Timestamp(calendar.getTimeInMillis());

    calendar.setTimeInMillis(oprEnd.getTime());
    calendar.add(Calendar.MINUTE, interval);
    Timestamp tsEndedAt = new Timestamp(calendar.getTimeInMillis());

    //@formatter:off
    result = PbillRecord
        .where("owner_lac = ? AND owner_num != ? AND owner_lac > 0 AND owner_ci > 0 " +
               "AND ((started_at BETWEEN ? and ?) OR (ended_at BETWEEN ? and ?)) ORDER BY started_at", 
               ownerPbRec.getOwnerLac(), ownerPbRec.getOwnerNumber(), tsStartedAt, oprStart, oprEnd, tsEndedAt);
    //@formatter:on
    return result;
  }

  /**
   * <pre>
   * 
   *    |-----|o---------------o|-----|
   *  t1-N   t1 本方号码通话时间段  t2   t2+N
   *  
   *  满足是该本方号码的伴随号码的条件是 通话开始时间在t2与t2+N之间 或者 通话结束时间在t1-N与t1之间
   * 
   * </pre>
   * 
   * @param ownerPbRec
   * @param interval
   * @param lacOrCi
   * @return
   */
  private List<PbillRecord> pbillRecordsOnSameCI(PbillRecord ownerPbRec,
      int interval) {

    Timestamp oprStart = ownerPbRec.getStartedAt();
    Timestamp oprEnd = ownerPbRec.getEndedAt();

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(oprStart.getTime());
    calendar.add(Calendar.MINUTE, -1 * interval);
    Timestamp tsStartedAt = new Timestamp(calendar.getTimeInMillis());

    calendar.setTimeInMillis(oprEnd.getTime());
    calendar.add(Calendar.MINUTE, interval);
    Timestamp tsEndedAt = new Timestamp(calendar.getTimeInMillis());
    
    //@formatter:off
    List<PbillRecord> result = PbillRecord
        .where("owner_ci = ? AND owner_num != ? AND owner_lac > 0 AND owner_ci > 0 " +
               "AND ((started_at BETWEEN ? and ?) OR (ended_at BETWEEN ? and ?)) ORDER BY started_at", 
               ownerPbRec.getOwnerCi(), ownerPbRec.getOwnerNumber(), oprEnd, tsEndedAt, tsStartedAt, oprStart);
    //@formatter:on
    return result;
  }

  private List<PbillRecord> pbillRecordsNearby(PbillRecord ownerPbillRecord,
      int interval, Double radius) {
    List<PbillRecord> result = new ArrayList<>();
    Timestamp oprStart = ownerPbillRecord.getStartedAt();
    Timestamp oprEnd = ownerPbillRecord.getEndedAt();

    // 计算出间隔一定时间后的开始时间
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(oprEnd.getTime());
    calendar.add(Calendar.MINUTE, interval);
    Timestamp tsEndedAt = new Timestamp(calendar.getTimeInMillis());
    // 计算出间隔一定时间后的结束时间
    calendar.setTimeInMillis(oprStart.getTime());
    calendar.add(Calendar.MINUTE, -1 * interval);
    Timestamp tsStartedAt = new Timestamp(calendar.getTimeInMillis());

    Double lat = ownerPbillRecord.getOwnerCtLat();
    Double lng = ownerPbillRecord.getOwnerCtLng();
    if (lat == null || lng == null) {
      return null;
    }
    SpatialContext geo = SpatialContext.GEO;
    Rectangle rect = geo.getDistCalc().calcBoxByDistFromPt(
        geo.makePoint(lng, lat), radius * DistanceUtils.KM_TO_DEG, geo, null);
    //@formatter:off
    String sql = " owner_num != ? AND ((started_at BETWEEN ? and ?) OR (ended_at BETWEEN ? and ?)) " +
                   "AND (owner_ct_lat BETWEEN ? AND ?) AND (owner_ct_lng BETWEEN ? AND ?) ORDER BY started_at";
    //@formatter:on

    result = PbillRecord.where(sql, ownerPbillRecord.getOwnerNumber(), oprEnd,
        tsEndedAt, tsStartedAt, oprStart,
        rect.getMinY(), rect.getMaxY(), rect.getMinX(), rect.getMaxX());

    return result;
  }

}