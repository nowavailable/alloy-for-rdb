package com.testdatadesigner.tdalloy.igniter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/**
 * Created by tsutsumi on 2015/08/21.
 */
public class Bootstrap {
    public static void setProps() throws IOException {
        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(Bootstrap.class.getResourceAsStream(
            "/naming_rules.properties"))) {
            properties.load(reader);
            for (Object key : properties.keySet()) {
                System.setProperty((String) key, properties.getProperty((String) key));
            }
        }
    }
}
