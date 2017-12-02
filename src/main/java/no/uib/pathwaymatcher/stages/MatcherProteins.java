package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.lang.NotImplementedException;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class MatcherProteins extends Matcher {
    @Override
    public SetMultimap<Proteoform, String> match(Set<Proteoform> entities) {

        SetMultimap<Proteoform, String> mapping = HashMultimap.create();

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
            }
        }

        return mapping;
    }
}
