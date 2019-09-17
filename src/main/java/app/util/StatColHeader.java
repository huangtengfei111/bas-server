package app.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatColHeader {

  private static Map<String, String> DURATION_CLASS_HEADERS = new HashMap<String, String>() {
    {
      put("0", "其他");
      put("1", "1~15秒");
      put("2", "16~90秒");
      put("3", "1.5~3分");
      put("4", "3~5分");
      put("5", "5~10分");
      put("6", ">10分");
    }
  };

  private static Map<String, String> BILL_TYPE_HEADERS = new HashMap<String, String>() {
    {
      put("1", "通话");
      put("2", "短信");
      put("3", "彩信");
    }
  };

  private static Map<String, String> ONUM_ST_HEADERS = new HashMap<String, String>() {
    {
      put("0", "其他");
      put("1", "本地");
      put("2", "漫游");
    }
  };

  private static Map<String, String> COMM_DIR_HEADERS = new HashMap<String, String>() {
    {
      put("0", "未知");
      put("11", "主叫");
      put("12", "<--");
      put("13", "呼转");
      put("21", "主短");
      put("22", "被短");
      put("31", "主彩");
      put("32", "被彩");
    }
  };

  private static Map<String, String> ST_L1_HEADERS = new HashMap<String, String>() {
    {
      put("0", "早晨");
      put("1", "上午");
      put("2", "中午");
      put("3", "下午");
      put("4", "傍晚");
      put("5", "晚上");
      put("6", "深夜");
      put("7", "凌晨");
    }
  };

  private static Map<String, String> DA_ST_L2_HEADERS = new HashMap<String, String>() {
    {
      put("0", "4:30~6:20");
      put("1", "6:21~7:10");
      put("2", "7:11~7:50");
      put("3", "7:51~8:25");
      put("4", "8:26~11:00");
      put("5", "11:01~11:30");
      put("6", "11:31~12:30");
      put("7", "12:31~13:20");
      put("8", "13:21~14:00");
      put("9", "14:01~16:50");
      put("10", "16:51~17:40");
      put("11", "17:41~18:50");
      put("12", "18:51~20:00");
      put("13", "20:01~21:50");
      put("14", "21:51~23:59");
      put("15", "0:00~4:29");
    }
  };

  private static Map<String, String> DA_ST_L1_HEADERS = new HashMap<String, String>() {
    {
      put("0", "4:30~7:30");
      put("1", "7:31~11:15");
      put("2", "11:16~13:30");
      put("3", "13:31~17:15");
      put("4", "17:16~19:00");
      put("5", "19:01~20:50");
      put("6", "20:51~23:59");
      put("7", "0:00~5:30");
    }
  };

  private static Map<String, String> STARTED_HOUR_CLASS = new HashMap<String, String>() {
    {
      put("0", "4时");
      put("1", "5时");
      put("2", "6时");
      put("3", "7时");
      put("4", "8时");
      put("5", "9时");
      put("6", "10时");
      put("7", "11时");
      put("8", "12时");
      put("9", "13时");
      put("10", "14时");
      put("11", "15时");
      put("12", "16时");
      put("13", "17时");
      put("14", "18时");
      put("15", "19时");
      put("16", "20时");
      put("17", "21时");
      put("18", "22时");
      put("19", "23时");
      put("20", "0时");
      put("21", "1时");
      put("22", "2时");
      put("23", "3时");
    }
  };
  private static Map<String, String> WEEK = new HashMap<String, String>() {
    {
      put("1", "周一");
      put("2", "周二");
      put("3", "周三");
      put("4", "周四");
      put("5", "周五");
      put("6", "周六");
      put("7", "周日");

    }
  };
  
  private static Map<String, String> OWNER_NUM_HEADERS = new HashMap<String, String>() {

    {
      put("owner_num", "本方号码");
      put("count", "联系次数");
      put("peer_nums_cnt", "联系人个数");
      put("total_duration", "总通话时间s");
      put("first_day", "首次时间");
      put("last_day", "末次");
      put("inter_days", "首末相距");
      put("online_days", "使用天数");
      put("offline_days", "未使用天数");
      put("owner_lac", "lac");
    }
      };
  
  private static Map<String, String> PHONE_HEADERS = new HashMap<String, String>() {
    {
      put("私人", "手机/私人");
      put("办公", "手机/办公");

    }
  };
  
  private static Map<String, String> LOCATION_HEADERS = new HashMap<String, String>() {
    {
      put("num", "电话");
      put("loc", "地址");
    }
  };

  
  public static List<String> DA_ST_L1_HEADERS_LIST = Arrays.asList("4:30~7:30", "7:31~11:15", "11:16~13:30",
      "17:16~19:00", "19:01~20:50", "20:51~23:59", "0:00~5:30");

  public static List<String> DA_ST_L2_HEADERS_LIST = Arrays.asList("4:30~6:20", "6:21~7:10", "7:11~7:50", "7:51~8:25",
      "8:26~11:00", "11:01~11:30", "11:31~12:30", "12:31~13:20", "13:21~14:00", "14:01~16:50", "16:51~17:40",
      "17:41~18:50", "18:51~20:00", "20:01~21:50", "21:51~23:59", "0:00~4:29");
  
  public static List<String> DURATION_CLASS_HEADERS_LIST = Arrays.asList("其他", "1~15秒", "16~90秒", "1.5~3分", "3~5分", "5~10分", "> 10分");
  
  public static List<String> HOUR_CLASS_HEADERS_LIST = Arrays.asList("4时", "5时", "6时", "7时", "8时", "9时", "10时", "11时",
      "12时", "13时", "14时", "15时", "16时", "17时", "18时", "19时", "20时", "21时", "22时", "23时", "0时", "1时", "2时", "3时");

  public static List<String> COMM_DIR_HEADERS_LIST = Arrays.asList("主叫", "<--", "呼转", "主短", "被短");
  

  public static String durationClass(String durationClass) {
    String label = DURATION_CLASS_HEADERS.get(durationClass);
    if (label == null) {
      return durationClass;
    }

    return label;
  }

  public static String billType(String billType) {
    String label = BILL_TYPE_HEADERS.get(billType);
    if (label == null) {
      return billType;
    }

    return label;
  }

  public static String commDirection(String commDir) {
    String label = COMM_DIR_HEADERS.get(commDir);
    if (label == null) {
      return commDir;
    }

    return label;
  }

  public static String ownerNumStatus(String oNumStatus) {
    String label = ONUM_ST_HEADERS.get(oNumStatus);
    if (label == null) {
      return oNumStatus;
    }

    return label;
  }

  public static String startedTimeL1(String startedTimeL1) {
    String label = ST_L1_HEADERS.get(startedTimeL1);
    if (label == null) {
      return startedTimeL1;
    }

    return label;
  }

  public static String daAndStartedTimeL2(String startedTimeL2) {
    String label = DA_ST_L2_HEADERS.get(startedTimeL2);
    if (label == null) {
      return startedTimeL2;
    }

    return label;
  }

  public static String daAndStartedTimeL1(String daAndStartedTimeL1) {
    String label = DA_ST_L1_HEADERS.get(daAndStartedTimeL1);
    if (label == null) {
      return daAndStartedTimeL1;
    }
    return label;
  }

  public static String startedHour(String startedHour) {
    String label = STARTED_HOUR_CLASS.get(startedHour);
    if (label == null) {
      return startedHour;
    }

    return label;
  }
  
  public static String week(String week) {
    String label = WEEK.get(week);
    if (label == null) {
      return week;
    }
    return label;
  }

  public static String ownerNum(String owner) {
    String label = OWNER_NUM_HEADERS.get(owner);
    if (label == null) {
      return owner;
    }
    return label;
  }

  public static String phoneMemo(String phoneMemo) {
    String label = PHONE_HEADERS.get(phoneMemo);
    if (label == null) {
      return phoneMemo;
    }
    return label;
  }

  public static String citizenMemo(List args) {
    String field = (String) args.get(0);
    Object memoVal = (String) args.get(1);
    if (memoVal != null) {
      String label = LOCATION_HEADERS.get(field);
      return ((label == null) ? field : label) + "(" + memoVal + ")";
    } else {
      return null;
    }
  }

}