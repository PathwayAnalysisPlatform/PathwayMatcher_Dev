package no.uib.pap.pathwaymatcher.Matching;

import java.util.Map;

import no.uib.pap.model.Proteoform;

public class ProteoformMatcherStrict extends ProteoformMatcher {

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

        if (rP.getPtms().entries().size() != iP.getPtms().entries().size()) {
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

        // All the input PTMs should be in the reference
        for (Map.Entry<String, Long> iPtm : iP.getPtms().entries()) {
            if (!rP.getPtms().containsEntry(iPtm.getKey(), iPtm.getValue())) {
                boolean anyMatches = false;
                for (Map.Entry<String, Long> rPtm : iP.getPtms().entries()) {
                    if (iPtm.getKey().equals(rPtm.getKey())) {
                        if (matches(iPtm.getValue(), rPtm.getValue())) {
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
