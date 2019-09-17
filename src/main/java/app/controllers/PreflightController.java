package app.controllers;

import org.javalite.activeweb.annotations.RESTful;

@RESTful
public class PreflightController extends APIController {
	public void index() {
    render().contentType("application/json");
  }

  public void options() {
  	super.options();
  }
}