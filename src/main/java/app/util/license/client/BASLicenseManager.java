package app.util.license.client;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseContentException;
import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseNotary;
import de.schlichtherle.license.LicenseParam;
import de.schlichtherle.license.NoLicenseInstalledException;
import de.schlichtherle.xml.GenericCertificate;

/**
 * 自定义LicenseManager，用于增加额外的服务器硬件信息校验
 *
 */
public class BASLicenseManager extends LicenseManager {
  private static Logger logger = LoggerFactory.getLogger(BASLicenseManager.class);

  private static final String XML_CHARSET = "UTF-8";
  private static final int DEFAULT_BUFSIZE = 8 * 1024;

  private static final String SUBJECT = "lic.subject";
  private static final String STORE_PASS = "lic.store.pass";
  private static final String PUBLIC_ALIAS = "lic.public.alias";

  private static final String EXC_PLAN_TYPE_INVALID = "EXC_PLAN_TYPE_INVALID";
  private static final String EXC_ACCT_LIMIT_POSITIVE = "EXC_ACCT_LIMIT_POSITIVE";

  private static final String EXC_LICENSE_HAS_EXPIRED = null;

  private LicenseClientParam clientParam;

  public BASLicenseManager(LicenseParam param) {
    super(param);
  }

  public BASLicenseManager(LicenseClientParam param) {
    super(initLicenseParam(param));
    this.clientParam = param;
  }

  public synchronized LicenseContent install() {
    LicenseContent result = null;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    try {
      // LicenseManager licenseManager = LicenseManagerHolder.getInstance(param);
      // uninstall();

      result = install(new File(this.clientParam.getLicensePath()));
      logger.info(MessageFormat.format("License installed, date：{0} - {1}", format.format(result.getNotBefore()),
                                       format.format(result.getNotAfter())));
    } catch (Exception e) {
      logger.error("License installed failed:", e);
    }

    return result;
  }

  public synchronized void uninstall() throws Exception {
    super.uninstall();
    // clean pref node
    // https://stackoverflow.com/questions/16361188/java-util-preferences-constructors
    Preferences preferences = Preferences.userNodeForPackage(BASLicenseManager.class);
    preferences.remove(SUBJECT);
    // preferences.removeNode();
  }

  @Override
  protected synchronized byte[] create(LicenseContent content, LicenseNotary notary) throws Exception {
    initialize(content);
    this.validateAtCreation(content);
    final GenericCertificate certificate = notary.sign(content);
    return getPrivacyGuard().cert2key(certificate);
  }

  /**
   * 复写install方法，其中validate方法调用本类中的validate方法，校验IP地址、Mac地址等其他信息
   * 
   * @return de.schlichtherle.license.LicenseContent
   */
  @Override
  protected synchronized LicenseContent install(final byte[] key, final LicenseNotary notary) throws Exception {
    final GenericCertificate certificate = getPrivacyGuard().key2cert(key);

    notary.verify(certificate);
    final LicenseContent content = (LicenseContent) this.load(certificate.getEncoded());
    this.validate(content);
    setLicenseKey(key);
    setCertificate(certificate);

    return content;
  }

  /**
   * 复写verify方法，调用本类中的validate方法，校验IP地址、Mac地址等其他信息
   * 
   * @return de.schlichtherle.license.LicenseContent
   * @throws NoLicenseInstalledException
   * @throws LicenseContentException
   */
  @Override
  protected synchronized LicenseContent verify(final LicenseNotary notary)
      throws Exception {
    GenericCertificate certificate = getCertificate();

    // Load license key from preferences,
    final byte[] key = getLicenseKey();
    if (null == key) {
      throw new NoLicenseInstalledException(getLicenseParam().getSubject());
    }

    certificate = getPrivacyGuard().key2cert(key);
    notary.verify(certificate);
    final LicenseContent content = (LicenseContent) this.load(certificate.getEncoded());
    this.validate(content);
    setCertificate(certificate);

    return content;
  }

  protected synchronized void validateAtCreation(final LicenseContent content) throws LicenseContentException {
    super.validate(content);
//    BusinessPlanParam param = (BusinessPlanParam) content.getExtra();
//    param.validate();
    LockServerParam param = (LockServerParam) content.getExtra();
    if (!BusinessPlanParam.TRIVAL.equalsIgnoreCase(param.getPlan())
        && !BusinessPlanParam.PERSONAL.equalsIgnoreCase(param.getPlan())
        && !BusinessPlanParam.PRO.equalsIgnoreCase(param.getPlan())
        && !BusinessPlanParam.ENTERPRISE.equalsIgnoreCase(param.getPlan())) {
      throw new LicenseContentException(EXC_PLAN_TYPE_INVALID);
    }

    if (param.getAcctLimit() == null || param.getAcctLimit() < 1) {
      throw new LicenseContentException(EXC_ACCT_LIMIT_POSITIVE);
    }
  }

