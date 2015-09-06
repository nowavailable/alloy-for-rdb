package com.testdatadesigner.tdalloy.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

import junit.framework.TestCase;

public class ImporterTest extends TestCase {

	private String filePath;

	protected void setUp() throws Exception {
		super.setUp();
        Bootstrap.setProps();
        //URL resInfo = this.getClass().getResource("/naming_rule.dump");
        URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
        filePath = resInfo.getFile();
	}

	public void testParse() {
		Importer importer = new Importer();
		try {
			importer.iceBreak(filePath, Importer.Database.MYSQL);

	        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
	        List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));

	        String seperator = "  ";
	        // String separator = "\t";
	        for (Atom result : ((Alloyable)list.get(0)).atoms) {
	            System.out.println(result.name
	                    + seperator
	                    + result.type.toString()
	                    + seperator
	                    + (result.originPropertyName.isEmpty() ? "-"
	                            : result.originPropertyName)
	                    + seperator
	                    + result.isAbstruct.toString()
	                    + seperator
	                    + (result.getParent() == null ? "-"
	                            : result.getParent().name)
	                    + seperator
	                    + (result.originTypeName.isEmpty() ? "-"
	                            : result.originTypeName)
	                    + seperator
	                    + (result.getExtended() == null ? "-"
	                            : result.getExtended().name));
	        }
	        System.out.println("-------------------------");
	        for (Relation result : ((Alloyable)list.get(0)).relations) {
	            System.out.println(result.name 
	                    + seperator + result.type.toString()
	                    + seperator + (AlloyableHandler.getOwner(result) == null ? "-" : AlloyableHandler.getOwner(result).name)
	                    + seperator + (AlloyableHandler.getRefTo(result) == null ? "-" : AlloyableHandler.getRefTo(result).name) + '(' + result.originColumnName + ')'
	                    + seperator + result.isNotEmpty);
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
	        for (Fact result : ((Alloyable)list.get(0)).facts) {
	            System.out.println(result.value + seperator
	                    + result.owners.stream().map(r -> r.name).collect(Collectors.joining(",")));
	        }
		} catch (ImportError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testAls() {
		Importer importer = new Importer();
		Alloyable alloyable = null;
		try {
			importer.iceBreak(filePath, Importer.Database.MYSQL);
	        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
	        List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));
	        alloyable = (Alloyable)list.get(0);
		} catch (ImportError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try (BufferedReader reader = importer.takeOut(alloyable)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
