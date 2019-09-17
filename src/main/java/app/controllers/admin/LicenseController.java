package app.controllers.admin;

import static org.javalite.common.Collections.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.controllers.APIController;
import app.exceptions.ErrorCodes;
import app.models.Setting;
import app.util.JsonHelper;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;
import app.util.license.client.LockServerParam;
import app.util.license.client.VersionizedParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;

public class LicenseController extends APIController {

  @GET
  public void showProfile() throws Exception {
    LicenseClientParam param = BASLicenseManager.loadClientParam();
    if (param != null) {
      LicenseManager manager = new BASLicenseManager(param);
      try {
        LicenseContent licenseContent = manager.verify();
        setOkView("show installed");
        view("licenseContent", licenseContent);
        render();
      } catch (Exception e) {
        logError(e.getMessage(), e);
      }
    }

    Map<String, String> profile = new HashMap<>();
    LockServerParam lsp = new LockServerParam();
    lsp.inspect();
    String hostId = lsp.getHostId();
    profile.put("hostId", hostId);
    setOkView("show empty profile");
    view("profile", profile);
    render();
  }

  /**
   * 导出许可证生成参数
   * 
   * @throws IOException
   */
  @POST
  public void downloadProfile() throws Exception {
    String json = getRequestString();
    Map map = JsonHelper.toMap(json);

    Map<String, Object> params = new HashMap<>();
    String plan = map.get("plan").toString();
    String custName = (String) map.get("customer");
    Map<String, Object> holder = new HashMap<>();
    holder.put("name", custName);
    holder.put("city", map.get("city"));
    holder.put("state", map.get("state"));
    holder.put("country", map.get("country"));
    params.put("holder", holder);
    params.put("plan", plan);

    LockServerParam lsp = new LockServerParam(VersionizedParam.V_1, plan);
    try {
      lsp.inspect();
      String hostId = lsp.getHostId();
      params.put("host", lsp);
      params.put("publicAlias", "pub_" + hostId);

      RandomNumberGenerator rng = new SecureRandomNumberGenerator();
      String salt = rng.nextBytes().toString();
      String passwd = new Sha256Hash(hostId, salt, 256).toBase64();
      params.put("password", passwd); // store password

      String fileName = custName + "_" + hostId + ".profile";
      OutputStream out =
          outputStream("application/octet-stream", map("Access-Control-Expose-Headers", "Content-Disposition",
                                                       "Content-Disposition", "attachment;filename=" + fileName),
                       200);
      JSON.writeJSONString(out, params);

//      ZipFile zip = new ZipFile("D:\\test.zip");
//      ZipParameters para = new ZipParameters();
//      para.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
//      para.setFileNameInZip(hostId);
//      para.setSourceExternalStream(true);
//      InputStream is = new ByteArrayInputStream(new String("这是文件内容").getBytes());
//      zip.addStream(is, para);

    } catch (Exception e) {
      logError(e.getMessage(), e);
    }
  }

  @GET
  public void installed() throws IOException {
    Map versionInfo = readGitProperties();
    LicenseClientParam param = BASLicenseManager.loadClientParam();
    if (param != null) {
      LicenseManager manager = new BASLicenseManager(param);
      try {
        LicenseContent licenseContent = manager.verify();
        setOkView("installed license");
        view("licenseContent", licenseContent);
        view("versionInfo", versionInfo);
        render("/admin/license/_license");
      } catch (Exception e) {
        logError(e.getMessage(), e);
        setErrorView("license error", ErrorCodes.LIC_ERROR);
        view("versionInfo", versionInfo);
        render("/admin/license/error");
      }
    } else {
      setErrorView("no license installed", ErrorCodes.LIC_NOT_INSTALLED);
      view("versionInfo", versionInfo);
      render("/admin/license/_license");
    }
  }
  
