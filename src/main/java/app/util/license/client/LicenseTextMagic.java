package app.util.license.client;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.schlichtherle.util.ObfuscatedString;

public class LicenseTextMagic {

  private static final String DEFAULT_SECRETKEY =
      new ObfuscatedString(new long[] { 0x454B61B173DE9AE0L, 0xFA94605415133793L, 0x5CFF2BAAEB28DBFBL })
          .toString(); /*
                        * => "3XR05RC4hxW9wMYi"
                        */
  private static final String DEFAULT_SALT =
      new ObfuscatedString(new long[] { 0xDA5E23191EC98D53L, 0x1DC04A23C588EABDL, 0x62E6AA1077ED6FF2L,
          0x99169160D416FF26L, 0xD130EBD512710794L }).toString(); /*
                                                                   * => "JB4PLEThF6LQZameVZnO2RT0kOKs5EKU"
                                                                   */
  private static final String ALGO = new ObfuscatedString(
      new long[] { 0x5989749AD7710348L, 0xA70E7A731384E094L, 0xAE952BB3E2B02CD1L, 0x1C9696256C992545L })
          .toString(); /*
                        * => "PBKDF2WithHmacSHA256"
                        */
  private static final String ALGO2 =
      new ObfuscatedString(new long[] { 0xA19DB95A283BB27EL, 0x136A8ABB232D5BA9L }).toString(); /* => "AES" */

  private String secretKey;
  private String salt;

  public LicenseTextMagic() {
    this.secretKey = DEFAULT_SECRETKEY;
    this.salt = DEFAULT_SALT;
  }

  public LicenseTextMagic(final String secretKey, final String salt) {
    this.secretKey = secretKey;
    this.salt      = salt;
  }

  public String encrypt(String strToEncrypt, String secret) {
    try {
      byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGO);
      KeySpec spec = new PBEKeySpec(this.secretKey.toCharArray(), this.salt.getBytes(), 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGO2);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
      return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    } catch (Exception e) {
      System.out.println("Error while encrypting: " + e.toString());
    }
    return null;
  }

  public String decrypt(String strToDecrypt, String secret) {
    try {
      byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGO);
      KeySpec spec = new PBEKeySpec(this.secretKey.toCharArray(), this.salt.getBytes(), 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGO2);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
      return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    } catch (Exception e) {
      System.out.println("Error while decrypting: " + e.toString());
    }
    return null;
  }

  public static void main(String[] args) {
    String originalString = "howtodoinjava.com好的";
    String secretKey = "okitistoooooooloooooogse好";

    LicenseTextMagic packer = new LicenseTextMagic();
    String encryptedString = packer.encrypt(originalString, secretKey);
    String decryptedString = packer.decrypt(encryptedString, secretKey);

    System.out.println(originalString);
    System.out.println(encryptedString);
    System.out.println(decryptedString);
  }
}
