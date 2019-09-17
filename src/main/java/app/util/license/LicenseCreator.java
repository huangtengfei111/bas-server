package app.util.license;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;

import app.models.License;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.CustomKeyStoreParam;
import app.util.license.client.LicenseCreatorParam;
import app.util.license.client.LockServerParam;
import app.util.license.client.VersionizedParam;
import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseParam;

/**
 *
 */
public class LicenseCreator {
  private static Logger logger = LoggerFactory.getLogger(LicenseCreator.class);

  private final static X500Principal DEFAULT_ISSUER =
      new X500Principal("CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN");
  private final static X500Principal DEFAULT_HOLDER =
      new X500Principal("CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN");
  private X500Principal issuer;
  private X500Principal holder;
  private LicenseCreatorParam param;

  public LicenseCreator(LicenseCreatorParam param) {
    this.issuer = DEFAULT_ISSUER;
    this.holder = DEFAULT_HOLDER;
    this.param = param;
  }

  public LicenseCreator(X500Principal holder, LicenseCreatorParam param) {
    this.issuer = DEFAULT_ISSUER;
    this.holder = holder;
    this.param  = param;
  }

  public LicenseCreator(X500Principal issuer, X500Principal holder, LicenseCreatorParam param) {
    this.issuer = issuer;
    this.holder = holder;
    this.param  = param;
  }

  public static void generateLicense(String privateAlias, String keyPassword, JSONObject json) throws Exception {
    Preconditions.checkNotNull(privateAlias);
    Preconditions.checkNotNull(keyPassword);
    
    // Map subject = json.get("subject");
    String storePassword = json.getString("password");
    String plan = json.getString("plan");
    JSONObject host = JSON.parseObject(json.getString("host"));
    String hostId = host.getString("id");
    String systemSN = host.getString("systemSN");
    String baseboardInfo = host.getString("baseboardInfo");
    String processorInfo = host.getString("processorInfo");
    JSONArray jsonAllowedIPAddress = host.getJSONArray("allowedIPAddress");
    List<String> allowedIPAddress = jsonAllowedIPAddress.toJavaList(String.class);
    JSONArray jsonAllowedMacAddress = host.getJSONArray("allowedMacAddress");
    List<String> allowedMacAddress = jsonAllowedMacAddress.toJavaList(String.class);
    String publicAlias = json.getString("publicAlias");
    JSONObject jsonHolder = json.getJSONObject("holder");
    String name = jsonHolder.getString("name");
    String country = jsonHolder.getString("country");
    String state = jsonHolder.getString("state");
    String city = jsonHolder.getString("city");
    
    LicenseCreatorParam param = new LicenseCreatorParam();
    param.setSubject(name);
    param.setConsumerType("User");
    // int acctLimit = 1;
    param.setConsumerAmount(1);
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date issuedAt = sdf.parse("2019-05-13 10:10:10");
    Date expiredAt = sdf.parse("2019-06-17 17:50:10");
    param.setIssuedAt(issuedAt);
    param.setExpiredAt(expiredAt);
    param.setLicensePath("/tmp/license.lic");
    param.setKeyPassword(keyPassword);
    param.setPrivateAlias(privateAlias);
    param.setStorePassword(storePassword);
    param.setPrivateKeysStorePath("/tmp/privateKeys.keystore");    
    
    LockServerParam lockServerParam = new LockServerParam(VersionizedParam.V_1, plan);
    lockServerParam.setHostId(hostId);
    lockServerParam.setSystemSN(systemSN);
    lockServerParam.setProcessorInfo(processorInfo);
    lockServerParam.setBaseboardInfo(baseboardInfo);
    lockServerParam.setAllowedIPAddress(allowedIPAddress);
    lockServerParam.setAllowedMacAddress(allowedMacAddress);
    param.setLockServerParam(lockServerParam);

    X500Principal holder = holder(name);
    LicenseCreator creator = new LicenseCreator(holder, param);
    creator.generateLicense();
  }

