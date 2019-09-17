package app.controllers.zuper;

import static org.javalite.app_config.AppConfig.p;
import static org.javalite.common.Collections.map;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.javalite.activeweb.FormItem;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.RESTful;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import app.controllers.APIController;
import app.exceptions.ErrorCodes;
import app.models.License;
import app.util.DateTimeConvert;
import app.util.JsonHelper;
import app.util.license.BASLicenseCreator;

@RESTful
public class LicensesController extends APIController {

  public void index() {
    List<License> licenses = License.where("deleted_at is null and path is not null");

    setOkView("list licenses");
    view("licenses", licenses);
    render();
  }

  public void create() throws Exception {
    String licenseBasedir = p("license.basedir");
    File lbdir = new File(licenseBasedir);
    lbdir.mkdirs();

    String json = getRequestString();
    Map params = JsonHelper.toMap(json);
    int licenseId = (Integer) params.get("license_id");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date expiredAt = sdf.parse((String) params.get("expired_at"));
    expiredAt = DateTimeConvert.atEndOfDay(expiredAt);
    
    int acctLimit = Integer.parseInt(params.get("acct_limit").toString());

    License license = License.findById(licenseId);
    if (license == null) {
      setErrorView("created license failed", ErrorCodes.LIC_MISS_PROFILE);
      render("/common/error");
    } else {
      if (license.getKeyPass() == null) {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        String salt = rng.nextBytes().toString();
        String privateKeyPass = new Sha256Hash(license.getHostId(), salt, 71).toBase64();
        license.setSalt(salt);
        license.setKeyPass(privateKeyPass);
        license.setExpiredAt(expiredAt);
        license.setAcctLimit(acctLimit);
        license.setIssuedAt(new Date());
        license.setIssuedBy(userIdInSession());

        File licenseBin = BASLicenseCreator.generateLicense(licenseBasedir, license);
        if (licenseBin != null) {
          license.setPath(licenseBin.getAbsolutePath());
          license.saveIt();

          setOkView("created license");
          view("id", license.getLongId());
          render("/common/ok");
        } else {
          setErrorView("generate license failed", ErrorCodes.LIC_GEN_FAILED);
          render("/common/error");
        }
      } else {
        setErrorView("created license failed", ErrorCodes.LIC_DIRTY_PROFILE);
        render("/common/error");
      }
    }
  }

  public void show() {
    long id = Long.parseLong(param("license_id"));
    License license = License.findById(id);
    if (license != null) {
      setOkView("show license");
      view("license", license);
      render("_license");
    } else {
      setErrorView("no such license", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    }
  }
  
  public void destroy() {
    long id = Long.parseLong(param("license_id"));
    License license = License.findById(id);
    if(license != null) {
      String basedir = p("license.basedir");
      BASLicenseCreator.removeKeys(basedir, license);

      license.setDeletedAt(new Date());
      if (license.saveIt()) {
        setOkView("deleted license");
        view("id", id);
        render("/common/ok");
      }
    } else {
      setErrorView("no such license", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    }
  }
  
  @POST
  public void uploadProfile() {
    List<FormItem> formItems = multipartFormItems();
    String json = null;
    for (FormItem item : formItems) {
      if (item.isFile()) {
        json = item.getStreamAsString();
        break;
      }
    }
    if (json != null) {
      License license = wrap(json);
      License l = License.findFirst("host_id = ? and deleted_at is null",
          license.getHostId());
      if (l != null && l.getPath() != null) {
        setErrorView("license already exists", ErrorCodes.LIC_EXIST);
        render("/common/error");
      } else {
        if (l != null) {
          license.setId(l.getLongId());
        }
        if (license.saveIt()) {
          setOkView("uploaded");
          view("license", license);
          render("_license");
        }
      }
    }
  }

  @GET
  public void download() throws IOException {
    long id = Long.parseLong(param("license_id"));
    License license = License.findById(id);
    if (license == null) {
      setErrorView("no such license", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    } else {
      String fileName = license.getHostId() + "-license.bin";
      OutputStream out =
          outputStream("application/octet-stream", map("Access-Control-Expose-Headers", "Content-Disposition",
                                                       "Content-Disposition", "attachment;filename=" + fileName),
                       200);
      Path p = Paths.get(license.getPath());
      if (p != null) {
        byte[] contents = Files.readAllBytes(Paths.get(license.getPath()));
        out.write(contents);
      } else {
        setErrorView("no such license in disk", ErrorCodes.LIC_ERROR);
        render("/common/error");
      }
    }
  }

  @POST
  public void upgrade() throws IOException, ParseException {
    String licenseBasedir = p("license.basedir");
    JSONObject jsonObject = JSONObject.parseObject(getRequestString());
    long licenseId = Long.parseLong(param("license_id"));
    License l = License.findById(licenseId);
    License license = License.findFirst("host_id = ? and deleted_at is null",
        l.getHostId());
    if (license == null) {
      setErrorView("no such license", ErrorCodes.LIC_ERROR);
      render("/common/error");
    } else {
      license.setPlan(jsonObject.getString("plan"));
      license.setAcctLimit(Long.parseLong(jsonObject.getString("acct_limit")));
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date expiredAt = sdf.parse((String) jsonObject.get("expired_at").toString());
      license.setExpiredAt(DateTimeConvert.atEndOfDay(expiredAt));
      File licenseBin = BASLicenseCreator.upgradeLicense(licenseBasedir,
          license);

      if (licenseBin != null) {
        license.saveIt();

        setOkView("created license");
        view("id", license.getLongId());
        render("/common/ok");
      } else {
        setErrorView("upgrade license failed", ErrorCodes.LIC_GEN_FAILED);
        render("/common/error");
      }
    }
  }

  private License wrap(String json) {
    License license = new License();
    JSONObject jsonObject = JSONObject.parseObject(json);

    String storePass = jsonObject.getString("password");
    String plan = jsonObject.getString("plan");
    JSONObject host = JSON.parseObject(jsonObject.getString("host"));

    String hostId = host.getString("hostId");
    String systemSN = host.getString("systemSN");
    String baseboardInfo = host.getString("baseboardInfo");
    String processorInfo = host.getString("processorInfo");

    JSONArray jsonAllowedIPAddress = host.getJSONArray("allowedIPAddress");
    List<String> allowedIPAddress = jsonAllowedIPAddress.toJavaList(String.class);
    JSONArray jsonAllowedMacAddress = host.getJSONArray("allowedMacAddress");
    List<String> allowedMacAddress = jsonAllowedMacAddress.toJavaList(String.class);

    String publicAlias = jsonObject.getString("publicAlias");
    JSONObject jsonHolder = jsonObject.getJSONObject("holder");
    String name = jsonHolder.getString("name");
    String country = jsonHolder.getString("country");
    String state = jsonHolder.getString("state");
    String city = jsonHolder.getString("city");

    String holder = "CN=" + name + ",OU=" + name + ",L=" + city + ",ST=" + state + ",C=" + country;
    String privateAlias = "pk_" + hostId;

    //@formatter:off
    String[] attributes = { "host_id", "holder", "system_sn", "baseboard_info",
                            "processor_info", "mac_address", "ip_address", "plan",
                            "public_alias", "store_pass", "private_alias"};
    String macAddress = String.join(",", allowedMacAddress);
    String ipAddress = String.join(",", allowedIPAddress);
    Object[] values = { hostId, holder, systemSN, baseboardInfo, processorInfo,
                        macAddress, ipAddress, plan, publicAlias, storePass, privateAlias};
    //@formatter:on
    license.set(attributes, values);

    return license;
  }

}
