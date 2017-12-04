package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.tools.PathwayStaticFactory;
import no.uib.pathwaymatcher.tools.ReactionStaticFactory;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.Map;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.BoolVars;
import static no.uib.pathwaymatcher.Conf.boolMap;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Finder {

    /**
     * Using the selected list of ewas, filter the resulting list of pathways/reactions that are hit by the input list.
     */

    public static TreeMultimap<Proteoform, Reaction> search(SetMultimap<Proteoform, String> mapping) {

        PathwayMatcher.logger.log(Level.INFO, "Getting pathways and reactions...\n0% ");

        TreeMultimap<Proteoform, Reaction> result = TreeMultimap.create();
        int percentage = 0;
        int processed = 0;
        PathwayMatcher.logger.log(Level.FINE, percentage + "% ");
        for (Map.Entry<Proteoform, String> entry : mapping.entries()) {

            ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
            String query = "";
            StatementResult queryResult;

            Pathway pathway = null;
            Reaction reaction = null;
            Proteoform proteoform = entry.getKey();

            if (boolMap.get(BoolVars.showTopLevelPathways)) {
                query = ReactomeQueries.getPathwaysByEwasWithTLP;
                queryResult = ConnectionNeo4j.session.run(query, Values.parameters("stId", entry.getValue()));
                while (queryResult.hasNext()) {
                    Record r = queryResult.next();

                    // Use the factory to avoid duplications in the TopLevelPathway pointers inside the pathway instances
                    pathway = PathwayStaticFactory.getInstance(
                            r.get("Pathway").asString(),
                            r.get("PathwayDisplayName").asString(),
                            r.get("TopLevelPathwayStId").asString(),
                            r.get("TopLevelPathwayDisplayName").asString());

                    reaction = ReactionStaticFactory.getInstance(
                            r.get("Reaction").asString(),
                            r.get("ReactionDisplayName").asString());

                    reaction.getPathwaySet().add(pathway);
                    assert reaction != null;
                    assert !reaction.getPathwaySet().isEmpty();
                    result.put(proteoform, reaction);
                }
            } else {
                query += ReactomeQueries.getPathwaysByEwas;
                queryResult = ConnectionNeo4j.session.run(query, Values.parameters("stId", entry.getValue()));
                while (queryResult.hasNext()) {
                    Record r = queryResult.next();

                    // The duplicates of the pathway and reaction instances are avoided with the TreeBasedTable
                    pathway = PathwayStaticFactory.getInstance(
                            r.get("Pathway").asString(),
                            r.get("PathwayDisplayName").asString(),
                            r.get("Pathway").asString(),
                            r.get("PathwayDisplayName").asString());

                    reaction = ReactionStaticFactory.getInstance(
                            r.get("Reaction").asString(),
                            r.get("ReactionDisplayName").asString());

                    reaction.getPathwaySet().add(pathway);
                    assert reaction != null;
                    assert !reaction.getPathwaySet().isEmpty();
                    result.put(proteoform, reaction);
                }
            }

            ConnectionNeo4j.session.close();
            processed++;
            int newPercentage = processed * 100 / mapping.entries().size();
            if (newPercentage > percentage + Conf.intMap.get(Conf.IntVars.percentageStep)) {
                percentage = newPercentage;
                PathwayMatcher.logger.log(Level.FINE, percentage + "% ");
            }
        }
        if (percentage != 100) {
            PathwayMatcher.logger.log(Level.FINE, "100%");
        }

        return result;
    }

}
