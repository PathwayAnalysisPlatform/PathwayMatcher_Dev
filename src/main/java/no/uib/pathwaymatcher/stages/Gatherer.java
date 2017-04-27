package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.model.ModifiedProtein;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.strVars;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.ModifiedResidue;
import no.uib.pathwaymatcher.model.Protein;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import no.uib.db.ReactomeQueries;
import no.uib.pathwaymatcher.Conf.InputType;
import static no.uib.pathwaymatcher.PathwayMatcher.uniprotSet;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Gatherer {

    public static void gatherCandidateEwas() {

        switch (strMap.get(strVars.inputType.toString())) {
            case InputType.uniprotList:
            case InputType.snpList:
            case InputType.peptideList:
                getCandidateEWAS();
                break;
            case InputType.maxQuantMatrix:
            case InputType.peptideListAndSites:
            case InputType.peptideListAndModSites:
            case InputType.uniprotListAndSites:
            case InputType.uniprotListAndModSites:
                getCandidateEWASWithPTMs();
                break;
        }
    }

    private static void getCandidateEWAS() {

        //Use the list of uniprot ids in memory to create a set with all the possible candidate EWAS for every Protein
        for (String id : uniprotSet) {

            ModifiedProtein mp = new ModifiedProtein();
            mp.baseProtein = new Protein();
            mp.baseProtein.id = id;       //Set the uniprot id

            //Query reactome for the candidate EWAS
            getCandidateEWAS(mp);
        }

    }

    private static void getCandidateEWAS(ModifiedProtein mp) {
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = "";
            StatementResult queryResult;

            if (!mp.baseProtein.id.contains("-")) {
                query = ReactomeQueries.getEwasAndPTMsByUniprotId;
            } else {
                query = ReactomeQueries.getEwasAndPTMsByUniprotIsoform;
            }

            queryResult = session.run(query, Values.parameters("id", mp.baseProtein.id));

            if (!queryResult.hasNext()) {                                             // Case 4: No protein found
                mp.status = 4;
            } else {
                while (queryResult.hasNext()) {
                    Record record = queryResult.next();
                    EWAS e = new EWAS();
                    e.matched = true;
                    e.stId = record.get("ewas").asString();

                    for (Object s : record.get("sites").asList()) {
                        e.PTMs.add(new ModifiedResidue("00000", Integer.valueOf(s.toString())));
                    }

                    for (int S = 0; S < record.get("mods").asList().size(); S++) {
                        e.PTMs.get(S).psimod = record.get("mods").asList().get(S).toString();
                    }
                    mp.EWASs.add(e);
                }
            }
            MPs.add(mp);
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + strMap.get(strVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
    }

    private static void getCandidateEWASWithPTMs() {
        try {
            //Read the list and create a set with all the possible candidate EWAS for every Protein
            BufferedReader br = new BufferedReader(new FileReader(strMap.get(strVars.standardFilePath.toString())));
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
                getCandidateEWASWithPTMs(mp);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("The standarized file was not found on: " + strMap.get(strVars.standardFilePath.toString()));
            System.exit(2);
            //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Error while trying to read the file: " + strMap.get(strVars.standardFilePath.toString()));
            System.exit(2);
            //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getCandidateEWASWithPTMs(ModifiedProtein mp) {
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = "";
            StatementResult queryResult;

            if (!mp.baseProtein.id.contains("-")) {
                query = ReactomeQueries.getEwasAndPTMsByUniprotId;
            } else {
                query = ReactomeQueries.getEwasAndPTMsByUniprotIsoform;
            }

            queryResult = session.run(query, Values.parameters("id", mp.baseProtein.id));

            //TODO Support for PTMs with unknown site
            if (!queryResult.hasNext()) {                                             // Case 4: No protein found
                mp.status = 4;
            } else {
                while (queryResult.hasNext()) {
                    Record record = queryResult.next();
                    EWAS e = new EWAS();

                    e.stId = record.get("ewas").asString();
                    e.displayName = record.get("displayName").asString();

                    for (Object s : record.get("sites").asList()) {
                        e.PTMs.add(new ModifiedResidue("00000", Integer.valueOf(s.toString())));
                    }

                    for (int S = 0; S < record.get("mods").asList().size(); S++) {
                        e.PTMs.get(S).psimod = record.get("mods").asList().get(S).toString();
                    }

                    mp.EWASs.add(e);
                }
            }
            MPs.add(mp);
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + strMap.get(strVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
    }

}
