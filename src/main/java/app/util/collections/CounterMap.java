package app.util.collections;

import java.util.HashMap;
import java.util.Map;

public class CounterMap {
  private Map<String, MutableInt> freq;

  public CounterMap() {
    freq = new HashMap<String, MutableInt>();
  }

  public void incr(String key) {
    MutableInt count = freq.get(key);
    if (count == null) {
      freq.put(key, new MutableInt());
    } else {
      count.increment();
    }
  }

  public int getCount(String key) {
    MutableInt count = freq.get(key);
    if (count == null) {
      return 0;
    } else {
      return count.get();
    }
  }

  public Map<String, Integer> toMap() {
    Map<String, Integer> map = new HashMap<>();
    for (Map.Entry<String, MutableInt> entry : freq.entrySet()) {
      String key = entry.getKey();
      MutableInt c = entry.getValue();
      map.put(key, c.get());
    }

    return map;
  }
}
