package app.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.FormItem;

import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.EventBus;

import app.jobs.AppEventListener;
import app.jobs.events.AppEvent;
import app.services.pb.PbillStatCapable;

/**
 * @author
 */
public abstract class APIController extends AppController implements PbillStatCapable {
  @Override
  protected String getContentType() {
    return "application/json";
  }

  @Override
  protected String getLayout() {
    return "/layouts/rest_layout";
  }

  protected int getCurrentPage(String page) {
    int currentPage = 1;
    if(page != null) {
      currentPage = Integer.parseInt(page);
      currentPage = currentPage > 0 ? currentPage : 1;
    }
    return currentPage;
  }

  protected int getPageSize(String size) {
  	int pageSize = 10;
  	if(size != null) {
      pageSize = Integer.parseInt(size);
      pageSize = pageSize > 0 ? pageSize : 10;
      pageSize = pageSize > 50 ? 50 : pageSize;
    }
    return pageSize;
  }

  protected <T extends Object> T notNull(T o, T defaultVal) {
    return ((o != null) ? o : defaultVal);
  }

  protected String notNull(Object o, String defaultVal) {
    return ((o != null) ? o.toString() : defaultVal);
  }

  protected void setOkView() {
    setOkView(null);
  }

  protected void setOkView(String message) {
    assign("message", message);
    assign("code", 200);
    assign("success", true);
  }

  protected void setErrorView(String message, int code) {
    assign("message", message);
    assign("code", code);
    assign("success", false);
  }

  protected void registerAndPost(AppEvent event, AppEventListener listener) {
    EventBus eventBus = new EventBus();
    eventBus.register(listener);
    eventBus.post(event);
  }

  protected void registerAndPost(AppEvent event, List<AppEventListener> listeners) {
    EventBus eventBus = new EventBus();
    for (AppEventListener listener : listeners) {
      eventBus.register(listener);
    }
    eventBus.post(event);
  }

  protected InputStream getResourceAsStream(String filePath) {
    return this.getClass().getClassLoader().getResourceAsStream(filePath);
  }

  protected JSONObject getJsonResource(String filePath) throws IOException {
    InputStream in = getResourceAsStream(filePath);

    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    StringBuilder responseStrBuilder = new StringBuilder();

    String inputStr = null;
    while ((inputStr = streamReader.readLine()) != null)
      responseStrBuilder.append(inputStr);

    return JSONObject.parseObject(responseStrBuilder.toString());
  }

  public Map<String, String> getFieldValues(List<FormItem> multipartFormItems) {
    Map<String, String> map = new HashMap<>();
    for (FormItem fi : multipartFormItems) {
      if (fi.isFormField()) {
        map.put(fi.getName(), fi.getStreamAsString());
      }
    }
    return map;
  }

  protected long userIdInSession() {
    Subject currentUser = SecurityUtils.getSubject();
    return (Long) currentUser.getSession().getAttribute("userId");
  }

  protected String basHomeDir() {
    String home = System.getProperty("user.home");
    String basHomeDir = FilenameUtils.normalize(home + "/.bas");
    new File(basHomeDir).mkdirs();
    return basHomeDir;
  }

  protected String basTmpDir() {
    String home = System.getProperty("user.home");
    String basTmpDir = FilenameUtils.normalize(home + "/.bas/tmp");
    File f = new File(basTmpDir); 
    f.mkdirs();
    //f.getAbsolutePath();
    return basTmpDir;
  }

  public void options() {
    header("Access-Control-Allow-Credentials", true);
    header("Access-Control-Allow-Origin", "*");
    header("Access-Control-Allow-Headers", "Origin,Content-Type,Accept");
    header("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,DELETE,HEAD");
    header("Allow", "GET, HEAD, POST, PUT, DELETE, OPTIONS");
    header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
 	  header("Content-Length", 0);
 	  header("Expires", "0");
  	respond("ok");
  }
}