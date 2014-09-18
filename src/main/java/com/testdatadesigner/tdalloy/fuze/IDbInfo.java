package com.testdatadesigner.tdalloy.fuze;

public interface IDbInfo {
    public Object getConnection();
    public void init();
    public Boolean isInit();
    public void connect();
}
