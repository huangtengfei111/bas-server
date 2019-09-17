package app.controllers.admin;

import java.util.List;

import org.javalite.activeweb.annotations.POST;

import app.controllers.APIController;
import app.exceptions.ErrorCodes;
import app.jobs.SyncCellTowerJob;
import app.models.CaseJob;
import app.util.task.TaskEngine;

public class CellTowersController extends APIController {

  private TaskEngine taskEngine = TaskEngine.getInstance();
  /**
   * 同步未结案的案件基站数据
   */
  @POST
  public void syncPbillCTs() {
    
    String sql = "SELECT * FROM case_jobs WHERE case_id = ? AND jtype = ? AND ended_at IS NULL";
    List<CaseJob> runningJobs = CaseJob.findBySQL(sql, 0L, CaseJob.JTYPE_SYNC_CELLTOWE);
    
    if (runningJobs.size() == 0) {
      SyncCellTowerJob task = new SyncCellTowerJob(0L);
      Long id = taskEngine.addTaskAndRun(task);
      setOkView();
      view("page_total", 1, "page_current", 1, "result", id);
      render("/background_task/index");
    } else {
      setErrorView("The task is running", ErrorCodes.JOB_ERROR);
      render("/common/_blank");
    }

  }
}
