package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;

public class MatcherProteins extends Matcher {
    @Override
    public SetMultimap<Proteoform, String> match(Set<Proteoform> entities) {
        throw new NotImplementedException();

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
    }

    //    private static void queryForCandidateEWAS(Proteoform mp) {
//        try {
////            System.out.println(mp.baseProtein.id);
//            Session session = ConnectionNeo4j.driver.session();
//
//            String query = "";
//            StatementResult queryResult;
//
//            if (!mp.baseProtein.id.contains("-")) {
//                query = ReactomeQueries.getEwasAndPTMsByUniprotId;
//            } else {
//                query = ReactomeQueries.getEwasAndPTMsByUniprotIsoform;
//            }
//
//            queryResult = session.run(query, Values.parameters("id", mp.baseProtein.id));
//
//            if (!queryResult.hasNext()) {                                             // Case 4: No protein found
//                mp.status = 4;
//            } else {
//                while (queryResult.hasNext()) {
//                    Record record = queryResult.next();
//                    EWAS e = new EWAS();
//                    e.matched = true;
//                    e.stId = record.get("ewas").asString();
//
//                    if (record.get("ptms").asList().size() > 0) {
//                        for (Object s : record.get("ptmList").asList()) {
//                            //System.out.println(s);
//
//                            String[] parts = s.toString().split(",");
//                            String mod = "";
//                            String site = "";
//
//                            mod = parts[1].replace("\"", "").replace("{", "").replace("}", "");
//                            mod = mod.split("=")[1];
//                            mod = (mod.equals("null") ? "00000" : mod);
//
//                            site = parts[0].replace("\"", "").replace("{", "").replace("}", "");
//                            site = site.split("=")[1];
//                            Integer pos = site.equals("null") ? null : Integer.valueOf(site);
//
//                            e.PTMs.add(new Modification(mod, pos));
//                        }
//                    }
//
//                    mp.EWASs.add(e);
//                }
//            }
//            MPs.add(mp);
//            session.close();
//        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
//            println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
//            System.exit(1);
//        }
//    }


}
