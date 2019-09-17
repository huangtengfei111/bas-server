package app.util.license;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.models.License;
import app.util.X500PrincipalHelper;
import app.util.ZipMaker;
import app.util.license.client.LicenseCreatorParam;
import app.util.license.client.LockServerParam;
import app.util.license.client.VersionizedParam;

public class BASLicenseCreator extends LicenseCreator {
  private static Logger logger = LoggerFactory.getLogger(BASLicenseCreator.class);

  public BASLicenseCreator(X500Principal holder, LicenseCreatorParam param) {
    super(holder, param);
  }

  public static File generateLicense(String basedir, License license) {
    return generateLicense(basedir, license, true);
  }

  /**
   * 升级许可证
   * 
   * @param basedir
   * @param license
   * @return
   */
  public static File upgradeLicense(String basedir, License license) {
    return generateLicense(basedir, license, false);
  }

  /**
   * 用license对象中的数据生成license文件
   * 
   * @param license
   * @return
   */
  public static File generateLicense(String basedir, License license, boolean genCert) {
    String hostId = license.getHostId();
    String _holder = license.getHolder();
    X500Principal holder = new X500Principal(_holder);
    X500PrincipalHelper xhelper = new X500PrincipalHelper(holder);

    File base = new File(FilenameUtils.normalize(basedir + "/" + hostId));
    base.mkdirs();
    
    String publicCert = publicCertPath(basedir, hostId);
    String privateKeystorePath = privateKeystorePath(basedir, hostId);
    String publicKeystorePath = publicKeystorePath(basedir, hostId);
    String licensePath = licensePath(basedir, hostId);
    
    int validity = 10;
    String subject = xhelper.getCN();
    // 生成license
    LicenseCreatorParam param = new LicenseCreatorParam();
    param.setSubject(subject);
    param.setConsumerType("User");
    param.setConsumerAmount(1);

    if (genCert) {
      try {
        JavaKeyTool.generateKeyPair(_holder, validity, license.getPrivateAlias(), privateKeystorePath,
                                    license.getKeyPass(), license.getStorePass());
        JavaKeyTool.exportCert(license.getPrivateAlias(), privateKeystorePath, license.getStorePass(), publicCert);
        JavaKeyTool.importCert(license.getPublicAlias(), publicCert, publicKeystorePath, license.getStorePass());    
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }
    param.setKeyPassword(license.getKeyPass());
    param.setPrivateAlias(license.getPrivateAlias());
    param.setStorePassword(license.getStorePass());
    param.setPrivateKeysStorePath(privateKeystorePath);
    
    Date issuedAt = new Date();
    
    Date expiredAt = license.getExpiredAt();
    param.setIssuedAt(issuedAt);
    param.setExpiredAt(expiredAt);
    param.setLicensePath(licensePath);

    LockServerParam lockServerParam = new LockServerParam(VersionizedParam.V_1, license.getPlan());
    lockServerParam.setHostId(hostId);
    lockServerParam.setAcctLimit(license.getAcctLimit());
    lockServerParam.setSystemId(license.getLongId());

    lockServerParam.setSystemSN(license.getSystemSN());
    lockServerParam.setProcessorInfo(license.getProcessorInfo());
    lockServerParam.setBaseboardInfo(license.getBaseboardInfo());
    
    List<String> ipAddress = license.getIpAddress();
    List<String> macAddress = license.getMacAddress();

    lockServerParam.setAllowedIPAddress(ipAddress);
    lockServerParam.setAllowedMacAddress(macAddress);
    param.setLockServerParam(lockServerParam);

    String metaPath = licenseMetaPath(basedir, hostId);
    BASLicenseCreator basCreator = new BASLicenseCreator(holder, param);
    
    if (basCreator.generateLicense()) {
      logger.info("{} license is generated", license);

      saveMeta(license, metaPath);
      ArrayList<File> files = new ArrayList<File>();
      files.add(new File(publicKeystorePath));
      files.add(new File(licensePath));
      files.add(new File(metaPath));

      File dest = new File(licenseBinPath(basedir, hostId));
      ZipMaker.zipIt(files, dest);
      return dest;
    } else {
      removeKeys(basedir, license);
      return null;
    }
  }

  public static void removeKeys(String basedir, License license) {
    logger.debug("Removing keys...");
    String hostId = license.getHostId();
    String privateKeystorePath = privateKeystorePath(basedir, hostId);
    String publicKeystorePath = publicKeystorePath(basedir, hostId);

    JavaKeyTool.deleteCert(license.getPrivateAlias(), privateKeystorePath, license.getStorePass());
    JavaKeyTool.deleteCert(license.getPublicAlias(), publicKeystorePath, license.getStorePass());
    
    String removedPrivKeystorePath = privateKeystorePath + ".rm@" + System.currentTimeMillis();
    String removedPubKeystorePath = publicKeystorePath + ".rm@" + System.currentTimeMillis();
    try {
      FileUtils.moveFile(new File(privateKeystorePath), new File(removedPrivKeystorePath));
      FileUtils.moveFile(new File(publicKeystorePath), new File(removedPubKeystorePath));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private static void saveMeta(License license, String file) {
    try {
      X500Principal holder = new X500Principal(license.getHolder());
      X500PrincipalHelper xhelper = new X500PrincipalHelper(holder);
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      Properties p = new Properties();
      p.put("subject", xhelper.getCN());
      p.put("publicAlias", license.getPublicAlias());
      p.put("password", license.getStorePass());
      p.put("issuedAt", format.format(license.getIssuedAt()));
      p.put("expiredAt", format.format(license.getExpiredAt()));

      FileOutputStream fr = new FileOutputStream(file);
      p.store(fr, "LICENSE META DATA");
      fr.close();
    } catch (FileNotFoundException e) {
      logger.error(e.getMessage(), e);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private static String privateKeystorePath(String basedir, String hostId) {
    return FilenameUtils.normalize(basedir + "/" + hostId + "/privateKeys.keystore");
  }

  private static String publicCertPath(String basedir, String hostId) {
    return FilenameUtils.normalize(basedir + "/" + hostId + "/certfile.cer");
  }

  private static String publicKeystorePath(String basedir, String hostId) {
    return FilenameUtils.normalize(basedir + "/" + hostId + "/publicCerts.keystore");
  }

  private static String licensePath(String basedir, String hostId) {
    return FilenameUtils.normalize(basedir + "/" + hostId + "/license.lic");
  }

  private static String licenseMetaPath(String basedir, String hostId) {
    return FilenameUtils.normalize(basedir + "/" + hostId + "/META");
  }

  private static String licenseBinPath(String basedir, String hostId) {
    return FilenameUtils.normalize(basedir + "/" + hostId + "/" + hostId + "-license.bin");
  }

  /**
   * 从json文件读取数据生成license文件
   * <pre>
   * 1. 读取许可证生成参数
   * 2. 生成对应的key
   * 3. 生成license
   * 4. 打包对应的许可证资料
   * </pre>
   * @param licParamFile
   * @return
   * @throws Exception 
   */
  public File generateLicense(File licParamFile) throws Exception {

    JSONObject json = loadLicParamFile(licParamFile);
    //生成对应的key
    String keyPassword = "private_password1234";
    X500Principal holder = new X500Principal("");
   
    String subject = json.getString("holder");
    String publicAlias = json.getString("publicAlias");
    String pubStorePass = json.getString("password");
    
    String privateAlias = "privateKey";
    String privateKeyStore = "/tmp/privateKeys.keystore";
    String certFile = "/tmp/certfile.cer";   
    String pubKeyStore = "/tmp/publicCerts.keystore";
    String privatePass = "private_password1234";
      
//    JavaKeyTool.generateKeyPair(subject, 1, privateAlias, privateKeyStore, pubStorePass);   
//    JavaKeyTool.exportCert(privateAlias, privateKeyStore, pubStorePass, certFile);   
//    JavaKeyTool.importCert(publicAlias, certFile, pubKeyStore, pubStorePass);
    
    //生成license
    generateLicense(privateAlias, privatePass, json);
    
    //打包对应的许可证资料
    ArrayList<File> files = new ArrayList<File>();    
    files.add(new File("/tmp/publicCerts.keystore"));
    files.add(new File("/tmp/license.lic"));
    files.add(new File("/tmp/META"));
    
    File dest=new File("/tmp/serverlicense.zip");// 压缩文件
    ZipMaker.zipIt(files, dest);    
    
    return dest;
  }
  
  
  //将json文件读到JSONObject中
  private JSONObject loadLicParamFile(File licParamFile) throws IOException {
   
    String str = FileUtils.readFileToString(licParamFile, "utf-8");
    JSONObject json = JSON.parseObject(str);     
    return json;
  }

  public static void main(String[] args) throws Exception {
    // File licParamFile = new
    // File("/home/dev/WorkSpace/bas-server/src/test/resources/deploy.license.json");
    
    Properties p = new Properties();
    p.put("abc", "123");
    p.put("date", "2109-10-19");
    // saveMeta("/tmp/test");
//        loadLicParamFile(licParamFile);
//    generateLicense(licParamFile);
    
//    ArrayList<File> files = new ArrayList<File>();    
//    files.add(new File("/tmp/publicCerts.keystore"));
//    files.add(new File("/tmp/license.lic"));
//    files.add(new File("/tmp/META"));
//    
//    File dest=new File("/tmp/serverlicense.zip");// 压缩文件
//    ZipMaker.zipIt(files, dest);
//    ZipMaker.zipItWithPassword(files, dest);
  }
}
