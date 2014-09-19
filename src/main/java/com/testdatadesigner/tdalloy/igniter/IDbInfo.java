package com.testdatadesigner.tdalloy.igniter;

public interface IDbInfo {
    public Object getConnection();
    public void init();
    public Boolean isInit();
    public void connect();
}
