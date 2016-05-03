package com.testdatadesigner.tdalloy.core.io.vertx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class WebSocketServer extends AbstractVerticle {
  
  public Map<BiFunction, Verticle> nextAction = new HashMap<>();
  private ServerWebSocket webSocketRef = null;

  public static void main(String[] args) {
    Runner.runServer(WebSocketServer.class);
  }

  @Override
  public void start() throws Exception {
    vertx.createHttpServer().
      websocketHandler(
        ws -> this.handler(
          ws, this::handle
        )
      ).requestHandler(req -> {
      //if (req.uri().equals("/")){
      //  req.response().sendFile("ws.html"); 
      //}
    }).listen(8080);

    EventBus eb = vertx.eventBus();
    eb.consumer("file_import_response", message -> {
      if (webSocketRef != null) {
        this.webSocketRef.writeBinaryMessage(Buffer.buffer(message.body().toString()));
      }
    });

  }

  public ServerWebSocket handler(ServerWebSocket ws, Handler<Buffer> handler) {
    this.webSocketRef = ws;
    return ws.handler(handler);
  }

  public ServerWebSocket handle(Buffer data) {
    // proc message
    
    if (this.nextAction.isEmpty()) {
      
      DeploymentOptions options = new DeploymentOptions().setWorker(true);
      JsonObject config = new JsonObject().
          put("params", new JsonArray(
              Arrays.asList("/Users/tsutsumi/JDev/src/alloy-for-rdb2/src/test/resources/naming_rule.dump", 
                  "mysql")));
      options.setConfig(config);
      vertx.deployVerticle("com.testdatadesigner.tdalloy.core.io.vertx.FileImportVerticle", options);
    } else {

    }
    //ws.writeBinaryMessage(Buffer.buffer("aaa"));
    return this.webSocketRef;
  }

}
