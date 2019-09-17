package app.controllers.filters;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import app.models.ApiTrack;
import app.models.Setting;
import app.util.http.HttpClientPool;
import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;
import app.util.license.client.LockServerParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;

public class TrackApiFilter extends HttpSupportFilter {
  private static final Logger log =
      LoggerFactory.getLogger(TrackApiFilter.class);
  private String path = "/track-api";

  @Override
  public void after() {
    if (!Setting.isSuperNode()) {
      String controller = getRoute().getController().getClass().getName();
      String action = getRoute().getActionName() + ":" + method();

      try {
        LicenseClientParam param = BASLicenseManager.loadClientParam();
        if (param != null) {
          LicenseManager manager = new BASLicenseManager(param);
          LicenseContent licenseContent = manager.verify();
          if (licenseContent != null) {
            String endpoint = Setting.centerBasHostPort();
            LockServerParam lsp = ((LockServerParam) licenseContent.getExtra());
            String appId = lsp.getSystemId().toString();
            String appKey = lsp.getHostId();

            URI uri = new URIBuilder().setScheme("http").setHost(endpoint).setPath(this.path).build();

            JSONObject json = new JSONObject();
            json.put("app_id", appId);
            json.put("app_key", appKey);
            json.put("controller", controller);
            json.put("action", action);
            long timeMark0 = System.currentTimeMillis();
//            AsyncHttpClient.instance().post(uri.toString(), json.toString());
            CloseableHttpClient httpclient = HttpClientPool.getClient();
            long timeMark1 = System.currentTimeMillis();
            // TODO : 打印响应的日志信息
            ResponseHandler<ApiTrack> rh = new ResponseHandler<ApiTrack>() {
              @Override
              public ApiTrack handleResponse(HttpResponse response)
                  throws ClientProtocolException, IOException {
                return null;
              }
            };
            StringEntity s =
                new StringEntity(json.toString(), ContentType.APPLICATION_JSON
                    .withCharset(StandardCharsets.UTF_8));

            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(s);
            httppost.setHeader("Accept-encoding", "UTF-8");
            long timeMark2 = System.currentTimeMillis();
            httpclient.execute(httppost, rh);
            long timeMark3 = System.currentTimeMillis();
            log.debug("timeMark0 : {}", timeMark0);
            log.debug("timeMark1 : {}", timeMark1);
            log.debug("timeMark2 : {}", timeMark2);
            log.debug("timeMark3 : {}", timeMark3);
            log.debug("timeMark0 to timeMark1 : {}", timeMark1 - timeMark0);
            log.debug("timeMark1 to timeMark2 : {}", timeMark2 - timeMark1);
            log.debug("timeMark2 to timeMark3 : {}", timeMark3 - timeMark2);
            log.debug("timeMark0 to timeMark3 : {}", timeMark3 - timeMark0);
          }
          }
      } catch (Exception e) {
        logError(e);
      }
    }
  }
}

