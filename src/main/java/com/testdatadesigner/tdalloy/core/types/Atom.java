package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class Atom implements Serializable {
  private static final long serialVersionUID = 1L;

  public String name = "";
  public Integer seq;
  // public Tipify type;
  //
  // public static enum Tipify {
  // ENTITY, // テーブル相当
  // PROPERTY, // カラム値である（Boolean型以外はこれにまとめる）
  // POLYMORPHIC_ABSTRACT, // ポリモーフィック関連のtypeの抽象化されたsig
  // POLYMOPHIC_IMPLIMENT, // ポリモーフィック関連のtypeの抽象化されたsigの継承先
  // BOOLEAN_FACTOR,
  // // TODO: 状態sig用。
  // // STATE,
  // }

  public Boolean isAbstruct = false;
  public String originPropertyName = "";
  public String originTypeName = ""; // カラムの型を格納 TODO: テーブルはどうする？ Table or View ?

  public Boolean ignore = Boolean.FALSE;

  public Atom() {
    super();
  }

  // public Atom(Tipify type) {
  // super();
  // this.type = type;
  // }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getSeq() {
    return seq;
  }

  public void setSeq(Integer seq) {
    this.seq = seq;
  }

  public Boolean getIsAbstruct() {
    return isAbstruct;
  }

  public void setIsAbstruct(Boolean isAbstruct) {
    this.isAbstruct = isAbstruct;
  }

  public String getOriginPropertyName() {
    return originPropertyName;
  }

  public void setOriginPropertyName(String originPropertyName) {
    this.originPropertyName = originPropertyName;
  }

  public String getOriginTypeName() {
    return originTypeName;
  }

  public void setOriginTypeName(String originTypeName) {
    this.originTypeName = originTypeName;
  }

  public Boolean getIgnore() {
    return ignore;
  }

  public void setIgnore(Boolean ignore) {
    this.ignore = ignore;
  }

}