  /**
   * 复写validate方法，增加IP地址、Mac地址等其他信息校验
   * 
   * @param content LicenseContent
   */
  @Override
  protected synchronized void validate(final LicenseContent content) throws LicenseContentException {
    // 'subject' must match the subject required by the application via the
    // LicenseParam interface.
    // 'holder', 'issuer' and 'issued' must be provided (i.e. not null).
    // If 'notBefore' or 'notAfter' are provided, the current date and time must
    // match their restrictions.
    // 'consumerType' must be provided and 'consumerAmount' must be positive. If a
    // user preference node is provided in the license parameters, 'consumerType'
    // must also match "User" (whereby case is ignored) and 'consumerAmount' must
    // equal 1.
    super.validate(content);
    
    final Date now = new Date();
    final Date notAfter = content.getNotAfter();
    if (null != notAfter && now.after(notAfter))
        throw new LicenseContentException(EXC_LICENSE_HAS_EXPIRED);
    
    LockServerParam lockServerParam = (LockServerParam) content.getExtra();
    lockServerParam.validate();
  }

  public static synchronized LicenseClientParam loadClientParam() {
    Preferences preferences = Preferences.userNodeForPackage(BASLicenseManager.class);
    logger.debug("preferences is {}", preferences);
    String subject = preferences.get(SUBJECT, null);
    if (subject == null) {
      return null;
    } else {
      String publicAlias = preferences.get(PUBLIC_ALIAS, null);
      String storePass = preferences.get(STORE_PASS, null);
      storePass = decryptStorePass(storePass, mix(subject, publicAlias));
      return new LicenseClientParam(subject, publicAlias, storePass);
    }
  }

  /**
   * 重写XMLDecoder解析XML
   * 
   * @return java.lang.Object
   */
  private synchronized Object load(String encoded) {
    BufferedInputStream inputStream = null;
    XMLDecoder decoder = null;
    try {
      inputStream = new BufferedInputStream(new ByteArrayInputStream(encoded.getBytes(XML_CHARSET)));

      decoder     = new XMLDecoder(new BufferedInputStream(inputStream, DEFAULT_BUFSIZE), null, null);

      return decoder.readObject();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      try {
        if (decoder != null) {
          decoder.close();
        }
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }

    return null;
  }

  /**
   * 初始化证书生成参数
   * 
   */
  private static synchronized LicenseParam initLicenseParam(LicenseClientParam param) {
    Preferences preferences = Preferences.userNodeForPackage(BASLicenseManager.class);

    CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());

    String subject = param.getSubject();
    String publicAlias = param.getPublicAlias();

    // @formatter:off
    KeyStoreParam publicStoreParam = 
          new CustomKeyStoreParam(BASLicenseManager.class, 
                                  param.getPublicKeysStorePath(),
                                  param.getPublicAlias(), 
                                  param.getStorePass(), null);
    // @formatter:on
    preferences.put(SUBJECT, subject);
    preferences.put(PUBLIC_ALIAS, publicAlias);
    String sp = obfuscateStorePass(param.getStorePass(), mix(subject, publicAlias));
    if (sp != null) {
      preferences.put(STORE_PASS, sp);
    }

    return new DefaultLicenseParam(param.getSubject(), preferences, publicStoreParam, cipherParam);
  }

  private static synchronized String obfuscateStorePass(final String storePass, final String secret) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");

      md.update(secret.getBytes());
      byte[] digest = md.digest();
      String s = DatatypeConverter.printHexBinary(digest).toUpperCase();
      LicenseTextMagic magic = new LicenseTextMagic();
      return magic.encrypt(storePass, s);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static synchronized String decryptStorePass(final String storePass, final String secret) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");

      md.update(secret.getBytes());
      byte[] digest = md.digest();
      String s = DatatypeConverter.printHexBinary(digest).toUpperCase();
      LicenseTextMagic magic = new LicenseTextMagic();
      return magic.decrypt(storePass, s);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static synchronized String mix(String a, String b) {
    final int aLength = a.length();
    final int bLength = b.length();
    final int min = Math.min(aLength, bLength);
    final StringBuilder sb = new StringBuilder(aLength + bLength);
    for (int i = 0; i < min; i++) {
      sb.append(a.charAt(i));
      sb.append(b.charAt(i));
    }
    if (aLength > bLength) {
      sb.append(a, bLength, aLength);
    } else if (aLength < bLength) {
      sb.append(b, aLength, bLength);
    }
    return sb.toString();
  }

}
