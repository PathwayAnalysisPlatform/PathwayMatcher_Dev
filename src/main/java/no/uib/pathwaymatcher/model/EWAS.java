package no.uib.pathwaymatcher.model;

import java.util.ArrayList;
import java.util.List;
import no.uib.pathwaymatcher.Conf;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class EWAS {

    public String stId;
    //    String name;
    public String displayName;
    public List<Modification> PTMs;              //An EWAS contains only ONE PTMConfiguration; a Proteoform can be matched to MANY PTMConfigurations
    public List<ReactionResultEntry> reactionsList;
    public boolean matched;

    public EWAS() {
        matched = false;
        PTMs = new ArrayList<Modification>(16);
        reactionsList = new ArrayList<ReactionResultEntry>(16);
    }

    public String printEwasPTMs() {
        String result = "[";
        int cont = 0;
        for (Modification ptm : PTMs) {
            if (cont > 0) {
                result += Conf.strMap.get(Conf.StrVars.ptmColSep);
            }
            result += ptm.toString();
            cont++;
        }
        return result + "]";
    }
}
