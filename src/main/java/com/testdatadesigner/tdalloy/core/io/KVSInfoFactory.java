package com.testdatadesigner.tdalloy.core.io;

import com.testdatadesigner.tdalloy.core.io.impl.MapDbInfo;

import java.util.EnumMap;

public final class KVSInfoFactory {  // singleton.
    private final static KVSInfoFactory INSTANCE = new KVSInfoFactory();
    private IKVSInfo kvsInfo = null;

    private String propKeyOfKvs = "kvs";
    private enum KeysOfKvs {DEFAULT, MAP_DB};
    public EnumMap<KeysOfKvs, String> KEY_OF_STORE_NAME =
        new EnumMap<KeysOfKvs, String>(KeysOfKvs.class) {
            {
                put(KeysOfKvs.DEFAULT, "MapDB");
                put(KeysOfKvs.MAP_DB, "MapDB");
            }
        };

    private KVSInfoFactory() {
        // ※とりあえずMapDB一択。
        if (System.getProperty(propKeyOfKvs).equals(KEY_OF_STORE_NAME.get(KeysOfKvs.MAP_DB))) {
            this.kvsInfo = new MapDbInfo();
        }
    }

    public static KVSInfoFactory getInstance() {
        return INSTANCE;
    }

    public synchronized IKVSInfo getKvsInfo() {
        return this.kvsInfo;
    }
}
