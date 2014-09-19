package com.testdatadesigner.tdalloy.fuze;

import com.testdatadesigner.tdalloy.fuze.IDbInfo;

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
