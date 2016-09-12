package com.testdatadesigner.tdalloy.core.io.vertx;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class WebSocketServer extends AbstractVerticle {
  public static byte[] END_MARKER = { (byte) '\n', (byte) ']', (byte) '\n' };
  
  private ServerWebSocket webSocketRef = null;
  private String actionStr = "";
  public String nextAction = "";
  private boolean ended = false;
  private ByteArrayOutputStream oStream = null;

  public static void main(String[] args) {
    Runner.runServer(WebSocketServer.class);
  }
  
  private void cancelAction() {
    this.nextAction = "";
    this.clearMessage();
  }

  private void clearMessage() {
    this.oStream = null;
    this.ended = false;
  }

  @Override
  public void start() throws Exception {
    /*
     *  tdalloy初期処理
     */
    Bootstrap.setProps();
    /*
     *  create server.
     */
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
    /* 
     * set listener for event bus's call. 
     */
    EventBus eb = vertx.eventBus();
    for (String actionKeyStr : Router.ACTION.keySet()) {
      String address = (String) Router.ACTION.get(actionKeyStr).get(Router.KEY_OF_RETURN);
      if (address == null || address.isEmpty())
          continue;
      eb.consumer(address, message -> {
        this.eventBusMessageHandler(address, message);
      });
    }
  }

  public ServerWebSocket handler(ServerWebSocket ws, Handler<Buffer> handler) {
    this.webSocketRef = ws;
    return ws.handler(handler);
  }

  public ServerWebSocket handle(Buffer data) {    
    /* 
     * read raw messages.
     */
    byte[] bytes = data.getBytes();
    if (bytes.length == END_MARKER.length) {
      int equal = 0;
      for (int i = 0; i < bytes.length; i++) {
        if (END_MARKER[i] == bytes[i]) {
          equal ++;
        }
      }
      if (bytes.length == equal)
        this.ended = true;
    }
    try (BufferedInputStream inStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, "UTF-8");) {
      if (this.oStream == null)
        this.oStream = new ByteArrayOutputStream();

      byte[] chunk = new byte[1024 * 128];
      int len = 0;
      while ((len = inStream.read(chunk)) != -1) {
        this.oStream.write(chunk, 0, len);
      }
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      this.clearMessage();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      this.clearMessage();
    }
    /* 
     * parse messages.
     */
    JsonObject json = new JsonObject();
    if (this.ended) {
      try {
        JsonArray jsonArray = new JsonArray(new String(this.oStream.toByteArray(), "UTF-8"));
        json = jsonArray.getJsonObject(0);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        this.clearMessage();
      }
    } else {
      return this.webSocketRef;
    }
    if (this.nextAction.isEmpty()) {
      this.actionStr = json.getString(Router.KEY_OF_ACTION);
    } else {
      this.actionStr = this.nextAction;
      this.nextAction = "";
    }
    /*
     * set options.
     */
    final JsonObject jsonObj = json;
    Map<String, Object> map = Router.ACTION.get(this.actionStr);
    List<String> keyList = (List) map.get(Router.KEY_OF_PARAMS_KEYS);
    List<JsonObject> paramsOfJson = keyList.stream().map(elm -> new JsonObject(new HashMap<String, Object>() {
      {
        this.put(elm, jsonObj.getValue(elm));
      }
    })).collect(Collectors.toList());
    this.nextAction = (String) map.get(Router.KEY_OF_NEXT_ACTION);
    /*
     * deploy verticle on demand.
     */
    if (map.get(Router.KEY_OF_HANDLER) != null) {
      DeploymentOptions options = new DeploymentOptions();
      if ((boolean) map.get(Router.KEY_OF_IS_WORKER))
        options.setWorker(true);
      JsonObject config = new JsonObject().
          put(Router.KEY_OF_ACTION, this.actionStr).
          put(Router.KEY_OF_PARAMS, new JsonArray(paramsOfJson));
      options.setConfig(config);
      vertx.deployVerticle((String) map.get(Router.KEY_OF_HANDLER), options); 
    }
    
    return this.webSocketRef;
  }

  public void eventBusMessageHandler(String address, Message<Object> message) {
    if (this.webSocketRef != null) {
      try {
        byte[] msgStream = ((String) message.body()).getBytes("UTF-8");
        BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(msgStream));
        byte[] chunk = new byte[1024 * 128];
        int len = 0;
        while((len = is.read(chunk)) != -1) {
          Buffer buffer = Buffer.buffer(chunk);
          this.webSocketRef.writeBinaryMessage(buffer);
        }
        // send a end-marker.
        this.webSocketRef.writeBinaryMessage(Buffer.buffer(END_MARKER));
      } catch (Exception e) {
        // TODO: logging
        this.webSocketRef.writeBinaryMessage(Buffer.buffer(e.getMessage()));
        this.cancelAction();
      }
    }
  }

}
