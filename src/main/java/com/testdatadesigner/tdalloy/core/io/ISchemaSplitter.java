package com.testdatadesigner.tdalloy.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ISchemaSplitter {
    public void prepare(InputStream ddlAll) throws IOException;
    public List<String> getRawTables();
}
