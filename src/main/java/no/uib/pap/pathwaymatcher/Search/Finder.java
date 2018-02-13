package no.uib.pap.pathwaymatcher.Search;

import static no.uib.pap.pathwaymatcher.Conf.boolMap;

import java.util.Map;
import java.util.logging.Level;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.Conf.BoolVars;
import no.uib.pap.pathwaymatcher.PathwayMatcher;
import no.uib.pap.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pap.pathwaymatcher.db.ReactomeQueries;
import no.uib.pap.pathwaymatcher.tools.PathwayStaticFactory;
import no.uib.pap.pathwaymatcher.tools.ReactionStaticFactory;

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

            Pathway tlp = null;
            Pathway pathway = null;
            Reaction reaction = null;
            Proteoform proteoform = entry.getKey();

            if (boolMap.get(BoolVars.showTopLevelPathways)) {
                query = ReactomeQueries.getPathwaysByEwasWithTLP;
                queryResult = ConnectionNeo4j.session.run(query, Values.parameters("stId", entry.getValue()));
                while (queryResult.hasNext()) {
                    Record r = queryResult.next();

                    // Use the factory to avoid duplications in the TopLevelPathway pointers inside the pathway instances
                    tlp = PathwayStaticFactory.getInstance(r.get("TopLevelPathwayStId").asString(), r.get("TopLevelPathwayDisplayName").asString());

                    pathway = PathwayStaticFactory.getInstance(r.get("Pathway").asString(), r.get("PathwayDisplayName").asString());
                    pathway.getTopLevelPathwaySet().add(tlp);

                    reaction = ReactionStaticFactory.getInstance(r.get("Reaction").asString(), r.get("ReactionDisplayName").asString());
                    reaction.getPathwaySet().add(pathway);
                    pathway.getReactionsFound().add(reaction);
                    pathway.getEntitiesFound().add(proteoform);

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
                            r.get("PathwayDisplayName").asString());

                    reaction = ReactionStaticFactory.getInstance(
                            r.get("Reaction").asString(),
                            r.get("ReactionDisplayName").asString());
                    reaction.getPathwaySet().add(pathway);
                    pathway.getReactionsFound().add(reaction);
                    pathway.getEntitiesFound().add(proteoform);

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
