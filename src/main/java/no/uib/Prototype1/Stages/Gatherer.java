package no.UiB.Prototype1.Stages;

import no.UiB.Prototype1.Model.ModifiedProtein;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.UiB.Prototype1.Configuration;
import no.UiB.Prototype1.Model.EWAS;
import no.UiB.Prototype1.Model.ModifiedResidue;
import no.UiB.Prototype1.Model.Protein;
import static no.UiB.Prototype1.Prototype1.MPs;
import no.uib.Prototype1.db.ConnectionNeo4j;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Gatherer {

    public static void gatherCandidates() {
        try {
            //Read the list and create a set with all the possible candidate EWAS for every Modified Protein
            BufferedReader br = new BufferedReader(new FileReader(Configuration.standarizedFile));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String[] modifications = (parts.length > 1) ? parts[1].split(";") : new String[0];
                ModifiedProtein mp = new ModifiedProtein();
                mp.baseProtein = new Protein();
                mp.baseProtein.id = parts[0];       //Set the uniprot id
                //Set the requested PTMs
                for (int ptm = 0; ptm < modifications.length; ptm++) {
                    String[] modParts = modifications[ptm].split(":");
                    mp.PTMs.add(new ModifiedResidue(modParts[0], Integer.valueOf(modParts[1])));
                }

                //Query reactome for the candidate EWAS
                getCandidateEWAS(mp);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("The standarized file was not found on: " + Configuration.standarizedFile);
            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Error while trying to read the file: " + Configuration.standarizedFile);
            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getCandidateEWAS(ModifiedProtein mp) {
        Session session = ConnectionNeo4j.driver.session();
        String query = "";
        StatementResult queryResult;

        if (!mp.baseProtein.id.contains("-")) {
            query = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}}),\n";
        } else {
            query = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:{id}}),\n";
        }

        query += "(ewas)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)\n"
                + "WHERE mr.coordinate IS NOT null\n"
                + "RETURN ewas.stId as stId, ewas.displayName as name, collect(mr.coordinate) as sites, collect(t.identifier) as mods";
        queryResult = session.run(query, Values.parameters("id", mp.baseProtein.id));
        
        //TODO Support for PTMs with unknown site
        if (!queryResult.hasNext()) {                                             // Case 4: No protein found
            mp.status = 4;
        } else {
            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                EWAS e = new EWAS();
                
                e.stId = record.get("stId").asString();
                e.displayName = record.get("displayName").asString();
                
                for (Object s : record.get("sites").asList()) {
                    e.PTMs.add(new ModifiedResidue("00000", Integer.valueOf(s.toString())));
                }
                
                for(int S = 0; S < record.get("mods").asList().size(); S++){
                    e.PTMs.get(S).psimod = record.get("mods").asList().get(S).toString();
                }

                mp.EWASs.add(e);
            }
        }
        MPs.add(mp);
    }
}
