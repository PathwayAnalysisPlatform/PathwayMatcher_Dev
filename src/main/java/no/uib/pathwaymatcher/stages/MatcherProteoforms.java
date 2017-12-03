package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.tools.Parser;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.Set;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;

public abstract class MatcherProteoforms extends Matcher{
    @Override
    public SetMultimap<Proteoform, String> match(Set<Proteoform> entities) {
        SetMultimap<Proteoform, String> mapping = HashMultimap.create();

        for (Proteoform iP : entities) {
            try {
                PathwayMatcher.logger.log(Level.FINE, iP.getUniProtAcc());
                Session session = ConnectionNeo4j.driver.session();

                String query = ReactomeQueries.getEwasAndPTMsByUniprotId;
                StatementResult queryResult = session.run(query, Values.parameters("id", iP.getUniProtAcc()));

                if (queryResult.hasNext()) {
                    Record record = queryResult.next();

                    Proteoform rP = new Proteoform(iP.getUniProtAcc());
                    rP.setStringStartCoordinate(record.get("startCoordinate").asString());
                    rP.setStringEndCoordinate(record.get("endCoordinate").asString());

                    if (record.get("ptms").asList().size() > 0) {
                        for (Object s : record.get("ptmList").asList()) {

                            String[] parts = s.toString().split(",");

                            String mod = parts[1].replace("\"", "").replace("{", "").replace("}", "");
                            mod = (mod.equals("null") ? "00000" : mod);

                            String coordinate = parts[0].replace("\"", "").replace("{", "").replace("}", "");
                            coordinate = coordinate.split("=")[1];

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
