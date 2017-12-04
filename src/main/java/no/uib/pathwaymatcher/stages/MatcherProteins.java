package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Proteoform;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.Set;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class MatcherProteins extends Matcher {
    @Override
    public TreeMultimap<Proteoform, String> match(Set<Proteoform> entities) {

        TreeMultimap<Proteoform, String> mapping = TreeMultimap.create();

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
            } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
                sendError(COULD_NOT_CONNECT_TO_NEO4j);
            } catch (org.neo4j.driver.v1.exceptions.ServiceUnavailableException e){
                sendError(COULD_NOT_CONNECT_TO_NEO4j);
            }
        }

        return mapping;
    }

    @Override
    public Boolean matches(Proteoform iP, Proteoform rP) {
        return iP.getUniProtAcc().equals(rP.getUniProtAcc());
    }
}
