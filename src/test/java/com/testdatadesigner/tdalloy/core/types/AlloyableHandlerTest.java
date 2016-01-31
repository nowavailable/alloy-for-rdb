package com.testdatadesigner.tdalloy.core.types;

import java.io.BufferedReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.IOGateway;
import com.testdatadesigner.tdalloy.core.io.IRdbSchemmaParser;
import com.testdatadesigner.tdalloy.core.io.ISchemaSplitter;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

import junit.framework.TestCase;

public class AlloyableHandlerTest extends TestCase {

    List<CreateTableNode> resultList = new ArrayList<CreateTableNode>();
    Alloyable currentAlloyable;
    AlloyableHandler alloyableHandler;
	Consumer<Serializable> setWarning;
    
    protected void setUp() throws Exception {
        super.setUp();
        Bootstrap.setProps();
        URL resInfo = this.getClass().getResource("/naming_rule_with_composite.sql");
        //URL resInfo = this.getClass().getResource("/naming_rule.dump");
        //URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
        String filePath = resInfo.getFile();
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

        IRdbSchemmaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(currentAlloyable);

//        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
//        map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
//        setWarning = o -> { 
//        	map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuildAll() throws Exception {
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
        String seperator = "  ";
        // String separator = "\t";
        for (IAtom result : this.currentAlloyable.atoms) {
            System.out.println(result.getName()
                    + seperator
                    + result.getClass().toString()
                    + seperator
                    + (result.getOriginPropertyName().isEmpty() ? "-"
                            : result.getOriginPropertyName())
                    + seperator
                    + result.getIsAbstruct().toString()
                    + seperator
                    + (result.getParent() == null ? "-"
                            : result.getParent().getName())
                    + seperator
                    + (result.getOriginTypeName().isEmpty() ? "-"
                            : result.getOriginTypeName())
                    + seperator
                    + (result.getClass().equals(AbstractRelationPolymorphicTypified.class) && 
                    		((AbstractRelationPolymorphicTypified)result).getExtended() == null ? "-"
                            : ((AbstractRelationPolymorphicTypified)result).getExtended().name));
        }
        System.out.println("-------------------------");
        for (IRelation result : this.currentAlloyable.relations) {
            System.out.println(result.getName() 
                    + seperator + result.getClass().toString()
                    + seperator + (AlloyableHandler.getOwner(result) == null ? "-" : AlloyableHandler.getOwner(result).getName())
                    + seperator + (AlloyableHandler.getRefTo(result) == null ? "-" : AlloyableHandler.getRefTo(result).getName()) + '(' + result.getOriginColumnName() + ')'
                    + seperator + result.getIsNotEmpty());
            if (result.getClass().toString().indexOf("MultipleRelation") > 0) {
                ((MultipleRelation) result).getRefToTypes().forEach(rel -> {
                    System.out.println("                         refTo: " + ((Relation)rel).getName());
                });
//                ((MultipleRelation) result).reverseOfrefToTypes.forEach(rel -> {
//                    System.out.println("                       parent: " + rel.name);
//                });
            }
        }
        System.out.println("-------------------------");
        for (Fact result : this.currentAlloyable.facts) {
            System.out.println(result.value + seperator
                    + result.owners.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
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
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

}
