package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.Modification;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.ReactionResultEntry;

import java.util.Set;
import java.util.TreeSet;

import static java.lang.Math.abs;
import static no.uib.pathwaymatcher.Conf.intMap;
import static no.uib.pathwaymatcher.Conf.strMap;

public class MatcherProteoformsStrict extends Matcher {
    @Override
    public TreeSet<ReactionResultEntry> match(Set<?> entities) {

        //For every modified protein in the input list, match the cantidate ewas with the original protein.
        for (
                int P = 0; P < MPs.size(); P++)

        {
            //Method 1: The modifications match exactly to the ones provided by the input file.
            //          If no modifications were provided, then all available ewas are matched.
            Proteoform mp = MPs.get(P);
            if (mp.PTMs.size() == 0) {
                for (EWAS e : mp.EWASs) {
                    e.matched = true;
                }
            } else {
                for (int C = 0; C < mp.EWASs.size(); C++) {                 //For each candidate ewas
                    switch (Conf.MatchType.valueOf(strMap.get(Conf.StrVars.matchType))) {
                        case all:
                            if (mp.PTMs.size() == mp.EWASs.get(C).PTMs.size()) {    //Check that the number of modifications is the same
                                boolean found = true;
                                for (Modification PTM : mp.PTMs) {               //Check that each modification is contained
                                    for (Modification CandPTM : mp.EWASs.get(C).PTMs) {
                                        if (PTM.site == null ^ CandPTM.site == null) {
                                            found = false;
                                            break;
                                        }
                                        if (!(PTM.site == null) && !(CandPTM.site == null)) {
                                            if (abs(CandPTM.site - PTM.site) > intMap.get(Conf.IntVars.siteRange)) {             //Verify that the site is in the distance range
                                                found = false;
                                                break;
                                            }
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
                                        if (PTM.site.equals(null) ^ CandPTM.site.equals(null)) {
                                            continue;
                                        }
                                        if (!PTM.site.equals(null) && !CandPTM.site.equals(null)) {
                                            if (abs(CandPTM.site - PTM.site) > intMap.get(Conf.IntVars.siteRange)) {             //Verify that the site is in the distance range
                                                continue;
                                            }
                                        }
                                        mp.EWASs.get(C).matched = true;
                                        break;
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
