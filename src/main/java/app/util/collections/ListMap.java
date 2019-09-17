package app.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.vavr.Tuple;

@SuppressWarnings("unchecked")
public class ListMap {
  private static final Logger log = LoggerFactory.getLogger(ListMap.class);

  public static final String MERGE_WITH_KEEP_POLICY = "keep";
  public static final String MERGE_WITH_REMOVE_POLICY = "remove";

  public static Long getCounter(List<Map> listMap, String fieldName) {
    Preconditions.checkArgument(listMap.size() == 1, "List must contain one element");
    Object v = listMap.get(0).get(fieldName);
    if (v == null) {
      return 0L;
    } else {
      return Long.parseLong(v.toString());
    }
  }

  public static Set valuesToSet(List<Map> listMap) {
    Set set = new HashSet();
    for (Map<Object, Object> map : listMap) {
      Set<Object> s = map.values().stream().collect(Collectors.toSet());
      set.addAll(s);
    }
    return set;
  }

  public static List valuesToList(List<Map> listMap) {
    List l = new ArrayList();
    for (Map<Object, Object> map : listMap) {
      List<Object> list = map.values().stream().collect(Collectors.toList());
      l.addAll(list);
    }
    return l;
  }
  /**
   * 
   * @param key
   * @param listMap
   * @return
   */
  public static LinkedHashMap<Object, Map> toHashMap(String key, List<Map> listMap) {
    Preconditions.checkNotNull(listMap);

    //@formatter:off
    LinkedHashMap<Object, Map> mapWithOrder = listMap.stream()
                                                     .collect(LinkedHashMap::new, // Supplier
                                                              (map, item) -> map.put(item.get(key), item), // Accumulator
                                                              Map::putAll);
    //@formatter:on
    return mapWithOrder;
  }

  public static LinkedHashMap<Object, Map> toHashMap(Tuple key, List<Map> listMap) {
    Preconditions.checkNotNull(listMap);

    //@formatter:off
    LinkedHashMap<Object, Map> mapWithOrder = listMap.stream()
                                                     .collect(LinkedHashMap::new, // Supplier
                                                              (map, item) -> map.put(item.get(key), item), // Accumulator
                                                              Map::putAll);
    //@formatter:on
    return mapWithOrder;
  }

  public static <T extends Object> LinkedHashMap<Object, T> toHashMap(String key, List<Map> listMap, Class<?> clazz) {
    List<String> k = Arrays.asList(new String[] { key });
    return toHashMap(k, listMap, clazz);
  }

  public static <T extends Object> LinkedHashMap<Object, T> toHashMap(List<String> comboKey, List<Map> listMap,
      Class<?> clazz) {
    Preconditions.checkNotNull(listMap);

    LinkedHashMap<Object, T> lhm = new LinkedHashMap<>();
    for (Map<Object, Object> map : listMap) {
      Object newK = null;
      if (comboKey.size() == 1) {
        newK = map.get(comboKey.get(0));
      } else {
        newK = new ArrayList();
        for (String k : comboKey) {
          ((List) newK).add(map.get(k));
        }
      }
      if (clazz.isAssignableFrom(Map.class)) {
        lhm.put(newK, (T) map);
      }
      if (clazz.isAssignableFrom(List.class)) {
        List<Map> l = new ArrayList<>();
        l.add(map);
        lhm.put(newK, (T) l);
      }
    }
    return lhm;
  }

  public static LinkedHashMap<Object, Map> toHashMap(String[] multiKeys, List<Map> listMap) {
    Preconditions.checkNotNull(listMap);

    LinkedHashMap<Object, Map> lhm = new LinkedHashMap<>();
    for (Map<Object, Object> map : listMap) {
      StringBuffer sb = new StringBuffer();
      for (String k : multiKeys) {
        sb.append(map.get(k).toString());
      }
      if (sb.length() > 0) {
        lhm.put(sb.toString(), map);
      }
    }
    return lhm;
  }

  /**
   * 
   * @param key
   * @param listMap
   * @param mergeFields
   * @param transformFn
   * @return
   */
  public static LinkedHashMap<Object, Map> toHashMapWithMergeFields(String key, List<Map> listMap, String[] mergeFields,
      Function mergeTransFun) {
    return toHashMapWithMergeFields(key, listMap, mergeFields, null, mergeTransFun, MERGE_WITH_KEEP_POLICY);
  }

