package com.testdatadesigner.tdalloy.core.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import org.mapdb.HTreeMap;

public class IOGateway {

	public enum StoreData {REF_WARNING_ON_BUILD, ALLOYABLE_ON_BUILD};
    public static EnumMap<StoreData, String> STORE_KEYS =
            new EnumMap<StoreData, String>(StoreData.class) {
                {
                    put(StoreData.REF_WARNING_ON_BUILD, "refWarningsOnBuild");
                    put(StoreData.ALLOYABLE_ON_BUILD, "alloyableOnBuild");
                }
            };

    public static List<String> readSchemesFromDDL(String path, ISchemaSplitter ddlSplitter) throws IOException {
        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(path))) {
            ddlSplitter.prepare(in);
        }
        List<String> results = ddlSplitter.getRawTables();
        return results;
    }
    
    public static HTreeMap<String, List<Serializable>> getKVSMap() {
    	return KVSInfoFactory.getInstance().getKvsInfo().getMap();
    }

}
