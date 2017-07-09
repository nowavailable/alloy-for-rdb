package com.testdatadesigner.tdalloy.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.testdatadesigner.tdalloy.core.conversation.PolymorphicRelationResolver;
import com.testdatadesigner.tdalloy.core.conversation.ResolvePolymorphicCommand;
import com.testdatadesigner.tdalloy.core.io.IOGateway;
import com.testdatadesigner.tdalloy.core.types.*;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ImporterTest extends TestCase {

  private String filePath;

  protected void setUp() throws Exception {
    super.setUp();
    Bootstrap.setProps();
    URL resInfo = this.getClass().getResource("/naming_rule.dump");
    // URL resInfo = this.getClass().getResource("/lotteries_raw.sql");
    filePath = resInfo.getFile();
  }

  public void testParse() throws IOException {
    Importer importer = new Importer();
    try {
      importer.parse(filePath, Importer.Database.MYSQL);

      Map<String, List<Serializable>> map = IOGateway.getKVSMap();
      List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));

      String separator = "  ";
      // String separator = "\t";
      for (IAtom result : ((Alloyable) list.get(0)).atoms) {
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
      for (IRelation result : ((Alloyable) list.get(0)).relations) {
        System.out.println(result.getName() + separator + result.getClass().getSimpleName() + separator
            + (result.getOwner() == null ? "-" : result.getOwner().getName()) + separator
            + (result.getRefTo() == null ? "-" : result.getRefTo().getName()) + '(' + result.getOriginColumnNames()
            + ')' + separator + result.getIsNotEmpty());
      }
      System.out.println("-------------------------");
      for (Fact result : ((Alloyable) list.get(0)).facts) {
        System.out.println(
            result.value + separator + result.owners.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
      }
    } catch (ImportError e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void testAls() throws IOException {
    Importer importer = new Importer();
    Alloyable alloyable = null;
    try {
      importer.parse(filePath, Importer.Database.MYSQL);
      Map<String, List<Serializable>> map = IOGateway.getKVSMap();
      List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));
      alloyable = (Alloyable) list.get(0);
    } catch (ImportError e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try (BufferedReader reader = importer.takeOut(alloyable)) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void testPolymorphicAlsRewrite() throws IOException {
    Importer importer = new Importer();
    Alloyable alloyable = null;
    try {
      importer.parse(filePath, Importer.Database.MYSQL);
      Map<String, List<Serializable>> map = IOGateway.getKVSMap();
      List<Serializable> list = map.get(IOGateway.STORE_KEYS.get(IOGateway.StoreData.ALLOYABLE_ON_BUILD));
      alloyable = (Alloyable) list.get(0);

      //
      List<IAtom> pseudoAtoms = alloyable.getPseudoAtoms();
      PseudoAtom dummy1 = (PseudoAtom) alloyable.atoms.stream().
        filter((atom) -> (atom.getName().equals(new String("Dummy1")))).
        collect(Collectors.toList()).
        get(0);
      dummy1.shouldReplaceTo = alloyable.atoms.stream().
        filter((atom) -> (atom.getName().equals(new String("Album")))).
        collect(Collectors.toList()).
        get(0);
      PseudoAtom dummy2 = (PseudoAtom) alloyable.atoms.stream().
        filter((atom) -> (atom.getName().equals(new String("Dummy2")))).
        collect(Collectors.toList()).
        get(0);
      dummy2.shouldReplaceTo = alloyable.atoms.stream().
        filter((atom) -> (atom.getName().equals(new String("Person")))).
        collect(Collectors.toList()).
        get(0);

      ResolvePolymorphicCommand resolvePolymorphicCommand = new ResolvePolymorphicCommand();
      resolvePolymorphicCommand.targetPseudoAtoms.add(dummy1);
      resolvePolymorphicCommand.targetPseudoAtoms.add(dummy2);

      PolymorphicRelationResolver polymorphicRelationResolver = new PolymorphicRelationResolver();
      polymorphicRelationResolver.proc(resolvePolymorphicCommand, alloyable);

    } catch (ImportError e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    StringBuilder str = new StringBuilder();
    try (BufferedReader reader = importer.takeOut(alloyable)) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        //System.out.println(line);
        str.append(line);
        str.append("\n");
      }
      String expected = new String("open util/boolean\n" + "sig Boundary { val: one Int }\n" + "\n"
        + "sig Album {\n" + "  photos: some Photo\n" + "}\n" + "sig Book {\n"
        + "  person: lone Person,\n" + "  price: lone Boundary\n" + "}\n" + "sig Paper {\n"
        + "  person: lone Person,\n" + "  price: lone Boundary\n" + "}\n" + "sig Person {\n"
        + "  books: set Book,\n" + "  papers: set Paper,\n" + "  zines: set Zine,\n"
        + "  photos: some Photo\n" + "}\n" + "sig Photo {\n"
        + "  photoableType: lone Photo_PhotoableType\n" + "}\n" + "sig Url {\n"
        + "  urlableType: lone Url_UrlableType\n" + "}\n" + "sig Bookmark {\n" + "  \n" + "}\n"
        + "sig Zine {\n" + "  person: lone Person,\n" + "  price: lone Boundary,\n"
        + "  is_old: lone Bool\n" + "}\n" + "abstract sig Photo_PhotoableType {\n" + "  \n" + "}\n"
        + "sig PhotoableAlbum extends Photo_PhotoableType {\n" + "  albums: one Album\n" + "}\n"
        + "sig PhotoablePerson extends Photo_PhotoableType {\n" + "  persons: one Person\n" + "}\n"
        + "abstract sig Url_UrlableType {\n" + "  \n" + "}\n" + "sig Dummy3 {\n"
        + "  urls: some Url\n" + "}\n" + "sig Dummy4 {\n" + "  urls: some Url\n" + "}\n"
        + "sig UrlableDummy3 extends Url_UrlableType {\n" + "  dummy3: one Dummy3\n" + "}\n"
        + "sig UrlableDummy4 extends Url_UrlableType {\n" + "  dummy4: one Dummy4\n" + "}\n" + "\n"
        + "fact {\n" + "  Person<:books = ~(Book<:person)\n"
        + "  Person<:papers = ~(Paper<:person)\n" + "  Person<:zines = ~(Zine<:person)\n"
        + "  (Photo.photoableType = Photo_PhotoableType) and (all p:Photo | p = (Photo<:photoableType).(p.(Photo<:photoableType)))\n"
        + "  Album<:photos = ~(Photo<:photoableType.albums)\n"
        + "  Person<:photos = ~(Photo<:photoableType.persons)\n"
        + "  (Url.urlableType = Url_UrlableType) and (all u:Url | u = (Url<:urlableType).(u.(Url<:urlableType)))\n"
        + "  Dummy3<:urls = ~(Url<:urlableType.dummy3)\n"
        + "  Dummy4<:urls = ~(Url<:urlableType.dummy4)\n" + "}\n" + "\n" + "run {}\n");
      Assert.assertEquals(str.toString(), expected);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void testJSON() throws Exception {
    Importer importer = new Importer();
    URL resInfo = this.getClass().getResource("/naming_rule.dump");
    String filePath = resInfo.getFile();
    Alloyable currentAlloyable = importer.getAlloyable(filePath, Importer.Database.MYSQL);
    String json = new Gson().toJson(currentAlloyable);
    System.out.println(json);
  }

  // TODO: JSONではなくmsgpackに変換/デコードする処理をNashorn上で?実行

  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
