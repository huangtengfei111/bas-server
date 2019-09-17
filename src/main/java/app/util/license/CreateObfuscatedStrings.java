package app.util.license;

import de.schlichtherle.util.ObfuscatedString;

public class CreateObfuscatedStrings {
  private static final String PUBLIC_KEYSTORE_FILENAME = "hyde.keystore";
// private static final String PUBLIC_KEYSTORE_FILENAME = new ObfuscatedString(new long[] {0x86DD4DBB5166C13DL, 0x4C79B1CDC313AE09L, 0x1A353051DAF6463BL}).toString();

  public static void main(String[] args) {
    String result = ObfuscatedString.obfuscate("hyde.keystore");
    System.out.format("KEYSTORE-FILENAME:  %s\n", result);

    String r1 = ObfuscatedString.obfuscate("JB4PLEThF6LQZameVZnO2RT0kOKs5EKU");
    System.out.format("%s\n", r1);

    String r2 = ObfuscatedString.obfuscate("3XR05RC4hxW9wMYi");
    System.out.format("%s\n", r2);

    String r3 = ObfuscatedString.obfuscate("AES");
    System.out.format("KEYSTORE-FILENAME:  %s\n", r3);

    final String PUBLIC_KEYSTORE_FILENAME =
        new ObfuscatedString(new long[] { 0x86DD4DBB5166C13DL, 0x4C79B1CDC313AE09L, 0x1A353051DAF6463BL }).toString();
    System.out.format("KEYSTORE-FILENAME:  %s\n", PUBLIC_KEYSTORE_FILENAME);

    String s1 = ObfuscatedString.obfuscate("start");
    System.out.format("%s\n", s1);

    String s2 = ObfuscatedString.obfuscate("stop");
    System.out.format("%s\n", s2);

  }
}
