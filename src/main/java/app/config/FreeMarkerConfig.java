package app.config;

import org.javalite.activeweb.freemarker.AbstractFreeMarkerConfig;

import app.config.freemarker.GetCellTowerMethod;
import app.config.freemarker.GetNumConnectionMethod;
import app.config.freemarker.MergeIfOverlapMethod;
import app.config.freemarker.StatOnBreakpointMethod;

/**
 * @author
 */
public class FreeMarkerConfig extends AbstractFreeMarkerConfig {
  @Override
  public void init() {
    // this is to override a strange FreeMarker default processing of numbers
    getConfiguration().setNumberFormat("0.####");
    getConfiguration().setDateFormat("yyyy-MM-dd");
    getConfiguration().setTimeFormat("HH:mm:ss");
    getConfiguration().setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
    // getConfiguration().setAPIBuiltinEnabled(true);
    getConfiguration().setSharedVariable("getCellTower", new GetCellTowerMethod());
    getConfiguration().setSharedVariable("getNumConnection", new GetNumConnectionMethod());
    getConfiguration().setSharedVariable("mergeIfOverlap", new MergeIfOverlapMethod());
    getConfiguration().setSharedVariable("statOnBreakpoint", new StatOnBreakpointMethod());
  }
}
