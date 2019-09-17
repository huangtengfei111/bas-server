package app.util.ct;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import app.exceptions.APIRuntimeException;
import app.models.pb.CellTower;
import app.util.http.HttpClientPool;

public class BasGeoLocQueryAdapter extends GeoLocQueryAdapter {
  private Logger log = LoggerFactory.getLogger(BasGeoLocQueryAdapter.class);
  private static final String SRC = "bas.geoloc";
  private String basHost;
  private String appId;
  private String appKey;
  private String path;
  private int reqBatchSize;

  private static final ObjectMapper mapper = new ObjectMapper();

  public BasGeoLocQueryAdapter() {
    this.basHost = "47.111.163.155:9091";
    this.path = "/cell-towers/multi-locs";
    this.reqBatchSize = 10;
  }

  public BasGeoLocQueryAdapter(String basHost, String appId, String appKey) {
    this.basHost      = basHost;
    this.appId        = appId;
    this.appKey       = appKey;
    this.path         = "/cell-towers/multi-locs";
    this.reqBatchSize = 400;
  }

  public BasGeoLocQueryAdapter(String basHost, String appId, String appKey, int reqBatchSize) {
    this.basHost      = basHost;
    this.appId        = appId;
    this.appKey       = appKey;
    this.path         = "/cell-towers/multi-locs";
    this.reqBatchSize = reqBatchSize > 1000 ? 1000 : reqBatchSize;
  }

  @Override
  public List<Map> doQuery(List<String> codes) throws Exception {
    return doQuery(codes, "16", BAS_COORD);
  }

  @Override
  public List<Map> doQuery(List<String> codes, String fmt, String cor) throws Exception {

    URI uri = new URIBuilder().setScheme("http").setHost(this.basHost).setPath(this.path)
        .build();
//    long timeMark0 = System.currentTimeMillis();
//    log.debug("timeMarker0 : {}", timeMark0);

//    CloseableHttpClient httpclient = HttpClients.createDefault();
    CloseableHttpClient httpclient = HttpClientPool.getClient();

//    long timeMark1 = System.currentTimeMillis();
//    log.debug("timeMarker1 : {}", timeMark1);

    ResponseHandler<List<Map>> rh = new ResponseHandler<List<Map>>() {

      @Override
      public List<Map> handleResponse(final HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
          throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        if (entity == null) {
          throw new ClientProtocolException("Response contains no content");
        }

        ContentType contentType = ContentType.getOrDefault(entity);
        Charset charset = contentType.getCharset();
        charset = (charset == null) ? StandardCharsets.UTF_8 : charset;

        Reader reader = new InputStreamReader(entity.getContent(), charset);
        Map result = mapper.readValue(reader, Map.class);
        reader.close();

//        long timeMark3 = System.currentTimeMillis();
//        log.debug("timeMarker3 : {}", timeMark3);
//        log.debug("timeMarker3 to timeMarker1 : {}", timeMark3 - timeMark1);
        if (result.get("meta") != null) {
          Map meta = (Map) result.get("meta");
          boolean success = (boolean) meta.get("success");
          if (success) {
            return (List<Map>) result.get("data");
          } else {
            String msg = (String) meta.get("message");
            throw new APIRuntimeException(msg);
          }
        } else {
          return null;
        }
      }
    };

    List<Map> ret = new ArrayList<>();
    int batches = (codes.size() / reqBatchSize) + (codes.size() % reqBatchSize == 0 ? 0 : 1);
    JSONObject json = new JSONObject();
    try {
      long timeMark5 = System.currentTimeMillis();
      for (int i = 0; i < batches; i++) {
        int fromIndex = i * reqBatchSize;
        int toIndex = (i + 1) * reqBatchSize > codes.size() ? codes.size() : (i + 1) * reqBatchSize;
        List<String> reqCodes = codes.subList(fromIndex, toIndex);
        json.clear();
        json.put("codes", reqCodes);
        json.put("fmt", fmt);
        json.put("cor", cor);
        json.put("app_id", this.appId);
        json.put("app_key", this.appKey);
        StringEntity s =
            new StringEntity(json.toString(), ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8));

        HttpPost httppost = new HttpPost(uri);
        httppost.setEntity(s);
        httppost.setHeader("Accept-encoding", "UTF-8");
//        long timeMark2 = System.currentTimeMillis();
//        log.debug("timeMark2 : {}", timeMark2);
//        log.debug("timeMark1 to timeMark2 : {}", timeMark2 - timeMark1);
        List<Map> r = httpclient.execute(httppost, rh);

        ret.addAll(r);
      }
//      long timeMark4 = System.currentTimeMillis();
//      log.debug("timeMark5 to timeMark4----{}", (timeMark5 - timeMark4));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

//    long timeMark10 = System.currentTimeMillis();
//    log.debug("timeMark1 to timeMark0----{}", (timeMark1 - timeMark0));
//    log.debug("http request takes----{}", (timeMark10 - timeMark0));
    return ret;
  }

  @Override
  public CellTower doQuery(Long mnc, Long lac, Long ci, String cor)
      throws Exception {
    CellTower ct = new CellTower();
    List<String> codes = new ArrayList<>();
//    codes.add(":" + mnc + ":" + lac + ":" + ci);
    codes.add( lac + ":" + ci + ":" + mnc);
    List<Map> ret = this.doQuery(codes, "10", cor);

    if (ret != null && ret.size() > 0) {
      Map map = ret.get(0);
      map.put("source", SRC);
      ct.fromMap(map);
      return ct;
    } 
    return null;
  }

  public static void main(String[] args) throws Exception {
    List<String> codes = new ArrayList<>();
    codes.add("6777:50A7:0");
    codes.add("5770:4D0D:0");
    codes.add("6777:4FED:0");
    codes.add("5770:98FF:0");
    codes.add("5872:28FD:0");

    for (String code : codes) {
      BasGeoLocQueryAdapter adapter = new BasGeoLocQueryAdapter();
      List<String> bs = new ArrayList<>();
      bs.add(code);
      List<Map> r = adapter.doQuery(bs);
      System.out.println(r);
    }
  }

}