  public static LinkedHashMap<Object, Map> toHashMapWithMerging(String key, List<Map> listMap, String[] mergeFields,
      Function mergeTransFun, String policy) {
    return toHashMapWithMergeFields(key, listMap, mergeFields, null, mergeTransFun, policy);
  }
  /**
   * 
   * @param key
   * @param listMap
   * @param mergeFields
   * @param transformFn
   * @param policy
   * @return
   */
  public static LinkedHashMap<Object, Map> toHashMapWithMergeFields(String key, List<Map> listMap, String[] mergeFields,
      Function keyTransFun, Function mergeTransFun, String policy) {
    Preconditions.checkNotNull(listMap);
    Preconditions.checkArgument(mergeFields.length == 2, "Merge fields length should be 2");

    String k1 = mergeFields[0];
    String k2 = mergeFields[1];

    LinkedHashMap<Object, Map> lhm = new LinkedHashMap<>();
    for (Map<Object, Object> map : listMap) {
      Object v1 = null;
      Object v2 = null;
      Object mKey = null;
      if (MERGE_WITH_REMOVE_POLICY.equals(policy)) {
        v1 = map.remove(k1);
        v2 = map.remove(k2);
        mKey = map.remove(key);
      } else {
        v1 = map.get(k1);
        v2 = map.get(k2);
        mKey = map.get(key);
      }
      if (keyTransFun != null) {
        mKey = keyTransFun.apply(mKey);
      }
      if (mergeTransFun != null) {
        Object transformedKey = mergeTransFun.apply(v1);
        map.put(transformedKey, v2);
      }
      lhm.put(mKey, map);
    }
    return lhm;
  }
  
  public static LinkedHashMap<Object, Map> zip(String key, List<Map>... lms) {
    LinkedHashMap<Object, Map> baseLhm = toHashMap(key, lms[0]);
    List<LinkedHashMap> others = new ArrayList<>();

    for (int i = 1; i < lms.length; i++) {
      LinkedHashMap<Object, Map> lhm = toHashMap(key, lms[i]);
      others.add(lhm);
    }

    for (Map.Entry<Object, Map> e : baseLhm.entrySet()) {
      Object bk = e.getKey();
      Map bv = e.getValue();
      for(LinkedHashMap<Object, Map> other: others) {
        Map ov = other.get(bk);
        bv.putAll(ov);
      }
    }
    return baseLhm;
  }

  public static LinkedHashMap expand(String commKey, List<Map> lm, List masterKeyVals, List slaveKeyVals) {
    
    return null;
  }
  /**
   * 
   * @param key
   * @param master
   * @param lm2
   * @return
   */
  public static LinkedHashMap<Object, Map> merge(String key, List<Map> master, List<Map> lm2) {
    LinkedHashMap<Object, Map> lhmMaster = toHashMap(key, master);
    LinkedHashMap<Object, Map> lhm2 = toHashMap(key, lm2);
    for (Map.Entry<Object, Map> entry : lhmMaster.entrySet()) {
      Object k0 = entry.getKey();
      Map v0 = entry.getValue();
      Map v = lhm2.get(k0);
      if(v0 != null && v!= null) {
        v0.putAll(v);
      }
    }
    return lhmMaster;
  }

  public static LinkedHashMap<Object, Map> merge(String key, List<Map> master,
      LinkedHashMap<Object, Map> other) {
    LinkedHashMap<Object, Map> lhmMaster = toHashMap(key, master);
    for (Map.Entry<Object, Map> entry : lhmMaster.entrySet()) {
      Object k0 = entry.getKey();
      Map v0 = entry.getValue();
      Map v = other.get(k0);
      v0.putAll(v);
    }
    return lhmMaster;
  }

  public static LinkedHashMap<Object, Map> merge(String key, LinkedHashMap<Object, Map> master, List<Map> other) {
    LinkedHashMap<Object, Map> lhm2 = toHashMap(key, other);
    for (Map.Entry<Object, Map> entry : master.entrySet()) {
      Object k0 = entry.getKey();
      Map v0 = entry.getValue();
      Map v = lhm2.get(k0);
      v0.putAll(v);
    }
    return master;
  }

  public static <T extends Object> LinkedHashMap<Object, T> merge(String key,
      LinkedHashMap<Object, T> master, List<Map> other, Class<?> clazz) {

    LinkedHashMap<Object, Map> lhm2 = toHashMap(key, other);
    for (Map.Entry<Object, T> entry : master.entrySet()) {
      Object k0 = entry.getKey();
      Map v = lhm2.get(k0);
      T v0 = entry.getValue();
      if(v0 == null) {
        if (clazz.isAssignableFrom(Map.class)) {
          Map m = new HashMap();
          m.putAll(v);
          master.put(k0, (T) m);
        }
        if (clazz.isAssignableFrom(List.class)) {
          List l = new ArrayList();
          l.add(v);
          master.put(k0, (T) l);
        }
      } else {
        if (clazz.isAssignableFrom(Map.class)) {
          ((Map) v0).putAll(v);
        }
        if (clazz.isAssignableFrom(List.class)) {
          ((List) v0).add(v);
        }
      }
    }
    return master;
  }

