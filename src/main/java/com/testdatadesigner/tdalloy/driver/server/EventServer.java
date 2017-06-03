package com.testdatadesigner.tdalloy.driver.server;

import java.io.IOException;

import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.testdatadesigner.tdalloy.driver.Bootstrap;

public class EventServer {
  public static void main(String[] args) {
    try {
      Bootstrap.setProps();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(8080);
    server.addConnector(connector);

    // Setup the basic application "context" for this application at "/"
    // This is also known as the handler tree (in jetty speak)
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);

    try {
      // Initialize javax.websocket layer
      ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

      // Add WebSocket endpoint to javax.websocket layer
      wscontainer.addEndpoint(EventSocket.class);

      server.start();
      server.dump(System.err);
      server.join();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }
  }
}