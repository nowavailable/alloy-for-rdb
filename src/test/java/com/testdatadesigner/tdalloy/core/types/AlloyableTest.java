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

        InputStream in = this.getClass().getResourceAsStream("/naming_rule.dump");
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
        this.currentAlloyable = this.currentAlloyable.buildFromTable(this.resultList);
        this.currentAlloyable = this.currentAlloyable.buildByInference(this.resultList);
        this.currentAlloyable = this.currentAlloyable.buildFromForeignKey(this.resultList);
        this.currentAlloyable = this.currentAlloyable.buildFromColumn(this.resultList);
        String seperator = "\t";
        for (Sig result : this.currentAlloyable.sigs) {
            System.out.println(result.name
                    + seperator
                    + result.type.toString()
                    + seperator
                    + result.originPropertyName
                    + seperator
                    + result.isAbstruct.toString()
                    + seperator
                    + (result.getParent() == null ? "-"
                            : result.getParent().name));
        }
        System.out.println("-------------------------");
        for (Relation result : this.currentAlloyable.relations) {
            System.out.println(result.name 
                    + seperator + result.type.toString()
                    + seperator + (result.owner == null ? "-" : result.owner.name)
                    + seperator + (result.refTo == null ? "-" : result.refTo.name));
            if (result.getClass().toString().indexOf("MultipleRelation") > 0) {
                ((MultipleRelation<Sig>)result).refToTypes.forEach(rel -> {
                    System.out.println("                         refTo: " + rel.name); 
                });
                ((MultipleRelation<Sig>)result).reverseOfrefToTypes.forEach(rel -> {
                    System.out.println("                       parent: " + rel.name); 
                });
            }
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
