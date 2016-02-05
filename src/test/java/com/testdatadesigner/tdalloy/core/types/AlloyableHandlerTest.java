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

import junit.framework.Assert;
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
                    + seperator + (AlloyableHandler.getRefTo(result) == null ? "-" : AlloyableHandler.getRefTo(result).getName()) + '(' + result.getOriginColumnNames() + ')'
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
                    + ((MissingAtom)result).getOwners().stream().map(atom -> atom.getName()).collect(Collectors.joining(",")));
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

        StringBuilder str = new StringBuilder();
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                str.append(line);
                str.append("\n");
            }
        }
        String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n"
            + "\n" + "sig Album {\n" + "  \n" + "}\n" + "sig Book {\n" + "  person: lone Person,\n"
            + "  price: lone Boundary\n" + "}\n" + "sig Paper {\n" + "  person: lone Person,\n"
            + "  price: lone Boundary\n" + "}\n" + "sig Person {\n" + "  books: set Book,\n"
            + "  papers: set Paper,\n" + "  zines: set Zine\n" + "}\n" + "sig Photo {\n"
            + "  photoableType: lone Photo_PhotoableType\n" + "}\n" + "sig Url {\n"
            + "  urlableType: lone Url_UrlableType\n" + "}\n" + "sig Bookmark {\n" + "  \n" + "}\n"
            + "sig Zine {\n" + "  person: lone Person,\n" + "  price: lone Boundary,\n"
            + "  is_old: lone Bool\n" + "}\n" + "abstract sig Photo_PhotoableType {\n" + "  \n"
            + "}\n" + "sig Dummy1 {\n" + "  photos: set Photo\n" + "}\n" + "sig Dummy2 {\n"
            + "  photos: set Photo\n" + "}\n" + "sig PhotoableDummy1 {\n"
            + "  dummy1s: lone Dummy1\n" + "}\n" + "sig PhotoableDummy2 {\n"
            + "  dummy2s: lone Dummy2\n" + "}\n" + "abstract sig Url_UrlableType {\n" + "  \n"
            + "}\n" + "sig Dummy3 {\n" + "  urls: set Url\n" + "}\n" + "sig Dummy4 {\n"
            + "  urls: set Url\n" + "}\n" + "sig UrlableDummy3 {\n" + "  dummy3s: lone Dummy3\n"
            + "}\n" + "sig UrlableDummy4 {\n" + "  dummy4s: lone Dummy4\n" + "}\n" + "\n"
            + "fact {\n" + "  Person<:books = ~(Book<:person)\n"
            + "  Person<:papers = ~(Paper<:person)\n" + "  Person<:zines = ~(Zine<:person)\n"
            + "  (Photo.photoableType = Photo_PhotoableType) and (all p:Photo | p = (Photo<:photoableType).(p.(Photo<:photoableType)))\n"
            + "  Dummy1<:photos = ~(Photo<:photoableType.dummy1s)\n"
            + "  Dummy2<:photos = ~(Photo<:photoableType.dummy2s)\n"
            + "  (Url.urlableType = Url_UrlableType) and (all u:Url | u = (Url<:urlableType).(u.(Url<:urlableType)))\n"
            + "  Dummy3<:urls = ~(Url<:urlableType.dummy3s)\n"
            + "  Dummy4<:urls = ~(Url<:urlableType.dummy4s)\n" + "}\n" + "\n" + "run {}\n");
        Assert.assertEquals(str.toString(), expected);
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

        StringBuilder str = new StringBuilder();
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                str.append(line);
                str.append("\n");
            }
        }
        String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n"
            + "\n" + "sig Actor {\n" + "  charactors: set Charactor,\n" + "  name: one Boundary\n"
            + "}\n" + "sig Movie {\n" + "  charactors: set Charactor,\n" + "  title: one Boundary\n"
            + "}\n" + "sig Charactor {\n" + "  actor: lone Actor,\n" + "  movie: lone Movie,\n"
            + "  goods: set Good,\n" + "  name: one Boundary\n" + "}\n" + "sig Good {\n"
            + "  charactor: one Charactor,\n" + "  name: one Boundary\n" + "}\n" + "\n" + "fact {\n"
            + "  Actor<:charactors = ~(Charactor<:actor)\n"
            + "  Movie<:charactors = ~(Charactor<:movie)\n"
            + "  Charactor<:goods = ~(Good<:charactor)\n"
            + "  all e,e':Charactor | e != e' => (e.actor -> e.movie != e'.actor -> e'.movie)\n"
            + "  all e,e':Good | e != e' => (e.charactor.actor -> e.charactor.movie != e'.charactor.actor -> e'.charactor.movie)\n"
            + "}\n" + "\n" + "run {}\n");
        Assert.assertEquals(str.toString(), expected);
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

        StringBuilder str = new StringBuilder();
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                str.append(line);
                str.append("\n");
            }
        }
        String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n"
            + "\n" + "sig Book {\n" + "  person: lone Boundary,\n" + "  price: lone Boundary\n"
            + "}\n" + "sig Paper {\n" + "  person: lone Boundary,\n" + "  price: lone Boundary\n"
            + "}\n" + "sig Photo {\n" + "  photoable_type: lone Boundary\n" + "}\n" + "\n"
            + "fact {\n" + "}\n" + "\n" + "run {}\n");
        Assert.assertEquals(str.toString(), expected);
    }

    public void testBuildAllAndOutputAls_CompositeInconsistency() throws Exception {
        URL resInfo = this.getClass().getResource("/naming_rule_with_composite_inconsistency.sql");
        String filePath = resInfo.getFile();
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

        IRdbSchemaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);

        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(currentAlloyable);
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);

        StringBuilder str = new StringBuilder();
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                str.append(line);
                str.append("\n");
            }
        }
        String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n"
            + "\n" + "sig Actor {\n" + "  charactors: set Charactor,\n" + "  name: one Boundary\n"
            + "}\n" + "sig Charactor {\n" + "  actor: lone Actor,\n" + "  movie: lone Boundary,\n"
            + "  goods: set Good,\n" + "  name: one Boundary\n" + "}\n" + "sig Good {\n"
            + "  charactor: one Charactor,\n" + "  name: one Boundary\n" + "}\n" + "\n" + "fact {\n"
            + "  Actor<:charactors = ~(Charactor<:actor)\n"
            + "  Charactor<:goods = ~(Good<:charactor)\n"
            + "  all e,e':Charactor | e != e' => (e.actor -> e.movie != e'.actor -> e'.movie)\n"
            + "  all e,e':Good | e != e' => (e.charactor.actor -> e.charactor.movie != e'.charactor.actor -> e'.charactor.movie)\n"
            + "}\n" + "\n" + "run {}\n");
    }

    public void testBuildAllAndOutputAls_NoRelations() throws Exception {
        URL resInfo = this.getClass().getResource("/naming_rule_with_no_relations.sql");
        String filePath = resInfo.getFile();
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

        IRdbSchemaParser parser = new MySQLSchemaParser();
        this.resultList = parser.inboundParse(results);

        this.currentAlloyable = new Alloyable();
        this.alloyableHandler = new AlloyableHandler(currentAlloyable);
        this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);

        StringBuilder str = new StringBuilder();
        try(BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()){
            String line = null;
            while ((line = outputToAlsReader.readLine()) != null) {
                str.append(line);
                str.append("\n");
            }
        }
        String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n" + "sig Book {\n"
            + "  person: lone Boundary,\n" + "  some: lone Boundary,\n" + "  price: lone Boundary\n"
            + "}\n" + "\n" + "fact {\n" + "}\n" + "\n" + "run {}" + "\n");
        Assert.assertEquals(str.toString(), expected);
    }
}
