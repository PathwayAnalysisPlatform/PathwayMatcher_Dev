package no.uib.pap.pathwaymatcher.Matching;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.PathwayMatcher;
import no.uib.pap.pathwaymatcher.Matching.Matcher;
import no.uib.pap.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pap.pathwaymatcher.db.ReactomeQueries;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.Set;
import java.util.logging.Level;

import static no.uib.pap.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;

public class MatcherProteins extends Matcher {
    @Override
    public TreeMultimap<Proteoform, String> match(Set<Proteoform> entities) {

        TreeMultimap<Proteoform, String> mapping = TreeMultimap.create();

        int cont = 0;
        int percentage = 0;

        for (Proteoform proteoform : entities) {
            try {
                PathwayMatcher.logger.log(Level.FINE, proteoform.getUniProtAcc());
                Session session = ConnectionNeo4j.driver.session();

                String query = ReactomeQueries.getEwasByUniprotId;
                StatementResult queryResult = session.run(query, Values.parameters("id", proteoform.getUniProtAcc()));

                if (queryResult.hasNext()) {

                    Record record = queryResult.next();

                    for(Object ewasObj : record.get("ewas").asList()){
                        mapping.put(proteoform, (String)ewasObj);
                    }
                }
                session.close();

                cont++;
                int newPercentage = cont * 100 / entities.size();
                if (newPercentage - percentage >= Conf.intMap.get(Conf.IntVars.percentageStep)) {
                    percentage = newPercentage;
                    logger.log(Level.FINE, percentage + "% ");
                }
            } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
                sendError(COULD_NOT_CONNECT_TO_NEO4j);
            } catch (org.neo4j.driver.v1.exceptions.ServiceUnavailableException e){
                sendError(COULD_NOT_CONNECT_TO_NEO4j);
            }
        }

        if (percentage == 100) {
            logger.log(Level.FINE, "");
        } else {
            logger.log(Level.FINE, "100%");
        }

        return mapping;
    }

    @Override
    public Boolean matches(Proteoform iP, Proteoform rP) {
        return iP.getUniProtAcc().equals(rP.getUniProtAcc());
    }
}
