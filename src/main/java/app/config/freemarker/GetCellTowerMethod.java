package app.config.freemarker;

import java.util.List;

import app.services.pb.CellTowerLocalCache;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class GetCellTowerMethod implements TemplateMethodModelEx {


  private CellTowerLocalCache cellTowerLocalCache = CellTowerLocalCache.getInstance();

  @Override
  public Object exec(List args) throws TemplateModelException {
    if (args.size() == 0)
      return null;

    String code = args.get(0).toString();
    if(code != null) {
      try {
        return cellTowerLocalCache.get(code);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

}
