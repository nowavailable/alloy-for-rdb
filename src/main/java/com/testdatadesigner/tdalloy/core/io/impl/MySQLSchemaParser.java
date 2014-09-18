package com.testdatadesigner.tdalloy.core.io.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.SQLParser;
import com.foundationdb.sql.parser.StatementNode;
import com.google.common.base.Joiner;

public class MySQLSchemaParser {

    List<Object> tables;

    private List<String> constraints = new ArrayList<String>();

    public void inboundParse(List<String> schemas) throws StandardException {

        List<Pattern> omitPatters = new ArrayList<Pattern>(){{
            // 3分割したgroupのうち、2番目が置換対象。1,3番目は温存。
            add(Pattern.compile("(int)(\\([\\d]+\\))(.?)"));
            add(Pattern.compile("(.?)(AUTO_INCREMENT)(.?)"));
            add(Pattern.compile("(.?)(,[\\s]+SPATIAL KEY .+`\\))(.?)"));
            add(Pattern.compile("(.?)(,[\\s]+FULLTEXT KEY .+`\\))(.?)"));
            add(Pattern.compile("(.?)(,[\\s]+KEY .+`\\))(.?)"));
            add(Pattern.compile("(\\))([\\s]+ENGINE=.+)(;)$"));
            // ↓これは最後に。順番注意。
            add(Pattern.compile("(.+)(;)(.?)$"));
        }};

        SQLParser parser = null;
        StatementNode stmt = null;
        for (String createTableStr : schemas) {
            String simplify = createTableStr;
            for (Pattern patternAndReplace : omitPatters) {
                Matcher matcher = patternAndReplace.matcher(simplify);
                simplify = matcher.replaceAll("$1$3");
            }

            // その他の置換
            simplify = optimizeDoubleToDecimal(simplify);
            simplify = optimizeUniqueConstraint(simplify);

            // CONSTRAINTS節の生成
            if (constraints.size() > 0) {
                String constraintStr = Joiner.on(", ").join(constraints);
                Pattern p_total = Pattern.compile("^(.+)(\\))$");
                Matcher m_total = p_total.matcher(simplify);
                simplify = m_total.replaceAll("$1, " + constraintStr + ')');
                constraints = new ArrayList<String>();
            }

            parser = new SQLParser();
            stmt = parser.parseStatement(simplify);
            //System.out.println(simplify);
            //stmt.treePrint();
        }
    }

    public String optimizeDoubleToDecimal(String createTableStr) {
        String decimalStr = "DECIMAL";
        String optimized = createTableStr;
        Pattern pattern =
                Pattern.compile("([\\s]+)(double)(\\([\\d]+,[\\d]+\\)[\\s]+)");
        Matcher matcher = pattern.matcher(createTableStr);
        while (matcher.find()) {
            optimized = matcher.replaceAll("$1" + decimalStr + "$3");
        }
        return optimized;
    }

    public String optimizeUniqueConstraint(String createTableStr) {
        String optimized = createTableStr;
        Pattern pattern =
                Pattern.compile("(.?)(,[\\s]+UNIQUE KEY( `\\w+`)( (\\([^)]+`)\\)))");
        Matcher matcher = pattern.matcher(createTableStr);
        while (matcher.find()) {
            this.constraints.add(
                "CONSTRAINT " + matcher.group(3) + " UNIQUE " + matcher.group(4)
            );
            optimized = matcher.replaceAll("$1");
        }
        return optimized;
    }
}
