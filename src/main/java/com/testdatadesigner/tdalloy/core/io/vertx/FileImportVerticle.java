package com.testdatadesigner.tdalloy.core.io.vertx;

import com.testdatadesigner.tdalloy.core.io.Importer;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;

public class FileImportVerticle extends AbstractVerticle {

  public FileImportVerticle() {
  }

  public void start() {
    JsonArray params = this.config().getJsonArray("params");

    String filePath = (String) params.getValue(0);
    String dbmsName = (String) params.getValue(1);
    String jsonStr = null;
    // TODO: filePath 実在チェック
    
    EventBus eb = vertx.eventBus();
    try {
      Bootstrap.setProps();
      Importer importer = new Importer();
      jsonStr = importer.getAlloyableJSON(filePath, dbmsName);
      eb.send("file_import_response", jsonStr);
    } catch (Exception e) {
//      errorPlaceHolder.message = "Import Error!";
      eb.send("file_import_response", "Import Error!");
    }
  }
}
