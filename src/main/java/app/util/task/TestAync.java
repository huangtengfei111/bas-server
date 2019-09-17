package app.util.task;

//TODO Task测试类
@Deprecated
public class TestAync {

  public static void main(String[] args) {
    // 单例模式 获得TaskEngine
    TaskEngine te = TaskEngine.getInstance();
    // 新建task类
    Task task = new Task() {
      @Override
      public Integer call() {
        try {

          Long i = 0l;
          while (i < 100) {
            Thread.sleep(1000);
            System.out.println("设置进度" + i);
            setProcessed(++i);
          }

        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return 0;
      }

      @Override
      public String getJobType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void doRun() throws Exception {
        // TODO Auto-generated method stub

      }

    };
//    执行task 并返回taskid
//    两种方式1.
//    Long id =te.addTask(task);
//    te.execute(id);
//    上面的方法并为对execute 并未对id进行判断,若传的是已经运行的id,会导致两个任务在跑
//    2.第二种方法添加直接运行
    Long id1 = te.addTask(task);
    te.execute(id1);
    Long id = te.addTaskAndRun(task);
    Task task2 = new Task() {
      @Override
      public Integer call() {
        try {

          int i = 0;
          while (i < 10000) {
            Thread.sleep(500);
            ++i;
            System.out.println("Task2:Task1 progress is " + te.getProcessed(id));
          }

        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return 0;
      }

      @Override
      public String getJobType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void doRun() throws Exception {
        // TODO Auto-generated method stub

      }
    };

    System.out.println("Remove task " + te.addTaskAndRun(task2));

    // -----------------------------------Mian
    // Print----------------------------------------------
    try {

      int i = 0;
      while (i < 100) {
        Thread.sleep(500);
        ++i;
        System.out.println("Mian:Task1 progress is " + te.getProcessed(id));
        if (i == 5)
          System.out.println("remove Task1 " + te.removeTask(id));

      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
}
