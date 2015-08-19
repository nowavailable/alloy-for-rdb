package com.testdatadesigner.tdalloy.core.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;

public class IOGatewayInner {

	public IOGatewayInner() {
	}

	public static List<String> readSchemesFromDDL(String path) throws IOException {
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
		try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(path))) {
	        ddlSplitter.prepare(in);			
		}
        List<String> results = ddlSplitter.getRawTables();
        return results;
	}

}
