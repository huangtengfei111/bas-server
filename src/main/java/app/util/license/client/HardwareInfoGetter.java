package app.util.license.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

public class HardwareInfoGetter {

  Logger log = LoggerFactory.getLogger(HardwareInfoGetter.class);

//  private final SystemInfo systemInfo;
  private final HardwareAbstractionLayer hal;

  public HardwareInfoGetter(SystemInfo si) {
//    this.systemInfo = si;
    this.hal = si.getHardware();
  }

  /**
   * 
   * @param computerSystem
   * @return
   */
  public String getSysemSN() {
    final ComputerSystem computerSystem = this.hal.getComputerSystem();
    return computerSystem.getModel() + "/" + computerSystem.getSerialNumber();
  }

  public List<String> getIPv4() {
    final NetworkIF[] networkIFs = this.hal.getNetworkIFs();
    List<String> ipv4List = new ArrayList<>();
    for (NetworkIF net : networkIFs) {
      ipv4List.addAll(Arrays.asList(net.getIPv4addr()));
    }
    return ipv4List;
  }

  public List<String> getMacAddress() {
    final NetworkIF[] networkIFs = this.hal.getNetworkIFs();
    List<String> macList = new ArrayList<>();
    for (NetworkIF net : networkIFs) {
      macList.add(net.getMacaddr());
    }
    return macList;
  }

  public String getProcessInfo() {
    final CentralProcessor processor = this.hal.getProcessor();

    return processor.getIdentifier() + "/" + processor.getProcessorID();
  }

  public String getBaseboardInfo() {
    final ComputerSystem computerSystem = this.hal.getComputerSystem();
    final Baseboard baseboard = computerSystem.getBaseboard();
    return baseboard.getManufacturer() + "/" + baseboard.getVersion() + "/" + baseboard.getSerialNumber();
  }

  public static void main(String[] args) {
    SystemInfo si = new SystemInfo();
    HardwareInfoGetter hiGetter = new HardwareInfoGetter(si);

    System.out.println("System SN: " + hiGetter.getSysemSN());
    System.out.println("Baseboard Info: " + hiGetter.getBaseboardInfo());
    System.out.println("Process Info: " + hiGetter.getProcessInfo());
    System.out.println("IPv4: " + hiGetter.getIPv4());
    System.out.println("Mac: " + hiGetter.getMacAddress());
  }
}
