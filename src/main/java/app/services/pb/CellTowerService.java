package app.services.pb;

import java.util.List;
import java.util.Map;

import app.models.pb.CellTower;

public interface CellTowerService {

  public Map<String, List> smartLookup(List<String> codes) throws Exception;

  public CellTower smartLookup(String code, boolean localOnly) throws Exception;
}
