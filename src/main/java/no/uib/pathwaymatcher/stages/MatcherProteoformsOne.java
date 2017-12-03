package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.lang.NotImplementedException;

import java.util.Map;
import java.util.Set;

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

        // All the reference PTMs should be in the input
        for(Map.Entry<String, Long> ptm : iP.getPtms().entries()){
            if(rP.getPtms().containsEntry(ptm.getKey(), ptm.getValue()))
                return true;
        }

        return false;
    }
}
