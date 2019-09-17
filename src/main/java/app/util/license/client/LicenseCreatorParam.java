package app.util.license.client;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * License生成类需要的参数
 */
public class LicenseCreatorParam implements Serializable {

  private static final long serialVersionUID = -7200892433208319107L;

  /**
   * 证书subject
   */
  private String subject;

  /**
   * 密钥别称
   */
  private String privateAlias;

  /**
   * 密钥密码（需要妥善保管，不能让使用者知道）
   */
  private String keyPassword;

  /**
   * 访问秘钥库的密码
   */
  private String storePassword;

  /**
   * 证书生成路径
   */
  private String licensePath;

  /**
   * 密钥库存储路径
   */
  private String privateKeysStorePath;

  /**
   * 证书生效时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date issuedAt = new Date();

  /**
   * 证书失效时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date expiredAt;

  /**
   * 用户类型
   */
  private String consumerType = "User";

  /**
   * 用户数量
   */
  private Integer consumerAmount = 1;

  /**
   * 描述信息
   */
  private String description = "";

  /**
   * 额外的服务器硬件校验信息
   */
  private LockServerParam lockServerParam;


  public LockServerParam getLockServerParam() {
    return lockServerParam;
  }

  public void setLockServerParam(LockServerParam lockServerParam) {
    this.lockServerParam = lockServerParam;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getPrivateAlias() {
    return privateAlias;
  }

  public void setPrivateAlias(String privateAlias) {
    this.privateAlias = privateAlias;
  }

  public String getKeyPassword() {
    return keyPassword;
  }

  public void setKeyPassword(String keyPass) {
    this.keyPassword = keyPass;
  }

  public String getStorePassword() {
    return storePassword;
  }

  public void setStorePassword(String storePass) {
    this.storePassword = storePass;
  }

  public String getLicensePath() {
    return licensePath;
  }

  public void setLicensePath(String licensePath) {
    this.licensePath = licensePath;
  }

  public String getPrivateKeysStorePath() {
    return privateKeysStorePath;
  }

  public void setPrivateKeysStorePath(String privateKeysStorePath) {
    this.privateKeysStorePath = privateKeysStorePath;
  }

  public Date getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(Date issuedTime) {
    this.issuedAt = issuedTime;
  }

  public Date getExpiredAt() {
    return expiredAt;
  }

  public void setExpiredAt(Date expiredAt) {
    this.expiredAt = expiredAt;
  }

  public String getConsumerType() {
    return consumerType;
  }

  public void setConsumerType(String consumerType) {
    this.consumerType = consumerType;
  }

  public Integer getConsumerAmount() {
    return consumerAmount;
  }

  public void setConsumerAmount(Integer consumerAmount) {
    this.consumerAmount = consumerAmount;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  // @formatter:off
  @Override
  public String toString() {
    return "LicenseCreatorParam{" + "subject='" + subject + '\'' 
        + ", privateAlias='" + privateAlias + '\''
        + ", keyPass='" + keyPassword + '\'' 
        + ", storePass='" + storePassword + '\'' 
        + ", licensePath='" + licensePath + '\''
        + ", privateKeysStorePath='" + privateKeysStorePath + '\'' 
        + ", issuedTime=" + issuedAt 
        + ", expiriedAt=" + expiredAt + ", consumerType='" + consumerType + '\'' 
        + ", consumerAmount=" + consumerAmount
        + ", description='" + description + '\'' 
        + ", lockServerParam=" + lockServerParam + '}';
  }
  // @formatter:on
}

