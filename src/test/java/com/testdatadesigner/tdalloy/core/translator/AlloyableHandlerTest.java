package com.testdatadesigner.tdalloy.core.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.CreateTableNode;
import com.testdatadesigner.tdalloy.core.io.IOGateway;
import com.testdatadesigner.tdalloy.core.rdbms.IRdbSchemaParser;
import com.testdatadesigner.tdalloy.core.rdbms.ISchemaSplitter;
import com.testdatadesigner.tdalloy.core.rdbms.impl.MySQLSchemaParser;
import com.testdatadesigner.tdalloy.core.rdbms.impl.MySQLSchemaSplitter;
import com.testdatadesigner.tdalloy.core.translater.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.MissingAtom;
import com.testdatadesigner.tdalloy.core.types.RelationPolymorphicTypified;
import com.testdatadesigner.tdalloy.driver.Bootstrap;

import junit.framework.Assert;
import junit.framework.TestCase;

public class AlloyableHandlerTest extends TestCase {

  List<CreateTableNode> resultList = new ArrayList<CreateTableNode>();
  Alloyable currentAlloyable;
  AlloyableHandler alloyableHandler;
  Consumer<Serializable> setWarning;

  protected void setUp() throws Exception {
    super.setUp();
    Bootstrap.setProps();
    // Map<String, List<Serializable>> map = IOGateway.getKVSMap();
    // map.put(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD), new ArrayList<Serializable>());
    // setWarning = o -> {
    // map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.REF_WARNING_ON_BUILD)).add(o);};
  }

  protected void tearDown() throws Exception {
    String separator = "  ";
    // String separator = "\t";
    for (IAtom result : this.currentAlloyable.atoms) {
      System.out.println(result.getName() + separator + result.getClass().getSimpleName() + separator
          + (result.getOriginPropertyName().isEmpty() ? "-" : result.getOriginPropertyName()) + separator
          + result.getIsAbstruct().toString() + separator
          + (result.getParent() == null ? "-" : result.getParent().getName()) + separator
          + (result.getOriginTypeName().isEmpty() ? "-" : result.getOriginTypeName()) + separator
          + (result.getClass().equals(RelationPolymorphicTypified.class)
              && ((RelationPolymorphicTypified) result).getExtended() != null
                  ? ((RelationPolymorphicTypified) result).getExtended().getName() : "-"));
    }
    System.out.println("-------------------------");
    for (IRelation result : this.currentAlloyable.relations) {
      System.out.println(result.getName() + separator + result.getClass().getSimpleName() + separator
          + (result.getOwner() == null ? "-" : result.getOwner().getName()) + separator
          + (result.getRefTo() == null ? "-" : result.getRefTo().getName()) + '(' + result.getOriginColumnNames() + ')'
          + separator + result.getIsNotEmpty());
    }
    System.out.println("-------------------------");
    for (Fact result : this.currentAlloyable.facts) {
      System.out.println(
          result.value + separator + result.owners.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
    }
    System.out.println("-------------------------");
    for (IAtom result : this.currentAlloyable.missingAtoms) {
      System.out.println(result.getName() + separator
          + ((MissingAtom) result).getOwners().stream().map(atom -> atom.getName()).collect(Collectors.joining(",")));
    }

    System.out.println("");
    System.out.println("==================================================");
    System.out.println("");
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        System.out.println(line);
      }
    }

