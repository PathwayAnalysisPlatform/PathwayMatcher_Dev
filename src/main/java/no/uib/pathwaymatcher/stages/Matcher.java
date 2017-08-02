package no.uib.pathwaymatcher.stages;

import static java.lang.Math.abs;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.MatchType;
import static no.uib.pathwaymatcher.Conf.intMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.model.Modification;
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
                for (EWAS e : mp.EWASs) {
                    e.matched = true;
                }
            } else {
                for (int C = 0; C < mp.EWASs.size(); C++) {                 //For each candidate ewas
                    switch (MatchType.valueOf(strMap.get(Conf.StrVars.matchType))) {
                        case all:
                            if (mp.PTMs.size() == mp.EWASs.get(C).PTMs.size()) {    //Check that the number of modifications is the same
                                boolean found = false;
                                for (Modification PTM : mp.PTMs) {               //Check that each modification is contained
                                    found = false;
                                    for (Modification CandPTM : mp.EWASs.get(C).PTMs) {
                                        if (abs(CandPTM.site - PTM.site) <= intMap.get(Conf.IntVars.siteRange)) {             //Verify that the site is in the distance range
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
                            break;
                        case atLeastOneSite:
                        default:
                            if (mp.PTMs.size() >= 1) {
                                for (Modification CandPTM : mp.EWASs.get(C).PTMs) {
                                    for (Modification PTM : mp.PTMs) {
                                        if (abs(CandPTM.site - PTM.site) <= intMap.get(Conf.IntVars.siteRange)) {             //Verify that the site is in the distance range
                                            mp.EWASs.get(C).matched = true;
                                            break;      // Stop iterating over the input PTMs
                                        }
                                    }
                                    if (mp.EWASs.get(C).matched) {  //Stop iterating over the Canditate Ewas PTMs
                                        break;
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    }
}
