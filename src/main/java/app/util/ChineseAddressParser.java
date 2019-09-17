package app.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

/**
 * 
 *
 */
public class ChineseAddressParser {
  public static final String PROVINCE = "province";
  public static final String CITY = "city";
  public static final String COUNTY = "county";
  public static final String TOWN = "town";
  public static final String VILLAGE = "village";


  public static Map<String, String> parseOneLine(String address) {

    Preconditions.checkArgument(address != null, "null address value: %s", address);

    String regex = "(?<province>[^省]+自治区|.*?省|.*?行政区|.*?市)(?<city>[^市]+自治州|.*?地区|.*?行政单位|.+盟|市辖区|.*?市|.*?县)(?<county>[^县]+县|[^度假|园]+区|.+市|.+旗|.+海域|.+岛)?(?<town>[^区|度假|园]+区|.+镇)?(?<village>.*)";
    Matcher m = Pattern.compile(regex).matcher(address);
    String province = null, city = null, county = null, town = null, village = null;
    Map<String, String> row = new HashMap<>();
    while (m.find()) {
      province = m.group(PROVINCE);
      row.put(PROVINCE, province == null ? "" : province.trim());
      city = m.group(CITY);
      row.put(CITY, city == null ? "" : city.trim());
      county = m.group(COUNTY);
      row.put(COUNTY, county == null ? "" : county.trim());
      town = m.group(TOWN);
      row.put(TOWN, town == null ? "" : town.trim());
      village = m.group(VILLAGE);
      row.put(VILLAGE, village == null ? "" : village.trim());
    }
    return row;
  }

  public static List<Map<String, String>> parseMultiLines(String address) {
    Preconditions.checkArgument(address != null, "null address value: %s", address);

    String regex = "(?<province>[^省]+自治区|.*?省|.*?行政区|.*?市)(?<city>[^市]+自治州|.*?地区|.*?行政单位|.+盟|市辖区|.*?市|.*?县)(?<county>[^县]+县|[^度假|园]+区|.+市|.+旗|.+海域|.+岛)?(?<town>[^区|度假|园]+区|.+镇)?(?<village>.*)";
    Matcher m = Pattern.compile(regex).matcher(address);
    String province = null, city = null, county = null, town = null, village = null;
    List<Map<String, String>> table = new ArrayList<Map<String, String>>();
    Map<String, String> row = null;
    while (m.find()) {
      row = new HashMap<>();
      province = m.group(PROVINCE);
      row.put(PROVINCE, province == null ? "" : province.trim());
      city = m.group(CITY);
      row.put(CITY, city == null ? "" : city.trim());
      county = m.group(COUNTY);
      row.put(COUNTY, county == null ? "" : county.trim());
      town = m.group(TOWN);
      row.put(TOWN, town == null ? "" : town.trim());
      village = m.group(VILLAGE);
      row.put(VILLAGE, village == null ? "" : village.trim());
      table.add(row);
    }
    return table;
  }
}
