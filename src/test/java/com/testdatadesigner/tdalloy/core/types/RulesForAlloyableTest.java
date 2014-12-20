package com.testdatadesigner.tdalloy.core.types;

import junit.framework.TestCase;

public class RulesForAlloyableTest extends TestCase {
    public void test正規表現確認() {
        String result = RulesForAlloyable.generateForeignKeyName("user_id", "photos");
        assertEquals("photos_User", result);
    }
}
