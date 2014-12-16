package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class Fact implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String value;
    public enum tipify {
        RELATION,
        RELATION_POLYMOPHIC,
        ROWS_CONSTRAINT,
    }
}
