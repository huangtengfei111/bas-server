package app.services.pb;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import app.models.pb.Pbill;

/**
 * @author
 */
public interface PbillService {
  public static final Integer SHOW_PEER_NUM = 1;

  public List<Map> doImport(String pbillImportId, Long caseId, String fileName, InputStream inputStream);
  
  public List<Map> connections(String json, Long caseId) throws Exception;

  public void afterImport(Long caseId, Map<String, Pbill> pbills);

}