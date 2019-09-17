package app.controllers.filters;

import org.javalite.activeweb.controller_filters.HttpSupportFilter;

public class APIFilter extends HttpSupportFilter {

  void setErrorView(String message, int code) {
    assign("message", message);
    assign("code", code);
    assign("success", false);
  }
}
