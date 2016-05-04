package com.testdatadesigner.tdalloy.core.io.vertx;

import java.util.stream.Collectors;
import com.testdatadesigner.tdalloy.core.io.Importer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FileImportVerticle extends AbstractVerticle {

  public static String KEY_OF_FILE_PATH = "filePath";
  public static String KEY_OF_DBMS_NAME = "dbmsName";

  public FileImportVerticle() {
  }

  public void start() {
    JsonArray params = this.config().getJsonArray(Router.KEY_OF_PARAMS);
    String filePath = params.stream()
        .filter(elm -> ((JsonObject) elm).containsKey(KEY_OF_FILE_PATH))
        .map(elm -> ((JsonObject) elm).getString(KEY_OF_FILE_PATH))
        .collect(Collectors.toList()).get(0);
    String dbmsName = params.stream()
        .filter(elm -> ((JsonObject) elm).containsKey(KEY_OF_DBMS_NAME))
        .map(elm -> ((JsonObject) elm).getString(KEY_OF_DBMS_NAME))
        .collect(Collectors.toList()).get(0);
    String jsonStr = null;

    // TODO: filePath 実在チェック

    EventBus eb = vertx.eventBus();
    try {
      Importer importer = new Importer();
      jsonStr = importer.getAlloyableJSON(filePath, dbmsName);
      String address = (String) Router.ACTION.get(this.config().getValue(Router.KEY_OF_ACTION)).get(Router.KEY_OF_RETURN);
      eb.send(address, jsonStr);
    } catch (Exception e) {
      // errorPlaceHolder.message = "Import Error!";
      eb.send("file_import_response", "Import Error!");
    }
  }
}
