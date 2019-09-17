package app.controllers;

import static org.javalite.common.Collections.map;

import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import com.google.inject.Inject;

import app.jobs.ConnectCellTowerJob;
import app.jobs.ConnectCitizenJob;
import app.jobs.FindOutlierNumsJob;
import app.jobs.MergePbillsJob;
import app.models.CaseJob;
import app.services.pb.CellTowerService;
import app.util.collections.ListMap;
import app.util.task.TaskEngine;
public class BackgroundTaskController extends APIController {
  private TaskEngine taskEngine = TaskEngine.getInstance();

  @Inject
  private CellTowerService cellTowerService;

  @POST
  public void connectCellTower() throws Exception {
    /**
     * task = new Task task.run -> taskid
     */
    Long caseId = Long.parseLong(param("case_id"));
    //@formatter:off
    String sql = "SELECT * FROM case_jobs WHERE case_id = ? AND jtype = ? AND ended_at IS NULL";
    //TODO 是否需要将缺失的基站排除掉,lac = 0的基站编码
    String pbillSql = "SELECT DISTINCT owner_ct_code FROM pbill_records pr " +
                      "LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                      "WHERE cp.case_id = ? AND owner_ct_code IS NOT NULL";
    //@formatter:on
    List<CaseJob> runningJobs = CaseJob.findBySQL(sql, caseId, CaseJob.JTYPE_CONNECT_CELLTOWER);
    if (runningJobs.size() == 0) {
      // 检查对应基站信息需要去同步
      // 1.查出与本地基站表中基站编码不同的话单基站编码
      List<Map> lm1 = Base.findAll(pbillSql, caseId);
      if (lm1.size() > 0) {
        List<String> pbillCellTowers = ListMap.valuesToList(lm1);
        logInfo("Total " + pbillCellTowers.size() + " cell towers code to be connected");
        cellTowerService.smartLookup(pbillCellTowers);
      }
      // 创建task 这是个task的子类
      ConnectCellTowerJob task = new ConnectCellTowerJob(caseId);
      // 添加运行返回 task id
      Long id = taskEngine.addTaskAndRun(task);
      setOkView();
      view("page_total", 1, "page_current", 1, "result", id);
      render("index");
    } else {
      setErrorView("The task is running", 400);
      render("/common/_blank");
    }

  }

  @POST
  public void findOutlierNums() {
    Long caseId = Long.parseLong(param("case_id"));
    String sql = "SELECT * FROM case_jobs WHERE case_id = ? AND jtype = ? AND ended_at IS NULL";
    List<CaseJob> runningJobs = CaseJob.findBySQL(sql, caseId, CaseJob.JTYPE_FIND_OUTLIER_NUM);
    if (runningJobs.size() == 0) {
      FindOutlierNumsJob task = new FindOutlierNumsJob(caseId);
      Long id = taskEngine.addTaskAndRun(task);

      setOkView();
      view("page_total", 1, "page_current", 1, "result", id);
      render("index");
    } else {
      setErrorView("The task is running", 400);
      render("/common/_blank");
    }
  }

  @POST
  public void connectCitizen() {
    Long caseId = Long.parseLong(param("case_id"));
    String sql = "SELECT * FROM case_jobs WHERE case_id = ? AND jtype = ? AND ended_at IS NULL";
    List<CaseJob> runningJobs = CaseJob.findBySQL(sql, caseId, CaseJob.JTYPE_CONNECT_CITIZEN);
    if (runningJobs.size() == 0) {
      ConnectCitizenJob task = new ConnectCitizenJob(caseId);
      Long id = taskEngine.addTaskAndRun(task);

      setOkView();
      view("page_total", 1, "page_current", 1, "result", id);
      render("index");
    } else {
      setErrorView("The task is running", 400);
      render("/common/_blank");
    }
  }


  @POST
  public void mergePbills() {
    Long caseId = Long.parseLong(param("case_id"));
    String sql = "SELECT * FROM case_jobs WHERE case_id = ? AND jtype = ? AND ended_at IS NULL";
    List<CaseJob> runningJobs = CaseJob.findBySQL(sql, caseId, CaseJob.JTYPE_MERGE_PBILLS);
    if (runningJobs.size() == 0) {
    MergePbillsJob task = new MergePbillsJob(caseId);
    Long id = taskEngine.addTaskAndRun(task);

    setOkView();
    view("page_total", 1, "page_current", 1, "result", id);
    render("index");
    } else {
      setErrorView("The task is running", 400);
      render("/common/_blank");
    }
  }

  // TODO 获取进度的方法 通过TaskEngine.getInstance().getProcessed(id) 获取
  @GET
  public void progress() {
    Long id = Long.parseLong(param("id"));
    Long processed = taskEngine.getProcessed(id);
    Long total = taskEngine.getTotal(id);
    int effectedRows = taskEngine.getEffectedRows(id);
    setOkView();
    view("page_total", 1, "page_current", 1, "result",
        map("processed", processed, "total", total, "effectedRows", effectedRows));
    render("progress");
  }

}