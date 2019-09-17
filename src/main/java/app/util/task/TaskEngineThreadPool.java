package app.util.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskEngineThreadPool extends ThreadPoolExecutor {

  public TaskEngineThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
      BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  public void destory() {
    this.shutdownNow();
  }

  public Future<Integer> exec(Task task) {
//    task.setExecutedAt(System.currentTimeMillis());
    Future<Integer> future = submit(task);
    System.out.println("成功添加任务并执行");
    return future;
  }
}