  public static <T extends Object> LinkedHashMap<Object, T> merge(String key, List<Map> master,
      LinkedHashMap<Object, T> other, Class<?> clazz) {
    List<String> comboKey = Arrays.asList(new String[] { key });
    return merge(comboKey, master, other, clazz);
  }

  public static <T extends Object> LinkedHashMap<Object, T> merge(List<String> comboKey, List<Map> master,
      LinkedHashMap<Object, T> other, Class<?> clazz) {
    LinkedHashMap<Object, T> lhm2 = toHashMap(comboKey, master, clazz);
    for (Map.Entry<Object, T> entry : lhm2.entrySet()) {
      Object k0 = entry.getKey();
      T v0 = entry.getValue();
      T v = other.get(k0);

      if (v == null) {
        continue;
      }

      if (v0 == null) {
        if (clazz.isAssignableFrom(Map.class)) {
          Map m = new HashMap();
          m.putAll((Map) v);
          lhm2.put(k0, (T) m);
        }
        if (clazz.isAssignableFrom(List.class)) {
          List l = new ArrayList();
          l.addAll((List) v);
          lhm2.put(entry.getKey(), (T) l);
        }
      } else {
        if (clazz.isAssignableFrom(Map.class)) {
          ((Map) v0).putAll((Map) v);
        }
        if (clazz.isAssignableFrom(List.class)) {
          ((List) v0).addAll((List) v);
        }
      }
    }
    return lhm2;
  }
  /**
   * 
   * @param listMap
   * @param fieldNullVal
   * @param fun
   * @return
   */
  public static LinkedHashMap<String, Long> toCountMap(List<Map> listMap, String fieldNullVal,
      Function<String, String> fun) {
    LinkedHashMap<String, Long> map = new LinkedHashMap<>();
    for (Map m : listMap) {
      String field = null;
      Long count = null;
      // for (Map.Entry a:s) {
      for (Object k : m.keySet()) {
        Object v = m.get(k); // e.getValue();
        // Object k = e.getKey();
        if ("count".equals(k.toString())) {
          count = Long.valueOf(v.toString());
        } else {
          if (v == null) {
            field = fieldNullVal;
          } else {
            field = v.toString();
          }
        }
      }
      if (field != null) {
        String fieldLabel = field;
        if (fun != null) {
          fieldLabel = fun.apply(field);
        }
        map.put(fieldLabel, count);
      }
    }
    return map;
  }

  /**
   * 
   * @param reducedKey
   * @param listMap
   * @return
   */
  public static LinkedHashMap<Object, List<Map>> mergeToList(String reducedKey, List<Map> listMap) {
    Preconditions.checkNotNull(listMap);
    LinkedHashMap<Object, List<Map>> result = new LinkedHashMap<>();
    for (Map map : listMap) {
      Object rKey = map.get(reducedKey);
      List<Map> l = result.get(rKey);
      if (l != null) {
        l.add(map);
      } else {
        l = new ArrayList<>();
        l.add(map);
        result.put(rKey, l);
      }
    }
    return result;
  }

  /**
   * Group ListMap by the reducedKey
   * 
   * @param reducedKey
   * @param listMap
   * @return
   */
  public static LinkedHashMap<Object, List<Map>> reduce(String reducedKey, List<Map> listMap) {
    LinkedHashMap<Object, List<Map>> result = new LinkedHashMap<>();
    for (Map map : listMap) {
      Object groupVal = map.remove(reducedKey);
      List<Map> list = result.get(groupVal);
      if (list == null) {
        list = new ArrayList<>();
        list.add(map);
        result.put(groupVal, list);
      } else {
        list.add(map);
      }
    }
    return result;
  }

  public static LinkedHashMap<Object, List<Map>> reduceWithMergeFields(String reducedKey, List<Map> listMap,
      String[] mergeFields, Function mergeTransFun) {
    return reduceWithMergeFields(reducedKey, listMap, mergeFields, null,
        mergeTransFun, MERGE_WITH_KEEP_POLICY);
  }

  public static LinkedHashMap<Object, List<Map>> reduceWithMergeFields(String reducedKey, List<Map> listMap,
      String[] mergeFields, Function mergeTransFun, String policy) {
    return reduceWithMergeFields(reducedKey, listMap, mergeFields, null, mergeTransFun, policy);
  }

