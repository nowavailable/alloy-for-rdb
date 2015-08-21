package com.testdatadesigner.tdalloy.igniter;

import junit.framework.TestCase;

/**
 * Created by tsutsumi on 2015/08/21.
 */
public class BootstrapTest extends TestCase {

    public void testSetProps() throws Exception {
        Bootstrap.setProps();
        assertEquals(System.getProperty("tables_and_columns"), "rails");
    }
}
