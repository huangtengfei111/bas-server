package app.util.task;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.models.CaseJob;

// TODO 问题1. 异常未详细规定,没有返回前端数据.
public abstract class Task implements Callable<Integer> {
  private static final Logger log = LoggerFactory.getLogger(Task.class);
  private Long id;
  private Long processed = 0l;

  private Long total = 0l;
  // TODO 该任务开始执行时的时间,可用来判断超时取消
  private Long executedAt = 0L;

  private int effectedRows = 0;

  private DB db;
  private Long caseId;

  public Task() {

  }

  public Task(Long caseId) {
    this.caseId = caseId;
    this.id = System.currentTimeMillis();
  }

  public Long getExecutedAt() {
    return executedAt;
  }

  @Override
  public Integer call() throws Exception {
    beforeRun();
    try {
      doRun();
    } catch (InterruptedException e) {
      // 任务取消
      TaskEngine.getInstance().removeTask(this);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      afterRun();
    }
    return 0;
  }

  public void setExecutedAt(Long startTime) {
    this.executedAt = startTime;
  }

  public Long getProcessed() {
    return processed;
  }

  public void setProcessed(long progress) {
    this.processed = progress;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(long count) {
    this.total = count;
  }

  public Long getId() {
    return this.id;
  }

  public Long getCaseId() {
    return this.caseId;
  }

  public void beforeRun() {
    db = new DB();
    db.open();

    CaseJob cj = CaseJob.findFirst("case_id = ? AND jtype = ?", this.caseId, getJobType());
    if (cj == null) {
      cj = new CaseJob(this.caseId, this.getId(), getJobType(),
          new Date());
    } else {
      cj.set("jid", this.getId());
      cj.set("executed_at", new Date());
      cj.set("ended_at", null);
      setExecutedAt(System.currentTimeMillis());
    }
    cj.saveIt();
  }

  public void afterRun() {
    // update case job
    try {
      Base.connection().setAutoCommit(true);
      CaseJob.update("ended_at = ?", "jid = ?", new Date(System.currentTimeMillis()), this.getId());

      log.info("Task({}: {}) takes {} seconds in total", this.id, getJobType(), (System.currentTimeMillis() - getExecutedAt()) / 1000);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (db != null)
      db.close();
  }

  public abstract String getJobType();

  public abstract void doRun() throws Exception;

  public int getEffectedRows() {
    return effectedRows;
  }

  public void setEffectedRows(int realCount) {
    this.effectedRows = realCount;
  }
}
