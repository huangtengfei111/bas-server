package app.util.license.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import de.schlichtherle.license.LicenseContentException;
import oshi.SystemInfo;

public class LockServerParam extends BusinessPlanParam {

  private static final String EXC_MAC_ADDRESSES_INVALID = "MAC addresses are invalid";

  private static final String EXC_BASEBOARD_INVALID = "Baseboard info is invalid";

  private static final String EXC_PROCESSOR_INVALID = "Processor info is invalid";

  private static final String EXC_SYSTEM_SN_INVALID = "System sn is invalid";

  private static final String EXC_NO_HDWI_DETECTED = "Can't get any hardware info";

  private static final long serialVersionUID = 6890820569595196511L;

  public static final String HW_SYSTEM_SN = "SYSTEM_SN";
  public static final String HW_IP_ADDRESS = "IP_ADDRESS";
  public static final String HW_MAC_ADDRESS = "MAC_ADDRESS";
  public static final String HW_PROCESSOR = "PROCESSOR";
  public static final String HW_BASEBOARD = "BASEBOARD";

  private Long systemId;

  private String systemSN;

  private List<String> allowedIPAddress;

  private List<String> allowedMacAddress;

  private String processorInfo;

  private String baseboardInfo;

  private String hostId;

  public LockServerParam() {
    super();
  }

  public LockServerParam(String version, String plan) {
    super(version, plan);
  }

  public void inspect() throws Exception {
    final SystemInfo si = new SystemInfo();
    final HardwareInfoGetter hardware = new HardwareInfoGetter(si);
    this.systemSN         = hardware.getSysemSN();
    this.allowedIPAddress = hardware.getIPv4();
    this.allowedMacAddress = hardware.getMacAddress();
    this.baseboardInfo     = hardware.getBaseboardInfo();
    this.processorInfo     = hardware.getProcessInfo();

    this.hostId            = calcHostId();
  }

  public String calcHostId() throws NoSuchAlgorithmException {
    StringBuffer hwdi = new StringBuffer();
    hwdi.append(this.systemSN);
    hwdi.append(this.baseboardInfo);
    hwdi.append(this.processorInfo);
    for (String mac : this.allowedMacAddress) {
      hwdi.append(mac);
    }
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(hwdi.toString().getBytes());
    byte[] digest = md.digest();
    return DatatypeConverter.printHexBinary(digest).toUpperCase();
  }

  public void validate() throws LicenseContentException {
    super.validate();

    SystemInfo si = new SystemInfo();
    HardwareInfoGetter hardware = new HardwareInfoGetter(si);

    final String systemSN = hardware.getSysemSN();
    final List<String> ipv4 = hardware.getIPv4();
    final List<String> macAddress = hardware.getMacAddress();
    final String processor = hardware.getProcessInfo();
    final String baseboard = hardware.getBaseboardInfo();

    if (systemSN == null && processor == null && baseboard == null && ipv4.size() == 0 && macAddress.size() == 0) {
      throw new LicenseContentException(EXC_NO_HDWI_DETECTED);
    }
    
    if (this.systemSN != null && !this.systemSN.equals(systemSN)) {
      throw new InvalidHostException(HW_SYSTEM_SN, EXC_SYSTEM_SN_INVALID);
    }

    if (this.processorInfo != null && !this.processorInfo.equals(processor)) {
      throw new InvalidHostException(HW_PROCESSOR, EXC_PROCESSOR_INVALID);
    }

    if (this.baseboardInfo != null && !this.baseboardInfo.equals(baseboard)) {
      throw new InvalidHostException(HW_BASEBOARD, EXC_BASEBOARD_INVALID);
    }   
    
    if (requiredLockIPAddress()) {
      List<String> _allowedIp = new ArrayList<>();
      _allowedIp.addAll(this.getAllowedIPAddress());
      _allowedIp.retainAll(ipv4);
      if (_allowedIp.size() == 0) {
        throw new InvalidHostException(HW_IP_ADDRESS, EXC_BASEBOARD_INVALID);
      }
    }

    List<String> _allowedMac = new ArrayList<>();
    _allowedMac.addAll(this.getAllowedMacAddress());
    _allowedMac.retainAll(macAddress);
    if (_allowedMac.size() == 0) {
      throw new InvalidHostException(HW_MAC_ADDRESS, EXC_MAC_ADDRESSES_INVALID);
    }

  }

  public boolean requiredLockIPAddress() {
    if (super.PRO.equalsIgnoreCase(getPlan()) || super.ENTERPRISE.equalsIgnoreCase(getPlan())) {
      return true;
    }
    return false;
  }

  public void setSystemId(Long systemId) {
    this.systemId = systemId;
  }

  public Long getSystemId() {
    return this.systemId;
  }

  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  public String getHostId() {
    return this.hostId;
  }

  public String getSystemSN() {
    return systemSN;
  }

  public void setSystemSN(String systemSN) {
    this.systemSN = systemSN;
  }

  public List<String> getAllowedIPAddress() {
    return allowedIPAddress;
  }

  public void setAllowedIPAddress(List<String> allowedIPAddress) {
    this.allowedIPAddress = allowedIPAddress;
  }

  public List<String> getAllowedMacAddress() {
    return allowedMacAddress;
  }

  public void setAllowedMacAddress(List<String> allowedMacAddress) {
    this.allowedMacAddress = allowedMacAddress;
  }

  public String getProcessorInfo() {
    return processorInfo;
  }

  public void setProcessorInfo(String processorInfo) {
    this.processorInfo = processorInfo;
  }

  public String getBaseboardInfo() {
    return baseboardInfo;
  }

  public void setBaseboardInfo(String baseboardInfo) {
    this.baseboardInfo = baseboardInfo;
  }

  
  @Override
  public String toString() {
  // @formatter:off
    return "LockServerParam{" + "systemSN=" + this.systemSN + 
             ",allowedIPAddress=" + this.allowedIPAddress + 
             ", allowedMacAddress=" + this.allowedMacAddress + 
             ", cpuSerial='" + this.processorInfo + '\'' + 
             ", mainBoardSerial='" + this.baseboardInfo + '\'' + '}';
  // @formatter:on
  }
  
  
}
