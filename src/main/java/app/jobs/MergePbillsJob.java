package app.jobs;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import app.models.CaseJob;
import app.util.collections.ListMap;
import app.util.task.Task;

/**
 * 1. merge_pr_id 填上 2. 对方号码的基站信息
 */
@SuppressWarnings("unchecked")
public class MergePbillsJob extends Task {
  private Logger log = LoggerFactory.getLogger(MergePbillsJob.class);

  public MergePbillsJob(Long caseId) {
    super(caseId);
  }

  @Override
  public String getJobType() {
    return CaseJob.JTYPE_MERGE_PBILLS;
  }

  /**
   * 1.获取本方号码话单,与对方号码话单列表,双方取交集. 2.将交集集合的size()设为任务总数.
   * 3.判断交集是否为空,若为空,直接将进度设为100%.
   * 
   * 4.若交集不为空,遍历交集集合,以交集中的号码作为本方号码,去数据库查出对应话单数据集合,且merged_pbr_id(话单合并id)为空.
   * 5.遍历话单数据集合,根据查出的pr.id和pr.started_at去查时差在10秒左右的对方号码话单集合.
   * 6.判断对方号码话单集合是否为空,若为空,continue.
   * 7.若不为空,继续判断集合size,若为1,直接进行话单合并//应该是数据库数据有问题,此步骤暂时取消.
   * 8.若不为1,判断对方号码话单集合中的本号号码是否与话单数据集合中的对方号码相等,若想等则进行合并,否则不合并.
   * 
   * 9.每遍历一次交集集合,任务进度+1.
   */
  @Override
  public void doRun() throws Exception {
    final int sensitiveInSec = 3;

    Set<String> peerList = getPeerNums();
    Set<String> ownerList = getOwnerNums();

    // 要关联的号码:从本方号码与对方号码取交集,得到双方共同拥有的号码
    Set<String> numsToProcessed = Sets.intersection(ownerList, peerList);
    log.info("{} nums to be processed", ownerList.size());

    // 将交集中的号码数量设为任务总数
    setTotal(numsToProcessed.size());

    // 判断是否有交集,有则继续,无则直接返回
    if (numsToProcessed.size() == 0) {
      setProcessed(getTotal());
      return;
    } else {
      //
      int batchSize = 5000;
      long lastPRId = 0;

      for (String num : numsToProcessed) {
        // 以batchSize为单位处理数据块
        while (true) {
          // 查对方号码(查出merged_pbr_id(合并id)为空的对方号码)
          //@formatter:off
          //TODO: 加上pr.started_at作为时间过滤来缩小结果集
          String findPeersSql = "SELECT pr.id AS pr_id, pr.started_at, pr.peer_num " +
                        "FROM pbill_records pr LEFT JOIN cases_pbills cp ON cp.pbill_id = pr.pbill_id " +
                        "WHERE cp.case_id = ? AND pr.owner_num = ? AND pr.merged_pbr_id IS NULL AND pr.id > ? " +
                        "ORDER BY pr.id ASC " +
                        "LIMIT ?";
          
          String findMergeRecSql = "SELECT pr.id " +
                        "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                        "WHERE cp.case_id = ? AND pr.owner_num = ? AND pr.peer_num = ? " + 
                            "AND pr.started_at BETWEEN DATE_ADD( ?, INTERVAL " + (- sensitiveInSec) + " SECOND) AND DATE_ADD( ? , INTERVAL " + sensitiveInSec + " SECOND) " + 
                        "ORDER BY pr.started_at LIMIT 1";        
          //@formatter:off
          List<Map> peers = Base.findAll(findPeersSql, getCaseId(), num, lastPRId, batchSize);
          
          if(peers.size() > 0) {
            // 跟要关联的号码有关的对方号码(时差在3s左右)
            for (Map map : peers) {//根据查出的pr.id和pr.started_at去查时差在10秒左右的对方号码id
              Long id = Long.parseLong(map.get("pr_id").toString());
              lastPRId = id;
              //log.debug("lastPRId = {}", lastPRId);
              Timestamp startedAt = (Timestamp) map.get("started_at");
              String peerNum = map.get("peer_num").toString();
    
              List<Map> peerPbillRecods =
                  Base.findAll(findMergeRecSql, getCaseId(), peerNum, num, startedAt, startedAt);
              if (peerPbillRecods.size() == 0) {
                continue;
              } else {
                Map pbRec = peerPbillRecods.get(0);
                Long id2 = Long.parseLong(pbRec.get("id").toString());
                mergePbillRecords(id, id2);
                setEffectedRows(getEffectedRows() + 2);
              }
            }
          } else { // end batch, and reset lastPRId
            lastPRId = 0; 
            break;
          }
          log.debug("peer size = {}, lastPRId = {}", peers.size(), lastPRId);
        }
        setProcessed(getProcessed() + 1);
        log.debug("Process is {}", getProcessed());
      }
    }
  }
  
  private void mergePbillRecords(Long prId1, Long prId2) {
    //将需要合并的话单的本方基站信息设入对方的对方基站信息中
    //@formtter:off
    String sql = "UPDATE pbill_records as pr, " + 
                     "(SELECT id, owner_ct_code, owner_lac, owner_ci, owner_mnc, owner_ct_id " +
                     "FROM pbill_records as pr2 WHERE pr2.id = ?) AS src " + 
                 "SET merged_pbr_id = src.id, peer_ct_code = src.owner_ct_code, " +
                     "peer_ci = src.owner_ci, peer_lac = src.owner_lac, " +
                     "peer_mnc = src.owner_mnc, peer_ct_id = src.owner_ct_id  " +
                 "WHERE pr.id = ? ";
    //@formatter:on
    Base.exec(sql, prId1, prId2);
    Base.exec(sql, prId2, prId1);
  }

  // 获得对方号码
  private Set<String> getPeerNums() {
    //@formatter:off
    String peerNumList = "SELECT DISTINCT pr.peer_num " +
                         "FROM pbill_records pr " +
                         "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                         "WHERE cp.case_id = ? AND pr.merged_pbr_id is NULL ";
    //@formatter:on
    return (Set<String>) ListMap.valuesToSet((Base.findAll(peerNumList, getCaseId())));
  }


  // 获得本方号码
  private Set<String> getOwnerNums() {
    //@formatter:off
    String ownerNumList = "SELECT DISTINCT pr.owner_num " +
                          "FROM pbill_records pr " +
                          "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                          "WHERE cp.case_id = ? AND pr.merged_pbr_id is NULL ";
    //@formatter:on
    return (Set<String>) ListMap.valuesToSet((Base.findAll(ownerNumList, getCaseId())));
  }
}

