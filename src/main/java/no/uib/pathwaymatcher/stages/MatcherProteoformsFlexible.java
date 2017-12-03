package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.tools.Parser;
import org.apache.commons.lang.NotImplementedException;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class MatcherProteoformsFlexible extends MatcherProteoforms {

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

        // All the reference PTMs should be in the input
        for(Map.Entry<String, Long> ptm : rP.getPtms().entries()){
            if(!iP.getPtms().containsEntry(ptm.getKey(), ptm.getValue()))
                return false;
        }

        return true;
    }

}
