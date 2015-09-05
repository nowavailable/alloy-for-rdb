package com.testdatadesigner.tdalloy.igniter.impl;

import java.util.List;
import com.testdatadesigner.tdalloy.igniter.IKVSInfo;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import java.io.Serializable;

public class MapDbInfo implements IKVSInfo {
    DB db;
    String STORE_NAME = "TDAlloyCore";

    public MapDbInfo() {
        this.db = DBMaker.newMemoryDirectDB().
            transactionDisable().
            closeOnJvmShutdown().
            make();
    }

    @Override
    public HTreeMap<String, List<Serializable>> getMap() {
        return this.db.getHashMap(STORE_NAME);
    }
}
