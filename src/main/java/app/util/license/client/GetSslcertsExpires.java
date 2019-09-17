package app.util.license.client;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * https://stackoverflow.com/questions/44838084/java-code-to-display-expiration-date-of-certificates-in-a-java-keystore
 * 
 * @author jacktang
 *
 */
public class GetSslcertsExpires {

  public static void main(String[] argv) throws Exception {

    try {
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      keystore.load(new FileInputStream("/DemoTrust.jks"), "DemoTrustKeyStorePassPhrase".toCharArray());
      Enumeration aliases = keystore.aliases();
      for (; aliases.hasMoreElements();) {
        String alias = (String) aliases.nextElement();
        Date certExpiryDate = ((X509Certificate) keystore.getCertificate(alias)).getNotAfter();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Tue Oct 17 06:02:22 AEST 2006
        Date today = new Date();
        long dateDiff = certExpiryDate.getTime() - today.getTime();
        long expiresIn = dateDiff / (24 * 60 * 60 * 1000);
        System.out.println("Certifiate: " + alias + "\tExpires On: " + certExpiryDate + "\tFormated Date: "
            + ft.format(certExpiryDate) + "\tToday's Date: " + ft.format(today) + "\tExpires In: " + expiresIn);
      }
    }

    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

