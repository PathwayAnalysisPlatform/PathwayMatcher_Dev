package no.uib.Prototype1.Stages;

import no.uib.Prototype1.Model.EWAS;
import no.uib.Prototype1.Model.ModifiedProtein;
import no.uib.Prototype1.Model.ModifiedResidue;
import static no.uib.Prototype1.Prototype1.MPs;

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
                                if (CandPTM.site == PTM.site) {
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