  public static LinkedHashMap<Object, List<Map>> reduceWithMergeFields(String reducedKey, List<Map> listMap,
      String[] mergeFields, Function keyTransFun, Function mergeTransFun, String policy) {
    List<String> comboKey = Arrays.asList(new String[] { reducedKey });
    return reduceWithMergeFields(comboKey, listMap, mergeFields, keyTransFun, mergeTransFun, policy);
  }

  public static LinkedHashMap<Object, List<Map>> reduceWithMergeFields(List<String> comboKey, List<Map> listMap,
      String[] mergeFields, Function mergeTransFun, String policy) {
    return reduceWithMergeFields(comboKey, listMap, mergeFields, null, mergeTransFun, policy);
  }

  /**
   * <pre>
   * | day  |  class | count |
   * | 0501 |   c1   | 100   |
   * | 0501 |   c2   | 70    |
   * | 0502 |   c1   | 4     |
   * | 0502 |   c2   | 90    |
   * </pre>
   * 
   * => 0501 : {c1:100, c2:70}, 0502 : {c1:4, c2:90}
   * 
   * @param listMap
   * @return
   */
  public static LinkedHashMap<Object, List<Map>> reduceWithMergeFields(List<String> comboKey, List<Map> listMap,
      String[] mergeFields, Function keyTransFun, Function mergeTransFun, String policy) {
    Preconditions.checkNotNull(listMap);
    Preconditions.checkArgument(mergeFields.length == 2, "Merge fields length should be 2");

    String f1 = mergeFields[0];
    String f2 = mergeFields[1];

    LinkedHashMap<Object, List<Map>> result = new LinkedHashMap<>();
    for (Map map : listMap) {
      Object newK = null;
      if (comboKey.size() == 1) {
        newK = map.get(comboKey.get(0));
      } else {
        newK = new ArrayList();
        for (String k : comboKey) {
          ((List) newK).add(map.get(k));
        }
      }
      // Object rKey = map.get(reducedKey);
      Object v1 = null;
      Object v2 = null;

      if (MERGE_WITH_REMOVE_POLICY.equals(policy)) {
        v1   = map.remove(f1);
        v2   = map.remove(f2);
      } else {
        v1   = map.get(f1);
        v2   = map.get(f2);
      }
      if (keyTransFun != null) {
        if (comboKey.size() == 1) {
          newK = keyTransFun.apply(newK);
        } else {
          newK = (List) keyTransFun.apply(newK);
        }
      }
      if (mergeTransFun != null) {
        v1 = mergeTransFun.apply(v1);
      }

      map.put(v1, v2);

      List<Map> lm = result.get(newK);
      if (lm != null) {
        lm.add(map);
      } else {
        lm = new ArrayList<>();
        lm.add(map);
        result.put(newK, lm);
      }
    }
    return result;
  }

  public static LinkedHashMap<Object, Map> reduceWithMergeMemoFields(String reducedKey, List<Map> listMap,
      String[] mergeFields, Function mergeTransFun, String policy) {
    Preconditions.checkNotNull(listMap);

    String f1 = mergeFields[0];
    String f2 = mergeFields[1];

    LinkedHashMap<Object, Map> result = new LinkedHashMap<>();
    for (Map map : listMap) {
      Object rKey = map.get(reducedKey);
      Object v1 = null;
      Object v2 = null;
      Object tKey = null;
      for (String field : mergeFields) {
        String memoField = field + "_memo";
        if (MERGE_WITH_REMOVE_POLICY.equals(policy)) {
          v1 = map.remove(field);
          v2 = map.remove(memoField);
        } else {
          v1 = map.get(field);
          v2 = map.get(memoField);
        }
        if (mergeTransFun != null) {
          List args = new ArrayList();
          args.add(field);
          args.add(v2);
          tKey = mergeTransFun.apply(args);
        }
        if (tKey != null) {
          map.put(tKey, v1);
        }
      }

      Map m = result.get(rKey);
      if (m != null) {
        m.putAll(map);
      } else {
        result.put(rKey, map);
      }
    }
    return result;
  }

