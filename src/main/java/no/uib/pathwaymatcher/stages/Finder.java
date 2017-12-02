package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeBasedTable;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.util.ArrayList;
import java.util.List;

import static no.uib.pathwaymatcher.Conf.BoolVars;
import static no.uib.pathwaymatcher.Conf.boolMap;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Finder {

    /**
     * Using the selected list of ewas, filter the resulting list of pathways/reactions that are hit by the input list.
     */

    public static TreeBasedTable<Proteoform, Pathway, Reaction> search(SetMultimap<Proteoform, String> mapping) {
        int percentage = 0;
//        print(percentage + "% ");
//        for (int I = 0; I < MPs.size(); I++) {
//            Proteoform mp = MPs.get(I);
//            //println("Pathways/Reactions for " + mp.baseProtein.id);
//            for (EWAS e : mp.EWASs) {
//                //println("EWAS " + e.stId);
//
//                ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
//                String query = "";
//                StatementResult queryResult;
//
//                if (boolMap.get(BoolVars.showTopLevelPathways)) {
//                    query = ReactomeQueries.getPathwaysByEwasWithTLP;
//                    queryResult = ConnectionNeo4j.session.run(query, Values.parameters("stId", e.stId));
//                    while (queryResult.hasNext()) {
//                        Record r = queryResult.next();
//                        e.reactionsList.add(new ReactionResultEntry(
//                                r.get("Reaction").asString(),
//                                r.get("ReactionDisplayName").asString(),
//                                r.get("Pathway").asString(),
//                                r.get("PathwayDisplayName").asString(),
//                                r.get("TopLevelPathwayStId").asString(),
//                                r.get("TopLevelPathwayDisplayName").asString()
//                        ));
//                    }
//                } else {
//                    query += ReactomeQueries.getPathwaysByEwas;
//                    queryResult = ConnectionNeo4j.session.run(query, Values.parameters("stId", e.stId));
//                    while (queryResult.hasNext()) {
//                        Record r = queryResult.next();
//                        e.reactionsList.add(new ReactionResultEntry(r.get("Reaction").asString(), r.get("ReactionDisplayName").asString(), r.get("Pathway").asString(), r.get("PathwayDisplayName").asString()));
//                    }
//                }
//
//                ConnectionNeo4j.session.close();
//            }
//            int newPercentage = I * 100 / MPs.size();
//            if (newPercentage > percentage + Conf.intMap.get(Conf.IntVars.percentageStep)) {
//                percentage = newPercentage;
//                print(percentage + "% ");
//            }
//        }
//        if (percentage == 100) {
//            println("");
//        } else {
//            println("100%");
//        }

        //        logger.log(Level.FINE, "Getting pathways and reactions...\n0% ");
//        int cont = 0;
//        int percent = 0;
//        int total = proteinList.size();
//        while (proteinList.size() > 0) {
//
//            Map.Entry<String, TreeSet<String>> proteinEntry = proteinList.pollFirstEntry();
//            String uniProtId = proteinEntry.getKey();
//            TreeSet<String> rsIdsMapped = proteinEntry.getValue();
//            if (Finder.containsUniProt(uniProtId)) {
//                List<String> rows = Finder.search(uniProtId);
//                for (String rsIdMapped : rsIdsMapped) {
//                    for (String row : rows) {
//                        outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
//                    }
//                }
//            }
//
//            int newPercent = cont * 100 / total;
//            if (percent < newPercent) {
//                logger.log(Level.FINER, newPercent + "% ");
//                if (newPercent % 10 == 0) {
//                    logger.log(Level.FINER, "");
//                }
//                percent = newPercent;
//            }
//            cont++;
//        }
//        logger.log(Level.FINER, "100% ");
        return null;
    }

    public static List<String> search(String uniProtId) {

        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
        List<String> result = new ArrayList<>();
        String query = ReactomeQueries.getPathwaysByUniProtId;
        if (boolMap.get(BoolVars.showTopLevelPathways)) {
            query = ReactomeQueries.getPathwaysByUniProtIdWithTLP;
        }
        StatementResult queryResult = ConnectionNeo4j.session.run(query, Values.parameters("id", uniProtId));

        while (queryResult.hasNext()) {
            Record r = queryResult.next();
            //result.add(r.get("pathway").asString().substring(6) + "," + r.get("reaction").asString().substring(6) + "," + uniProtId);
            if (boolMap.get(BoolVars.showTopLevelPathways)) {
                result.add(r.get("TopLevelPathwayStId").asString() + "," + r.get("TopLevelPathwayName").asString() + "," + r.get("pathway").asString() + "," + r.get("reaction").asString() + "," + uniProtId);
            } else {
                result.add(r.get("pathway").asString() + "," + r.get("reaction").asString() + "," + uniProtId);
            }
        }

        ConnectionNeo4j.session.close();
        return result;
    }

    public static Boolean containsUniProt(String uniProtId) {

        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
        Boolean result = false;
        String query = ReactomeQueries.containsUniProtId;
        StatementResult queryResult = ConnectionNeo4j.session.run(query, Values.parameters("id", uniProtId));

        if (queryResult.hasNext()) {
            result = true;
        }
        ConnectionNeo4j.session.close();

        return result;
    }
}
