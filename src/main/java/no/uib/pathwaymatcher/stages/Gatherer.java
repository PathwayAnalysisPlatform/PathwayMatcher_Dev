package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.model.ModifiedProtein;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.Modification;
import no.uib.pathwaymatcher.model.Protein;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Pair;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.IntVars;
import static no.uib.pathwaymatcher.Conf.boolMap;
import no.uib.pathwaymatcher.PathwayMatcher;
import static no.uib.pathwaymatcher.PathwayMatcher.print;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.PathwayMatcher.uniprotSet;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Gatherer {

    public static void gatherCandidateEwas() {

        switch (Conf.InputTypeEnum.valueOf(strMap.get(StrVars.inputType.toString()))) {
            case uniprotList:
            case rsidList:
            case peptideList:
            case ensemblList:
            case geneList:
                getCandidateEWAS();
                break;
            case maxQuantMatrix:
            case peptideListAndSites:
            case peptideListAndModSites:
            case uniprotListAndSites:
            case uniprotListAndModSites:
                getCandidateEWASWithPTMs();
                break;
        }
    }

    private static void getCandidateEWAS() {

        //Use the list of uniprot ids in memory to create a set with all the possible candidate EWAS for every Protein
        int percentage = 0;
        int I = 0;
        print(percentage + "% ");

        for (String id : uniprotSet) {

            ModifiedProtein mp = new ModifiedProtein();
            mp.baseProtein = new Protein();
            mp.baseProtein.id = id;       //Set the uniprot id

            //Query reactome for the candidate EWAS
            getCandidateEWAS(mp);

            I++;
            int newPercentage = I * 100 / uniprotSet.size();
            if (newPercentage > percentage + Conf.intMap.get(IntVars.percentageStep)) {
                percentage = newPercentage;
                print(percentage + "% ");
            }
        }
        if (percentage == 100) {
            println("");
        } else {
            println("100%");
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
                        e.PTMs.add(new Modification("00000", Integer.valueOf(s.toString())));
                    }

                    for (int S = 0; S < record.get("mods").asList().size(); S++) {
                        e.PTMs.get(S).psimod = record.get("mods").asList().get(S).toString();
                    }
                    mp.EWASs.add(e);
                }
            }
            MPs.add(mp);
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
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

                for (int ptm = 0; ptm < modifications.length; ptm++) { //Set the requested PTMs
                    String[] modParts = modifications[ptm].split(":");
                    mp.PTMs.add(new Modification(modParts[0], Integer.valueOf(modParts[1])));
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
                        e.PTMs.add(new Modification("00000", Integer.valueOf(s.toString())));
                    }

                    for (int S = 0; S < record.get("mods").asList().size(); S++) {
                        e.PTMs.get(S).psimod = record.get("mods").asList().get(S).toString();
                    }

                    mp.EWASs.add(e);
                }
            }
            MPs.add(mp);
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
    }

    //-i rs41280031 -v ./resources/vep/ -t snpRsid
    //The latest version of VEP tables are: https://github.com/SelectionPredisposed/post-association/tree/master/resources/ensembl
    public static void gatherPathways(String rsId) {

        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows

        //Validate snpRsid format
        if (!rsId.matches(Conf.InputPatterns.snpRsid)) {
            PathwayMatcher.println("The input rsid provided is not valid: " + rsId);
            System.exit(1);
        }

        Preprocessor.validateVepTables();

        try {
            //Validate output file
            FileWriter output = new FileWriter(strMap.get(StrVars.output));

            //Search in all vep tables for each chromosome
            Boolean rsIdFound = false;
            for (int chr = 1; chr <= 22; chr++) {
                PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
                try {
                    BufferedReader br = getBufferedReader(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                    String[] fields = br.readLine().split(" ");
                    for (String line; (line = br.readLine()) != null;) {
                        Pair<String, String> snp = getRsIdAndSwissProt(line);
                        if (snp.getLeft().startsWith("id")) {
                            continue;
                        }
                        if (snp.getLeft().equals(rsId)) {
                            rsIdFound = true;
                            if (!snp.getRight().equals("NA")) {
                                String[] ids = snp.getRight().split(",");
                                for (String id : ids) {
                                    if (!proteinList.containsKey(id)) {
                                        proteinList.put(id, new TreeSet<>());
                                    }
                                    proteinList.get(id).add(rsId);
                                }
                            }
                        } else if (rsIdFound) {
                            break;
                        }
                    }
                } catch (FileNotFoundException ex) {
                    PathwayMatcher.println("The vep table for chromosome " + chr + " was not found.");
                } catch (IOException ex) {
                    PathwayMatcher.println("There was a problem reading the vep table for chromosome " + chr + ".");
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

    /**
     * Creates a file with the pathways and reactions for all the rsIds
     * specified in the input configuration variable. This method is of low
     * memory consumption and fast performance, but requires that the rsIds are
     * ordered by chromosome and location, there are no repeated and all of them
     * must be defined in the vepTables.
     *
     * @param rsId
     */
    public static void gatherPathways() {

        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows

        // Validate that input file exists
        if (!(new File(strMap.get(StrVars.input)).exists())) {
            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
            System.exit(1);
        }

        // Validate vepTablesPath
        Preprocessor.validateVepTables();

        // For each rsId in the input file
        int chr = 1;
        Boolean vepTablesFinished = false;
        File inputFile = new File(strMap.get(StrVars.input));
        File vepTable = new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));   //Start from vep table of chromosome 1
        try {
            Scanner inputScanner = new Scanner(inputFile);
            Scanner vepScanner = new Scanner(vepTable);
            String rsId = "";
            String vepRow = "";

            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
            if (inputScanner.hasNext() && inputScanner.hasNext()) {
                rsId = inputScanner.nextLine();
                vepRow = vepScanner.nextLine();
                Pair<String, String> snp = getRsIdAndSwissProt(vepRow);
                if (snp.getLeft().startsWith("id")) {
                    vepRow = vepScanner.nextLine();
                    snp = getRsIdAndSwissProt(vepRow);
                }
                while (true) {
                    while (!rsId.equals(snp.getLeft())) {                          // While the rsIds are different, search in all tables in order
                        while (!vepScanner.hasNext()) {                         //If the vepTable is finished, try to go to the next chromosome table
                            vepScanner.close();
                            chr++;
                            if (chr > 22) {
                                vepTablesFinished = true;
                                break;
                            }
                            vepTable = new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                            vepScanner = new Scanner(vepTable);
                            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
                        }
                        if (vepTablesFinished) {
                            break;
                        }
                        vepRow = vepScanner.nextLine();
                        snp = getRsIdAndSwissProt(vepRow);
                        if (snp.getLeft().startsWith("id")) {
                            vepRow = vepScanner.nextLine();
                            snp = getRsIdAndSwissProt(vepRow);
                        }
                    }
                    if (vepTablesFinished) {
                        break;
                    }
                    while (rsId.equals(snp.getLeft())) {                            //When they are the same on both lists, process all rows  
                        if (!snp.getRight().equals("NA")) {
                            String[] ids = snp.getRight().split(",");
                            for (String id : ids) {
                                if (!proteinList.containsKey(id)) {
                                    proteinList.put(id, new TreeSet<>());
                                }
                                proteinList.get(id).add(rsId);
                            }
                        }
                        while (!vepScanner.hasNext()) {                         //If the vepTable is finished, try to go to the next chromosome table
                            vepScanner.close();
                            chr++;
                            if (chr > 22) {
                                vepTablesFinished = true;
                                break;
                            }
                            vepTable = new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                            vepScanner = new Scanner(vepTable);
                            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
                        }
                        if (vepTablesFinished) {
                            break;
                        }
                        vepRow = vepScanner.nextLine();
                        snp = getRsIdAndSwissProt(vepRow);
                        if (snp.getLeft().startsWith("id")) {
                            vepRow = vepScanner.nextLine();
                            snp = getRsIdAndSwissProt(vepRow);
                        }
                    }
                    if (vepTablesFinished) {
                        break;
                    }
                    if (inputScanner.hasNext()) {
                        rsId = inputScanner.nextLine();
                    } else {
                        break;
                    }
                }

                // Search for the pathways of all the unique proteins
                println("Number of proteins mapped: " + proteinList.size());
                print("Getting pathways and reactions...\n0% ");
                int cont = 0;
                double percent = 0;
                for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
                    String uniProtId = proteinEntry.getKey();
                    TreeSet<String> rsIdsMapped = proteinEntry.getValue();
                    List<String> rows = Filter.getFilteredPathways(uniProtId);
                    for (String rsIdMapped : rsIdsMapped) {
                        for (String row : rows) {
                            outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
                        }
                    }
                    double newPercent = cont * proteinList.size() / 100.0;
                    if (percent / 10 > newPercent / 10) {
                        print(newPercent + "% ");
                    }
                    cont++;

                }

                // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
                FileWriter output;
                try {
                    output = new FileWriter(strMap.get(StrVars.output));
                    for (String row : outputList) {
                        output.write(row + "\n");
                    }
                    output.close();
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

    /**
     * Creates a file with the pathways and reactions for all the rsIds
     * specified in the input configuration variable. This method is of low
     * memory consumption and fast performance, but requires that the rsIds are
     * ordered by chromosome and location, there are no repeated and all of them
     * must be defined in the vepTables. -i snpList005.csv -v ./resources/vep/
     * -t rsidList The latest version of VEP tables are:
     * https://github.com/SelectionPredisposed/post-association/tree/master/resources/ensembl
     *
     * @param rsId
     */
    public static void gatherPathwaysFromGeneticVariants(Boolean missing) {

        //If the current id is required, then it is sent to the output
        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows
        HashSet<String> snpSet = new HashSet<String>();

        // Validate that input file exists
        if (!(new File(strMap.get(StrVars.input)).exists())) {
            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
            System.exit(1);
        }

        // Validate vepTablesPath
        Preprocessor.validateVepTables();

        // Create a set with all the requested rsIds
        try {
            BufferedReader br = getBufferedReader(strMap.get(StrVars.input));

            for (String snp; (snp = br.readLine()) != null;) {
                if (snp.matches(Conf.InputPatterns.snpRsid)) {
                    snpSet.add(snp);
                }
            }
        } catch (FileNotFoundException ex) {
            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
            System.exit(1);
        } catch (IOException ex) {
            PathwayMatcher.println("There was a problem reading the Input file specified.");
            System.exit(1);
        }

        // Traverse all the vepTables
        for (int chr = 1; chr <= 22; chr++) {
            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
            try {
                BufferedReader br = getBufferedReader(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                String[] fields = br.readLine().split(" ");
                for (String line; (line = br.readLine()) != null;) {
                    Pair<String, String> snp = getRsIdAndSwissProt(line);
                    if (!snp.getRight().equals("NA")) {
                        if (snpSet.contains(snp.getLeft())) {
                            String[] ids = snp.getRight().split(",");
                            for (String id : ids) {
                                if (!proteinList.containsKey(id)) {
                                    proteinList.put(id, new TreeSet<>());
                                }
                                proteinList.get(id).add(snp.getLeft());
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                PathwayMatcher.println("There was a problem opening the vepTable for chromosome " + chr);
                //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            } catch (IOException ex) {
                PathwayMatcher.println("There was a problem reading the vepTable for chromosome " + chr);
                //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }

        // Search for the pathways of all the unique proteins
        println("Number of proteins mapped: " + proteinList.size());
        try {
            FileWriter proteinsEncodedFile = new FileWriter("./proteinsEncoded.csv");
            for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
                proteinsEncodedFile.write(proteinEntry.getKey() + "\n");
            }
            proteinsEncodedFile.close();
        } catch (IOException ex) {
            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        print("Getting pathways and reactions...\n0% ");
        int cont = 0;
        int percent = 0;
        int total = proteinList.size();
        while (proteinList.size() > 0) {

            Map.Entry<String, TreeSet<String>> proteinEntry = proteinList.pollFirstEntry();
            String uniProtId = proteinEntry.getKey();
            TreeSet<String> rsIdsMapped = proteinEntry.getValue();
            if (Filter.containsUniProt(uniProtId)) {
                List<String> rows = Filter.getFilteredPathways(uniProtId);
                for (String rsIdMapped : rsIdsMapped) {
                    for (String row : rows) {
                        outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
                    }
                }
            }

            int newPercent = cont * 100 / total;
            if (percent < newPercent) {
                print(newPercent + "% ");
                if (newPercent % 10 == 0) {
                    println("");
                }
                percent = newPercent;
            }
            cont++;
        }
        println("100% ");

        // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
        print("Writing result to file...\n0% ");
        percent = 0;
        cont = 0;
        total = outputList.size();
        FileWriter output;
        try {
            output = new FileWriter(strMap.get(StrVars.output));
            if (boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
                output.write("TopLevelPathwayId,TopLevelPathwayName,");
            }
            output.write("pathway,reaction,protein,rsid\n");
            while (outputList.size() > 0) {
                output.write(outputList.pollFirst() + "\n");
                int newPercent = cont * 100 / total;
                if (percent < newPercent) {
                    print(newPercent + "% ");
                    if (newPercent % 10 == 0) {
                        println("");
                    }
                    percent = newPercent;
                }
                cont++;
            }
            output.close();
        } catch (IOException ex) {
            System.out.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
            System.exit(1);
        }
    }

    /**
     * Get all input in memory Then traverse all VEP tables. Check if it exists
     * in the input.
     */
    public static void gatherPathwaysFromVCF() {

        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows
        HashSet<String> variantSet = new HashSet<String>();

        // Validate that input file exists
        if (!(new File(strMap.get(StrVars.input)).exists())) {
            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
            System.exit(1);
        }

        // Validate vepTablesPath
        Preprocessor.validateVepTables();

        // Create a set with all the requested variants
        try {
            BufferedReader br = getBufferedReader(strMap.get(StrVars.input));

            for (String line; (line = br.readLine()) != null;) {
                if (line.matches(Conf.InputPatterns.vcfRecord)) {
                    Pattern pattern = Pattern.compile(Conf.InputPatterns.vcfRecordFirst4Cols);
                    java.util.regex.Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        variantSet.add(matcher.group(1).trim().replace(".", "NA"));
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
            System.exit(1);
        } catch (IOException ex) {
            PathwayMatcher.println("There was a problem reading the Input file specified.");
            System.exit(1);
        }

        // Traverse all the vepTables
        for (int chr = 1; chr <= 22; chr++) {
            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
            try {
                BufferedReader br = getBufferedReader(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                String[] fields = br.readLine().split(" ");
                for (String line; (line = br.readLine()) != null;) {
                    Pair<String, String> recordAndProt = getRecordAndSwissProt(line);
                    if (!recordAndProt.getRight().equals("NA")) {       //If there is any protein mapped to these variant
                        if (variantSet.contains(recordAndProt.getLeft())) {
                            String[] ids = recordAndProt.getRight().split(",");
                            for (String id : ids) {
                                if (!proteinList.containsKey(id)) {
                                    proteinList.put(id, new TreeSet<>());
                                }
                                proteinList.get(id).add(recordAndProt.getLeft());
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                PathwayMatcher.println("There was a problem opening the vepTable for chromosome " + chr);
                //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            } catch (IOException ex) {
                PathwayMatcher.println("There was a problem reading the vepTable for chromosome " + chr);
                //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }

        // Search for the pathways of all the unique proteins
        println("Number of proteins mapped: " + proteinList.size());
        try {
            FileWriter proteinsEncodedFile = new FileWriter("./proteinsEncoded.csv");
            for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
                proteinsEncodedFile.write(proteinEntry.getKey() + "\n");
            }
            proteinsEncodedFile.close();
        } catch (IOException ex) {
            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        print("Getting pathways and reactions...\n0% ");
        int cont = 0;
        int percent = 0;
        int total = proteinList.size();
        while (proteinList.size() > 0) {

            Map.Entry<String, TreeSet<String>> proteinEntry = proteinList.pollFirstEntry();
            String uniProtId = proteinEntry.getKey();
            TreeSet<String> rsIdsMapped = proteinEntry.getValue();
            if (Filter.containsUniProt(uniProtId)) {
                List<String> rows = Filter.getFilteredPathways(uniProtId);
                for (String rsIdMapped : rsIdsMapped) {
                    for (String row : rows) {
                        outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
                    }
                }
            }

            int newPercent = cont * 100 / total;
            if (percent < newPercent) {
                print(newPercent + "% ");
                if (newPercent % 10 == 0) {
                    println("");
                }
                percent = newPercent;
            }
            cont++;
        }
        println("100% ");

        // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
        print("Writing result to file...\n0% ");
        percent = 0;
        cont = 0;
        total = outputList.size();
        FileWriter output;
        try {
            output = new FileWriter(strMap.get(StrVars.output));
            if (boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
                output.write("TopLevelPathwayId,TopLevelPathwayName,");
            }
            output.write("pathway,reaction,protein,rsid\n");
            while (outputList.size() > 0) {
                output.write(outputList.pollFirst() + "\n");
                int newPercent = cont * 100 / total;
                if (percent < newPercent) {
                    print(newPercent + "% ");
                    if (newPercent % 10 == 0) {
                        println("");
                    }
                    percent = newPercent;
                }
                cont++;
            }
            output.close();
        } catch (IOException ex) {
            System.out.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
            System.exit(1);
        }
    }

    private static BufferedReader getBufferedReader(String path) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        if (path.endsWith(".gz")) {
            InputStream fileStream = new FileInputStream(path);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream);
            br = new BufferedReader(decoder);
        } else {
            br = new BufferedReader(new FileReader(strMap.get(StrVars.input)));
        }

        return br;
    }

    private static Pair<String, String> getRsIdAndSwissProt(String line) {
        String[] fields = line.split(" ");
        return new Pair<>(fields[Conf.intMap.get(Conf.IntVars.rsidIndex)], fields[Conf.intMap.get(Conf.IntVars.swissprotIndex)]);
    }
    
    private static Pair<String, String> getRecordAndSwissProt(String line) {
        String[] fields = line.split(" ");
        String record = fields[0];
        for(int I = 1; I <= 3; I++){
            record += " " + fields[I];
        }
        return new Pair<>(record, fields[Conf.intMap.get(Conf.IntVars.swissprotIndex)]);
    }
}
