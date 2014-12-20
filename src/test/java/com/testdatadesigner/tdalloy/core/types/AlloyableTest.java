package com.testdatadesigner.tdalloy.core.types;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.IRdbSchemmaParser;
import com.testdatadesigner.tdalloy.core.io.ISchemaSplitter;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;

import junit.framework.TestCase;

public class AlloyableTest extends TestCase {

    List<CreateTableNode> resultList = new ArrayList<CreateTableNode>();
    Alloyable currentAlloyable;
    
    protected void setUp() throws Exception {
        super.setUp();

        InputStream in = this.getClass().getResourceAsStream("/wanda_developmant.referance.sql");
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        List<String> results = ddlSplitter.getRawTables();

        IRdbSchemmaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuildAll() throws Exception {
        // 期待値
        this.currentAlloyable = this.currentAlloyable.buildTableSigs(this.resultList);
        this.currentAlloyable = this.currentAlloyable.buildForeignKeyRelations(this.resultList);
        this.currentAlloyable = this.currentAlloyable.buildColumnSigs(this.resultList);
        for (Sig result : this.currentAlloyable.sigs) {
            System.out.println(result.name);
        }
        for (Relation result : this.currentAlloyable.relations) {
            System.out.println(result.name);
        }
    }

    public void testBuildTableSigs() throws Exception {
        // 期待値
        
    }

    public void testBuildColumnSigs() throws Exception {
        // 期待値
        
    }

    public void testBuildFields() throws Exception {
        // 期待値
        
    }

}
