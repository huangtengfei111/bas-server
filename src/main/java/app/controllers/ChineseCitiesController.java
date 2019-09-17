package app.controllers;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.javalite.activeweb.annotations.GET;

import app.services.ChineseCitiesProvider;
import io.vavr.Tuple2;

public class ChineseCitiesController extends APIController {
  @GET
  public void provinces() throws IOException {
    ChineseCitiesProvider ccp = ChineseCitiesProvider.getInstance();
    Map<String, String> provinces = ccp.provinces();
    
    setOkView();
    view("oneMap", provinces);
    render("/reports/map");    
  }

  @GET
  public void cities() {
    String provinceCode = param("province_code");
    ChineseCitiesProvider ccp = ChineseCitiesProvider.getInstance();
    Set<Tuple2<String, String>> cities = ccp.cities(provinceCode);
  
    setOkView();
    view("set", cities);
    render();  
  }
}
