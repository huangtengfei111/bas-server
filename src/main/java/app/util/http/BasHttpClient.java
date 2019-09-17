package app.util.http;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.models.Setting;

public class BasHttpClient {
  private static final Logger log = LoggerFactory.getLogger(BasHttpClient.class);

  public static void touch() {
    if (Setting.isSuperNode())
      return;

    ResponseHandler<StatusLine> rh = new ResponseHandler<StatusLine>() {
      @Override
      public StatusLine handleResponse(final HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        return statusLine;
      }
    };

    try {
      CloseableHttpClient httpclient = HttpClientPool.getClient();

      final String hostPort = Setting.centerBasHostPort();
      final URI uri = new URIBuilder().setScheme("http").setHost(hostPort).setPath("/ping").build();

      HttpGet httpget = new HttpGet(uri);
      // httppost.setEntity(s);
      httpget.setHeader("Accept-encoding", "UTF-8");
      StatusLine status = httpclient.execute(httpget, rh);
      log.info("Status of ping: {}", status.getStatusCode());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }

  public static void trace() {

  }
}
