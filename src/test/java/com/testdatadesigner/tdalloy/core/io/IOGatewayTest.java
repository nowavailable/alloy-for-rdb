package com.testdatadesigner.tdalloy.core.io;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Properties;

public class IOGatewayTest extends TestCase {
  public IOGatewayTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(IOGatewayTest.class);
  }

  public void setUp() throws Exception {
    super.setUp();

  }

  public void testLocalDB() throws Exception {
    // システムプロパティのget
    Properties props = System.getProperties();
    props.getProperty("sss");

    // リソースファイルの読み込み

    // システムプロパティへのset

  }
}