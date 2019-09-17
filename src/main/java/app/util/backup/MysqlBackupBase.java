package app.util.backup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;

public class MysqlBackupBase {
  static final String SQL_START_PATTERN = "-- start";
  static final String SQL_END_PATTERN = "-- end";

  public static void main(String[] args) {
    System.out.println("11");
//    Connection connection = Base.connection();
//    System.out.println("connection.getCatalog()");
  }
  
  static List<String> getPartTables(String database) throws SQLException {
    List<String> table = new ArrayList<>();
    List<Map> lm = Base
        .findAll("SHOW TABLE STATUS FROM `" + database + "` WHERE Name NOT IN ('cell_towers','call_attributions');");
    for (Map map : lm) {
      table.add(map.get("Name").toString());
    }
    return table;
  }

  /**
   * This is a utility function to get the names of all the tables that're in
   * the database supplied
   * 
   * @param database the database name
   * @param stmt     Statement object
   * @return List\<String\>
   * @throws SQLException exception
   */
  static List<String> getAllTables(String database) throws SQLException {
    List<String> table = new ArrayList<>();
    List<Map> lm = Base.findAll("SHOW TABLE STATUS FROM `" + database + "`;");
    for (Map map : lm) {
      table.add(map.get("Name").toString());
    }
    return table;
  }

  /**
   * This function is an helper function that'll generate a DELETE FROM
   * database.table SQL to clear existing table
   * 
   * @param database database
   * @param table    table
   * @return String sql to delete the all records from the table
   */
  static String getEmptyTableSQL(String database, String table) {
    //@formatter:off
    String safeDeleteSQL = "SELECT IF( \n" + 
                             "(SELECT COUNT(1) as table_exists FROM information_schema.tables \n" + 
                             "WHERE table_schema='" + database + "' AND table_name='" + table + "') > 1, \n" + 
                             "'DELETE FROM " + table + "', \n" + "'SELECT 1') INTO @DeleteSQL; \n" + 
                             "PREPARE stmt FROM @DeleteSQL; \n" + "EXECUTE stmt; DEALLOCATE PREPARE stmt; \n";
    //@formatter:on

    return "\n" + SQL_START_PATTERN + "\n" + safeDeleteSQL + "\n" + "\n"
        + SQL_END_PATTERN + "\n";

  }
}
