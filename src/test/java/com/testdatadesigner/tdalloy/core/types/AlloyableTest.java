package com.testdatadesigner.tdalloy.core.types;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.IOGatewayInner;
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

//        InputStream in = this.getClass().getResourceAsStream("/naming_rule.dump");
//        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
//        ddlSplitter.prepare(in);
//        List<String> results = ddlSplitter.getRawTables();
        URL resInfo = this.getClass().getResource("/naming_rule.dump");
        String filePath = resInfo.getFile();
        List<String> results = IOGatewayInner.readSchemesFromDDL(filePath);

        IRdbSchemmaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuildAll() throws Exception {
        this.currentAlloyable = this.currentAlloyable.buildFromDDL(this.resultList);
        String seperator = "  ";
        // String separator = "\t";
        for (Atom result : this.currentAlloyable.atoms) {
            System.out.println(result.name
                    + seperator
                    + result.type.toString()
                    + seperator
                    + result.originPropertyName
                    + seperator
                    + result.isAbstruct.toString()
                    + seperator
                    + (result.getParent() == null ? "-"
                            : result.getParent().name)
                    + seperator
                    + (result.originTypeName.isEmpty() ? "-"
                            : result.originTypeName));
        }
        System.out.println("-------------------------");
        for (Relation result : this.currentAlloyable.relations) {
            System.out.println(result.name 
                    + seperator + result.type.toString()
                    + seperator + (result.owner == null ? "-" : result.owner.name)
                    + seperator + (result.refTo == null ? "-" : result.refTo.name));
            if (result.getClass().toString().indexOf("MultipleRelation") > 0) {
                ((MultipleRelation) result).refToTypes.forEach(rel -> {
                    System.out.println("                         refTo: " + rel.name);
                });
                ((MultipleRelation) result).reverseOfrefToTypes.forEach(rel -> {
                    System.out.println("                       parent: " + rel.name);
                });
            }
        }
        System.out.println("-------------------------");
        for (Fact result : this.currentAlloyable.facts) {
            System.out.println(result.value + seperator
                    + result.owners.stream().map(r -> r.name).collect(Collectors.joining(",")));
        }
    }

    public void testBuildTableAtoms() throws Exception {
        // 期待値
        
    }

    public void testBuildColumnAtoms() throws Exception {
        // 期待値
        
    }

    public void testBuildFields() throws Exception {
        // 期待値
        
    }

    public void testOutputToAls() throws Exception {
        this.currentAlloyable = this.currentAlloyable.buildFromDDL(this.resultList);
        File outputToAls = this.currentAlloyable.outputToAls();
        System.out.println(outputToAls.getAbsolutePath());
    }

}
