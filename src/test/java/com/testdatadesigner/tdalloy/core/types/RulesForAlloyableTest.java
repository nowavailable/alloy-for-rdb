package com.testdatadesigner.tdalloy.core.types;

import java.util.Arrays;
import java.util.List;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

import junit.framework.TestCase;

public class RulesForAlloyableTest extends TestCase {
    IRulesForAlloyable namingRule;

    protected void setUp() throws Exception {
        super.setUp();
        Bootstrap.setProps();
        namingRule = RulesForAlloyableFactory.getInstance().getRule();
    }

    public void test正規表現確認() {
        String result = namingRule.foreignKeyName("user_id", "photos");
        assertEquals("user", result);
    }
    
    public void testReverse() {
        String result_1 = namingRule.reverse("BookPrice");
        assertEquals("book_prices", result_1);

        String result_2 = namingRule.reverse("BooksPrice");
        assertEquals("books_prices", result_2);
    }
    
    public void testポリモーフィック関連用カラムかどうか() {
        List<String> bag = Arrays.asList("campaign", "photable");
        Boolean result1 = namingRule.isInferencedPolymorphic("photable_id", bag);
        assertEquals(Boolean.TRUE, result1);
        Boolean result2 = namingRule.isInferencedPolymorphic("photable_type", bag);
        assertEquals(Boolean.TRUE, result2);
        Boolean result3 = namingRule.isInferencedPolymorphic("id_of_campaign", bag);
        assertEquals(Boolean.FALSE, result3);
    }
}
