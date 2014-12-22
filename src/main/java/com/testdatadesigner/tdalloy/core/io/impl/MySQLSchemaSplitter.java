package com.testdatadesigner.tdalloy.core.io.impl;

import com.testdatadesigner.tdalloy.core.io.ISchemaSplitter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MySQLSchemaSplitter implements ISchemaSplitter {
    private List<String> rawTables = new ArrayList<String>();
    private final char SPLITTER_STR = ';';
    private final List<String> SKIP_MARKER = Arrays.asList(
            "/*", 
            "--",
            "DROP TABLE", 
            "LOCK TABLE", 
            "UNLOCK TABLE", 
            "INSERT INTO ",
            "ALTER TABLE"
            );

    @Override
    public void prepare(InputStream schemaAll) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                schemaAll, "UTF-8"));
        String line = null;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            // dump ファイルに含まれるコメント行その他不要行の処理。
            boolean isSkip = false;
            for (String marker : SKIP_MARKER) {
                // TODO: 正規表現に？ もしくはStreamの時点でフィルタリング？（java8）
                if (line.contains(marker)) {
                    isSkip = true;
                    break;
                }
            }
            if (isSkip) {
                continue;
            }
            if (line.length() == 0) {
                continue;
            }

            buffer.append(line);
            // 終端の処理。
            if (line.indexOf(SPLITTER_STR) == line.length() - 1) {
                rawTables.add(buffer.toString());
                buffer = new StringBuilder();
            }
        }
    }

    @Override
    public List<String> getRawTables() {
        return rawTables;
    }

}
