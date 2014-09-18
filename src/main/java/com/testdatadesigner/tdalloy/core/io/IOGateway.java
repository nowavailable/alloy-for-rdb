package com.testdatadesigner.tdalloy.core.io;

import com.testdatadesigner.tdalloy.fuze.Dao;
import com.testdatadesigner.tdalloy.fuze.DbInfoFactory;
import com.testdatadesigner.tdalloy.fuze.IDbInfo;

/**
 * ファイルI/O および DBのI/O のファサード。
 */
public class IOGateway {

    protected Dao dao;

    /**
     * スタンダロン用
     */
    public IOGateway() {
        // TODO: 実装切り替えの引数
        IDbInfo dbInfo = DbInfoFactory.getInstance().
                getDbInfo(new String());
        if (dbInfo.getConnection() != null) {
            if (!dbInfo.isInit()) {
                dbInfo.init();
            }
        } else {
            dbInfo.connect();
            dbInfo.init();
        }
        this.initDao(dbInfo);
    }

    /**
     * mysql用
     * connection管理等は外部依存。
     */
    public IOGateway(IDbInfo dbInfo) {
        if (dbInfo.getConnection() != null) {
            this.initDao(dbInfo);
        } else {
            //
            throw new  IllegalArgumentException();
        }
    }

    public void initDao(IDbInfo dbInfo) {
        this.dao = Dao.getInstance();
        if (this.dao.getDbInfo() != null) {
            this.dao.setDbInfo(dbInfo);
        }
    }

    public void splitDDLFile(String path) {

    }

}
