package com.testdatadesigner.tdalloy.core.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;

public class IOGatewayInner {
    
    private enum StoreData {REF_WARNING_ON_BUILD, ALLOYABLE_ON_BUILD};
    public EnumMap<StoreData, String> STORE_KEYS =
            new EnumMap<StoreData, String>(StoreData.class) {
                {
                    put(StoreData.REF_WARNING_ON_BUILD, "refWarningOnBuild");
                    put(StoreData.ALLOYABLE_ON_BUILD, "alloyableOnBuild");
                }
            };
    
    public static List<String> readSchemesFromDDL(String path) throws IOException {
        ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(path))) {
            ddlSplitter.prepare(in);
        }
        List<String> results = ddlSplitter.getRawTables();
        return results;
    }

}
