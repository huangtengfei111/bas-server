package app.config.freemarker;

import java.util.List;
import java.util.concurrent.ExecutionException;

import app.services.pb.NumConnectionCache;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class GetNumConnectionMethod implements TemplateMethodModelEx {

  private NumConnectionCache numConnCache = NumConnectionCache.getInstance();

  @Override
  public Object exec(List args) throws TemplateModelException {
    if (args.size() < 2)
      return null;

    Long caseId = Long.parseLong(args.get(0).toString());
    String pnum = args.get(1).toString();
    try {
      return numConnCache.get(caseId, pnum);
    } catch (ExecutionException e) {
      throw new TemplateModelException(e);
    }
  }

}
