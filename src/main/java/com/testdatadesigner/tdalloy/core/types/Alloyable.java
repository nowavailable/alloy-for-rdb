package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.List;

public class Alloyable implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Sig> sigs;
    public List<Relation> relations;
    public List<Fact> facts;
    public Boolean isRailsOriented;

    public void fixPolymophic() {
    	
    }
    
    public void fixOneToOne() {
    	
    }
    
    public String toString() {
    	return null;
    }
}
