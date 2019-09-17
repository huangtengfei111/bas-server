package app.util.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

  private AtomicInteger count = new AtomicInteger(0);

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r);
    String threadName = TaskEngineThreadPool.class.getSimpleName() + count.addAndGet(1);
    t.setName(threadName);
    return t;
  }
}
