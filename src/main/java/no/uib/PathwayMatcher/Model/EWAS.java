package no.uib.PathwayMatcher.Model;

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
    public List<ModifiedResidue> PTMs;              //An EWAS contains only ONE PTMConfiguration; a ModifiedProtein can be matched to MANY PTMConfigurations
    public List<Reaction> reactionsList;
    public boolean matched;
    
    public EWAS(){
        matched = false;
        PTMs = new ArrayList<ModifiedResidue>(16);
        reactionsList = new ArrayList<Reaction>(16);
    }
}