  public static void main(String[] args) {
    Map<String, String> header = new HashMap<>();
    header.put("1", "header 1");
    header.put("2", "header 2");
    
    Map<String, Object> m1 = new HashMap<>();
    m1.put("day", "20190104");
    m1.put("cl", "1");
    m1.put("count", "90");

    Map<String, Object> m2 = new HashMap<>();
    m2.put("day", "20190101");
    m2.put("cl", "2");
    m2.put("count", "109");

    List<Map> lm = new ArrayList<>();
    lm.add(m1);
    lm.add(m2);

    Map<String, Object> m21 = new HashMap<>();
    m21.put("day", "20190104");
    m21.put("x", "abc");

    Map<String, Object> m22 = new HashMap<>();
    m22.put("day", "20190101");
    m22.put("x", "uij");

    List<Map> lm2 = new ArrayList<>();
    lm2.add(m21);
    lm2.add(m22);

    List<Map> lm3 = new ArrayList<>();
    Map<String, String> m31 = new HashMap<>();
    m31.put("dc", "0");
    m31.put("stlc", "1");
    m31.put("c", "10");
    Map<String, String> m32 = new HashMap<>();
    m32.put("dc", "0");
    m32.put("stlc", "2");
    m32.put("c", "80");
    
    Map<String, String> m33 = new HashMap<>();
    m33.put("dc", "1");
    m33.put("stlc", "1");
    m33.put("c", "50");
    Map<String, String> m34 = new HashMap<>();
    m34.put("dc", "1");
    m34.put("stlc", "2");
    m34.put("c", "70");
    lm3.add(m31);
    lm3.add(m32);
    lm3.add(m33);
    lm3.add(m34);

    List<Map> lm4 = new ArrayList<>();
    Map<String, String> m35 = new HashMap<>();
    m35.put("dc", "0");
    m35.put("n", "71");
    Map<String, String> m36 = new HashMap<>();
    m36.put("dc", "1");
    m36.put("n", "59");

    lm4.add(m35);
    lm4.add(m36);

    List<Map> lm5 = new ArrayList<>();
    Map<String, String> m55 = new HashMap<>();
    m55.put("dc", "a");
    m55.put("n", "71");

    Map<String, String> m56 = new HashMap<>();
    m56.put("dc", "b");
    m56.put("n", "59");

    Map<String, String> m57 = new HashMap<>();
    m57.put("dc", "a");
    m57.put("n", "88");

    lm5.add(m55);
    lm5.add(m56);
    lm5.add(m57);

    List<Map> lm6 = new ArrayList<>();
    Map<String, String> m58 = new HashMap<>();
    m58.put("dc", "123");
    m58.put("n", "70");
    Map<String, String> m59 = new HashMap<>();
    m59.put("dc", "456");
    m59.put("n", "80");
    Map<String, String> m60 = new HashMap<>();
    m60.put("dc", "456");
    m60.put("n", "90");

    lm6.add(m58);
    lm6.add(m59);
    lm6.add(m60);

    List<Map> lm7 = new ArrayList<>();
    Map<String, String> m78 = new HashMap<>();
    m78.put("dc", "123");
    m78.put("x", "70");
    List<Map> lm8 = new ArrayList<>();
    Map<String, String> m79 = new HashMap<>();
    m79.put("dc", "123");
    m79.put("y", "80");
    List<Map> lm9 = new ArrayList<>();
    Map<String, String> m70 = new HashMap<>();
    m70.put("dc", "123");
    m70.put("z", "90");

    lm7.add(m78);
    lm8.add(m79);
    lm9.add(m70);

//    LinkedHashMap<Object, Map> lhm = toHashMap("day", lm);
//    System.out.println(lhm);

    System.out.println("===");
    String[] mergeFields = { "cl", "count" };
    LinkedHashMap<Object, Map> lhm2 = toHashMapWithMerging("day", lm, mergeFields, (l) -> {
      return header.get(l);
    }, MERGE_WITH_KEEP_POLICY);
    System.out.println("lhm2" + lhm2);
    System.out.println("===");
    LinkedHashMap<Object, Map> lhm3 = merge("day", lm, lm2);
    System.out.println("lhm3" + lhm3);
    System.out.println("===");
    String[] mf = {"stlc", "c"};
    LinkedHashMap l =
        //toHashMapWithMerging("dc", lm3, mf, (dc) -> "x" + dc, (stlc) -> "y" + stlc, MERGE_WITH_KEEP_POLICY);
        reduceWithMergeFields("dc", lm3, mf, /* (dc) -> "x" + dc */null, (stlc) -> "y" + stlc,
                              MERGE_WITH_REMOVE_POLICY);
    System.out.println("list=" + l);
    LinkedHashMap merge = merge("dc", l, lm4, List.class);
    System.out.println("merged=" + merge);

    LinkedHashMap l3 = reduce("dc", lm5);
    System.out.println("reduced=" + l3);
    System.out.println("lm6 = " + lm6);
    LinkedHashMap<Object, List<Map>> l4 = mergeToList("dc", lm6);
    System.out.println("l4 = " + l4);

    LinkedHashMap<Object, Map> l5 = zip("dc", lm7, lm8, lm9);
    System.out.println("l5 = " + l5);
  }
}
