package no.UiB.Prototype1.Model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class EWAS {
    public String stId;
//    String name;
    public String displayName;
    public List<ModifiedResidue> PTMs; //An EWAS contains only ONE PTMConfiguration; a ModifiedProtein can be matched to MANY PTMConfigurations
    
    public EWAS(){
        PTMs = new ArrayList<ModifiedResidue>(16);
    }
}
