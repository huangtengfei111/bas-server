package app.util.ct;

import java.util.List;
import java.util.Map;

import app.models.pb.CellTower;

public abstract class GeoLocQueryAdapter {
  public static final String WGS84_COR = "0";
  public static final String GMAPS_COORD = "1";
  public static final String BMAPS_COORD = "2";
  public static final String BAS_COORD = "3";
  public static final int CHINA_MCC = 460;

  public abstract List<Map> doQuery(List<String> codes, String fmt, String cor) throws Exception;

  public abstract List<Map> doQuery(List<String> codes) throws Exception;

  public abstract CellTower doQuery(Long mnc, Long lac, Long ci, String cor)
      throws Exception;
}
