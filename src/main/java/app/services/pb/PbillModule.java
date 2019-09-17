package app.services.pb;

import com.google.inject.AbstractModule;

/**
 * @author
 */
public class PbillModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PbillService.class).to(PbillServiceImpl.class).asEagerSingleton();
    bind(PbillStatService.class).to(PbillStatServiceImpl.class).asEagerSingleton();
    bind(BackupNumsAnalyzer.class).to(BackupNumsAnalyzerImpl.class).asEagerSingleton();
    bind(CellTowerService.class).to(CellTowerServiceImpl.class).asEagerSingleton();
  }
}