    super.tearDown();
  }

  public void testBuildAll() throws IOException, StandardException, IllegalAccessException {
    URL resInfo = this.getClass().getResource("/naming_rule.dump");
    // URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
    String filePath = resInfo.getFile();
    ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
    List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

    IRdbSchemaParser parser = new MySQLSchemaParser();
    try {
      this.resultList = parser.inboundParse(results);
    } catch (StandardException e) {
      e.printStackTrace();
      throw e;
    }

    this.currentAlloyable = new Alloyable();
    this.alloyableHandler = new AlloyableHandler(currentAlloyable);
    try {
      this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      throw e;
    }

    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n" + "sig Album {\n"
        + "  \n" + "}\n" + "sig Book {\n" + "  person: lone Person,\n" + "  price: lone Boundary\n" + "}\n"
        + "sig Paper {\n" + "  person: lone Person,\n" + "  price: lone Boundary\n" + "}\n" + "sig Person {\n"
        + "  books: set Book,\n" + "  papers: set Paper,\n" + "  zines: set Zine\n" + "}\n" + "sig Photo {\n"
        + "  photoableType: lone Photo_PhotoableType\n" + "}\n" + "sig Url {\n"
        + "  urlableType: lone Url_UrlableType\n" + "}\n" + "sig Bookmark {\n" + "  \n" + "}\n" + "sig Zine {\n"
        + "  person: lone Person,\n" + "  price: lone Boundary,\n" + "  is_old: lone Bool\n" + "}\n"
        + "abstract sig Photo_PhotoableType {\n" + "  \n" + "}\n" + "sig Dummy1 {\n" + "  photos: some Photo\n" + "}\n"
        + "sig Dummy2 {\n" + "  photos: some Photo\n" + "}\n" + "sig PhotoableDummy1 extends Photo_PhotoableType {\n"
        + "  dummy1s: one Dummy1\n" + "}\n" + "sig PhotoableDummy2 extends Photo_PhotoableType {\n"
        + "  dummy2s: one Dummy2\n" + "}\n" + "abstract sig Url_UrlableType {\n" + "  \n" + "}\n" + "sig Dummy3 {\n"
        + "  urls: some Url\n" + "}\n" + "sig Dummy4 {\n" + "  urls: some Url\n" + "}\n"
        + "sig UrlableDummy3 extends Url_UrlableType {\n" + "  dummy3s: one Dummy3\n" + "}\n"
        + "sig UrlableDummy4 extends Url_UrlableType {\n" + "  dummy4s: one Dummy4\n" + "}\n" + "\n" + "fact {\n"
        + "  Person<:books = ~(Book<:person)\n" + "  Person<:papers = ~(Paper<:person)\n"
        + "  Person<:zines = ~(Zine<:person)\n"
        + "  (Photo.photoableType = Photo_PhotoableType) and (all p:Photo | p = (Photo<:photoableType).(p.(Photo<:photoableType)))\n"
        + "  Dummy1<:photos = ~(Photo<:photoableType.dummy1s)\n"
        + "  Dummy2<:photos = ~(Photo<:photoableType.dummy2s)\n"
        + "  (Url.urlableType = Url_UrlableType) and (all u:Url | u = (Url<:urlableType).(u.(Url<:urlableType)))\n"
        + "  Dummy3<:urls = ~(Url<:urlableType.dummy3s)\n" + "  Dummy4<:urls = ~(Url<:urlableType.dummy4s)\n" + "}\n"
        + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  private void procDDL(URL resInfo) throws IOException, StandardException, IllegalAccessException {
    String filePath = resInfo.getFile();
    ISchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
    List<String> results = IOGateway.readSchemesFromDDL(filePath, ddlSplitter);

    IRdbSchemaParser parser = new MySQLSchemaParser();
    this.resultList = parser.inboundParse(results);

    this.currentAlloyable = new Alloyable();
    this.alloyableHandler = new AlloyableHandler(currentAlloyable);
    this.currentAlloyable = this.alloyableHandler.buildFromDDL(this.resultList);
  }

  /*
   * 外部キー。ネーミングルールに則った判定
   */
  public void testSimepleRelationNamingRule() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_simple_relation_by_naming_rule.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Actor {\n" + "  characters: set Character,\n" + "  name: one Boundary\n" + "}\n"
        + "sig Character {\n" + "  actor: one Actor,\n" + "  name: one Boundary\n" + "}\n" + "\n"
        + "fact {\n" + "  Actor<:characters = ~(Character<:actor)\n" + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
    /* als上での、ユニーク制約に対するチェックロジック
      assert dup {
        all a,a': Actor |
        (a != a' && a.characters != none && a'.characters != none) =>
        a.characters != a'.characters
      }
      check dup
     */
  }

  /*
   * 外部キー。REFERENCES宣言に則った判定
   */
  public void testSimpleRelationConstraint() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_simple_relation_by_constraints.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Actor {\n" + "  characters: set Character,\n" + "  name: one Boundary\n" + "}\n"
        + "sig Character {\n" + "  actor: one Actor,\n" + "  name: one Boundary\n" + "}\n" + "\n"
        + "fact {\n" + "  Actor<:characters = ~(Character<:actor)\n" + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  /*
   * 外部キー。One to one.
   */
  public void testSimpleRelationOneToOne() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_simple_relation_one_to_one.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Cup {\n" + "  saucers: set Saucer\n" + "}\n" + "sig Saucer {\n" + "  cup: one Cup\n"
        + "}\n" + "\n" + "fact {\n" + "  Cup<:saucers = ~(Saucer<:cup)\n"
        + "  all e,e':Saucer | e != e' => (e.cup) != (e'.cup)\n" + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  /*
   * 複数外部キー。ネーミングルールに則った判定による
   * 注文明細、商品、顧客、を例に
   */
  public void testMultiFKey() throws Exception {
    URL resInfo = this.getClass().getResource(
        "/ddl_foreign_keys_naming_rule.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Item {\n" + "  order_details: set OrderDetail\n" + "}\n" + "sig Customer {\n"
        + "  order_details: set OrderDetail\n" + "}\n" + "sig Order {\n"
        + "  order_details: set OrderDetail\n" + "}\n" + "sig OrderDetail {\n"
        + "  customer: one Customer,\n" + "  item: one Item,\n" + "  order: one Order\n" + "}\n"
        + "\n" + "fact {\n" + "  Customer<:order_details = ~(OrderDetail<:customer)\n"
        + "  Item<:order_details = ~(OrderDetail<:item)\n"
        + "  Order<:order_details = ~(OrderDetail<:order)\n"
        + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  /*
   * 複合ユニーク制約。ネーミングルールに則った判定による外部キー間のユニーク
   * 注文明細、商品、顧客、を例に
   */
  public void testCompositeUnique() throws Exception {
    URL resInfo = this.getClass().getResource(
        "/ddl_foreign_keys_naming_rule_with_composite_unique.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Item {\n" + "  order_details: set OrderDetail\n" + "}\n" + "sig Customer {\n"
        + "  order_details: set OrderDetail\n" + "}\n" + "sig Order {\n"
        + "  order_details: set OrderDetail\n" + "}\n" + "sig OrderDetail {\n"
        + "  customer: one Customer,\n" + "  item: one Item,\n" + "  order: one Order\n" + "}\n"
        + "\n" + "fact {\n" + "  Customer<:order_details = ~(OrderDetail<:customer)\n"
        + "  Item<:order_details = ~(OrderDetail<:item)\n"
        + "  Order<:order_details = ~(OrderDetail<:order)\n"
        + "  all e,e':OrderDetail | e != e' => (e.customer->e.item->e.order) != (e'.customer->e'.item->e'.order)\n"
        + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
    /* als上での、ユニーク制約に対するチェックロジック
      assert dup {
        all d,d': OrderDetail |
          (d != d') =>
            (d.customer -> d.item -> d.order) !=
              (d'.customer -> d'.item -> d'.order)
      }
      check dup
     */
  }

  /*
   * 複数外部キーで複合ユニーク制約。REFERENCES宣言に則った判定による外部キー間のユニーク
   * 注文明細、商品、顧客、を例に
   */
  public void testMultiFKeyConstraint_with_CompositeUnique() throws Exception {
    URL resInfo = this.getClass().getResource(
        "/ddl_foreign_keys_constraint_with_composite_unique.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Item {\n" + "  order_details: set OrderDetail\n" + "}\n" + "sig Customer {\n"
        + "  order_details: set OrderDetail\n" + "}\n" + "sig Order {\n"
        + "  order_details: set OrderDetail\n" + "}\n" + "sig OrderDetail {\n"
        + "  order: one Order,\n" + "  customer: one Customer,\n" + "  item: one Item\n" + "}\n"
        + "\n" + "fact {\n" + "  Order<:order_details = ~(OrderDetail<:order)\n"
        + "  Customer<:order_details = ~(OrderDetail<:customer)\n"
        + "  Item<:order_details = ~(OrderDetail<:item)\n"
        + "  all e,e':OrderDetail | e != e' => (e.customer->e.item->e.order) != (e'.customer->e'.item->e'.order)\n"
        + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  /*
   * Actor, Movie, Character を例に。
   * idの無い交差テーブル = 複合主キー。
   */
  public void testCompositePKey() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_composite_pkey.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Actor {\n" + "  characters: set Character,\n" + "  name: one Boundary\n" + "}\n"
        + "sig Movie {\n" + "  characters: set Character,\n" + "  title: one Boundary\n" + "}\n"
        + "sig Character {\n" + "  actor: one Actor,\n" + "  movie: one Movie,\n"
        + "  name: one Boundary\n" + "}\n" + "\n" + "fact {\n"
        + "  Actor<:characters = ~(Character<:actor)\n"
        + "  Movie<:characters = ~(Character<:movie)\n"
        + "  all e,e':Character | e != e' => (e.actor->e.movie) != (e'.actor->e'.movie)\n" + "}\n"
        + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
    /* als上での、ユニーク制約に対するチェックロジック
      assert dup {
        all c,c': Character |
          (c != c') =>
            (c.actor -> c.movie) != (c'.actor -> c'.movie)
      }
      check dup
     */
  }

  /*
   * Actor, Movie, Character を例に。
   * idの無い交差テーブル = 複合主キー
   * と、する代わりに、ふたつの外部キーをユニークとする。
   * （※この交差テーブルを参照する外部キーは、testCompositePKey()と同じになる）
   * （※そしてどちらの表現でも、alloy上では同じ意味になる。）
   */
  public void testCompositePsuedoPKey() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_composite_psuedo_pkey.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }

    URL resInfo_another = this.getClass().getResource("/ddl_composite_pkey.sql");
    this.procDDL(resInfo_another);
    StringBuilder str_another = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str_another.append(line);
        str_another.append("\n");
      }
    }

    Assert.assertEquals(str.toString(), str_another.toString());
  }

  /*
   * Actor, Movie, Character, Novelty を例に。
   * idの無い交差テーブル. ふたつの外部キーをユニークとする。
   * この外部キーを参照。つまり複合キーの伝播。
   * そしてNovelty は、Character と同じActor, Movie を参照する。
   */
  public void testCompositePKeyReferred() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_composite_fkey.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Actor {\n" + "  characters: set Character,\n" + "  novelties: set Novelty,\n"
        + "  name: one Boundary\n" + "}\n" + "sig Movie {\n" + "  characters: set Character,\n"
        + "  novelties: set Novelty,\n" + "  title: one Boundary\n" + "}\n" + "sig Character {\n"
        + "  actor: one Actor,\n" + "  movie: one Movie,\n" + "  novelties: set Novelty,\n"
        + "  name: one Boundary\n" + "}\n" + "sig Novelty {\n" + "  character: one Character,\n"
        + "  actor: one Actor,\n" + "  movie: one Movie,\n" + "  name: one Boundary\n" + "}\n"
        + "\n" + "fact {\n" + "  Actor<:characters = ~(Character<:actor)\n"
        + "  Movie<:characters = ~(Character<:movie)\n"
        + "  Character<:novelties = ~(Novelty<:character)\n"
        + "  Actor<:novelties = ~(Novelty<:actor)\n" + "  Movie<:novelties = ~(Novelty<:movie)\n"
        + "  all e,e':Character | e != e' => (e.actor->e.movie) != (e'.actor->e'.movie)\n"
        + "  all e,e':Novelty | e != e' => (e.actor->e.movie) != (e'.actor->e'.movie)\n"
        + "  all e:Novelty | e.character.actor = e.actor && e.character.movie = e.movie\n" + "}\n"
        + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  /*
   * 外部キーにユニーク制約が宣言されていない例
   */
  public void testCompositeImplicitUnique() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_composite_fkey_not_unique.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    /*
      ユニーク制約つきのキーを参照したからといって、
      参照する元の外部キーでもユニーク制約を継承するわけでは無い。
      これは↓ Counter example が出る
      assert dup {
        all e,e' : Novelty | (e != e') => (e.actor->e.movie) != (e'.actor->e'.movie)
      }
      check dup
     */
  }

  /*
   * persons テーブルが参照されているが、DDLにpersons テーブルは含まれていない。
   */
  public void testInconsistency() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_inconsistency.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n" + "sig Book {\n"
        + "  person: lone Boundary,\n" + "  price: lone Boundary\n" + "}\n" + "sig Paper {\n"
        + "  person: lone Boundary,\n" + "  price: lone Boundary\n" + "}\n" + "sig Photo {\n"
        + "  photoable_type: lone Boundary\n" + "}\n" + "\n" + "fact {\n" + "}\n" + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  /*
   * 複合外部キーから参照されているテーブルのうち、ひとつがDDLに含まれていない。
   */
  public void testInconsistencyComposite() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_inconsistency_composite.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Actor {\n" + "  characters: set Character,\n" + "  novelties: set Novelty,\n"
        + "  name: one Boundary\n" + "}\n" + "sig Character {\n" + "  actor: lone Actor,\n"
        + "  movie: lone Boundary,\n" + "  novelties: set Novelty,\n" + "  name: one Boundary\n"
        + "}\n" + "sig Novelty {\n" + "  character: one Character,\n" + "  actor: one Actor,\n"
        + "  movie: one Boundary,\n" + "  name: one Boundary\n" + "}\n" + "\n" + "fact {\n"
        + "  Actor<:characters = ~(Character<:actor)\n"
        + "  Character<:novelties = ~(Novelty<:character)\n"
        + "  Actor<:novelties = ~(Novelty<:actor)\n"
        + "  all e,e':Character | e != e' => (e.actor->e.movie) != (e'.actor->e'.movie)\n"
        + "  all e,e':Novelty | e != e' => (e.actor->e.movie) != (e'.actor->e'.movie)\n"
        + "  all e:Novelty | e.character.actor = e.actor && e.character.movie = e.movie\n" + "}\n"
        + "\n" + "run {}\n");
    Assert.assertEquals(str.toString(), expected);
  }

  public void testNoRelations() throws Exception {
    URL resInfo = this.getClass().getResource("/ddl_no_relations.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n" + "sig Book {\n"
        + "  person: lone Boundary,\n" + "  some: lone Boundary,\n" + "  price: lone Boundary\n" + "}\n" + "\n"
        + "fact {\n" + "}\n" + "\n" + "run {}" + "\n");
    Assert.assertEquals(str.toString(), expected);
  }

  public void testCompositePractical() throws Exception {
    URL resInfo = this.getClass().getResource("/reservation.sql");
    this.procDDL(resInfo);
    StringBuilder str = new StringBuilder();
    try (BufferedReader outputToAlsReader = this.alloyableHandler.outputToAls()) {
      String line = null;
      while ((line = outputToAlsReader.readLine()) != null) {
        str.append(line);
        str.append("\n");
      }
    }
    String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Apply {\n" + "  deal: one Deal,\n" + "  frame: one Frame,\n"
        + "  user: one Boundary,\n" + "  campaign: one Campaign,\n" + "  shop: one Shop,\n"
        + "  deal_number: one Boundary,\n" + "  room_number: one Boundary,\n"
        + "  from_to_str: one Boundary,\n" + "  quantity: one Boundary,\n"
        + "  created_at: one Boundary\n" + "}\n" + "sig Campaign {\n" + "  applies: set Apply,\n"
        + "  deals: set Deal,\n" + "  frames: set Frame,\n" + "  rooms: set Room,\n"
        + "  url_key: one Boundary,\n" + "  name: one Boundary,\n"
        + "  display_started_at: one Boundary,\n" + "  display_ended_at: one Boundary,\n"
        + "  deal_scheduling_type: one Boundary,\n" + "  limit_by_term: one Boundary,\n"
        + "  term_day_for_limit: one Boundary,\n" + "  created_at: one Boundary,\n"
        + "  updated_at: one Boundary\n" + "}\n" + "sig Deal {\n" + "  applies: set Apply,\n"
        + "  campaign: one Campaign,\n" + "  deal_number: one Boundary,\n"
        + "  started_at: one Boundary,\n" + "  ended_at: one Boundary,\n"
        + "  lottery_started_at: one Boundary,\n" + "  resume_datetime: lone Boundary,\n"
        + "  status: one Boundary,\n" + "  created_at: one Boundary,\n"
        + "  updated_at: one Boundary\n" + "}\n" + "sig Frame {\n" + "  applies: set Apply,\n"
        + "  room: one Room,\n" + "  campaign: one Campaign,\n" + "  shop: one Shop,\n"
        + "  reservations: set Reservation,\n" + "  from_to_str: one Boundary,\n"
        + "  room_number: one Boundary,\n" + "  limit: one Boundary,\n"
        + "  created_at: one Boundary,\n" + "  updated_at: one Boundary\n" + "}\n"
        + "sig Reservation {\n" + "  frame: one Frame,\n" + "  lottery_win: lone Boundary,\n"
        + "  reservation_number: one Boundary,\n" + "  status: one Boundary,\n"
        + "  created_at: one Boundary,\n" + "  updated_at: one Boundary\n" + "}\n" + "sig Room {\n"
        + "  frames: set Frame,\n" + "  campaign: one Campaign,\n" + "  shop: one Shop,\n"
        + "  room_number: one Boundary,\n" + "  created_at: one Boundary,\n"
        + "  updated_at: one Boundary\n" + "}\n" + "sig Shop {\n" + "  applies: set Apply,\n"
        + "  frames: set Frame,\n" + "  rooms: set Room,\n" + "  name: one Boundary,\n"
        + "  url_key: one Boundary,\n" + "  active_flag: lone Bool,\n"
        + "  started_at: lone Boundary,\n" + "  ended_at: lone Boundary,\n"
        + "  tel: lone Boundary,\n" + "  email: lone Boundary,\n" + "  memo: lone Boundary,\n"
        + "  deleted_at: lone Boundary,\n" + "  created_at: one Boundary,\n"
        + "  updated_at: one Boundary,\n" + "  free_tag: lone Boundary,\n"
        + "  addition_button_text: lone Boundary\n" + "}\n" + "\n" + "fact {\n"
        + "  Deal<:applies = ~(Apply<:deal)\n" + "  Frame<:applies = ~(Apply<:frame)\n"
        + "  Room<:frames = ~(Frame<:room)\n" + "  Campaign<:applies = ~(Apply<:campaign)\n"
        + "  Shop<:applies = ~(Apply<:shop)\n" + "  Campaign<:deals = ~(Deal<:campaign)\n"
        + "  Campaign<:frames = ~(Frame<:campaign)\n" + "  Shop<:frames = ~(Frame<:shop)\n"
        + "  Frame<:reservations = ~(Reservation<:frame)\n"
        + "  Campaign<:rooms = ~(Room<:campaign)\n" + "  Shop<:rooms = ~(Room<:shop)\n"
        + "  all e,e':Apply | e != e' => (e.deal_number->e.campaign->e.shop->e.room_number->e.from_to_str) != (e'.deal_number->e'.campaign->e'.shop->e'.room_number->e'.from_to_str)\n"
        + "  all e,e':Deal | e != e' => (e.campaign->e.deal_number) != (e'.campaign->e'.deal_number)\n"
        + "  all e,e':Frame | e != e' => (e.from_to_str->e.campaign->e.shop->e.room_number) != (e'.from_to_str->e'.campaign->e'.shop->e'.room_number)\n"
        + "  all e,e':Reservation | e != e' => (e.frame->e.reservation_number) != (e'.frame->e'.reservation_number)\n"
        + "  all e,e':Room | e != e' => (e.campaign->e.shop->e.room_number) != (e'.campaign->e'.shop->e'.room_number)\n"
        + "  all e:Apply | e.deal.campaign = e.campaign && e.deal.deal_number.val = e.deal_number.val\n"
        + "  all e:Apply | e.frame.from_to_str.val = e.from_to_str.val && e.frame.campaign = e.campaign && e.frame.shop = e.shop && e.frame.room_number.val = e.room_number.val\n"
        + "  all e:Frame | e.room.campaign = e.campaign && e.room.shop = e.shop && e.room.room_number.val = e.room_number.val\n"
        + "}\n" + "\n" + "run {}\n");
  }

}
