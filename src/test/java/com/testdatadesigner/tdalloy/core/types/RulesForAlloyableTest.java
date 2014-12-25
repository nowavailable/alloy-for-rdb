package com.testdatadesigner.tdalloy.core.types;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class RulesForAlloyableTest extends TestCase {
    public void test正規表現確認() {
        String result = RulesForAlloyable.foreignKeyName("user_id", "photos");
        assertEquals("photos_User", result);
    }
    
    public void testReverse() {
        String result_1 = RulesForAlloyable.reverse("BookPrice");
        assertEquals("book_prices", result_1);

        String result_2 = RulesForAlloyable.reverse("BooksPrice");
        assertEquals("books_prices", result_2);
    }
    
    public void testポリモーフィック関連用カラムかどうか() {
        List<String> bag = Arrays.asList("campaign", "photable");
        Boolean result1 = RulesForAlloyable.isInferencedPolymophic("photable_id", bag);
        assertEquals(Boolean.TRUE, result1);
        Boolean result2 = RulesForAlloyable.isInferencedPolymophic("photable_type", bag);
        assertEquals(Boolean.TRUE, result2);
        Boolean result3 = RulesForAlloyable.isInferencedPolymophic("id_of_campaign", bag);
        assertEquals(Boolean.FALSE, result3);
    }
}
