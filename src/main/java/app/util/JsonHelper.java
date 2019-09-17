package app.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author
 */
public class JsonHelper {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static Map toMap(String json) {
    if (json == null)
      return null;
    try {
      return mapper.readValue(json, Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map toMapWithIgnores(String json, String[] ignoreFields) {
    try {
      Map map = mapper.readValue(json, Map.class);
      for (String field : ignoreFields) {
        map.remove(field);
      }
      return map;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map toMapWithIncludes(String json, String[] includeFields) {
    try {
      Map map = mapper.readValue(json, Map.class);
      Map nmap = new HashMap();
      for (String field : includeFields) {
        nmap.put(field, map.get(field));
      }
      return nmap;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map[] toMaps(String json) {
    try {
      return mapper.readValue(json, Map[].class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}