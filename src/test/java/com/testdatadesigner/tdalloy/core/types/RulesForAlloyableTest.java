package com.testdatadesigner.tdalloy.core.types;

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
}
