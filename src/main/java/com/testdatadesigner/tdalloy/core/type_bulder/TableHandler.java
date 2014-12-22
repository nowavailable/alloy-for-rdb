package com.testdatadesigner.tdalloy.core.type_bulder;

import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class TableHandler {

	public Sig build(CreateTableNode tableNode) {
        Sig sig = new Sig(Sig.Tipify.ENTITY);
        sig.originPropertyName = tableNode.getFullName();
        sig.name = RulesForAlloyable.tableSigName(tableNode.getFullName());
        return sig;
	}
}