  /**
   * 生成License证书
   */
  public boolean generateLicense() {
    try {
      LicenseManager licenseManager = new BASLicenseManager(initLicenseParam());
      LicenseContent licenseContent = initLicenseContent();

      licenseManager.store(licenseContent, new File(param.getLicensePath()));

      return true;
    } catch (Exception e) {
      logger.error("Generate license failed: {}", param, e);
      return false;
    }
  }

  /**
   * 初始化证书生成参数
   */
  private LicenseParam initLicenseParam() {
    Preferences preferences = Preferences.userNodeForPackage(LicenseCreator.class);

    // 设置对证书内容加密的秘钥
    CipherParam cipherParam = new DefaultCipherParam(param.getStorePassword());
    // @formatter:off
    KeyStoreParam privateStoreParam = 
        new CustomKeyStoreParam(LicenseCreator.class, 
                                param.getPrivateKeysStorePath(),
                                param.getPrivateAlias(), 
                                param.getStorePassword(), 
                                param.getKeyPassword());
    
    LicenseParam licenseParam =
        new DefaultLicenseParam(param.getSubject(), preferences, privateStoreParam, cipherParam);
    // @formatter:on
    return licenseParam;  
  }

  /**
   * 设置证书生成正文信息
   * @return de.schlichtherle.license.LicenseContent
   */
  private LicenseContent initLicenseContent() {
    LicenseContent licenseContent = new LicenseContent();
    licenseContent.setIssuer(this.issuer);
    licenseContent.setHolder(this.holder);

    licenseContent.setSubject(param.getSubject());
    licenseContent.setIssued(param.getIssuedAt());
    licenseContent.setNotBefore(param.getIssuedAt());
    licenseContent.setNotAfter(param.getExpiredAt());
    licenseContent.setConsumerType(param.getConsumerType());
    licenseContent.setConsumerAmount(param.getConsumerAmount());
    licenseContent.setInfo(param.getDescription());

    // 扩展校验服务器硬件信息
     licenseContent.setExtra(param.getLockServerParam());

    return licenseContent;
  }

  public X500Principal getHolder() {
    return this.holder;
  }

  private static X500Principal holder(String orgName) {
    String name = "CN=localhost, OU=localhost, O=" + orgName + ", L=SH, ST=SH, C=CN";
    return new X500Principal(name);
  }

  public static void main(String[] args) throws Exception {
    File file = new File("/home/dev/WorkSpace/bas-server/src/test/resources/deploy.license.json");
    License license = new License();
//    generateLicense("privateKey", "private_password1234", file, license);
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    Date issuedAt = sdf.parse("2019-05-13 10:10:10");
//    Date expiredAt = sdf.parse("2019-05-17 17:50:10");
//
//    LicenseCreatorParam param = new LicenseCreatorParam();
//    param.setSubject("test_license");
//    param.setConsumerType("User");
//    param.setConsumerAmount(1);
//    // LocalDateTime issuedAt = LocalDateTime.of(2019, 5, 14, 18, 34, 48);
//    // LocalDateTime expiredAt = LocalDateTime.of(2019, 5, 15, 18, 34, 48);
//    // 生效时间: Mon May 13 11:49:57 CST 2019, 失效时间: Thu May 10 11:49:57 CST 2029
//    param.setIssuedAt(issuedAt);
//    param.setExpiredAt(expiredAt);
//    param.setLicensePath("/tmp/license.lic");
//    param.setKeyPassword("private_password1234");
//    param.setPrivateAlias("privateKey");
//    param.setStorePassword("public_password1234");
//    param.setPrivateKeysStorePath("/tmp/privateKeys.keystore");
//
//    LockServerParam lockServerParam = new LockServerParam();
//    lockServerParam.setPlan(BusinessPlanParam.PRO);
//    lockServerParam.inspect();
//    param.setLockServerParam(lockServerParam);
//
//    LicenseCreator creator = new LicenseCreator(param);
//    creator.generateLicense();
    System.out.println("==> generated");
  }
}
