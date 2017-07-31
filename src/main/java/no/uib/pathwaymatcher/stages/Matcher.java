package no.uib.pathwaymatcher.stages;

import static java.lang.Math.abs;
import no.uib.pathwaymatcher.Conf;
import static no.uib.pathwaymatcher.Conf.intMap;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.model.ModifiedResidue;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Matcher {
    //For each modified protein in the input list, filter the candidate ewas. (Match the requested ewas with a subset of candidate ewas)

    public static void matchCandidates() {
        //For every modified protein in the input list, match the cantidate ewas with the original protein.
        for (int P = 0; P < MPs.size(); P++) {
            //Method 1: The modifications match exactly to the ones provided by the input file.
            //          If no modifications were provided, then all available ewas are matched.
            ModifiedProtein mp = MPs.get(P);
            if (mp.PTMs.size() == 0) {
                for(EWAS e : mp.EWASs){
                    e.matched = true;
                }
            } else {
                for (int C = 0; C < mp.EWASs.size(); C++) {                 //For each candidate ewas
                    if (mp.PTMs.size() == mp.EWASs.get(C).PTMs.size()) {    //Check that the number of modifications is the same
                        boolean found = false;
                        for (ModifiedResidue PTM : mp.PTMs) {               //Check that each modification is contained
                            found = false;
                            for (ModifiedResidue CandPTM : mp.EWASs.get(C).PTMs) {
                                if (abs(CandPTM.site-PTM.site) <= intMap.get(Conf.IntVars.siteRange)) {             //Verify that the site is in the distance range
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                break;
                            }
                        }
                        if (found) {
                            mp.EWASs.get(C).matched = true;
                        }
                    }
                }
            }
        }
    }
}