  @POST
  public void install() throws Exception {
    List<FormItem> formItems = multipartFormItems(); 
    String destDir = basHomeDir() + "/";
    for (FormItem item : formItems) {
      if (item.isFile()) { // handle file
        File file = new File(destDir + item.getFileName());
        inputstreamtofile(item.getInputStream(), file);
        unZipFiles(file, destDir);
//        unzip(item.getInputStream(), basTmpDir());
      }
    }
    String metaPath = FilenameUtils.normalize(basHomeDir() + "/META");
    InputStream is = new FileInputStream(new File(metaPath));
    Properties meta = new Properties();
    meta.load(is);
    LicenseClientParam param = new LicenseClientParam();
    String subject = meta.getProperty("subject");
    String storePass = meta.getProperty("password");
    String publicAlias = meta.getProperty("publicAlias");
    param.setSubject(subject);
    param.setStorePass(storePass);
    param.setPublicAlias(publicAlias);

    String licensePath = FilenameUtils.normalize(basHomeDir() + "/license.lic");
    param.setLicensePath(licensePath);
    String publicCertsKeystorePath = FilenameUtils.normalize(basHomeDir() + "/publicCerts.keystore");
    param.setPublicKeysStorePath(publicCertsKeystorePath);
    try {
      BASLicenseManager manager = new BASLicenseManager(param);
      LicenseContent licenseContent = manager.install();
      if (manager.verify() != null) {
        LockServerParam lsParam = (LockServerParam) licenseContent.getExtra();
        Long systemId = lsParam.getSystemId();
        Setting.setSystemId(systemId);        
        
        setOkView("installed lincense");
        view("licenseContent", licenseContent);
        render("/admin/license/_license");
      }
    } catch (Exception e) {
      logError(e);
      setErrorView("install lincense failed", ErrorCodes.LIC_ERROR);
      render("/common/error");
    }
  }

  @GET
  public void uninstall() {
    LicenseClientParam param = BASLicenseManager.loadClientParam();
    if (param != null) {
      LicenseManager manager = new BASLicenseManager(param);
      try {
        manager.uninstall();
        setOkView("uninstalled license");
        render("/common/_blank");
      } catch (Exception e) {
        logError(e.getMessage(), e);
        setErrorView("uninstall license error", ErrorCodes.LIC_ERROR);
        render("/common/error");
      }
    } else {
      setErrorView("no license installed", ErrorCodes.LIC_NOT_INSTALLED);
      render("/common/error");
    }
  }

  private Map readGitProperties() {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("git.properties");
    try {
      String raw = readFromInputStream(inputStream);
      inputStream.close();
      JSONObject json = JSONObject.parseObject(raw);
      return JSONObject.toJavaObject(json, Map.class);
    } catch (IOException e) {
      logError(e.getMessage(), e);
      return null;
    }
  }

  private String readFromInputStream(InputStream inputStream) throws IOException {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    }
    return resultStringBuilder.toString();
  }

  private void unZipFiles(File zipFile, String descDir) throws IOException {
    File pathFile = new File(descDir);
    if (!pathFile.exists()) {
      pathFile.mkdirs();
    }
    java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile);
    for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      String zipEntryName = entry.getName();
      InputStream in = zip.getInputStream(entry);
      String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
      ;
      // 判断路径是否存在,不存在则创建文件路径
      File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
      if (!file.exists()) {
        file.mkdirs();
      }
      // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
      if (new File(outPath).isDirectory()) {
        continue;
      }
      OutputStream out = new FileOutputStream(outPath);
      byte[] buf1 = new byte[1024];
      int len;
      while ((len = in.read(buf1)) > 0) {
        out.write(buf1, 0, len);
      }
      in.close();
      out.close();
    }
  }

  public void inputstreamtofile(InputStream ins, File file) {
    try {
      OutputStream os = new FileOutputStream(file);
      int bytesRead = 0;
      byte[] buffer = new byte[8192];
      while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
        os.write(buffer, 0, bytesRead);
      }
      os.close();
      ins.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}