package app.util.ct;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import de.schlichtherle.util.ObfuscatedString;

public class CellTowerWitch {
  private static final Logger log = LoggerFactory.getLogger(CellTowerWitch.class);

  private static final double X_MAGIC = 35000.0;
  private static final double Y_MAGIC = 27000.0;

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

  public static final double[] toxifyCoord(double x, double y) {
    return coordOffsetEncrypt(x, y);
  }

  public static final double[] detoxifyCoord(double x, double y) {
    return croodOffsetDecrypt(x, y);
  }

  public static final String toxifyAddress(String address) {
    try {
//      byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
//      IvParameterSpec ivspec = new IvParameterSpec(iv);
//
//      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGO);
//      KeySpec spec = new PBEKeySpec(DEFAULT_SECRETKEY.toCharArray(), DEFAULT_SALT.getBytes(), 65536, 256);
//      SecretKey tmp = factory.generateSecret(spec);
//      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGO2);
//
//      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
//      return Base64.getEncoder().encodeToString(cipher.doFinal(address.getBytes("UTF-8")));
      String secretKey = DEFAULT_SECRETKEY;
      byte[] secretKeys =
          Hashing.sha1().hashString(secretKey, StandardCharsets.UTF_8).toString().substring(0, 16)
              .getBytes(StandardCharsets.UTF_8);

      final SecretKey secret = new SecretKeySpec(secretKeys, "AES");

      final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secret);

      final AlgorithmParameters params = cipher.getParameters();

      final byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
      final byte[] cipherText = cipher.doFinal(address.getBytes(StandardCharsets.UTF_8));

      return DatatypeConverter.printHexBinary(iv) + DatatypeConverter.printHexBinary(cipherText);
    } catch (Exception e) {
      System.out.println("Error while encrypting: " + e.toString());
    }
    return null;
  }

  public static final String detoxifyAddress(String address) {
    try {
//      byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
//      IvParameterSpec ivspec = new IvParameterSpec(iv);
//
//      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGO);
//      KeySpec spec = new PBEKeySpec(DEFAULT_SECRETKEY.toCharArray(), DEFAULT_SALT.getBytes(), 65536, 256);
//      SecretKey tmp = factory.generateSecret(spec);
//      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGO2);
//
//      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
//      return new String(cipher.doFinal(Base64.getDecoder().decode(address)));
      String secretKey = DEFAULT_SECRETKEY;
      byte[] secretKeys =
          Hashing.sha1().hashString(secretKey, StandardCharsets.UTF_8).toString().substring(0, 16)
              .getBytes(StandardCharsets.UTF_8);

      // grab first 16 bytes - that's the IV
      String hexedIv = address.substring(0, 32);

      // grab everything else - that's the cipher-text (encrypted message)
      String hexedCipherText = address.substring(32);

      byte[] iv = DatatypeConverter.parseHexBinary(hexedIv);
      byte[] cipherText = DatatypeConverter.parseHexBinary(hexedCipherText);

      final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKeys, "AES"), new IvParameterSpec(iv));

      return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    } catch (Exception e) {
      System.out.println("Error while decrypting: " + e.toString());
    }
    return null;
  }
  /**
   * 将真实地理坐标转换到加密经纬度坐标
   * 
   * <pre>
   * function coordOffsetEncrypt(x,y) { 
   * x = parseFloat(x)*100000%36000000; 
   * y = parseFloat(y)*100000%36000000;
   * 
   * _X =
   * parseInt(((Math.cos(y/100000))*(x/18000))+((Math.sin(x/100000))*(y/9000))+x);
   * _Y =
   * parseInt(((Math.sin(y/100000))*(x/18000))+((Math.cos(x/100000))*(y/9000))+y);
   * 
   * return [_X/100000.0,_Y/100000.0]; 
   * }
   * </pre>
   * 
   * @param x 经度值
   * @param y 维度值
   * @returns [x,y]
   * 
   */
  private static double[] coordOffsetEncrypt(double x, double y) {
    x = x * 1000000 % 360000000;
    y = y * 1000000 % 360000000;

    double percision = 1000000.0;
    int _x = (int) (((Math.cos(y / percision)) * (x / X_MAGIC)) +
                    ((Math.sin(x / percision)) * (y / Y_MAGIC)) + x);
    int _y = (int) (((Math.sin(y / percision)) * (x / X_MAGIC)) +
                    ((Math.cos(x / percision)) * (y / Y_MAGIC)) + y);
    
    return new double[] { _x / percision, _y / percision };
  }  

  /**
   * 将经纬坐标解密为真实地理坐标
   *
   * <pre>
   * function coordOffsetDecrypt(x,y){ x = parseFloat(x)*100000%36000000; y =
   * parseFloat(y)*100000%36000000;
   * 
   * x1 =
   * parseInt(-(((Math.cos(y/100000))*(x/18000))+((Math.sin(x/100000))*(y/9000)))+x);
   * y1 =
   * parseInt(-(((Math.sin(y/100000))*(x/18000))+((Math.cos(x/100000))*(y/9000)))+y);
   * 
   * x2 =
   * parseInt(-(((Math.cos(y1/100000))*(x1/18000))+((Math.sin(x1/100000))*(y1/9000)))+x+((x>0)?1:-1));
   * y2 =
   * parseInt(-(((Math.sin(y1/100000))*(x1/18000))+((Math.cos(x1/100000))*(y1/9000)))+y+((y>0)?1:-1));
   * console.log(y2);
   * return [x2/100000.0,y2/100000.0]; }
   * </pre>
   * 
   * @param x 经度值
   * @param y 维度值
   * @returns [x,y]
   */
  private static double[] croodOffsetDecrypt(double x, double y) {
    x = x * 1000000 % 360000000;
    y = y * 1000000 % 360000000;

    double percision = 1000000.0;
    int x1 = (int) (-(((Math.cos(y / percision)) * (x / X_MAGIC)) +
                      ((Math.sin(x / percision)) * (y / Y_MAGIC))) +
                    x);
    int y1 = (int) (-(((Math.sin(y / percision)) * (x / X_MAGIC)) +
                      ((Math.cos(x / percision)) * (y / Y_MAGIC))) +
                    y);

    int x2 = (int) (-(((Math.cos(y1 / percision)) * (x1 / X_MAGIC)) +
                      ((Math.sin(x1 / percision)) * (y1 / Y_MAGIC))) +
                    x
        + ((x > 0) ? 1 : -1));
    int y2 = (int) (-(((Math.sin(y1 / percision)) * (x1 / X_MAGIC)) +
                      ((Math.cos(x1 / percision)) * (y1 / Y_MAGIC))) +
                    y
        + ((y > 0) ? 1 : -1));

    return new double[] { x2 / percision, y2 / percision };
  }  


  public static void main(String[] args) {
//    try {
//      // encryptDB();
//      String s = toxifyAddress("浙江省温州市鹿城区水心街道水心住宅区藕组团1号楼;杏花路与十七中路路口西北77米");
//      System.out.println(s);
//      String s2 = detoxifyAddress(s);
//      System.out.println(s2);
//    } catch (Exception e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//    double x1 = 120.72531244, y1 = 28.00252887;
//    System.out.println("x1 = " + x1 + ", y1 = " + y1);
//    double[] r1 = coordOffsetEncrypt(x1, y1);
//    System.out.println("encrypted: x=" + r1[0] + ", y=" + r1[1]);
//

    double[] r1 = { 120.68239500, 27.98625600 };
    double[] r2 = croodOffsetDecrypt(r1[0], r1[1]);
    double[] c3 = CoordinateTransformUtil.gcj02tobd09(r2[0], r2[1]);
    System.out.println("baidu: x=" + c3[0] + ", y=" + c3[1]);

  }
}
