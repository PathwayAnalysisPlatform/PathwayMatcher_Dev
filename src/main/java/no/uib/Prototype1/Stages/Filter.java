package no.UiB.Prototype1.Stages;

import no.UiB.Prototype1.Prototype1;
import static no.UiB.Prototype1.Prototype1.hitPathways;
import static no.UiB.Prototype1.Prototype1.hitReactions;
import static no.UiB.Prototype1.Prototype1.matchedEWAS;
import static no.UiB.Prototype1.Prototype1.println;
import no.uib.Prototype1.db.ConnectionNeo4j;
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
        //For every matched EWAS, get the hit pathways
        for (String e : matchedEWAS) {
            println("Pathways/Reactions for " + e);
            queryForPathways(e);
        }
    }

    private static void queryForPathways(String e) {
        Session session = ConnectionNeo4j.driver.session();
        String query = "";
        StatementResult queryResult;

        query += "MATCH (p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n"
                + "(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{stId:{stId}})\n"
                + "RETURN p.stId AS Pathway, rle.stId AS Reaction";

        queryResult = session.run(query, Values.parameters("stId", e));

        while (queryResult.hasNext()) {
            Record r = queryResult.next();
            hitPathways.add(r.get("Pathway").asString());
            hitReactions.add(r.get("Reaction").asString());
        }

        session.close();
    }
}
