/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.UiB.Prototype1.Stages;

import no.UiB.Prototype1.Model.ModifiedProtein;
import no.UiB.Prototype1.Model.ModifiedResidue;
import static no.UiB.Prototype1.Prototype1.MPs;
import static no.UiB.Prototype1.Prototype1.matchedEWAS;

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
            ModifiedProtein mp = MPs.get(P);
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
                        if(!found){
                            break;
                        }
                    }
                    if(found){
                        matchedEWAS.add(mp.EWASs.get(C).stId);
                    }
                }
            }

        }
    }
}
