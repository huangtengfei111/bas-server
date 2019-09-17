package app.controllers;

import java.util.HashMap;
import java.util.Map;

import org.javalite.activeweb.annotations.GET;

import io.vavr.Tuple;
import io.vavr.Tuple2;

public class SVGController extends APIController {

  /**
   * 根据数值生产对应的SVG圆形
   */
  @GET
  public void circle() {
    Map<String, Tuple2<Integer, String>> SIZE_COLOR_MAP = new HashMap<>();
    SIZE_COLOR_MAP.put("1", Tuple.of(20, "#ffee00")); // light
    SIZE_COLOR_MAP.put("2", Tuple.of(23, "#ffcc00"));
    SIZE_COLOR_MAP.put("3", Tuple.of(26, "#ffbb00"));
    SIZE_COLOR_MAP.put("4", Tuple.of(29, "#ff9900"));

    SIZE_COLOR_MAP.put("5", Tuple.of(32, "#ff8800"));
    SIZE_COLOR_MAP.put("6", Tuple.of(35, "#ff7700"));

    SIZE_COLOR_MAP.put("7", Tuple.of(38, "#ff6600"));
    SIZE_COLOR_MAP.put("8", Tuple.of(41, "#ff4400"));
    SIZE_COLOR_MAP.put("9", Tuple.of(44, "#ff2200"));
    SIZE_COLOR_MAP.put("10", Tuple.of(47, "#ff0000")); // dark
    Tuple2<Integer, String> sizeAndColor = SIZE_COLOR_MAP.get(param("level"));
    view("sizeAndColor", sizeAndColor);
    render("/svg/circle").noLayout().contentType("image/svg+xml");
  }
}
