package com.testdatadesigner.tdalloy.core.types;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.testdatadesigner.tdalloy.igniter.Bootstrap;
import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.IRdbSchemaParser;
import com.testdatadesigner.tdalloy.core.io.ISchemaSplitter;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;

import junit.framework.TestCase;

public class ParameterizedTest extends TestCase {

    List<CreateTableNode> resultList = new ArrayList<CreateTableNode>();
    Alloyable currentAlloyable;
    AlloyableHandler alloyableHandler;
	private Consumer<Serializable> setWarning;

    protected void setUp() throws Exception {
        super.setUp();
        Bootstrap.setProps();
        InputStream in = this.getClass().getResourceAsStream("/naming_rule.dump");
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        List<String> results = ddlSplitter.getRawTables();

        IRdbSchemaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(this.currentAlloyable);

//        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
//        map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
//        setWarning = o -> { 
//        	map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testAlloyableToInversed() throws IllegalAccessException {
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
        Parameterized parameterized = new Parameterized();

    }

}
