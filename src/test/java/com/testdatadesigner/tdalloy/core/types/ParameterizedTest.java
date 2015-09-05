package com.testdatadesigner.tdalloy.core.types;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;
import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.IRdbSchemmaParser;
import com.testdatadesigner.tdalloy.core.io.ISchemaSplitter;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;

import junit.framework.TestCase;

public class ParameterizedTest extends TestCase {

    List<CreateTableNode> resultList = new ArrayList<CreateTableNode>();
    IAlloyable currentAlloyable;
    AlloyableHandler alloyableHandler;

    protected void setUp() throws Exception {
        super.setUp();
        Bootstrap.setProps();
        InputStream in = this.getClass().getResourceAsStream("/naming_rule.dump");
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        List<String> results = ddlSplitter.getRawTables();

        IRdbSchemmaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(this.currentAlloyable);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testAlloyableToInversed() throws IllegalAccessException {
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
        Parameterized parameterized = new Parameterized();

    }

}
