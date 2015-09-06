package com.testdatadesigner.tdalloy.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.AlloyableHandler;

/**
 * + データスキーマインポートファイルのI/O + 結果出力ファイルのI/O + 結果出力前オブジェクトの状態操作
 */
public class Parser {

    public enum Database {MYSQL};
    public Database database;
    
    // TODO: ネーミングルールの指定と保持メソッド
    
    public void parseDDL(String path, Database database) throws ImportError {
    	/*
    	 * データベースの種別を処理
    	 */
    	this.database = database;
    	ISchemaSplitter ddlSplitter = null;
        IRdbSchemmaParser parser = null;
    	if (this.database.equals(Database.MYSQL)) {
    		ddlSplitter = new MySQLSchemaSplitter();
    		parser = new MySQLSchemaParser();
		}
    	/*
    	 * DDLを読み込み
    	 */
        List<String> results = null;
		try {
			results = IOGateway.readSchemesFromDDL(path, ddlSplitter);
		} catch (IOException e) {
			throw new ImportError();
		}
        List<CreateTableNode> parsed = null;
        try {
			parsed = parser.inboundParse(results);
		} catch (StandardException e) {
			throw new ImportError();
		}
        /*
         * Alloyableオブジェクトに変換
         */
        Map<String, List<Serializable>> map = IOGateway.getKVSMap();
        map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD), new ArrayList<Serializable>());
        map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
        Consumer<Serializable> setWarning = o -> { 
        	map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};
        AlloyableHandler alloyableHandler = new AlloyableHandler(new Alloyable());
        try {
			Alloyable currentAlloyable = alloyableHandler.buildFromDDL(parsed, setWarning);
			map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD)).add(currentAlloyable);
		} catch (IllegalAccessException e) {
			throw new ImportError();
		}
    }
    
    public OutputStream getAls(Alloyable alloyable) {
        return null;
    }
}
