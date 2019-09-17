package app.servlets;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreflightHandler implements Filter {
  // Logger
  private static final Logger logger = LoggerFactory.getLogger(PreflightHandler.class);
  // Request headers
  private static final String ORIGIN_HEADER = "Origin";
  public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";

  @Override
  public void init(FilterConfig config) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    throws IOException, ServletException {

    handle((HttpServletRequest)request, (HttpServletResponse)response, chain);
	}

  private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
    throws IOException, ServletException {

    String origin = request.getHeader(ORIGIN_HEADER);
    String path = request.getRequestURI();
 
    if (origin != null && !origin.equals("") && isEnabled(request)) {
      if(isPreflightRequest(request)) {
        logger.info("Handle preflight request: " + path);

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,Accept");
        response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,DELETE,HEAD");
        response.setHeader("Allow", "GET,HEAD,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Content-Length", "0");
        response.setHeader("Expires", "0");

        response.setStatus(HttpServletResponse.SC_OK);
        return;
      }
    }

    logger.debug("Chain other filter");
    // pass the request along the filter chain
    chain.doFilter(request, response);

  }

  protected boolean isEnabled(HttpServletRequest request) {
    // WebSocket clients such as Chrome 5 implement a version of the WebSocket
    // protocol that does not accept extra response headers on the upgrade response
    for (Enumeration<String> connections = request.getHeaders("Connection"); connections.hasMoreElements();){
      String connection = (String)connections.nextElement();
      if ("Upgrade".equalsIgnoreCase(connection)){
        for (Enumeration<String>  upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements();){
          String upgrade = (String)upgrades.nextElement();
          if ("WebSocket".equalsIgnoreCase(upgrade))
            return false;
        }
      }
    }
    return true;
  }

  private boolean isPreflightRequest(HttpServletRequest request){
    String method = request.getMethod();
    if (!"OPTIONS".equalsIgnoreCase(method))
      return false;
    if (request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null)
      return false;
    return true;
  }  
}