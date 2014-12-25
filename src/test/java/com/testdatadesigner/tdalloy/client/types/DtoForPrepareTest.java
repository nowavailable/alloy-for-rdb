package com.testdatadesigner.tdalloy.client.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Column;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Relation;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Table;

import junit.framework.TestCase;

public class DtoForPrepareTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
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
        relation1.type = DtoForPrepare.RelationType.POLYMOPHIC;
        column1.relation = relation1;

        Column column2 = dto.constructColumn();
        column2.name = "photoable_id";
        Relation relation2 = dto.constructRelation();
        relation2.type = DtoForPrepare.RelationType.POLYMOPHIC;
        column2.relation = relation2;
        List<Column> columns =
                new ArrayList<Column>() {
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
