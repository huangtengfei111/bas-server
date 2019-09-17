package app.jobs;

import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.models.CaseJob;
import app.models.pb.CellTower;
import app.models.pb.PbillRecord;
import app.services.pb.CellTowerLocalCache;
import app.util.collections.ListMap;
import app.util.task.Task;

public class ConnectCellTowerJob extends Task {
  private Logger log = LoggerFactory.getLogger(ConnectCellTowerJob.class);
  private CellTowerLocalCache ctLocalCache = CellTowerLocalCache.getInstance();

  public ConnectCellTowerJob(Long caseId) {
    super(caseId);
  };

  @Override
  public String getJobType() {
    return CaseJob.JTYPE_CONNECT_CELLTOWER;
  }

  /**
   * <pre>
   * 1.查找与基站关联的话单总数,并将总数设进任务总数中 
   * 2.查找话单中没有重复的话单总数,并存入List<Map>集合中
   * 3.判断集合是否大于0,若大于0 则遍历集合,并以每一个遍历出的mnc,lac,ci去查询基站cell_tower 获得基站ID
   * 并将id更新到话单的owner_ct_id中,若小于0,直接将进度设为total(100%).
   * </pre>
   */
  @Override
  public void doRun() throws Exception {

    // 查出需要关联基站的话单总数,存进lm集合
    //@formatter:off
    String countSql = "SELECT count(1) AS total " + 
                      "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                      "WHERE cp.case_id = ? AND pr.owner_ct_id IS NULL AND " +
                        "(pr.owner_lac IS NOT NULL AND pr.owner_lac > 0 AND pr.owner_ci IS NOT NULL AND pr.owner_ci > 0)";
    //@formatter:on
    List<Map> lm = Base.findAll(countSql, this.getCaseId());
    Long total = ListMap.getCounter(lm, "total");
    setTotal(total);// 将总数设进任务总数中

    log.info("{} pbill records to be connected", total);

    if (total == 0)
      return;

    // 查出所有owner_ct_id 为空的所有的 owner_mnc,owner_lac,owner_ci 并以这三个字段来分组得出数据总和,存进toConnectList集合
    //@formatter:off
    String findCT2ConnSql = "SELECT owner_mnc AS mnc, owner_lac AS lac, owner_ci AS ci, count(1) AS count " +
                            "FROM pbill_records AS pr LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                            "WHERE owner_ct_id IS NULL AND cp.case_id = ? AND " + 
                               "(pr.owner_lac IS NOT NULL AND pr.owner_lac > 0 AND pr.owner_ci IS NOT NULL AND pr.owner_ci > 0) " + 
                            "GROUP BY pr.owner_mnc,  pr.owner_lac, pr.owner_ci";
    //@formatter:on

    List<Map> toConnectList = Base.findAll(findCT2ConnSql, this.getCaseId());
    if (toConnectList.size() == 0) {//判断toConnectList是否为空
      setProcessed(total); // toConnectList为空,直接将进度设为total(100%)
    } else {
      int trxSize = 2000, i = 0;
      Base.connection().setAutoCommit(false);

      for (Map map : toConnectList) {// toConnectList不为空,遍历toConnectList
        if(i == 0) {
          Base.openTransaction();
        }
        Long lac = Long.parseLong(map.get("lac").toString());
        Long mnc = Long.parseLong(map.get("mnc").toString());
        Long ci = Long.parseLong(map.get("ci").toString());
        Long count = Long.parseLong(map.get("count").toString());

        // 从cache中取出
        CellTower cellTower = ctLocalCache.get(mnc, lac, ci);

        log.debug("processing ct(count:{}) => {}", count, cellTower);

        // @formatter:off
        String updateSql = "owner_ct_id = ?, owner_ct_lat = ?, owner_ct_lng = ?, " + 
                           "owner_ct_city = ?, owner_ct_dist = ?, owner_ct_town = ?"; 
        String whereSql = "owner_lac = ? AND owner_mnc = ? AND owner_ci = ? AND  owner_ct_id IS NULL";
        // @formatter:on

        if (cellTower != null) {// 判断基站信息是否为空,不为空则将基站信息设入对应话单
          int updated =
              PbillRecord.update(updateSql,
                                 whereSql,
                                 cellTower.getId(), cellTower.getLat(), cellTower.getLng(), cellTower.getCity(),
                                 cellTower.getDistrict(), cellTower.getTown(), lac, mnc, ci);
          log.debug("updated => {}", updated);
          setEffectedRows(getEffectedRows() + updated);
        }

        setProcessed(getProcessed() + count);// 将完成的任务,count设入进度中
        i = i + 1;
        if (i == trxSize) {
          Base.commitTransaction();
          i = 0; // reset i
        }
      }
      if (i > 0) {
        Base.commitTransaction(); // commit last batch
      }
      Base.connection().setAutoCommit(true);
    }

    log.debug("processed is {},and total {}", getProcessed(), total);
  }
}