package app.util.license;

public class JavaKeyTool {

  // List keystore
  public static void list(String privKeystore, String pubStorePass) {
    String command = " -list " + " -v " + " -keystore " + privKeystore + " -storepass " + pubStorePass;
    execute(command);
  }

  /**
   * Generate keypair
   * 
   * <pre>
   * keytool -genkeypair -keysize 1024 -validity 3650 -alias
   * "privateKey" -keystore "privateKeys.keystore" -storepass
   * "public_password1234" -keypass "private_password1234" -dname "CN=localhost,
   * OU=localhost, O=localhost, L=SH, ST=SH, C=CN"
   * </pre>
   */
//  public static void generateKeyPair() {
  public static void generateKeyPair(String holder, int validity, String privAlias, String privKeystore,
      String privKeyPass, String pubStorePass) {
//    try {
//      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//      keyPairGenerator.initialize(1024);
//      KeyPair keyPair = keyPairGenerator.generateKeyPair();
//      PublicKey publicKey = keyPair.getPublic();
//      PrivateKey privateKey = keyPair.getPrivate();
//
//      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//      char[] keyStorePassword = "123abc".toCharArray();
//      try (InputStream keyStoreData = new FileInputStream("keystore.ks")) {
//        keyStore.load(keyStoreData, keyStorePassword);
//      }
//    } catch (NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    } catch (KeyStoreException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (Exception e) {
//
//    }
    //@formatter:off
//    String command = " -genkeypair -keyalg RSA -sigalg SHA256withRSA -keysize 1024 -validity " + validity + " -alias " + privAlias + 
//                     " -keystore " + privKeystore + " -storepass " + pubStorePass + " -keypass " + privKeyPass + 
//                     " -dname " + holder ;
    String command = " -genkeypair -keysize 1024 -validity " + validity + " -alias " + privAlias + 
        " -keystore " + privKeystore + " -storepass " + pubStorePass + " -keypass " + privKeyPass + 
        " -dname " + holder ;
    //@formatter:on
    execute(command);
  }

  /**
   * <pre>
   * keytool -exportcert -alias "privateKey" -keystore "privateKeys.keystore" -storepass "public_password1234" 
   * -file "certfile.cer"
   * </pre>
   * 
   * @param privateAlias
   * @param keyStore
   * @param pubStorePass
   * @param certFile
   */
  public static void exportCert(String privateAlias, String privKeyStore, String pubStorePass, String certFile) {
    //@formatter:off
    String command = " -exportcert -alias " + privateAlias + " -keystore " + privKeyStore +  
                     " -storepass " + pubStorePass + " -file " + certFile ;
    //@formatter:on
    execute(command);
  }

  /**
   * <pre>
   * keytool -import -alias "publicCert" -file "certfile.cer" -keystore "publicCerts.keystore" -storepass "public_password1234"
   * </pre>
   */
  public static void importCert(String publicAlias, String certFile, String pubKeyStore, String pubStorePass) {
    //@formatter:off
    String command = " -import -alias " + publicAlias + " -file " + certFile + " -keystore " + pubKeyStore + 
                     " -storepass " + pubStorePass + " -noprompt";
    //@formatter:on
    execute(command);
  }
  
  public static void deleteCert(String privateAlias, String privKeystore, String pubStorePass) {
    //@formatter:off
    String command = "-delete -alias " + privateAlias + " -keystore " + privKeystore + " -storepass " + pubStorePass;
    //@formatter:on
    execute(command);
  }

  // Execute the commands
  public static void execute(String command) {
    try {
      printCommand(command);
      sun.security.tools.keytool.Main.main(parse(command));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // Parse command
  private static String[] parse(String command) {
    String[] options = command.trim().split("\\s+");
    return options;
  }

  // Print the command
  private static void printCommand(String command) {
    System.out.println(command);
  }

  public static void main(String[] args) {
    String privateAlias = "privKey-1";
    String privKeyPass = "private_password1234";
    String privateKeystore = "privateKey-1.keystore";
    String pubStorePass = "public_password1234";
    String certFile = "certfile.cer";
    String publicAlias = "publicCert";
    String holder = "CN=localhost,OU=localhost,O=localhost,L=SH,ST=SH,C=CN";
    int validity = 1;
    
    generateKeyPair(holder, validity, privateAlias, privateKeystore, privKeyPass, pubStorePass);
    list(privateKeystore, pubStorePass);
    deleteCert(privateAlias, privateKeystore, pubStorePass);
  }
}
