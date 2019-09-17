package app.controllers;

import org.javalite.activeweb.annotations.GET;

public class PingController extends APIController {

  @GET
  public void index() {
    setOkView("pong");
    view("resp","pong");
    
    render();
  }
}
