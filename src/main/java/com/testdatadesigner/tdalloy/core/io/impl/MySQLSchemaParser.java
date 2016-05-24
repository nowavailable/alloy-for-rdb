package com.testdatadesigner.tdalloy.core.io.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.CreateTableNode;
import com.foundationdb.sql.parser.SQLParser;
import com.foundationdb.sql.parser.StatementNode;
import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.io.IRdbSchemaParser;

/**
 * @author tsutsumi
 * 
 */
public class MySQLSchemaParser implements IRdbSchemaParser {

  private List<String> constraints = new ArrayList<>();

  /**
   * MySQLのCREATE TABLE文の方言を標準的な書式に直して、fdbのパーサーを通す。
   * 
   * @param schemas
   * @return fdbのStatementNode。
   * @throws StandardException
   */
  @Override
  public List<CreateTableNode> inboundParse(List<String> schemas) throws StandardException {

    List<Pattern> omitPatterns = Arrays.asList(
        Pattern.compile("(,)([\\s]+SPATIAL KEY [^)]+`)(\\))"),
        Pattern.compile("([\\s]+USING BTREE)"),
        Pattern.compile("(,)([\\s]+FULLTEXT KEY [^)]+`)(\\))"), 
        Pattern.compile("(,)([\\s]+KEY [^)]+`)(\\))")
        );
    // 3分割したgroupのうち、2番目が置換対象、1,3番目は温存するパターン。
    List<Pattern> omit2ndPatters = Arrays.asList(Pattern.compile("(int)(\\([\\d]+\\))(.?)"),
        Pattern.compile("(.?)(AUTO_INCREMENT)(.?)"), Pattern.compile("(\\))([\\s]+ENGINE=.+)(;)$"),
        Pattern.compile("(.+)(;)(.?)$"));

    SQLParser parser = null;
    StatementNode stmt = null;
    List<CreateTableNode> nodeList = new ArrayList<>();

    for (String createTableStr : schemas) {
      String simplify = createTableStr;
      for (Pattern pattern : omitPatterns) {
        Matcher matcher = pattern.matcher(simplify);
        while (matcher.find()) {
          simplify = matcher.replaceAll("");
        }
      }
      for (Pattern patternAndReplace : omit2ndPatters) {
        Matcher matcher = patternAndReplace.matcher(simplify);
        while (matcher.find()) {
          simplify = matcher.replaceAll("$1$3");
        }
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
        constraints = new ArrayList<>();
      }
      // System.out.println(simplify);
      parser = new SQLParser();
      stmt = parser.parseStatement(simplify);
      // stmt.treePrint();
      nodeList.add((CreateTableNode) stmt);
    }
    return nodeList;
  }

  /**
   * 精度とスケールの指定つきのdoubleのカラム型定義を、 DECIMALとして書き換える。
   * 
   * @param createTableStr
   *          最適化すべきMySQLのCreate Table 文
   * @return UNIQUE INDEX 定義が最適化された Create Table 文
   */
  public String optimizeDoubleToDecimal(String createTableStr) {
    String decimalStr = "DECIMAL";
    String optimized = createTableStr;
    Pattern pattern = Pattern.compile("([\\s]+)(double)(\\([\\d]+,[\\d]+\\)[\\s]+)");
    Matcher matcher = pattern.matcher(createTableStr);
    while (matcher.find()) {
      optimized = matcher.replaceAll("$1" + decimalStr + "$3");
    }
    return optimized;
  }

  /**
   * MySQL固有書式ののUNIQUE INDEX 定義を、標準的なそれに置換して返す。
   * 
   * @param createTableStr
   *          最適化すべきMySQLのCreate Table 文
   * @return UNIQUE INDEX 定義が最適化された Create Table 文
   */
  public String optimizeUniqueConstraint(String createTableStr) {
    String optimized = createTableStr;
    Pattern pattern = Pattern.compile("(.?)(,[\\s]+UNIQUE KEY( `\\w+`)( (\\([^)]+`)\\)))");
    Matcher matcher = pattern.matcher(createTableStr);
    while (matcher.find()) {
      this.constraints.add("CONSTRAINT " + matcher.group(3) + " UNIQUE " + matcher.group(4));
      optimized = matcher.replaceAll("$1");
    }
    return optimized;
  }
}
