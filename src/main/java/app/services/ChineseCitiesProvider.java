package app.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import app.services.pb.NumConnectionCache;
import io.vavr.Tuple2;

public class ChineseCitiesProvider {
  private static Map<String, String> provinces = null;
  private static SetMultimap<String, Tuple2<String, String>> cities = null;
  private static ChineseCitiesProvider instance = null;

  private ChineseCitiesProvider() {
  }

  public static ChineseCitiesProvider getInstance() {
    if (instance == null) {
      synchronized (NumConnectionCache.class) {
        if (instance == null) {
          provinces = new HashMap<>();
          cities = HashMultimap.create();
          instance = new ChineseCitiesProvider();
        }
      }
    }
    return instance;
  }

  public Map<String, String> provinces() throws IOException {
    return provinces;
  }

  public Set<Tuple2<String, String>> cities(String provinceCode) {
    Set<Tuple2<String, String>> set = cities.get(provinceCode);
    return set;
  }

  public void load() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("provinces.json");
    String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    List<Map> lm1 = JSONObject.parseArray(json, Map.class);
    for (Map map : lm1) {
      provinces.put(map.get("name").toString(), map.get("code").toString());
    }
    
    inputStream = classLoader.getResourceAsStream("cities.json");
    json        = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    List<Map> lm = JSONObject.parseArray(json, Map.class);
    for (Map map : lm) {
      Tuple2<String, String> tuple2 = new Tuple2<>(map.get("name").toString(),
          map.get("code").toString());
      cities.put(map.get("provinceCode").toString(), tuple2);
    }
  }
}
