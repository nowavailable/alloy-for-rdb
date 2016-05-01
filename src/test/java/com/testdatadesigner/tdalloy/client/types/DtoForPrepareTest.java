package com.testdatadesigner.tdalloy.client.types;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.foundationdb.sql.parser.CreateTableNode;
import com.google.gson.Gson;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Column;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Relation;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Table;
import com.testdatadesigner.tdalloy.core.io.IOGateway;
import com.testdatadesigner.tdalloy.core.io.IRdbSchemaParser;
import com.testdatadesigner.tdalloy.core.io.ISchemaSplitter;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.io.impl.MySQLSchemaSplitter;
import com.testdatadesigner.tdalloy.core.translater.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.igniter.Bootstrap;

import junit.framework.TestCase;

public class DtoForPrepareTest extends TestCase {

  List<CreateTableNode> resultList = new ArrayList<CreateTableNode>();
  Alloyable currentAlloyable;

  protected void setUp() throws Exception {

    super.setUp();
    Bootstrap.setProps();
    URL resInfo = this.getClass().getResource("/naming_rule.dump");
    // URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
    String filePath = resInfo.getFile();
    ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
    List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

    IRdbSchemaParser parser = new MySQLSchemaParser();
    this.resultList = parser.inboundParse(results);

    // Map<String, List<Serializable>> map = IOGateway.getKVSMap();
    // map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
    // Consumer<Serializable> setWarning = o -> {
    // map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};

    AlloyableHandler alloyableHandler = new AlloyableHandler(new Alloyable());
    // this.currentAlloyable = alloyableHandler.buildFromDDL(this.resultList, setWarning);
    this.currentAlloyable = alloyableHandler.buildFromDDL(this.resultList);

  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testAlloyableToDTO() {

    DtoForPrepare dto = new DtoForPrepare();
    dto.buiildFromAlloyable(this.currentAlloyable);
    String json = new Gson().toJson(this.currentAlloyable);
    System.out.println(json);
  }

  public void testToJson() {
    DtoForPrepare dto = new DtoForPrepare();
    List<String> tables = Arrays.asList("books", "zines");
    for (String tableName : tables) {
      Column column1 = dto.constructColumn();
      column1.name = "name";
      // Relation relation1 = dto.constructRelation();
      // relation1.type = DtoForPrepare.RelationType.NONE;
      // column1.relation = relation1;

      Column column2 = dto.constructColumn();
      column2.name = "person_id";
      Relation relation2 = dto.constructRelation();
      relation2.type = DtoForPrepare.RelationType.MANY_TO_ONE;
      relation2.refTo = Arrays.asList("persons");
      column2.relation = relation2;

      Table table = dto.constructTable();
      table.name = tableName;
      table.columns = new ArrayList<Column>() {
        {
          this.add(column1);
          this.add(column2);
        }
      };
      dto.tables.add(table);
    }

    Column column1 = dto.constructColumn();
    column1.name = "photoable_type";
    Relation relation1 = dto.constructRelation();
    relation1.type = DtoForPrepare.RelationType.POLYMORPHIC;
    column1.relation = relation1;

    Column column2 = dto.constructColumn();
    column2.name = "photoable_id";
    Relation relation2 = dto.constructRelation();
    relation2.type = DtoForPrepare.RelationType.POLYMORPHIC;
    column2.relation = relation2;
    List<Column> columns = new ArrayList<Column>() {
      {
        this.add(column1);
        this.add(column2);
      }
    }.stream().sorted((a, b) -> a.relation.type.toString().compareTo(b.relation.type.toString()))
        .collect(Collectors.toList());

    Table table1 = dto.constructTable();
    table1.name = "photos";
    table1.columns = new ArrayList<Column>() {
      {
        this.add(column1);
        this.add(column2);
      }
    };
    dto.tables.add(table1);

    Table table2 = dto.constructTable();
    table1.name = "persons";
    dto.tables.add(table2);

    String json = new Gson().toJson(dto);
    System.out.println(json);
  }
}
