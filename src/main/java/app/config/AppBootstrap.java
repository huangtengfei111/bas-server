package app.config;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.javalite.activejdbc.Configuration;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.Registry;
import org.javalite.activejdbc.connection_config.ConnectionJdbcSpec;
import org.javalite.activejdbc.connection_config.ConnectionSpec;
import org.javalite.activeweb.AppContext;
import org.javalite.activeweb.Bootstrap;
import org.javalite.app_config.AppConfig;
import org.javalite.db_migrator.DbUtils;
import org.javalite.db_migrator.MigrationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import app.services.ChineseCitiesProvider;
import app.services.CitizenModule;
import app.services.pb.NumConnectionCache;
import app.services.pb.PbillModule;
import app.util.http.BasHttpClient;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;
import app.util.license.client.NTPClientThread;
/**
 * @author
 */
public class AppBootstrap extends Bootstrap {
  private static final Logger log = LoggerFactory.getLogger(AppBootstrap.class);

  private static final String SUBJECT = "lic.subject";
  private static final String PUBLIC_ALIAS = "lic.public.alias";
  private static final String STORE_PASS = "lic.store.pass";

  public void init(AppContext context) {

    List<String> ntpPeers = AppConfig.getProperties("ntp.peers");
    Integer ntpPollMs = AppConfig.pInteger("ntp.pollms");
    try {
      DB db = new DB();
      Configuration config = Registry.instance().getConfiguration();
      ConnectionSpec spec = config.getCurrentConnectionSpec();

      ConnectionJdbcSpec jdbcSpec = (ConnectionJdbcSpec) spec;
      createDB(jdbcSpec);


      db.open();
      DbUtils.attach(db.getConnection());
      migrateDB();
      initCache();

      // init http client pool
      BasHttpClient.touch();

      db.close();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }



//    log.info("Setup shiro security manager ...");
//  	Factory<SecurityManager> factory = new IniSecurityManagerFactory();
//		SecurityManager securityManager = factory.getInstance();
//		SecurityUtils.setSecurityManager(securityManager);


    // Start NTP client thread
    try {
      NTPClientThread ntp = new NTPClientThread(ntpPeers, ntpPollMs);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    // Load license manager
    LicenseClientParam param = BASLicenseManager.loadClientParam();
    if (param != null) {
      BASLicenseManager licenseManager = new BASLicenseManager(param);
      try {
        licenseManager.verify();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      log.error("NO LICENSE FOUND");
    }

  }

  @Override
  public Injector getInjector() {
    return Guice.createInjector(new PbillModule(), 
    	                          new CitizenModule());
  }

  /**
   * @param root open connection to root URL
   */
  private void openConnection(ConnectionJdbcSpec jdbcSpec, boolean root) throws IOException {
    String url = jdbcSpec.getUrl();
    String driver = jdbcSpec.getDriver();
    String username = jdbcSpec.getUser();
    String password = jdbcSpec.getPassword();
    url = root ? DbUtils.extractServerUrl(url) : url;
    DbUtils.openConnection(driver, url, username, password);
  }

  private void createDB(ConnectionJdbcSpec jdbcSpec) throws IOException {
    openConnection(jdbcSpec, true);
    String databaseName = DbUtils.extractDatabaseName(jdbcSpec.getUrl());
    if (!DbUtils.dbExists(databaseName)) {
      String createSql = "create database %s";
      DbUtils.exec(format(createSql, databaseName));
    }
    DbUtils.closeConnection();
  }

  private void migrateDB() {
    String migrationsPath = AppConfig.p("database.migrations");
    log.info("Do migration from {} ", migrationsPath);
    try {
      if (migrationsPath != null && new File(migrationsPath).exists()) {
        File migrationsDir = new File(migrationsPath);
        if (migrationsDir.exists()) {
          new MigrationManager(migrationsPath).migrate("UTF-8");
          // FileUtils.deleteQuietly(migrationsDir);
        }
      }
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
    }
  }

  private void initCache() {
    try {
      // NumRelCache inCommonsCache = NumRelCache.getInstance();
      // inCommonsCache.loadAll();
      NumConnectionCache numConnectionCache = NumConnectionCache.getInstance();
      numConnectionCache.loadAll();
      ChineseCitiesProvider ccp = ChineseCitiesProvider.getInstance();
      ccp.load();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }

}
