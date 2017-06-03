package com.testdatadesigner.tdalloy.driver;

import com.testdatadesigner.tdalloy.driver.Bootstrap;

import junit.framework.TestCase;

/**
 * Created by tsutsumi on 2015/08/21.
 */
public class BootstrapTest extends TestCase {

  public void testSetProps() throws Exception {
    Bootstrap.setProps();
    assertEquals(System.getProperty("naming_convention"), "rails");
  }
}
