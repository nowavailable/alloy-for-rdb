package com.testdatadesigner.tdalloy.core.io;

import org.mapdb.HTreeMap;
import java.io.Serializable;
import java.util.List;

public interface IKVSInfo {
  public HTreeMap<String, List<Serializable>> getMap();
}
