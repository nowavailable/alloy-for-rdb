package com.testdatadesigner.tdalloy.core.type_bulder;

import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class TableHandler {

    public Sig build(String tableName) {
        Sig sig = new Sig(Sig.Tipify.ENTITY);
        sig.originPropertyName = tableName;
        sig.name = RulesForAlloyable.tableSigName(tableName);
        return sig;
    }
}
