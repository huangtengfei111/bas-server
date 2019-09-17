package app;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.javalite.activeweb.RequestDispatcher;
import org.javalite.app_config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.controllers.filters.CrossOriginFilter;
import app.servlets.PreflightHandler;
import app.util.Obfuscator;
/**
 * Starting app in an embedded Jetty server. Have to run instrumentation before
 * start if not already.
 *
 */
public class Standalone {
  private static Logger logger = LoggerFactory.getLogger(Standalone.class);
  private static int DEFAULT_WEB_PORT = 8080;
  private static int DEFAULT_API_PORT = 9091;

  private static final String CMD1 =
      new Obfuscator(new long[] { 0x4021C63848EA5583L, 0x5EDF299E7A474547L }).toString(); /*
                                                                                           * => "start"
                                                                                           */
  private static final String CMD2 =
      new Obfuscator(new long[] { 0x10BE74CF50978D0AL, 0x302EE1FEC0FDFDEDL }).toString(); /*
                                                                                           * => "stop"
                                                                                           */

  public static void main(String[] args) throws Exception {
    System.setProperty("org.eclipse.jetty.LEVEL", "INFO");

    if (CMD1.equals(args[0])) {
      start(args);
    } else if (CMD2.equals(args[0])) {
      stop(args);
    }
  }

  static void start(String[] args) throws Exception {
    int webPort = AppConfig.pInteger("app.web.port");
    int apiPort = AppConfig.pInteger("app.api.port");
    webPort = webPort > 0 ? webPort : DEFAULT_WEB_PORT;
    apiPort = apiPort > 0 ? apiPort : DEFAULT_API_PORT;

    Server server = new Server();// 创建一个服务

    ServerConnector webAppConnector = new ServerConnector(server);// 创建一个连接器对象,并受理服务
    webAppConnector.setPort(webPort);// 设置监听端口
    webAppConnector.setName("ConnWeb");
    server.addConnector(webAppConnector);

    ServerConnector apiAppConnector = new ServerConnector(server);
    apiAppConnector.setPort(apiPort);
    apiAppConnector.setName("ConnApi");
    server.addConnector(apiAppConnector);

    // web app
    URL webRootLocation =
        Standalone.class.getClass().getResource("/webroot/index.html");// 获取到对应的URL
    if (webRootLocation == null) {
      throw new IllegalStateException(
          "Unable to determine webroot URL location");
    }

    URI webRootUri = URI.create(webRootLocation.toURI().toASCIIString()
        .replaceFirst("/index.html$", "/"));
    System.err.printf("Web app root URI: %s%n", webRootUri);

    ServletContextHandler webApp = new ServletContextHandler();// 创建一个处理器对象,对资源进行管理
    webApp.setContextPath("/");// 将文件路径交给处理器处理
    webApp.setBaseResource(Resource.newResource(webRootUri));// 设置一个默认的资源路径
    webApp.setWelcomeFiles(new String[] { "index.html" });// 设置默认打开页面
    webApp.setVirtualHosts(new String[] { "@ConnWeb" }); // connector webApp

    webApp.getMimeTypes().addMimeMapping("txt", "text/plain;charset=utf-8");
    webApp.addServlet(DefaultServlet.class, "/");// 向处理器中添加默认的servlet

    // api app
    URL apiWebRootLocation = Standalone.class.getClass().getResource("/webapp/index.html");
    if (apiWebRootLocation == null) {
      throw new IllegalStateException("Unable to determine api webroot URL location");
    }

    URI apiWebRootUri = URI.create(apiWebRootLocation.toURI().toASCIIString()
        .replaceFirst("/index.html$", "/"));
    System.err.printf("API app root URI: %s%n", apiWebRootUri);

    ServletContextHandler apiApp = new ServletContextHandler(ServletContextHandler.SESSIONS);
    apiApp.setBaseResource(Resource.newResource(apiWebRootUri));

    apiApp.setContextPath("/");
    apiApp.setVirtualHosts(new String[] { "@ConnApi" }); // connector webApp
    configApiApp(apiApp);
    apiApp.addServlet(DefaultServlet.class, "/");

    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setIncludedMethods("POST", "GET");
    gzipHandler.setIncludedMimeTypes("text/html", "text/plain", "text/xml", "text/css", "application/javascript",
                                     "text/javascript", "application/json");
    gzipHandler.setInflateBufferSize(2048);
    gzipHandler.setMinGzipSize(2048);

    ShutdownHandler shutdownHandler = new ShutdownHandler("iamok", false, true);
    
    HandlerCollection contexts = new HandlerCollection();
    contexts.addHandler(apiApp);
    contexts.addHandler(webApp);
    // contexts.addHandler(gzipHandler);
    contexts.addHandler(shutdownHandler);

    server.setHandler(contexts);

    // Start Server
    server.start();
    server.dumpStdErr();
    server.join();
  }

  static void configApiApp(ServletContextHandler app) {
    app.addEventListener(new EnvironmentLoaderListener());
    // Add the filter, and then use the provided FilterHolder to configure it
    FilterHolder cors = app.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
    cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
    cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "POST,GET,OPTIONS,PUT,DELETE");
    cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
//    FilterHolder cosfh = app.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
//    cosfh.setInitParameters(map("allowedOrigins", "*", "allowedMethods", "POST,GET,OPTIONS,PUT,DELETE",
//                                "allowedHeaders", "Origin, Content-Type"));
    app.addFilter(PreflightHandler.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    app.addFilter(ShiroFilter.class, "/*",
                  EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE,
                             DispatcherType.ERROR));

    FilterHolder rdfh = app.addFilter(RequestDispatcher.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    rdfh.setInitParameter("exclusions", "css,images,ico,echo,webpack_hmr,js/");
    rdfh.setInitParameter("root_controller", "home");
  }

  /**
   * Stops a running web application powered with Jetty.
   * <p/>
   * <p/>
   * Default TCP port is used to communicate with Jetty.
   */
  static public void stop(String[] args) {
    attemptShutdown(9091, "iamok");
  }

  /**
   * Stops a running web application powered with Jetty.
   *
   * @param stopPort TCP port used to communicate with Jetty.
   */
  static void attemptShutdown(int port, String shutdownCookie) {
    try {
        URL url = new URL("http://localhost:" + port + "/shutdown?token=" + shutdownCookie);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.getResponseCode();
        logger.info("Shutting down " + url + ": " + connection.getResponseMessage());
    } catch (SocketException e) {
        logger.debug("Not running");
        // Okay - the server is not running
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }
}
