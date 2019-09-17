package app.util;

/**
 * https://regex101.com
 *
 */
public class RegExpPattern {

  public static final String CN_ANY_NUM_RULE = "^\\d{3,12}$"; // 所有号码
  public static final String CN_MOBILE_NUM_RULE = "^\\d{11}$"; // 手机号码
  public static final String CN_VEN_NUM_RULE = "^\\d{6}$"; // 短号
  public static final String CN_REL_NUM_RULE = "^\\d{3,6}$";
  public static final String CN_CT_CODE_RULE =
      "^[a-fA-F0-9]+:[a-fA-F0-9]+:[a-fA-F0-9]+$"; // 基站

  public static void main(String[] args) {
    String num = "13579658810";
    String venNum = "466661";
    String ctCode = "6F6D:7B7F:0";
    String relNum = "12345";
    boolean matches = num.matches(CN_ANY_NUM_RULE);
    boolean matches2 = num.matches(CN_MOBILE_NUM_RULE);
    boolean matches3 = venNum.matches(CN_VEN_NUM_RULE);
    boolean matches5 = relNum.matches(CN_REL_NUM_RULE);
    boolean matches4 = ctCode.matches(CN_CT_CODE_RULE);
    System.out.println(matches);
    System.out.println(matches2);
    System.out.println(matches3);
    System.out.println(matches4);
    System.out.println(matches5);
  }
}
