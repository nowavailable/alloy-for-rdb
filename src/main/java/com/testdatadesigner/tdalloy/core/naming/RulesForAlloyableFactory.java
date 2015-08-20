package com.testdatadesigner.tdalloy.core.naming;

public final class RulesForAlloyableFactory {  // singleton.
    private final static RulesForAlloyableFactory instance = new RulesForAlloyableFactory();
    private IRulesForAlloyable rule = null;

    private RulesForAlloyableFactory() {
    }

    public static RulesForAlloyableFactory getInstance() {
        return instance;
    }

    public IRulesForAlloyable getRule() {
        if (this.rule == null) {
            // TODO: 実装切り替えの
            this.rule = new RulesForAlloyableRails();
        }
        return this.rule;
    }
}
