package app.util.license.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NTPClientThread implements AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(NTPClientThread.class);

  final List<InetAddress> ntpPeers;
  NTPUDPClient ntpUdpClient;
  Thread pollThread = null;
  final long pollMs;
  private TimeInfo timeInfo;
  private long timeInfoSetLocalTime;

  private void pollNtpServer() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          Thread.sleep(this.pollMs);
          Collections.shuffle(ntpPeers);
          for (InetAddress ntpPeer : ntpPeers) {
            TimeInfo ti = ntpUdpClient.getTime(ntpPeer);
            // log.debug("{} ==> {}", ntpPeer, ti);
            long diff0 = ti.getMessage().getReceiveTimeStamp().getTime() - System.currentTimeMillis();
            // System.out.println("diff0 = " + diff0);
            this.setTimeInfo(ti);
          }
        } catch (SocketTimeoutException ste) {
          // log.error(ste.getMessage(), ste);
        }
      }
    } catch (InterruptedException ie) {
      log.error(ie.getMessage(), ie);
    } catch (IOException ex) {
      // log.error(ex.getMessage(), ex);
    }
  }

  public NTPClientThread(String[] hosts, int pollMs) throws UnknownHostException, SocketException {
    this(Arrays.asList(hosts), pollMs);
  }

  /**
   * Connect to host and poll the host every poll_ms milliseconds. 
   * Thread is started in the constructor.
   * @param host
   * @param poll_ms
   * @throws UnknownHostException
   * @throws SocketException
   */
  public NTPClientThread(List<String> hosts, int pollMs) throws UnknownHostException, SocketException {
    if (pollMs == 0 || pollMs > 6000000) {
      pollMs = 600000;
    }
    this.pollMs  = pollMs;
    this.ntpPeers = new ArrayList<>();
    for (String host : hosts) {
      ntpPeers.add(InetAddress.getByName(host));
    }
    ntpUdpClient = new NTPUDPClient();
    ntpUdpClient.setDefaultTimeout(10000);
    ntpUdpClient.open();
    ntpUdpClient.setSoTimeout(pollMs * 2 + 20);
    pollThread = new Thread(this::pollNtpServer, "pollNtpServer(" + String.join(",", hosts) + "," + pollMs + ")");
    pollThread.start();
  }

  /**
   * Get the value of timeInfo
   *
   * @return the value of timeInfo
   */
  public synchronized TimeInfo getTimeInfo() {
    return timeInfo;
  }

  private synchronized void setTimeInfo(TimeInfo timeInfo) {
    this.timeInfo        = timeInfo;
    timeInfoSetLocalTime = System.currentTimeMillis();
  }

  /**
   * Returns milliseconds just as System.currentTimeMillis() but using the latest
   * estimate from the remote time server.
   * 
   * @return the difference, measured in milliseconds, between the current time
   *         and midnight, January 1, 1970 UTC.
   */
  public long currentTimeMillis() {
    long diff = System.currentTimeMillis() - timeInfoSetLocalTime;
    System.out.println("diff = " + diff);
    return timeInfo.getMessage().getReceiveTimeStamp().getTime() + diff;
  }

  @Override
  public void close() throws Exception {
    if (null != pollThread) {
      pollThread.interrupt();
      pollThread.join(200);
      pollThread = null;
    }
    if (null != ntpUdpClient) {
      ntpUdpClient.close();
      ntpUdpClient = null;
    }
  }

  /**
   * Polls an NTP server printing the current Date as recieved from it an the
   * difference between that and System.currentTimeMillis()
   * 
   * @param args host name of ntp server in first element
   * @throws UnknownHostException
   * @throws SocketException
   * @throws InterruptedException
   * @throws IOException
   * @throws Exception
   */
  public static void main(String[] args)
      throws UnknownHostException, SocketException, InterruptedException, IOException, Exception {
    if (args.length < 1) {
      args = new String[] { "1.cn.pool.ntp.org", "0.cn.pool.ntp.org" };
    }

    try (NTPClientThread ntp = new NTPClientThread(args, 100)) {

      for (int i = 0; i < 10; i++) {
        Thread.sleep(1000);
        long t1 = System.currentTimeMillis();
        long t2 = ntp.currentTimeMillis();
        long t3 = System.currentTimeMillis();

        Date d = new Date(t2);
        System.out.println(d + " :  diff = " + (t3 - t2) + " ms");
      }
    }
  }
}
