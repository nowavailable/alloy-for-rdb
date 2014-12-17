package com.testdatadesigner.tdalloy.core.io;

import java.util.List;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.CreateTableNode;

public interface IRdbSchemmaParser {

	/**
	 * 各RDBMS製品のCREATE TABLE文の方言を標準的な書式に直して、foundation-dbのパーサーを通す。
	 * @param schemas
	 * @return foundation-dbのStatementNode。
	 * @throws StandardException
	 */
	public List<CreateTableNode> inboundParse(List<String> schemas)
	        throws StandardException;

}