package app.models.pb;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

import app.util.collections.ListMap;
import io.vavr.Tuple4;

/**
 *
 */
public class PbillRecord extends Model implements Comparable<PbillRecord> {
  private String ownerCtAddr;
  private String peerCtAddr;
  private boolean highlight;

  public PbillRecord() {
  }

  public String getOwnerCtAddr() {
    return ownerCtAddr;
  }

  public void setOwnerCtAddr(String ownerCtAddr) {
    this.ownerCtAddr = ownerCtAddr;
  }

  public String getPeerCtAddr() {
    return peerCtAddr;
  }

  public void setPeerCtAddr(String peerCtAddr) {
    this.peerCtAddr = peerCtAddr;
  }

  // 本方号码
  public String getOwnerNumber() {
    return getString("owner_num");
  }

  // 对方号码
  public String getPeerNumber() {
    return getString("peer_num");
  }

  // 开始时间
  public Date getStartedDay() {
    return getDate("started_day");
  }

  // 开始通话时间
  public Timestamp getStartedAt() {
    return getTimestamp("started_at");
  }
  //结束通话时间
  public Timestamp getEndedAt() {
    return getTimestamp("ended_at");
  }

  public String getOwnerCtCode() {
    return getString("owner_ct_code");
  }

  public Long getOwnerLac() {
    return getLong("owner_lac");
  }

  public Long getOwnerCi() {
    return getLong("owner_ci");
  }

  public Long getOwnerMnc() {
    return getLong("owner_mnc");
  }

  public Double getOwnerCtLat() {
    BigDecimal d = getBigDecimal("owner_ct_lat");
    if(d != null) {
      return d.doubleValue();
    }
    return null;
  }

  public Double getOwnerCtLng() {
    BigDecimal d = getBigDecimal("owner_ct_lng");
    if(d != null) {
      return d.doubleValue();
    }
    return null;    
  }

  public CellTower getOwnerCellTower() {
    Integer ownerCTId = getInteger("owner_ct_id");
    if (ownerCTId > 0) {
      return CellTower.findById(ownerCTId);
    }
    return null;
  }

  public Long getOwnerCtId() {
    Long ownerCTId = getLong("owner_ct_id");
    if (ownerCTId != null) {
      return ownerCTId;
    }
    return null;
  }

  public Long getPeerCtId() {
    Long peerCTId = getLong("peer_ct_id");
    if (peerCTId != null) {
      return peerCTId;
    }
    return null;
  }

  public String getOwnerCommLoc() {
    return getString("owner_comm_loc");
  }

  public Date getAlyzDay() {
    return getDate("alyz_day");
  }
  
  public Integer getTimeClass() {
    return getInteger("time_class");
  }
  
  public String getOwnerCname() {
    return getString("owner_cname");
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
  }

  public boolean getHighlight() {
    return this.highlight;
  }

  public List<String> suggestionsONSameCIInCase(Long caseId, Long ctCI) {
    //@formatter:off
    String sql = "SELECT DISTINCT owner_ct_code FROM pbill_records as pr LEFT JOIN cases_pbills as cp " + 
                 "ON pr.pbill_id = cp.pbill_id " + 
                 "WHERE cp.case_id = ? AND pr.owner_lac > 0 AND pr.owner_ci = ?";
    //@formatter:on
    List<Map> lm = Base.findAll(sql, caseId, ctCI);
    return ListMap.valuesToList(lm);
  }

  public List<CellTower> suggestionsOnCityAndCI(String city, Long ctCI) {
    city = "%" + city + "%";
    return CellTower.where("ci = ? AND city LIKE ?", ctCI, city);
  }

  @Override
  public int compareTo(PbillRecord pbillRecord) {
    if (this.getStartedAt() == null && pbillRecord.getStartedAt() == null) {
      return 0;
    }
    if (this.getStartedAt() != null && pbillRecord.getStartedAt() != null) {
      long ms1 = this.getStartedAt().getTime();
      long ms2 = pbillRecord.getStartedAt().getTime();
      return (ms1 - ms2) >= 0 ? 1 : -1;
    } else if (this.getStartedAt() != null && pbillRecord.getStartedAt() == null) {
      return 1;
    } else {
      return -1;
    }
  }
  
  public static long countByBreakpoint(Tuple4<Long, String, Date, Date> key) {
    long caseId = key._1;
    String ownerNum = key._2;
    Date start = key._3;
    Date end = key._4;
    
    String sql = "SELECT COUNT(1) as count " +
                 "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " +
                 "WHERE cp.case_id = ? AND pr.owner_num = ? AND pr.started_at BETWEEN ? AND ?";
    List<Map> count = Base.findAll(sql, caseId, ownerNum, start, end);
    
    return Long.parseLong(count.get(0).get("count").toString());
  }
}
