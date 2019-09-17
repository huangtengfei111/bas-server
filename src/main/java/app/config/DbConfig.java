package app.config;

import org.javalite.activeweb.AbstractDBConfig;
import org.javalite.activeweb.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DbConfig extends AbstractDBConfig {
  private static final Logger log = LoggerFactory.getLogger(DbConfig.class);

  public void init(AppContext context) {

    String file = System.getProperty("env.connections.file");
    if (file == null) {
      file = "/database.properties";
    }

    configFile(file);

    // environment("production").jndi("jdbc/simple_production");
  }
}
