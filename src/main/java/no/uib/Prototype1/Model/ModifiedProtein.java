package no.uib.Prototype1.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis FranciscoHernández Sánchez
 */
public class ModifiedProtein {
//    public String stId;
//    public String name;
//    public String displayName;

    public int status;
    public Protein baseProtein;
    public List<ModifiedResidue> PTMs;                      //Requested PTM Configuration (Set of modifications)
    public List<EWAS> EWASs;                                //Matched ewas according to the PTMConfiguration //An EWAS contains only ONE PTMConfiguration; a ModifiedProtein can have MANY PTMConfigurations

    public ModifiedProtein() {
        PTMs = new ArrayList<ModifiedResidue>(16);
        EWASs = new ArrayList<EWAS>(16);
    }
}
