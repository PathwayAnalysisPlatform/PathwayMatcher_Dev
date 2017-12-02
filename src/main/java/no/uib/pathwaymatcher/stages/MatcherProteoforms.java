package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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

public abstract class MatcherProteoforms extends Matcher{

    @Override
    public SetMultimap<Proteoform, String> match(Set<Proteoform> entities) {
        SetMultimap<Proteoform, String> mapping = HashMultimap.create();

//        for (Proteoform proteoform : entities) {
//            try {
//                PathwayMatcher.logger.log(Level.FINE, proteoform.getUniProtAcc());
//                Session session = ConnectionNeo4j.driver.session();
//
//                String query = ReactomeQueries.getEwasByUniprotId;
//                StatementResult queryResult = session.run(query, Values.parameters("id", proteoform.getUniProtAcc()));
//
//                if (queryResult.hasNext()) {
//
//                    Record record = queryResult.next();
//                    mapping.put(proteoform, record.get("ewas").asString());
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
//                else{
//                    PathwayMatcher.logger.log
//                }
//                MPs.add(mp);
//                session.close();
//            } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
//                sendError(COULD_NOT_CONNECT_TO_NEO4j);
//            }
//
//        }

        return mapping;
    }
}
