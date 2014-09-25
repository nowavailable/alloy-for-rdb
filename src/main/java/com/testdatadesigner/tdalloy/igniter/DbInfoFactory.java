package com.testdatadesigner.tdalloy.igniter;

import com.testdatadesigner.tdalloy.igniter.impl.DbInfo;

public final class DbInfoFactory {  // singleton.
    private final static DbInfoFactory instance = new DbInfoFactory();
    private IDbInfo dbInfo = null;

    private DbInfoFactory() {
    }

    public static DbInfoFactory getInstance() {
        return instance;
    }

    public synchronized IDbInfo getDbInfo(String someString) {
        // TODO: 実装切り替えの引数
        if (someString instanceof String) {
            if (this.dbInfo == null) {
                this.dbInfo = new DbInfo();
            }
        }
        return this.dbInfo;
    }
}
