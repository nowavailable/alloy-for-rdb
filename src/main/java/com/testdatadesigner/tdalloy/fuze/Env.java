package com.testdatadesigner.tdalloy.fuze;

import java.util.Properties;

public class Env {

    public String userId;

    public Env(String userId) {
        this.userId = (userId.isEmpty()) ? userId : this.generateUserId();
    }

    public String generateUserId() {
        // TODO: generate or get
    	// セッションへのInterFaceをプロパティとして保持
    	// セッションはSingleton
        return null;
    }

    public String getUserId() {
        return this.userId;
    }

    public static Properties getProperties() {
        // TODO:
    	Properties systemProp = System.getProperties();
        return systemProp;
    }
}
