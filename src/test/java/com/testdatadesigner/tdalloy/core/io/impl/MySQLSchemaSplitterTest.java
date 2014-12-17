package com.testdatadesigner.tdalloy.core.io.impl;

import junit.framework.TestCase;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySQLSchemaSplitterTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testPrepare() throws Exception {
        //File ddlFile = new File("src/test/resources/structure.sql");
        InputStream in = this.getClass().getResourceAsStream("/structure.sql");
        MySQLSchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        List<String> results = ddlSplitter.getRawTables();

        validation(results);

        in = this.getClass().getResourceAsStream("/sampledatas.dump");
        ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        results = ddlSplitter.getRawTables();

        validation(results);
    }

    private void validation(List<String> rawTables) {
        Pattern pattern = Pattern.compile("^CREATE TABLE .+");
        for (String createTableStr : rawTables) {
            //System.out.println(createTableStr);
            Matcher matcher = pattern.matcher(createTableStr);
            //System.out.println(matcher.matches());
            assertEquals(true, matcher.matches());
        }
    }

//    public void testGetRawTables() throws Exception {
//    }
}