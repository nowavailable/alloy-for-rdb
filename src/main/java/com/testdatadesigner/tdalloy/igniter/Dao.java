package com.testdatadesigner.tdalloy.igniter;

import com.testdatadesigner.tdalloy.igniter.IDbInfo;

public final class Dao  {  // singleton.
    private final static Dao instance = new Dao();

    private Dao() {
    }

    public static Dao getInstance() {
        return instance;
    }

    private IDbInfo dbInfo = null;

    public IDbInfo getDbInfo() {
        return this.dbInfo;
    }

    public void setDbInfo(IDbInfo dbInfo) {
        this.dbInfo = dbInfo;
    }

    public Boolean activeCheck() {
        return null;
    }
}
