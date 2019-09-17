package app.util.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlExport {
  private static final Logger log = LoggerFactory.getLogger(MysqlExport.class);
  private static final String DEFAULT_BACKUP_DIR = "backups";
  public static final String ADD_IF_NOT_EXISTS = "ADD_IF_NOT_EXISTS";

  private String dirName;

  public MysqlExport(String backupDir) {
    this.dirName = (backupDir == null) ? DEFAULT_BACKUP_DIR : backupDir;
  }

  /**
   * This is the entry point for exporting the database. It performs validation
   * and the initial object initializations, database connection and setup before
   * ca
   * 
   * @throws IOException            exception
   * @throws SQLException           exception
   * @throws ClassNotFoundException exception
   */
  public void export(Connection connection) throws IOException, SQLException, ClassNotFoundException {

    String database = connection.getCatalog();
    String sql = exportToSql(connection);

    File file = new File(this.dirName);
    if (!file.exists()) {
      boolean res = file.mkdirs();
      if (!res) {
        log.error("Unable to create backup dir: " + file.getAbsolutePath());
        throw new IOException("Unable to create temp dir: " + file.getAbsolutePath());
      }
    }

    String sqlFileName = getSqlFilename(database);
    FileOutputStream outputStream = new FileOutputStream(this.dirName + "/" + sqlFileName);
    outputStream.write(sql.getBytes());
    outputStream.close();
  }

  /**
   * This function will generate the insert statements needed to recreate the
   * table under processing.
   * 
   * @param table the table to get inserts statement for
   * @return String generated SQL insert
   * @throws SQLException exception
   */
  private String getDataInsertStatement(String table, Connection connection)
      throws SQLException {

    StringBuilder sql = new StringBuilder();

    Statement statement = connection.createStatement();
    ResultSet rs = statement
        .executeQuery("SELECT * FROM " + "`" + table + "`;");

    // move to the last row to get max rows returned
    rs.last();
    int rowCount = rs.getRow();
    // there are no records just return empty string
    if (rowCount <= 0) {
      return sql.toString();
    }

    sql.append("\n--").append("\n-- Inserts of ").append(table)
        .append("\n--\n\n");

    // temporarily disable foreign key constraint
    sql.append("\n/*!40000 ALTER TABLE `").append(table)
        .append("` DISABLE KEYS */;\n");

    sql.append("\n--\n").append(MysqlBackupBase.SQL_START_PATTERN)
        .append(" table insert : ").append(table).append("\n--\n");

    sql.append("INSERT INTO `").append(table).append("`(");

    ResultSetMetaData metaData = rs.getMetaData();

    int columnCount = metaData.getColumnCount();

    // generate the column names that are present
    // in the returned result set
    // at this point the insert is INSERT INTO (`col1`, `col2`, ...)
    for (int i = 0; i < columnCount; i++) {
      sql.append("`").append(metaData.getColumnName(i + 1)).append("`, ");
    }

    // remove the last whitespace and comma
    sql.deleteCharAt(sql.length() - 1).deleteCharAt(sql.length() - 1)
        .append(") VALUES \n");

    // now we're going to build the values for data insertion
    rs.beforeFirst();
    while (rs.next()) {
      sql.append("(");
      for (int i = 0; i < columnCount; i++) {

        int columnType = metaData.getColumnType(i + 1);
        int columnIndex = i + 1;

        // this is the part where the values are processed based on their type
        if (Objects.isNull(rs.getObject(columnIndex))) {
          sql.append("").append(rs.getObject(columnIndex)).append(", ");
        } else if (columnType == Types.INTEGER || columnType == Types.TINYINT
            || columnType == Types.BIT) {
          sql.append(rs.getInt(columnIndex)).append(", ");
        } else {

          String val = rs.getString(columnIndex);
          // escape the single quotes that might be in the value
          val = val.replace("'", "\\'");

          sql.append("'").append(val).append("', ");
        }
      }

      // now that we're done with a row
      // let's remove the last whitespace and comma
      sql.deleteCharAt(sql.length() - 1).deleteCharAt(sql.length() - 1);

      // if this is the last row, just append a closing
      // parenthesis otherwise append a closing parenthesis and a comma
      // for the next set of values
      if (rs.isLast()) {
        sql.append(")");
      } else {
        sql.append("),\n");
      }
    }

    // now that we are done processing the entire row
    // let's add the terminator
    sql.append(";");

    sql.append("\n--\n").append(MysqlBackupBase.SQL_END_PATTERN)
        .append(" table insert : ").append(table).append("\n--\n");

    // enable FK constraint
    sql.append("\n/*!40000 ALTER TABLE `").append(table)
        .append("` ENABLE KEYS */;\n");

    return sql.toString();
  }

  /**
   * This will generate the SQL statement for creating the table supplied in the
   * method signature
   * 
   * @param table the table concerned
   * @return String
   * @throws SQLException exception
   */

  private String getTableInsertStatement(String table) throws SQLException {

    StringBuilder sql = new StringBuilder();

//    boolean addIfNotExists = Boolean
//        .parseBoolean(properties.containsKey(ADD_IF_NOT_EXISTS)
//            ? properties.getProperty(ADD_IF_NOT_EXISTS, "true")
//            : "true");
    boolean addIfNotExists = true;

    if (table != null && !table.isEmpty()) {
      List<Map> listMap = Base
          .findAll("SHOW CREATE TABLE " + "`" + table + "`;");
      for (Map map : listMap) {
        String qtbl = map.get("Table").toString();
        String query = map.get("Create Table").toString();
        sql.append("\n\n--");
        sql.append("\n").append(MysqlBackupBase.SQL_START_PATTERN)
            .append("  table dump : ").append(qtbl);
        sql.append("\n--\n\n");

        if (addIfNotExists) {
          query = query.trim().replace("CREATE TABLE",
              "CREATE TABLE IF NOT EXISTS");
        }

        sql.append(query).append(";\n\n");
      }

      sql.append("\n\n--");
      sql.append("\n").append(MysqlBackupBase.SQL_END_PATTERN)
          .append("  table dump : ").append(table);
      sql.append("\n--\n\n");
    }

    return sql.toString();
  }

  /**
   * This is the entry function that'll coordinate getTableInsertStatement() and
   * getDataInsertStatement() for every table in the database to generate a
   * whole script of SQL
   * 
   * @return String
   * @throws SQLException exception
   */
  private String exportToSql(Connection connection) throws SQLException {
    String database = connection.getCatalog();
    StringBuilder sql = new StringBuilder();
    sql.append("--");
    sql.append("\n-- Generated by BAS MYSQL-BACKUP");
    sql.append("\n-- Date: ")
        .append(new SimpleDateFormat("d-M-Y H:m:s").format(new Date()));
    sql.append("\n--");

    // these declarations are extracted from HeidiSQL
    sql.append(
        "\n\n/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;")
        .append("\n/*!40101 SET NAMES utf8 */;")
        .append("\n/*!50503 SET NAMES utf8mb4 */;")
        .append(
            "\n/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;")
        .append(
            "\n/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");

    // get the tables that are in the database
    List<String> tables = MysqlBackupBase.getAllTables(database);

    // for every table, get the table creation and data
    // insert statement
    for (String s : tables) {
      try {
        sql.append(getTableInsertStatement(s.trim()));
        sql.append(getDataInsertStatement(s.trim(), connection));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    sql.append("\n/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;").append(
        "\n/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;")
        .append(
            "\n/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");

    return sql.toString();
  }



  /**
   * This will get the final output sql file name.
   * 
   * @return String
   */
  public String getSqlFilename(String database) {
    return new SimpleDateFormat("yyyyMMdd-HHmmSS").format(new Date()) + "_"
        + database + "_database.backup";
  }

//  public File getGeneratedZipFile() {
//    if (generatedZipFile != null && generatedZipFile.exists()) {
//      return generatedZipFile;
//    }
//    return null;
//  }
}
