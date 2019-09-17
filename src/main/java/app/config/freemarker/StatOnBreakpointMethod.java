package app.config.freemarker;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class StatOnBreakpointMethod implements TemplateMethodModelEx {

  @Override
  public Object exec(List args) throws TemplateModelException {
    Long caseId = (Long) args.get(0);
    String num = (String) args.get(1);

    return null;
  }

}
