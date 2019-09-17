package app.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javalite.activeweb.annotations.GET;

import com.github.javafaker.Faker;

public class MockDataController extends APIController {

  @GET
  public void connections() {
    Faker faker = new Faker(new Locale("zh_CN"));
    List<Map> conns = new ArrayList<>();
    String freq = faker.phoneNumber().cellPhone();
    for (int i = 0; i < 30; i++) {
      Map<String, Object> conn = new HashMap<>();
      String source = faker.phoneNumber().cellPhone();
      if (i % 5 == 0) {
        source = freq;
      }
      conn.put("source", source);
      conn.put("target", faker.phoneNumber().cellPhone());
      conn.put("category", "");
      conn.put("count", (int) (Math.random() * 20));

      conns.add(conn);
    }

    setOkView();
    view("listMap", conns);
    render("/reports/listMap");
  }
}
