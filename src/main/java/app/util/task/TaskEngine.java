package app.util.task;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

//TODO 问题1.超时任务取消没有实现.问题2. 没有使用完成后任务的删除 移除方法removeTask
// 问题3. 线程池没有调用关闭 
public class TaskEngine {
  private static TaskEngine intance = null;
  private TaskEngineThreadPool taskPool = null;

  private volatile HashMap<Long, Task> taskMap = new HashMap<>();

  private HashMap<Long, Future<Integer>> futureMap = new HashMap<>();

  // 核心线程数. 最大线程数 , 空闲线程最大值, 空闲线程存在时间,任务队列,新建线程工厂,RejectedExecutionHandler
  private TaskEngine() {
    taskPool = new TaskEngineThreadPool(30, 30, 30, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10),
        new CustomThreadFactory(), new CustomRejectedExecutionHandler());
  }

  public static synchronized TaskEngine getInstance() {
    if (intance == null) {
      intance = new TaskEngine();
    }
    return intance;
  }

  // 添加任务
  public Long addTask(Task task) {
    Long id = System.currentTimeMillis();
    taskMap.put(id, task);
    return id;
  }

  // 执行任务
  public boolean execute(Long id) {
    if (taskMap.keySet().contains(id)) {
      futureMap.put(id, taskPool.exec(taskMap.get(id)));
      return true;
    }
    return false;
  }

  // 添加并且运行
  public Long addTaskAndRun(Task task) {
    Long id = task.getId();
    taskMap.put(id, task);
    futureMap.put(id, taskPool.exec(task));
    return id;
  }

  // 移除任务
  public boolean removeTask(Long id) {
    if (taskMap.keySet().contains(id)) {
      boolean result = true;
      if (futureMap.get(id).isDone()) {
        result = futureMap.get(id).cancel(true);
      }
      taskMap.remove(id);
      return result;
    }
    return false;
  }

//移除任务
  public boolean removeTask(Task task) {
    Long taskid = 0l;
    for (Long id : taskMap.keySet()) {
      if (task.equals(taskMap.get(id))) {
        taskid = id;
        break;
      }
    }
    return removeTask(taskid);
  }
  
  // 关闭线程池
  public void destory() {
    taskPool.destory();
  }

  // 若没有此任务或任务已被移除则任务进度为-1
  public Long getProcessed(Long id) {
    if (taskMap.keySet().contains(id))
      return taskMap.get(id).getProcessed();
    return -1L;
  }

  // 若没有此任务或任务已被移除则任务进度为-1
  public Long getTotal(Long id) {
    if (taskMap.keySet().contains(id))
      return taskMap.get(id).getTotal();
    return -1L;
  }

  public int getEffectedRows(Long id) {
    if (taskMap.keySet().contains(id))
      return  taskMap.get(id).getEffectedRows();
    return -1;
  }

}
