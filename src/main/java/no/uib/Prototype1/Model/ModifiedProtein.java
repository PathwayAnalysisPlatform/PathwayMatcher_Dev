package no.UiB.Prototype1.Model;

import java.util.List;

/**
 * @author Luis FranciscoHernández Sánchez
 */
public class ModifiedProtein {
    public String stId;
    public String name;
    public String displayName;
    public Protein baseProtein;
    public List<ModifiedResidue> PTMConfiguration;         //Requested PTMConfiguration
    public List<EWAS> matchedEwasList;                     //Matched ewas according to the PTMConfiguration //An EWAS contains only ONE PTMConfiguration; a ModifiedProtein can have MANY PTMConfigurations
}
