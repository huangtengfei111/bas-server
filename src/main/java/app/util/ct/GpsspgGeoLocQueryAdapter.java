package app.util.ct;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;

import app.models.pb.CellTower;
import app.util.ChineseAddressParser;

public class GpsspgGeoLocQueryAdapter extends GeoLocQueryAdapter {
  public static final String OID = "10079";
  public static final String KEY = "9961x7y0x2749785v2010xv0277u527vv0763";
  public static final String SRC = "gpsspg";
  private static final String HOST = "api.gpsspg.com";
  private static final String PATH = "/bs/";

  private static final ObjectMapper mapper = new ObjectMapper();

  private String oid;
  private String key;

  public GpsspgGeoLocQueryAdapter() {
    this.oid = OID;
    this.key = KEY;  
  }
  
  public GpsspgGeoLocQueryAdapter(String oid, String key) {
    this.oid = oid;
    this.key = key;  
  }

  public List<Map> doQuery(List<String> bs) throws Exception {
    return doQuery(bs, "10", BMAPS_COORD);
  }

  public List<Map> doQuery(List<String> bs, String fmt, String coord) throws Exception {
    URI uri = new URIBuilder()
        .setScheme("http")
        .setHost(HOST)
        .setPath(PATH)
        .setParameter("oid", this.getOid())
        .setParameter("key", this.getKey())
        .setParameter("bs", String.join("|", bs))
        .setParameter("to", coord)
        .build();     

    CloseableHttpClient httpclient = HttpClients.createDefault();
    ResponseHandler<List<Map>> rh = new ResponseHandler<List<Map>>() {

      @Override
      public List<Map> handleResponse(final HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        if (entity == null) {
          throw new ClientProtocolException("Response contains no content");
        }
        //Gson gson = new GsonBuilder().create();
        ContentType contentType = ContentType.getOrDefault(entity);
        Charset charset = contentType.getCharset();
        Reader reader = new InputStreamReader(entity.getContent(), charset);
        Map result = mapper.readValue(reader, Map.class);
        
        if(result.get("status") != null && result.get("match") != null &&
            "200".equals(result.get("status").toString()) && 
              "1".equals(result.get("match").toString()) ) {
          return (List<Map>)result.get("result");
        } else {
          return null;
        }
      }
    };

    List<Map> ret = null;

    try{
      HttpGet httpget = new HttpGet(uri);
      ret = httpclient.execute(httpget, rh);
    } finally {
      httpclient.close();
    }

    System.out.println(ret);

    return ret;
  }

  /*
   * 
   */
  public CellTower doQuery(Long mnc, Long lac, Long ci, String coord)
      throws Exception {
    CellTower ct = null;
    List<String> bs = new ArrayList<>();
    bs.add(CHINA_MCC + "," + mnc + "," + lac + "," + ci);

    List<Map> ret = this.doQuery(bs);

    if (ret != null && ret.size() > 0) {
      Map map = ret.get(0);
      BigDecimal lat = new BigDecimal(map.get("lat").toString());
      BigDecimal lng = new BigDecimal(map.get("lng").toString());
      String rad = map.get("radius").toString();
      String address = map.get("address").toString();
      Map<String, String> addrMap = ChineseAddressParser.parseOneLine(address);
      double[] dCoord = null;
      ct = new CellTower(CHINA_MCC, mnc, lac, ci);
      if (GMAPS_COORD.equals(coord)) {
        ct.setCoord(lat, lng);
        ct.setGCoord(lat, lng);

        dCoord = CoordinateTransformUtil.gcj02tobd09(lng.doubleValue(), lat.doubleValue());
        ct.setBCoord(BigDecimal.valueOf(dCoord[0]), BigDecimal.valueOf(dCoord[1]));
      }

      if (BMAPS_COORD.equals(coord)) {
        ct.setBCoord(lat, lng);

        dCoord = CoordinateTransformUtil.gcj02tobd09(lng.doubleValue(), lat.doubleValue());
        BigDecimal lat2 = BigDecimal.valueOf(dCoord[0]);
        BigDecimal lng2 = BigDecimal.valueOf(dCoord[1]);
        ct.setCoord(lat2, lng2);
        ct.setGCoord(lat2, lng2);
      }

      ct.setProvince(addrMap.get(ChineseAddressParser.PROVINCE));
      ct.setCity(addrMap.get(ChineseAddressParser.CITY));
      ct.setDistrict(addrMap.get(ChineseAddressParser.COUNTY));
      ct.setTown(addrMap.get(ChineseAddressParser.TOWN));
      ct.setAddress(address);
      ct.setSource(SRC);     
    }
    return ct;
  }
  
  public String gpsId(Long mnc, long lac, long ci) {
    return null;
  }
  
  public String getOid() {
    return this.oid;
  }
  
  public String getKey() {
    return this.key;
  }
  
  public static void main(String[] args) throws Exception {
    GpsspgGeoLocQueryAdapter adapter = new GpsspgGeoLocQueryAdapter(OID, KEY);
    List<String> bs = new ArrayList<>();
    bs.add("460,0,26464,10997");
    //bs.add("460,0,21632,19745");
    adapter.doQuery(bs);
    // CellTower ct = adapter.doQuery("0", 26464, 10997, GMAPS_COORD);
    // System.out.println(ct);
  }
}