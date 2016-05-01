package com.testdatadesigner.tdalloy.core.naming;

import java.util.EnumMap;

public final class RulesForAlloyableFactory { // singleton.
  private final static RulesForAlloyableFactory INSTANCE = new RulesForAlloyableFactory();
  private IRulesForAlloyable rule = null;

  private String propKeyOfNamingConvention = "naming_convention";

  private enum KeysOfNamingConvention {
    DEFAULT, RAILS
  };

  public EnumMap<KeysOfNamingConvention, String> KEY_OF_NAMING_RULE = new EnumMap<KeysOfNamingConvention, String>(
      KeysOfNamingConvention.class) {
    {
      put(KeysOfNamingConvention.DEFAULT, "rails");
      put(KeysOfNamingConvention.RAILS, "rails");
    }
  };

  private RulesForAlloyableFactory() {
  }

  public static RulesForAlloyableFactory getInstance() {
    return INSTANCE;
  }

  public IRulesForAlloyable getRule() {
    if (this.rule == null) {
      if (System.getProperty(propKeyOfNamingConvention).equals(KEY_OF_NAMING_RULE.get(KeysOfNamingConvention.RAILS))) {
        this.rule = new RulesForAlloyableRails();
      } else {
        this.rule = new RulesForAlloyableRails();
      }
    }
    return this.rule;
  }
}
