package com.testdatadesigner.tdalloy.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public interface ISchemaSplitter {
    public void prepare(InputStream ddlAll);
    public List<String> getRawTables();
}
