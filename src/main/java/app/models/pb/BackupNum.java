package app.models.pb;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.util.collections.SortedMapUtils;

public class BackupNum implements Comparable {
  private String masterNum;
  private String backupNum;
  private String cname;
  private Long count;
  private Long dayCount;
  private Date tempDay;
  private Long workTimeCount;
  private Long privateTimeCount;
  private List<PbillRecord> pbillRecords;

  public BackupNum() {
    this.count    = 1l;
    this.dayCount = 1l;
    this.workTimeCount = 0l;
    this.privateTimeCount = 0l;
  }

  public String getMasterNum() {
    return masterNum;
  }

  public void setMasterNum(String masterNum) {
    this.masterNum = masterNum;
  }

  public String getBackupNum() {
    return backupNum;
  }

  public void setBackupNum(String backupNum) {
    this.backupNum = backupNum;
  }

  public Long getCount() {
    return count;
  }

  public void incCount() {
    this.count++;
  }

  public void incDayCount() {
    this.dayCount++;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public Long getDayCount() {
    return dayCount;
  }

  public void setDayCount(Long dayCount) {
    this.dayCount = dayCount;
  }

  public Long getWorkTimeCount() {
    return workTimeCount;
  }

  public void setWorkTimeCount(Long workTimeCount) {
    this.workTimeCount = workTimeCount;
  }

  public Long getPrivateTimeCount() {
    return privateTimeCount;
  }

  public void setPrivateTimeCount(Long privateTimeCount) {
    this.privateTimeCount = privateTimeCount;
  }

  public List<PbillRecord> getPbillRecords() {
    return pbillRecords;
  }

  public void setPbillRecords(List<PbillRecord> pbillRecords) {
    this.pbillRecords = pbillRecords;
  }

  public void setTempDay(Date day) {
    this.tempDay = day;
  }

  public Date getTempDay() {
    return this.tempDay;
    
  }   
  public String getCname() {
    return cname;
  }

  public void setCname(String backupNumCname) {
    this.cname = backupNumCname;
  }

  public void setTimeCount(int timeClass) {
    if(timeClass == 0) {
      this.privateTimeCount++;
    }else {
      this.workTimeCount++;
    } 
  } 

  public static Map<String, BackupNum> generateSummary(
      List<PbillRecord> pbillRecords) {
    /**
     * <pre>
     * 1. 找出主号码以及伴随号码 
     * 2.
     * </pre>
     * 
     * 找出伴随号码backNum 做统计
     */

    String backNum = null;
    Map<String, BackupNum> map = new HashMap<>();

    for (PbillRecord pbillRecord : pbillRecords) {
      backNum = pbillRecord.getOwnerNumber();
      BackupNum summary = map.get(backNum);
      if (summary != null) {
        // 更新各种count
        summary.incCount();
        if (!summary.getTempDay().equals(pbillRecord.getStartedDay())) {
          summary.incDayCount();
          summary.setTempDay(pbillRecord.getStartedDay());
        }
        summary.setTimeCount(pbillRecord.getTimeClass());
      } else {
        BackupNum backupNum = new BackupNum();
        backupNum.setCname(pbillRecord.getOwnerCname());
        backupNum.setBackupNum(backNum);
        backupNum.setTempDay(pbillRecord.getStartedDay());
        backupNum.setTimeCount(pbillRecord.getTimeClass());
        map.put(backNum, backupNum);
      }
    }

    Map<String, BackupNum> sorted = SortedMapUtils.sortByValues(map);

    return sorted;
  }

  @Override
  public int compareTo(Object o) {
    if (o == null)
      return -1;
    if (o instanceof BackupNum) {
      BackupNum bNum = (BackupNum) o;
      if (bNum.getDayCount() == null && this.getDayCount() == null) {
        return 0;
      }
      if (this.getDayCount() != null) {
        return -(this.getDayCount().compareTo(bNum.getDayCount()));
      } else {
        return -(bNum.getDayCount().compareTo(this.getDayCount()));
      }
    }
    return -1;
  }

}
