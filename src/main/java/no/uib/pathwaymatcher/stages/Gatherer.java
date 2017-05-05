package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.model.ModifiedProtein;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.StrVars;
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
import no.uib.model.Pair;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.InputType;
import no.uib.pathwaymatcher.PathwayMatcher;
import static no.uib.pathwaymatcher.PathwayMatcher.uniprotSet;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Gatherer {

    public static void gatherCandidateEwas() {

        switch (strMap.get(StrVars.inputType.toString())) {
            case InputType.uniprotList:
            case InputType.rsidList:
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
            System.out.println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
    }

    private static void getCandidateEWASWithPTMs() {
        try {
            //Read the list and create a set with all the possible candidate EWAS for every Protein
            BufferedReader br = new BufferedReader(new FileReader(strMap.get(StrVars.standardFilePath.toString())));
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
            System.out.println("The standarized file was not found on: " + strMap.get(StrVars.standardFilePath.toString()));
            System.exit(2);
            //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Error while trying to read the file: " + strMap.get(StrVars.standardFilePath.toString()));
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
            System.out.println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
    }

    public static void gatherPathways(String rsId) {

        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows

        //Validate rsid format
        if (!rsId.matches(Conf.InputPatterns.rsid)) {
            PathwayMatcher.println("The input rsid provided is not valid: " + rsId);
            System.exit(1);
        }

        // Validate vepTablesPath
        File vepDirectory = new File(strMap.get(StrVars.vepTablesPath));
        if (!vepDirectory.exists()) {
            PathwayMatcher.println("The vepTablesPath provided does not exist.");
            System.exit(1);
        } else {
            for (int C = 1; C <= 22; C++) {
                if (!(new File(strMap.get(StrVars.vepTablesPath) + "/chr" + C + "_processed.txt").exists())) {
                    PathwayMatcher.println("The vep table for chromosome " + C + " was not found. Expected: " + strMap.get(StrVars.vepTablesPath) + "/chr" + C + "_processed.txt");
                    System.exit(1);
                }
            }
        }

        try {
            //Validate output file
            FileWriter output = new FileWriter(strMap.get(StrVars.output));

            //Search in all vep tables for each chromosome
            Boolean rsIdFound = false;
            for (int C = 1; C <= 22; C++) {
                try (BufferedReader br = new BufferedReader(new FileReader(strMap.get(StrVars.vepTablesPath) + "/chr" + C + "_processed.txt"))) {
                    for (String line; (line = br.readLine()) != null;) {
                        Pair<String, String> snp = getRsIdAndSwissProt(line);
                        if (snp.getL().startsWith("id")) {
                            continue;
                        }
                        if (snp.getL().equals(rsId)) {
                            rsIdFound = true;
                            if (!snp.getR().equals("NA")) {
                                String[] ids = snp.getR().split(",");
                                for (String id : ids) {
                                    if (!proteinList.containsKey(id)) {
                                        proteinList.put(id, new TreeSet<>());
                                    }
                                    proteinList.get(id).add(rsId);
                                }
                            }    
                        }
                        else if(rsIdFound){
                            break;
                        }
                    }
                } catch (FileNotFoundException ex) {
                    PathwayMatcher.println("The vep table for chromosome " + C + " was not found.");
                } catch (IOException ex) {
                    PathwayMatcher.println("There was a problem reading the vep table for chromosome " + C + ".");
                }

                if (rsIdFound) {
                    break;
                }
            }

            // Search for the pathways of all the unique proteins
            for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
                String uniProtId = proteinEntry.getKey();
                TreeSet<String> rsIdsMapped = proteinEntry.getValue();
                List<String> rows = Filter.getFilteredPathways(uniProtId);
                for (String rsIdMapped : rsIdsMapped) {
                    for (String row : rows) {
                        outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
                    }
                }
            }

            // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
            for (String row : outputList) {
                output.write(row + "\n");
            }
            
            output.close();

        } catch (IOException ex) {
            PathwayMatcher.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
        }
    }

    public static void gatherPathways() {

        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows

        // Validate that input file exists
        if (!(new File(strMap.get(StrVars.input)).exists())) {
            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
            System.exit(1);
        }

        // Validate vepTablesPath
        if (!(new File(strMap.get(StrVars.vepTablesPath)).exists())) {
            PathwayMatcher.println("The vepTablesPath provided is not valid.");
            System.exit(1);
        } else {
            for (int C = 1; C <= 22; C++) {
                if (!(new File(StrVars.vepTablesPath + "/chr" + C + "_processed").exists())) {
                    PathwayMatcher.println("The vep table for chromosome " + C + " was not found.");
                    System.exit(1);
                }
            }
        }

        // For each rsId in the input file
        int chr = 1;
        Boolean vepTablesFinished = false;
        File inputFile = new File(strMap.get(StrVars.input));
        File vepTable = new File(strMap.get(StrVars.vepTablesPath + "/chr" + chr + "_processed"));   //Start from vep table of chromosome 1
        try {
            Scanner inputScanner = new Scanner(inputFile);
            Scanner vepScanner = new Scanner(vepTable);
            String rsId = "";
            String vepRow = "";

            if (inputScanner.hasNext() && inputScanner.hasNext()) {
                rsId = inputScanner.next();
                vepRow = vepScanner.next();
                Pair<String, String> snp = getRsIdAndSwissProt(vepRow);
                while (true) {
                    while (!rsId.equals(snp.getL())) {                          // While the rsIds are different, search in all tables in order
                        while (!vepScanner.hasNext()) {                         //If the vepTable is finished, try to go to the next chromosome table
                            vepScanner.close();
                            chr++;
                            if (chr > 22) {
                                vepTablesFinished = true;
                                break;
                            }
                            vepTable = new File(strMap.get(StrVars.vepTablesPath + "/chr" + chr + "_processed"));
                            vepScanner = new Scanner(vepTable);
                        }
                        vepRow = vepScanner.next();
                        snp = getRsIdAndSwissProt(vepRow);
                        if (snp.getL().startsWith("id")) {
                            vepRow = vepScanner.next();
                            snp = getRsIdAndSwissProt(vepRow);
                        }
                    }
                    if (vepTablesFinished) {
                        break;
                    }
                    while (rsId.equals(snp.getL())) {                            //When they are the same on both lists, process all rows  
                        if (!snp.getR().equals("NA")) {
                            String[] ids = snp.getR().split(",");
                            for (String id : ids) {
                                if (proteinList.containsKey(id)) {
                                    proteinList.get(id).add(rsId);
                                } else {
                                    proteinList.put(id, new TreeSet<>());
                                }
                            }
                        }
                        while (!vepScanner.hasNext()) {                         //If the vepTable is finished, try to go to the next chromosome table
                            vepScanner.close();
                            chr++;
                            if (chr > 22) {
                                vepTablesFinished = true;
                                break;
                            }
                            vepTable = new File(strMap.get(StrVars.vepTablesPath + "/chr" + chr + "_processed"));
                            vepScanner = new Scanner(vepTable);
                        }
                        if (vepTablesFinished) {
                            break;
                        }
                        vepRow = vepScanner.next();
                        snp = getRsIdAndSwissProt(vepRow);
                        if (snp.getL().startsWith("id")) {
                            vepRow = vepScanner.next();
                            snp = getRsIdAndSwissProt(vepRow);
                        }
                    }
                    if (inputScanner.hasNext()) {
                        rsId = inputScanner.next();
                    } else {
                        break;
                    }
                }

                // Search for the pathways of all the unique proteins
                for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
                    String uniProtId = proteinEntry.getKey();
                    TreeSet<String> rsIdsMapped = proteinEntry.getValue();
                    List<String> rows = Filter.getFilteredPathways(uniProtId);
                    for (String rsIdMapped : rsIdsMapped) {
                        for (String row : rows) {
                            outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
                        }
                    }
                }

                // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
                FileWriter output;
                try {
                    output = new FileWriter(strMap.get(StrVars.output));
                    for (String row : outputList) {
                        output.write(row + "\n");
                    }
                } catch (IOException ex) {
                    PathwayMatcher.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
                    System.exit(1);
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private static Pair<String, String> getRsIdAndSwissProt(String line) {
        String[] fields = line.split(" ");
        return new Pair<>(fields[2], fields[15]);
    }

}
