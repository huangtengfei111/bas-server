package app.controllers.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;
import org.javalite.app_config.AppConfig;

import com.alibaba.fastjson.JSONObject;

import app.controllers.APIController;
import app.util.backup.MysqlExport;
//import app.util.backup.MysqlImport;

@RESTful
public class BackupsController extends APIController {

  @GET
  public void export() throws Exception {
    Connection connection = Base.connection();
    String backupDir = AppConfig.p("database.backup");
    MysqlExport me = new MysqlExport(backupDir);
    me.export(connection);

    setOkView("dump mysql database");
    render("/common/_blank");
  }

  @POST
  public void restore()
      throws IOException, ClassNotFoundException, SQLException {
    JSONObject fileName = JSONObject.parseObject(getRequestString());
    String exportFileName = fileName.getString("name");

    String defaultPath = "tmp/backups/";
    String exportFilePath = defaultPath + exportFileName;
    String importSql = new String(Files.readAllBytes(Paths.get(exportFilePath)));

    Connection connection = Base.connection();
//    MysqlImport mysqlImport = MysqlImport.builder();
//    mysqlImport.importDatabase(connection, importSql);

    setOkView("restore mysql backup");
    render("/common/_blank");
  }

  public void index() {
    String exportFilePath = "tmp/backups";
    File exportFolder = new File(exportFilePath);

    Map<String, Object> exportFilesMap = new HashMap<>();
    if (exportFolder.exists()) {
      File[] exportFiles = exportFolder.listFiles();
      for (int i = 0; i < exportFiles.length; i++) {
        File exportFile = exportFiles[i];
        if (exportFile.isFile()) {
          Date createdAt = new Date(exportFile.lastModified());
          exportFilesMap.put(exportFile.getName(), createdAt);
        }
      }
    }

    logDebug("exportFilesMap : " + exportFilesMap);
    setOkView();
    view("exportFiles", exportFilesMap);
    render();
  }

}
