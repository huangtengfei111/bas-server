package app.util.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.alibaba.fastjson.JSONArray;

public class ListUtils {

  public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
    if (list1 == null && list2 == null) {
      return true;
    }
    if ((list1 == null && list2 != null) || (list1 != null && list2 == null)) {
      return false;
    }
    return new HashSet<>(list1).equals(new HashSet<>(list2));
  }

  public static List jsonArrayToList(JSONArray arr) {
    if (arr == null)
      return null;

    List l = new ArrayList<>();
    for (int i = 0; i < arr.size(); i++) {
      l.add(arr.get(i));
    }
    return l;
  }
}
