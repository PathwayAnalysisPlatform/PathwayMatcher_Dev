package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.lang.NotImplementedException;

import java.util.Map;
import java.util.Set;

import static java.lang.Math.abs;

public class MatcherProteoformsStrict extends MatcherProteoforms {

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

        if(rP.getPtms().entries().size() != iP.getPtms().entries().size()){
            return false;
        }

        // All the reference PTMs should be in the input
        for(Map.Entry<String, Long> ptm : rP.getPtms().entries()){
            if(!iP.getPtms().containsEntry(ptm.getKey(), ptm.getValue()))
                return false;
        }

        // All the input PTMs should be in the reference
        for(Map.Entry<String, Long> ptm : iP.getPtms().entries()){
            if(!rP.getPtms().containsEntry(ptm.getKey(), ptm.getValue()))
                return false;
        }

        return true;
    }

}
