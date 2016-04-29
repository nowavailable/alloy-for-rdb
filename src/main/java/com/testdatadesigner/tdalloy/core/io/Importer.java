package com.testdatadesigner.tdalloy.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.CreateTableNode;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;
import com.testdatadesigner.tdalloy.core.translater.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

/**
 * + データスキーマインポートファイルのI/O + 結果出力ファイルのI/O + 結果出力前オブジェクトの状態操作
 */
public class Importer {

    public enum Database {MYSQL};
    public Database database;
    
    public Importer() throws IOException {
    }
    
    public String getAlloyableJSON(String filePath, String dbmsName) {
    	Alloyable currentAlloyable = null;
		try {
	    	if (dbmsName.equals("mysql")) {
	    		this.database = Importer.Database.MYSQL;
	    	} else {
	    		throw new ImportError("No DBMS type.");
	    	}
			currentAlloyable = this.getAlloyable(filePath, database);
		} catch (ImportError e) {
			e.printStackTrace();
			return e.getCause().toString();
		}
        String jsonStr = new Gson().toJson(currentAlloyable);
		return jsonStr;
    }

    public Alloyable getAlloyable(String filePath, Database database) throws ImportError {
		this.iceBreak(filePath, database);
        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
        List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));
        return (Alloyable)list.get(0);
    }
    
    public void iceBreak(String path, Database database) throws ImportError {
    	/*
    	 * データベースの種別を処理
    	 */
    	this.database = database;
    	ISchemaSplitter ddlSplitter = null;
        IRdbSchemaParser parser = null;
    	if (this.database.equals(Database.MYSQL)) {
    		ddlSplitter = new MySQLSchemaSplitter();
    		parser = new MySQLSchemaParser();
		}
    	/*
    	 * DDLを読み込み
    	 */
        List<String> results = null;
        List<CreateTableNode> parsed = null;
		try {
			results = IOGateway.readSchemesFromDDL(path, ddlSplitter);
			parsed = parser.inboundParse(results);
		} catch (IOException e) {
			throw new ImportError();
		} catch (StandardException e) {
			throw new ImportError();
		}
        /*
         * Alloy互換オブジェクトに変換
         */
        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
        map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD), new ArrayList<Serializable>());
        //※参照、被参照の関係に不整合があった場合ワーニングとして永続化
        //map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
        //Consumer<Serializable> setWarning = o -> { 
        //	map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};
        AlloyableHandler alloyableHandler = new AlloyableHandler(new Alloyable());
        try {
			Alloyable currentAlloyable = alloyableHandler.buildFromDDL(parsed);
			map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD)).add(currentAlloyable);
		} catch (IllegalAccessException e) {
			throw new ImportError();
		}
    }
    
    public BufferedReader takeOut(Alloyable alloyable) throws IOException {
    	AlloyableHandler alloyableHandler = new AlloyableHandler(alloyable);
    	return alloyableHandler.outputToAls();
    }
}
