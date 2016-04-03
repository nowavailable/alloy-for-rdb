package com.testdatadesigner.tdalloy.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testdatadesigner.tdalloy.core.types.RelationPolymorphicTypified;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

import junit.framework.TestCase;

public class ImporterTest extends TestCase {

	private String filePath;

	protected void setUp() throws Exception {
		super.setUp();
        Bootstrap.setProps();
        URL resInfo = this.getClass().getResource("/naming_rule.dump");
        //URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
        filePath = resInfo.getFile();
	}

	public void testParse() {
		Importer importer = new Importer();
		try {
			importer.iceBreak(filePath, Importer.Database.MYSQL);

	        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
	        List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));

	        String separator = "  ";
	        // String separator = "\t";
	        for (IAtom result : ((Alloyable)list.get(0)).atoms) {
	            System.out.println(result.getName()
	                    + separator
	                    + result.getClass().getSimpleName()
	                    + separator
	                    + (result.getOriginPropertyName().isEmpty() ? "-"
	                            : result.getOriginPropertyName())
	                    + separator
	                    + result.getIsAbstruct().toString()
	                    + separator
	                    + (result.getParent() == null ? "-"
	                            : result.getParent().getName())
	                    + separator
	                    + (result.getOriginTypeName().isEmpty() ? "-"
	                            : result.getOriginTypeName())
	                    + separator
	                    + (result.getClass().equals(RelationPolymorphicTypified.class) && 
	                    		((RelationPolymorphicTypified)result).getExtended() != null ? 
	                    				((RelationPolymorphicTypified)result).getExtended().getName() : "-"));
	        }
	        System.out.println("-------------------------");
	        for (IRelation result : ((Alloyable)list.get(0)).relations) {
	            System.out.println(result.getName() 
	                    + separator + result.getClass().getSimpleName()
	                    + separator + (result.getOwner() == null ? "-" : result.getOwner().getName())
	                    + separator + (result.getRefTo() == null ? "-" : result.getRefTo().getName()) + '(' + result.getOriginColumnNames() + ')'
	                    + separator + result.getIsNotEmpty());
	        }
	        System.out.println("-------------------------");
	        for (Fact result : ((Alloyable)list.get(0)).facts) {
	            System.out.println(result.value + separator
	                    + result.owners.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
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
