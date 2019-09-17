package app.jobs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import app.models.CaseJob;
import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import app.util.collections.ListMap;
import app.util.task.Task;

public class ConnectCitizenJob extends Task{
  private static Logger log = LoggerFactory.getLogger(ConnectCellTowerJob.class);

  public ConnectCitizenJob(Long caseId) {
    super(caseId);
  };

  @Override
  public void doRun() throws Exception {
    Long caseId = getCaseId();
    // SQL
    //@formatter:off
    String getAllCPNumsSql = "SELECT c.id AS id , cp.num AS num, c.name AS name, c.category AS category " + 
                             "FROM citizen_phones as cp " +
                             "LEFT JOIN citizens as c ON cp.citizen_id = c.id " +
                             "WHERE LENGTH(cp.num) > 6";
    String getOwnerNumsSql = "SELECT DISTINCT pr.owner_num AS owner_num FROM pbill_records AS pr " +
                             "LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                             "WHERE cp.case_id = ? AND (owner_citizen_id is NULL OR peer_citizen_id is NULL) "; 
    String getPeerNumsSql = "SELECT DISTINCT pr.peer_num AS peer_num FROM pbill_records AS pr " +
                            "LEFT JOIN cases_pbills AS cp ON pr.pbill_id = cp.pbill_id " +
                            "WHERE cp.case_id = ? AND (owner_citizen_id is NULL OR peer_citizen_id is NULL) "; 
    //@formatter:on

    // 人员库号码集合
    List<Map> allCPNums = Base.findAll(getAllCPNumsSql);
    LinkedHashMap<Object, List<Map>> allCPNumMap =
        ListMap.reduce("num", allCPNums);
    
    List<Map> ownerNums = Base.findAll(getOwnerNumsSql, caseId);
    Set<String> ownerNumSet = ListMap.valuesToSet(ownerNums);
    List<Map> peerNums = Base.findAll(getPeerNumsSql, caseId);
    Set<String> peerNumSet = ListMap.valuesToSet(peerNums);
    Set<String> ownerAndPeerNums = Sets.union(ownerNumSet, peerNumSet);
    Set<Object> sub = Sets.intersection(allCPNumMap.keySet(), ownerAndPeerNums);

    setTotal(sub.size());
    

    // TODO:比对短号有问题,比对长号没问题,短号与虚拟网有关联
    for (Object ownerAndPeerNum : sub) {
      Map cp = allCPNumMap.get(ownerAndPeerNum).get(0);
      if (cp != null) {
        Long cpId = Long.parseLong(cp.get("id").toString());
        String cpName = cp.get("name").toString();
        String category = cp.get("category") == null ? null : cp.get("category").toString();
        cpName = (category == null ? "" : category + "-") + cpName;
        PbillRecord.update("owner_citizen_id = ?, owner_cname = ?",
            "owner_num = ?", cpId, cpName, ownerAndPeerNum);
        PbillRecord.update("peer_citizen_id = ?, peer_cname = ?",
            "peer_num = ?", cpId, cpName, ownerAndPeerNum);
        Pbill.update("owner_name = ?", "owner_num = ?", cpName,
            ownerAndPeerNum);
      }

      setProcessed(getProcessed() + 1);
    }
  }

  @Override
  public String getJobType() {
    return CaseJob.JTYPE_CONNECT_CITIZEN;
  }
  
}