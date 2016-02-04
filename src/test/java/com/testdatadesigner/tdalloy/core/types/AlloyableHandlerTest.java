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
import com.testdatadesigner.tdalloy.core.io.IRdbSchemaParser;
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
//        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
//        map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
//        setWarning = o -> { 
//        	map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};
    }

    protected void tearDown() throws Exception {
        String seperator = "  ";
        // String separator = "\t";
        for (IAtom result : this.currentAlloyable.atoms) {
            System.out.println(result.getName()
                    + seperator
                    + result.getClass().getSimpleName()
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
                    + (result.getClass().equals(RelationPolymorphicTypified.class) && 
                    		((RelationPolymorphicTypified)result).getExtended() != null ? 
                    				((RelationPolymorphicTypified)result).getExtended().getName() : "-"));
        }
        System.out.println("-------------------------");
        for (IRelation result : this.currentAlloyable.relations) {
            System.out.println(result.getName() 
                    + seperator + result.getClass().getSimpleName()
                    + seperator + (AlloyableHandler.getOwner(result) == null ? "-" : AlloyableHandler.getOwner(result).getName())
                    + seperator + (AlloyableHandler.getRefTo(result) == null ? "-" : AlloyableHandler.getRefTo(result).getName()) + '(' + result.getOriginColumnName() + ')'
                    + seperator + result.getIsNotEmpty());
        }
        System.out.println("-------------------------");
        for (Fact result : this.currentAlloyable.facts) {
            System.out.println(result.value + seperator
                    + result.owners.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
        }
        System.out.println("-------------------------");
        for (IAtom result : this.currentAlloyable.missingAtoms) {
            System.out.println(result.getName()
                    + seperator
                    + result.getClass().getSimpleName()
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
                    + (result.getClass().equals(RelationPolymorphicTypified.class) && 
                    		((RelationPolymorphicTypified)result).getExtended() != null ? 
                    				((RelationPolymorphicTypified)result).getExtended().getName() : "-"));
        }
        
        System.out.println("");
        System.out.println("==================================================");
        System.out.println("");
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                System.out.println(line);
            }
        }

        super.tearDown();
    }

    public void testBuildAll() throws Exception {
        URL resInfo = this.getClass().getResource("/naming_rule.dump");
        //URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
        String filePath = resInfo.getFile();
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

        IRdbSchemaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(currentAlloyable);
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
    }


    public void testBuildAllAndOutputAls_CompositeIndex() throws Exception {
        URL resInfo = this.getClass().getResource("/naming_rule_with_composite.sql");
        String filePath = resInfo.getFile();
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

        IRdbSchemaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(currentAlloyable);
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
    }

    public void testBuildAllAndOutputAls_Inconsistency() throws Exception {
        URL resInfo = this.getClass().getResource("/naming_rule_with_inconsistency.sql");
        String filePath = resInfo.getFile();
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

        IRdbSchemaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);
        
        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(currentAlloyable);
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
    }

}
