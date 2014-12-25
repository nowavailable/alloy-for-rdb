package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.testdatadesigner.tdalloy.core.types.DtoForPrepare.Column;
import com.testdatadesigner.tdalloy.core.types.DtoForPrepare.Relation;

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
        for (String table : tables) {
            Column column1 = dto.constructColumn();
            column1.name = "name";

            Column column2 = dto.constructColumn();
            column2.name = "person_id";
            
            Relation relation = dto.constructRelation();
            relation.type = DtoForPrepare.RelationType.MANY_TO_ONE;
            relation.refTo = Arrays.asList("persons");
            column2.relation = relation;
            dto.tables.put(table, new ArrayList<Column>() {
                {
                    this.add(column1);
                    this.add(column2);
                }
            });
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
        dto.tables.put("photos", columns);

        dto.tables.put("persons", new ArrayList<Column>());

        String json = new Gson().toJson(dto);
        System.out.println(json);
        
    }
}
