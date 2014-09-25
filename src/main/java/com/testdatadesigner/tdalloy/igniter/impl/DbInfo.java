package com.testdatadesigner.tdalloy.igniter.impl;


import com.testdatadesigner.tdalloy.igniter.IDbInfo;

public class  DbInfo implements IDbInfo {

    public DbInfo() {
        // TODO: Env経由でプロパティファイルを読み込み
        // インメモリDBへの接続を準備。
    }

    @Override
    public Object getConnection() {
        return null;
    }

    @Override
    public Boolean isInit() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void connect() {

    }
}
