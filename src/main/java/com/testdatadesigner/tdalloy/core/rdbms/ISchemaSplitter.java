package com.testdatadesigner.tdalloy.core.rdbms;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ISchemaSplitter {
  public void prepare(InputStream ddlAll) throws IOException;

  public List<String> getRawTables();
}
