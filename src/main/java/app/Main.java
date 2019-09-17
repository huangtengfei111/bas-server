package app;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starting app in an embedded Jetty server. Have to run instrumentation before
 * start if not already.
 *
 */
public class Main {

  public static void main(String[] args) throws Exception {
    Server server = new Server();

    WebAppContext webapp = new WebAppContext("src/main/webapp", "/");
    webapp.addAliasCheck(new AllowSymLinkAliasChecker());
    server.setHandler(webapp);
    server.start();
    server.dumpStdErr();
    server.join();
  }
}
