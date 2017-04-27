package no.uib.pathwaymatcher.stages;

import no.uib.db.ReactomeQueries;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.model.Reaction;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Filter {
    //Using the selected list of ewas, filter the resulting list of pathways/reactions that are hit by the input list.

    public static void getFilteredPathways() {
        for (ModifiedProtein mp : MPs) {
            println("Pathways/Reactions for " + mp.baseProtein.id);
            for (EWAS e : mp.EWASs) {
                println("EWAS " + e.stId);

                Session session = ConnectionNeo4j.driver.session();
                String query = "";
                StatementResult queryResult;

                query += ReactomeQueries.getPathwaysByEwas;

                queryResult = session.run(query, Values.parameters("stId", e.stId));

                while (queryResult.hasNext()) {
                    Record r = queryResult.next();
                    e.reactionsList.add(new Reaction(r.get("Reaction").asString(), r.get("ReactionDisplayName").asString(), r.get("Pathway").asString(), r.get("PathwayDisplayName").asString()));
                }

                session.close();
            }
        }
    }
}
