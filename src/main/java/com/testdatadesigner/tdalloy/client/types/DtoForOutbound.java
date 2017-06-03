package com.testdatadesigner.tdalloy.client.types;

import java.util.Map;

public class DtoForOutbound {

  public DtoForOutbound() {
  }

  public enum Prop {
    VALUES, REFS, TYPES, COMMENTS, LENGTHES
  }  
  public Map<String, Map<Prop, Map<String, Object>>> tableData;
  
  public void init() {
    
  }
  
  // TODO: パスを指定(xpathを解釈？)して部分書き換えするメソッド

}
