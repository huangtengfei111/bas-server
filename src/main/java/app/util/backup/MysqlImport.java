package app.util.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlImport {
  private Logger log = LoggerFactory.getLogger(MysqlImport.class);
  private boolean deleteExisting;
  private boolean dropExisting;

  private MysqlImport() throws IOException {
    this.deleteExisting = true;
    this.dropExisting = true;
  }

  public static MysqlImport builder() throws IOException {
    return new MysqlImport();
  }

  public boolean restore(Connection connection, String backupFile) {
    try {
      String sql = new String(Files.readAllBytes(Paths.get(backupFile)));
      importDatabase(connection, sql);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean importDatabase(Connection connection, String sqlString)
      throws SQLException, ClassNotFoundException {
    if (sqlString == null || sqlString.trim().equals(""))
      return true;

    String database = connection.getCatalog();
    // connect to the database
    Statement stmt = connection.createStatement();
    // set max_allowed_packet
    stmt.execute("SET GLOBAL max_allowed_packet=1073741824"); // set 1G

    if (deleteExisting || dropExisting) {

      // get all the tables, so as to eliminate delete errors due to
      // non-existent tables
      List<String> tables = MysqlBackupBase.getAllTables(database);
      log.debug("tables found for deleting/dropping: \n" + tables.toString());

      // execute delete query
      for (String table : tables) {

        // if deleteExisting and dropExisting is true
        // skip the deleteExisting query
        // dropExisting will take care of both
        if (deleteExisting && !dropExisting) {
          String delQ = "DELETE FROM " + "`" + table + "`;";
          log.debug("adding " + delQ + " to batch");
          stmt.addBatch(delQ);
        }

        if (dropExisting) {
          String dropQ = "DROP TABLE IF EXISTS " + "`" + table + "`";
          log.debug("adding " + dropQ + " to batch");
          stmt.addBatch(dropQ);
        }
      }
    }

    // disable foreign key check
    stmt.addBatch("SET FOREIGN_KEY_CHECKS = 0");

    // now process the sql string supplied
    while (sqlString.contains(MysqlBackupBase.SQL_START_PATTERN)) {

      // get the chunk of the first statement to execute
      int startIndex = sqlString.indexOf(MysqlBackupBase.SQL_START_PATTERN);
      int endIndex = sqlString.indexOf(MysqlBackupBase.SQL_END_PATTERN);

      String executable = sqlString.substring(startIndex, endIndex);
      log.debug("adding extracted executable SQL chunk to batch : \n" + executable);
      stmt.addBatch(executable);

      // remove the chunk from the whole to reduce it
      sqlString = sqlString.substring(endIndex + 1);

      // repeat
    }

    // add enable foreign key check
    stmt.addBatch("SET FOREIGN_KEY_CHECKS = 1");

    // now execute the batch
//    long[] result = stmt.executeLargeBatch();
    long[] result = stmt.executeLargeBatch();

    String resultString = Arrays.stream(result).mapToObj(String::valueOf)
        .reduce("", (s1, s2) -> s1 + ", " + s2 + ", ");
    log.debug(result.length
        + " queries were executed in batches for provided SQL String with the following result : \n"
        + resultString);

    stmt.close();
    connection.close();

    return true;
  }
}
