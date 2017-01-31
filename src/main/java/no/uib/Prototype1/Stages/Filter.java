package no.uib.Prototype1.Stages;

import no.uib.Prototype1.Model.EWAS;
import no.uib.Prototype1.Model.ModifiedProtein;
import static no.uib.Prototype1.Prototype1.MPs;
import static no.uib.Prototype1.Prototype1.println;
import no.uib.Prototype1.Model.Reaction;
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
        for (ModifiedProtein mp : MPs) {
            println("Pathways/Reactions for " + mp.baseProtein.id);
            for (EWAS e : mp.EWASs) {
                println("EWAS " + e.stId);

                Session session = ConnectionNeo4j.driver.session();
                String query = "";
                StatementResult queryResult;

                query += "MATCH (p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n"
                        + "(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{stId:{stId}})\n"
                        + "RETURN p.stId AS Pathway, p.displayName AS PathwayDisplayName, rle.stId AS Reaction, rle.displayName as ReactionDisplayName";

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
