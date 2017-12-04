package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.tools.Parser;
import org.neo4j.driver.v1.*;

import java.util.Set;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;

public abstract class MatcherProteoforms extends Matcher{
    @Override
    public TreeMultimap<Proteoform, String> match(Set<Proteoform> entities) {
        TreeMultimap<Proteoform, String> mapping = TreeMultimap.create();

        for (Proteoform iP : entities) {
            try {
                PathwayMatcher.logger.log(Level.FINE, iP.getUniProtAcc());
                Session session = ConnectionNeo4j.driver.session();

                String query = ReactomeQueries.getEwasAndPTMsByUniprotId;
                StatementResult queryResult = session.run(query, Values.parameters("id", iP.getUniProtAcc()));

                while (queryResult.hasNext()) {
                    Record record = queryResult.next();

                    Proteoform rP = new Proteoform(iP.getUniProtAcc());
                    rP.setStartCoordinate(record.get("startCoordinate").asLong());
                    rP.setEndCoordinate(record.get("endCoordinate").asLong());

                    if (record.get("ptms").asList().size() > 0) {
                        for (Object s : record.get("ptms").asList()) {

                            String[] parts = s.toString().split(":");

                            String mod = parts[0].replace("\"", "").replace("{", "").replace("}", "");
                            mod = (mod.equals("null") ? "00000" : mod);

                            String coordinate = parts[1].replace("\"", "").replace("{", "").replace("}", "");

                            rP.addPtm(mod, Parser.interpretCoordinateFromStringToLong(coordinate));
                        }
                    }

                    // Compare the input proteoform (iP) with the reference proteoform (rP)
                    if (matches(iP, rP)) {
                        mapping.put(rP, record.get("ewas").asString());
                    }
                }
                session.close();
            } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
                sendError(COULD_NOT_CONNECT_TO_NEO4j);
            }
        }
        return mapping;
    }

    public abstract Boolean matches(Proteoform iP, Proteoform rP);

    boolean matches(Long iC, Long rC){
        if(iC != null){ if(iC == -1L) iC = null; }
        if(rC != null){ if(rC == -1L) rC = null; }
        if(iC != null && rC != null){
            if(iC != rC){
                if(Math.abs(iC-rC) > Conf.intMap.get(Conf.IntVars.margin)){
                    return false;
                }
            }
        }
        return true;
    }
}
