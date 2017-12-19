package no.uib.pathwaymatcher.Matching;

import no.uib.pathwaymatcher.Matching.MatcherProteoforms;
import no.uib.pathwaymatcher.model.Proteoform;

import java.util.Map;

import static no.uib.pathwaymatcher.model.Error.sendError;

public class MatcherProteoformsFlexible extends MatcherProteoforms {

    @Override
    public Boolean matches(Proteoform iP, Proteoform rP) {

        // Check the uniprot accession, including the isoform matches
        if (iP.getUniProtAcc() == null) {
            throw new IllegalArgumentException();
        }

        if (rP.getUniProtAcc() == null) {
            throw new IllegalArgumentException();
        }

        if (!iP.getUniProtAcc().equals(rP.getUniProtAcc())) {
            return false;
        }

        if (!matches(iP.getStartCoordinate(), rP.getStartCoordinate())) {
            return false;
        }

        if (!matches(iP.getEndCoordinate(), rP.getEndCoordinate())) {
            return false;
        }

        // All the reference PTMs should be in the input
        for (Map.Entry<String, Long> rPtm : rP.getPtms().entries()) {
            if (!iP.getPtms().containsEntry(rPtm.getKey(), rPtm.getValue())) {
                boolean anyMatches = false;
                for (Map.Entry<String, Long> iPtm : iP.getPtms().entries()) {
                    if (rPtm.getKey().equals(iPtm.getKey())) {
                        if (matches(rPtm.getValue(), iPtm.getValue())) {
                            anyMatches = true;
                            break;
                        }
                    }
                }
                if (!anyMatches) {
                    return false;
                }
            }
        }

        return true;
    }

}
