package no.UiB.Prototype1.Stages;

import java.util.ArrayList;
import java.util.List;
import no.UiB.Prototype1.Prototype1;
import static no.UiB.Prototype1.Prototype1.hitPathways;
import static no.UiB.Prototype1.Prototype1.matchedEWAS;
import no.uib.Prototype1.db.ConnectionNeo4j;
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
        //For every matched EWAS, get the hit pathways
        for (String ewas : matchedEWAS) {
            queryForPathways(ewas);
        }
    }

    private static void queryForPathways(String id) {
        Session session = ConnectionNeo4j.driver.session();
        String query = "";
        StatementResult queryResult;

        query += "MATCH (p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n"
                + "(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{stId:'R-HSA-141433'})\n"
                + "RETURN p.stId AS Pathway, rle.stId AS Reaction";

        queryResult = session.run(query, Values.parameters("id", id));

        while (queryResult.hasNext()) {
            hitPathways.add(queryResult.next().get("Pathway").asString());
            Prototype1.hitReactions.add(queryResult.next().get("Reaction").asString());
        }

        session.close();
    }
}
