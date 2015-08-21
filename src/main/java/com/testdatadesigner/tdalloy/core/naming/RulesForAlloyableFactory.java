package com.testdatadesigner.tdalloy.core.naming;

import java.util.EnumMap;

public final class RulesForAlloyableFactory {  // singleton.
    private final static RulesForAlloyableFactory instance = new RulesForAlloyableFactory();
    private IRulesForAlloyable rule = null;

    private String propKeyOfNamingRule = "tables_and_columns";
    private enum KeysOfNamingRule {DEFAULT, RAILS};
    public EnumMap<KeysOfNamingRule, String> KEY_OF_NAMING_RULE =
        new EnumMap<KeysOfNamingRule, String>(KeysOfNamingRule.class) {
            {
                put(KeysOfNamingRule.DEFAULT, "rails");
                put(KeysOfNamingRule.RAILS, "rails");
            }
        };

    private RulesForAlloyableFactory() {
    }

    public static RulesForAlloyableFactory getInstance() {
        return instance;
    }

    public IRulesForAlloyable getRule() {
        if (this.rule == null) {
            if (System.getProperty(propKeyOfNamingRule).equals(KEY_OF_NAMING_RULE.get(KeysOfNamingRule.RAILS))) {
                this.rule = new RulesForAlloyableRails();
            } else {
                this.rule = new RulesForAlloyableRails();
            }
        }
        return this.rule;
    }
}
