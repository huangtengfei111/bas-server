/**
 * 
 */
package app.jobs;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.models.CaseJob;
import app.models.pb.OutlierNum;
import app.models.pb.Pbill;
import app.util.task.Task;

public class FindOutlierNumsJob extends Task {

  private Logger log = LoggerFactory.getLogger(FindOutlierNumsJob.class);

  public FindOutlierNumsJob(Long caseId) {
    super(caseId);
  }

  @Override
  public String getJobType() {
    return CaseJob.JTYPE_FIND_OUTLIER_NUM;
  }

  public void doRun() throws Exception {
    
    /*
     * 四种异常号码类型:
     * 1.某个话单联系的对方号码个数少于50人，
     * 2.连续不通话日期超过3天,
     * 3.总未使用天数超过10天,
     * 4.平均每天通话次数少于等于3次。
     */

    // 2.1获得所有通话号码
    //@formatter:off
    String allOwnerNumSql = "SELECT DISTINCT pr.owner_num FROM pbill_records pr " +
                            "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                            "WHERE  cp.case_id = ?";
    //@formatter:on
    List<Map> allOwnerNum = Base.findAll(allOwnerNumSql, getCaseId());
    List<String> allOwnerNumList =
        listMapToListString("owner_num", allOwnerNum);
    // 1.某个话单联系的对方号码个数少于50人
    //@formatter:off
    String pRecordLessSql = "SELECT pr.owner_num FROM pbill_records pr " +
                            "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                            "WHERE cp.case_id = ? GROUP BY pr.owner_num HAVING COUNT(distinct pr.peer_num) < 50";
    //@formatter:on

    List<Map> pRecordLess = Base.findAll(pRecordLessSql, getCaseId());
    List<String> pRecordLessList = listMapToListString("owner_num", pRecordLess);// 使用此方法将owner_num依次取出,存入list集合
    // 3.总未使用天数超过10天；未使用天数的总数
    List<String> vals0 = new ArrayList<>();
    vals0.add(getCaseId().toString());
    List<String> ph = new ArrayList<>();
    for (String ownerNum : allOwnerNumList) {
      ph.add("?");
      vals0.add(ownerNum);
    }
    String ownerConds = "pr.owner_num IN (" + String.join(",", ph) + ") ";
    //@formatter:off
    String unusedDaysSql = "SELECT pr.owner_num FROM pbill_records pr " +
                           "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                           "WHERE cp.case_id = ? AND " + ownerConds +
                           "GROUP BY pr.owner_num " +
                           "HAVING (datediff(MAX(pr.ended_at), MIN(pr.started_day)) - COUNT(DISTINCT pr.started_day)) > 10"; 
    //@formatter:on

    List<Map> unusedDays = Base.findAll(unusedDaysSql, vals0.stream().toArray(Object[]::new));
    List<String> unusedDaysList = listMapToListString("owner_num", unusedDays);

    // 4.平均每天通话少于3次
    //@formatter:off
    String avgDailyCallSql = "SELECT pr.owner_num FROM pbill_records pr " +
                             "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                             "WHERE cp.case_id = ? GROUP BY pr.owner_num " +
                             "HAVING COUNT(1) / datediff(MAX(pr.ended_at), MIN(pr.started_day)) < 3";
    //@formatter:on
    List<Map> avgDailyCall = Base.findAll(avgDailyCallSql, getCaseId());
    List<String> avgDailyCallList = listMapToListString("owner_num", avgDailyCall);
    // 2.连续不通话日期超过3天，

    // 2.2根据本方号码获得通话时间
    //@formatter:off
    String startedDaySql = "SELECT pr.started_day FROM pbill_records pr " +
                           "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                           "WHERE cp.case_id = ? AND pr.owner_num = ? GROUP BY pr.started_day ORDER BY pr.started_day";
    //@formatter:on

    List<String> callDaysList = new ArrayList<>();
    for (String num : allOwnerNumList) {
      List<Map> item = Base.findAll(startedDaySql, getCaseId(), num);// 根据本方号码获得通话时间
      List<Date> startedDayList = listMapToListDate("started_day", item);// 使用此方法将started_time依次取出,存入list集合
      log.debug("startedDayList'size is {}", startedDayList.size());
      for (int i = 0; i < startedDayList.size(); i++) {
        Date d1 = startedDayList.get(i);
        Date d2 = null;
        if (i + 1 < startedDayList.size()) {// 判断是否是最后一个元素
          d2 = startedDayList.get(i + 1);// 若不是,获取下一元素的值,赋予d2
          long daysBetween = (d2.getTime() - d1.getTime()) / (60 * 60 * 24 * 1000);//获取两时间相隔天数
          log.debug("daysBetween is {}", daysBetween);
        if (daysBetween > 3) {
          callDaysList.add(num);// 若相隔天数大于3天,将本方号码存进callDaysList集合中
        }
        }

      }
    }
    int total = pRecordLessList.size() + callDaysList.size() +
                unusedDaysList.size() + avgDailyCallList.size();
    setTotal(total);// 将四种异常类型的任务集合设入任务总数中
    //@formatter:off
    String pbillidSql = "SELECT id FROM pbill where owner_num = ?";//查出需要存入数据库的pbill_id
    String sql = "INSERT IGNORE INTO outlier_nums SET  pbill_id = ? , num = ?, flaw_type =? ,created_at = now()";
    //@formatter:on

    // 开始回写数据
    try {
      Base.openTransaction();
      // 清空outlier_nums表中的数据
      //@formatter:off
      String deleteSql = "DELETE otn FROM outlier_nums AS  otn " +
                         "LEFT JOIN cases_pbills AS  cp ON otn.pbill_id = cp.pbill_id " +
                         "WHERE cp.case_id = ?";
      //@formatter:on
      Base.exec(deleteSql, getCaseId());
      PreparedStatement ps = Base.startBatch(sql);
      for (String num : pRecordLessList) {
        Pbill pbill = Pbill.findOrCreate(num);
        Base.addBatch(ps, pbill.getId(), num,
            OutlierNum.LESS_PEER_NUM_FLAW_TYPE);// 将话单联系的对方号码个数少于50人的本方号码,存进数据库
        setProcessed(getProcessed() + 1);
      }

      for (String num : callDaysList) {
        Pbill pbill = Pbill.findOrCreate(num);
        Base.addBatch(ps, pbill.getId(), num,
            OutlierNum.CONTINUOUS_NO_CALL_FLAW_TYPE);// 将连续不通话日期超过3天的本方号码,存进数据库
        setProcessed(getProcessed() + 1);
      }

      for (String num : unusedDaysList) {
        Pbill pbill = Pbill.findOrCreate(num);
        Base.addBatch(ps, pbill.getId(), num,
            OutlierNum.TOTAL_UNUSEED_DAYS_FLAW_TYPE);// 将总未使用天数超过10天的本方号码,存进数据库
        setProcessed(getProcessed() + 1);
      }

      for (String num : avgDailyCallList) {
        Pbill pbill = Pbill.findOrCreate(num);
        Base.addBatch(ps, pbill.getId(), num,
            OutlierNum.AVERAGE_DAILY_CALLS_FLAW_TYPE);// 将平均每天通话次数少于等于3次的本方号码,存进数据库
        setProcessed(getProcessed() + 1);
      }

      Base.executeBatch(ps);
      Base.commitTransaction();

    } catch (Exception e) {
      e.printStackTrace();
      Base.rollbackTransaction();
    }
    log.debug(
        "pRecordLessList'size is {},and callDaysList'size is {},and unusedDaysList'size is {},and avgDailyCallList'size is {}",
        pRecordLessList.size(), callDaysList.size(), unusedDaysList.size(),
        avgDailyCallList.size());
    log.debug("{} total", total);
    log.debug("processed : {}", getProcessed());

  }
  private List<String> listMapToListString(String field, List<Map> source) {
    List<String> result = new ArrayList<>();
    for (Map value : source) {
      result.add((String) value.get(field));// 根据field取得对应的map值
    }
    return result;
  }

  private List<Date> listMapToListDate(String field, List<Map> source) {
    List<Date> result = new ArrayList<>();
    for (Map value : source) {
      result.add((Date) value.get(field));
    }
    return result;
  }
}
