package com.testdatadesigner.tdalloy.driver.vertx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Router {

  public Router() {
  }

  public static String KEY_OF_ACTION = "action";
  public static String KEY_OF_PARAMS = "params";
  
  public static String KEY_OF_PARAMS_KEYS = "paramsKeys";
  public static String KEY_OF_HANDLER = "handler";
  public static String KEY_OF_NEXT_ACTION = "nextAction";
  public static String KEY_OF_RETURN = "return";
  public static String KEY_OF_IS_WORKER = "isWorker";

  public static Map<String, Map<String, Object>> ACTION = new HashMap(){
    {
      this.put("importRequest", new HashMap(){{
        this.put(KEY_OF_PARAMS_KEYS, Arrays.asList(FileImportVerticle.KEY_OF_FILE_PATH, FileImportVerticle.KEY_OF_DBMS_NAME));
        this.put(KEY_OF_HANDLER, FileImportVerticle.class.getCanonicalName());
        this.put(KEY_OF_NEXT_ACTION, "editAlloyable");
        this.put(KEY_OF_RETURN, "importResponse");
        this.put(KEY_OF_IS_WORKER, Boolean.TRUE);
      }});
    }
  };
  
}
