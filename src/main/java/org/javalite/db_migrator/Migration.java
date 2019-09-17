package org.javalite.db_migrator;

import static org.javalite.common.Util.blank;
import static org.javalite.db_migrator.DbUtils.exec;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration implements Comparable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);
  private static final String DEFAULT_DELIMITER = ";";
  private static final String DELIMITER_KEYWORD = "DELIMITER";
  private static final String[] COMMENT_CHARS = new String[] { "--", "#", "//" };

  private File migrationFile;
  private String version;

  public Migration(String version, File migrationFile) {
    this.migrationFile = migrationFile;
    this.version       = version;
  }

  public String getVersion() {
    return version;
  }

  public String getName() {
    return migrationFile.getName();
  }

  public void migrate(String encoding) throws Exception {
    List<String> lines = Files.readAllLines(Paths.get(migrationFile.getCanonicalPath()),
                                            encoding != null ? Charset.forName(encoding) : Charset.defaultCharset());
    String delimiter = DEFAULT_DELIMITER;
    List<String> statements = new ArrayList<>();
    try {
      String currentStatement = "";
      for (String line : lines) {
        line = line.trim();
        if (!commentLine(line) && !blank(line)) {
          if (line.startsWith(DELIMITER_KEYWORD)) {
            delimiter = line.substring(10).trim();
          } else if (line.endsWith(delimiter)) {
            currentStatement += line.substring(0, line.length() - delimiter.length());
            if (!blank(currentStatement)) {
              statements.add(currentStatement);
            }
            currentStatement = "";
          } else {
            currentStatement += line + System.getProperty("line.separator");
          }
        }
      }

      if (!blank(currentStatement)) {
        statements.add(currentStatement);
      }

      for (String statement : statements) {
        exec(statement);
      }
    } catch (Exception e) {
      LOGGER.error("Error executing migration file: {}", migrationFile.getCanonicalPath(), e);
      throw e;
    }
  }

  private boolean commentLine(String line) {
    for (String cc : COMMENT_CHARS) {
      if (line.trim().startsWith(cc)) {
        return true;
      }
    }
    return false;
  }

  public int compareTo(Object o) {
    Migration other = (Migration) o;
    return this.getVersion().compareTo(other.getVersion());
  }
}

