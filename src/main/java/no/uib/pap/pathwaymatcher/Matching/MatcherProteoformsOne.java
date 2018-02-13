package no.uib.pap.pathwaymatcher.Matching;

import java.util.Map;

import no.uib.pap.model.Proteoform;

public class MatcherProteoformsOne extends MatcherProteoforms {
    @Override
    public Boolean matches(Proteoform iP, Proteoform rP) {

        // Check the uniprot accession, including the isoform matches
        if(iP.getUniProtAcc() == null){
            throw new IllegalArgumentException();
        }

        if(rP.getUniProtAcc() == null){
            throw new IllegalArgumentException();
        }

        if(!iP.getUniProtAcc().equals(rP.getUniProtAcc())){
            return false;
        }

        if(!matches(iP.getStartCoordinate(), rP.getStartCoordinate())){
            return false;
        }

        if(!matches(iP.getEndCoordinate(), rP.getEndCoordinate())){
            return false;
        }

        if(rP.getPtms().entries().size() == 0){
            return true;
        }

        // At least one of the reference ptms should be in the input
        for(Map.Entry<String, Long> rPtm : rP.getPtms().entries()){
            if(iP.getPtms().containsEntry(rPtm.getKey(), rPtm.getValue())){
                return true;
            }
            for (Map.Entry<String, Long> iPtm : iP.getPtms().entries()) {
                if (rPtm.getKey().equals(iPtm.getKey())) {
                    if (matches(rPtm.getValue(), iPtm.getValue())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
