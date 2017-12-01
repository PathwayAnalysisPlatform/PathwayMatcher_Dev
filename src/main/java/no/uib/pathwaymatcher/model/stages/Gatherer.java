package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.IntVars;
import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.*;
import no.uib.pathwaymatcher.util.InputPatterns;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.*;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Rsid;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Vcf_Record;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Gatherer {

    public static void gatherCandidates() {

        switch (Conf.InputTypeEnum.valueOf(strMap.get(StrVars.inputType.toString()))) {
            case uniprotList:
            case rsidList:
            case peptideList:
            case ensemblList:
            case geneList:
//                getCandidateEWAS();
                break;
            case peptideListAndModSites:
            case uniprotListAndModSites:
//                getCandidateEWASWithPTMs();
                break;
            case rsid:
//                gatherPathwaysFromGeneticVariants(true);
                break;
            case vcf:
//                gatherPathwaysFromVCF();
                break;
        }
    }

//    private static void getCandidateEWAS() {
//
//        //Use the list of uniprot ids in memory to create a set with all the possible candidate EWAS for every Protein
//        int percentage = 0;
//        int I = 0;
//        print(percentage + "% ");
//
//        for (String id : identifiersSet) {
//
//            Proteoform mp = new Proteoform();
//            mp.baseProtein = new Protein();
//            mp.baseProtein.id = id;       //Set the uniprot id
//
//            //Query reactome for the candidate EWAS
//            queryForCandidateEWAS(mp);
//
//            I++;
//            int newPercentage = I * 100 / identifiersSet.size();
//            if (newPercentage > percentage + Conf.intMap.get(IntVars.percentageStep)) {
//                percentage = newPercentage;
//                print(percentage + "% ");
//            }
//        }
//        if (percentage == 100) {
//            println("");
//        } else {
//            println("100%");
//        }
//    }
//

//

//
//    //-i rs41280031 -v ./resources/vep/ -t snpRsid
//    //The latest version of VEP tables are: https://github.com/SelectionPredisposed/post-association/tree/master/resources/ensembl
//    public static void gatherPathways(String rsId) {
//
//        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
//        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows
//
//        //Validate snpRsid format
//        if (!matches_Rsid(rsId)) {
//            PathwayMatcher.println("The input rsid provided is not valid: " + rsId);
//            System.exit(1);
//        }
//
//        Preprocessor.validateVepTables();
//
//        try {
//            //Validate output file
//            FileWriter output = new FileWriter(strMap.get(StrVars.output));
//
//            //Search in all vep tables for each chromosome
//            Boolean rsIdFound = false;
//            for (int chr = 1; chr <= 22; chr++) {
//                PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
//                try {
//                    BufferedReader br = getBufferedReader(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
//                    String[] fields = br.readLine().split(" ");
//                    for (String line; (line = br.readLine()) != null; ) {
//                        Pair<String, String> snp = getRsIdAndSwissProtFromVep(line);
//                        if (snp.getLeft().startsWith("id")) {
//                            continue;
//                        }
//                        if (snp.getLeft().equals(rsId)) {
//                            rsIdFound = true;
//                            if (!snp.getRight().equals("NA")) {
//                                String[] ids = snp.getRight().split(",");
//                                for (String id : ids) {
//                                    if (!proteinList.containsKey(id)) {
//                                        proteinList.put(id, new TreeSet<>());
//                                    }
//                                    proteinList.get(id).add(rsId);
//                                }
//                            }
//                        } else if (rsIdFound) {
//                            break;
//                        }
//                    }
//                } catch (FileNotFoundException ex) {
//                    PathwayMatcher.println("The vep table for chromosome " + chr + " was not found.");
//                } catch (IOException ex) {
//                    PathwayMatcher.println("There was a problem reading the vep table for chromosome " + chr + ".");
//                }
//
//                if (rsIdFound) {
//                    break;
//                }
//            }
//
//            // Search for the pathways of all the unique proteins
//            for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
//                String uniProtId = proteinEntry.getKey();
//                TreeSet<String> rsIdsMapped = proteinEntry.getValue();
//                List<String> rows = Finder.search(uniProtId);
//                for (String rsIdMapped : rsIdsMapped) {
//                    for (String row : rows) {
//                        outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
//                    }
//                }
//            }
//
//            // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
//            for (String row : outputList) {
//                output.write(row + "\n");
//            }
//
//            output.close();
//
//        } catch (IOException ex) {
//            PathwayMatcher.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
//        }
//    }
//
//    /**
//     * Creates a file with the pathways and reactions for all the rsIds
//     * specified in the input configuration variable. This method is of low
//     * memory consumption and fast performance, but requires that the rsIds are
//     * ordered by chromosome and location, there are no repeated and all of them
//     * must be defined in the vepTables.
//     *
//     */
//    public static void gatherPathways() {
//
//        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
//        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows
//
//        // Validate that input file exists
//        if (!(new File(strMap.get(StrVars.input)).exists())) {
//            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
//            System.exit(1);
//        }
//
//        // For each rsId in the input file
//        int chr = 1;
//        Boolean vepTablesFinished = false;
//        File inputFile = new File(strMap.get(StrVars.input));
//        File vepTable = new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));   //Start from vep table of chromosome 1
//        try {
//            Scanner inputScanner = new Scanner(inputFile);
//            Scanner vepScanner = new Scanner(vepTable);
//            String rsId = "";
//            String vepRow = "";
//
//            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
//            if (inputScanner.hasNext() && inputScanner.hasNext()) {
//                rsId = inputScanner.nextLine();
//                vepRow = vepScanner.nextLine();
//                Pair<String, String> snp = getRsIdAndSwissProtFromVep(vepRow);
//                if (snp.getLeft().startsWith("id")) {
//                    vepRow = vepScanner.nextLine();
//                    snp = getRsIdAndSwissProtFromVep(vepRow);
//                }
//                while (true) {
//                    while (!rsId.equals(snp.getLeft())) {                          // While the rsIds are different, search in all tables in order
//                        while (!vepScanner.hasNext()) {                         //If the vepTable is finished, try to go to the next chromosome table
//                            vepScanner.close();
//                            chr++;
//                            if (chr > 22) {
//                                vepTablesFinished = true;
//                                break;
//                            }
//                            vepTable = new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
//                            vepScanner = new Scanner(vepTable);
//                            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
//                        }
//                        if (vepTablesFinished) {
//                            break;
//                        }
//                        vepRow = vepScanner.nextLine();
//                        snp = getRsIdAndSwissProtFromVep(vepRow);
//                        if (snp.getLeft().startsWith("id")) {
//                            vepRow = vepScanner.nextLine();
//                            snp = getRsIdAndSwissProtFromVep(vepRow);
//                        }
//                    }
//                    if (vepTablesFinished) {
//                        break;
//                    }
//                    while (rsId.equals(snp.getLeft())) {                            //When they are the same on both lists, process all rows
//                        if (!snp.getRight().equals("NA")) {
//                            String[] ids = snp.getRight().split(",");
//                            for (String id : ids) {
//                                if (!proteinList.containsKey(id)) {
//                                    proteinList.put(id, new TreeSet<>());
//                                }
//                                proteinList.get(id).add(rsId);
//                            }
//                        }
//                        while (!vepScanner.hasNext()) {                         //If the vepTable is finished, try to go to the next chromosome table
//                            vepScanner.close();
//                            chr++;
//                            if (chr > 22) {
//                                vepTablesFinished = true;
//                                break;
//                            }
//                            vepTable = new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
//                            vepScanner = new Scanner(vepTable);
//                            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
//                        }
//                        if (vepTablesFinished) {
//                            break;
//                        }
//                        vepRow = vepScanner.nextLine();
//                        snp = getRsIdAndSwissProtFromVep(vepRow);
//                        if (snp.getLeft().startsWith("id")) {
//                            vepRow = vepScanner.nextLine();
//                            snp = getRsIdAndSwissProtFromVep(vepRow);
//                        }
//                    }
//                    if (vepTablesFinished) {
//                        break;
//                    }
//                    if (inputScanner.hasNext()) {
//                        rsId = inputScanner.nextLine();
//                    } else {
//                        break;
//                    }
//                }
//
//                // Search for the pathways of all the unique proteins
//                println("Number of proteins mapped: " + proteinList.size());
//                print("Getting pathways and reactions...\n0% ");
//                int cont = 0;
//                double percent = 0;
//                for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
//                    String uniProtId = proteinEntry.getKey();
//                    TreeSet<String> rsIdsMapped = proteinEntry.getValue();
//                    List<String> rows = Finder.search(uniProtId);
//                    for (String rsIdMapped : rsIdsMapped) {
//                        for (String row : rows) {
//                            outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
//                        }
//                    }
//                    double newPercent = cont * proteinList.size() / 100.0;
//                    if (percent / 10 > newPercent / 10) {
//                        print(newPercent + "% ");
//                    }
//                    cont++;
//
//                }
//
//                // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
//                FileWriter output;
//                try {
//                    output = new FileWriter(strMap.get(StrVars.output));
//                    for (String row : outputList) {
//                        output.write(row + "\n");
//                    }
//                    output.close();
//                } catch (IOException ex) {
//                    PathwayMatcher.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
//                    System.exit(1);
//                }
//
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
//            System.exit(1);
//        }
//    }
//
//    /**
//     * Creates a file with the pathways and reactions for all the rsIds
//     * specified in the input configuration variable. This method is of low
//     * memory consumption and fast performance, but requires that the rsIds are
//     * ordered by chromosome and location, there are no repeated and all of them
//     * must be defined in the vepTables. -i snpList005.csv -v ./resources/vep/
//     * -t rsidList The latest version of VEP tables are:
//     * https://github.com/SelectionPredisposed/post-association/tree/master/resources/ensembl
//     *
//     * @param missing
//     */
//    public static void gatherPathwaysFromGeneticVariants() {
//
//        //If the current id is required, then it is sent to the output
//
//    }
//
//    /**
//     * Get all input in memory Then traverse all VEP tables. Check if it exists
//     * in the input.
//     */
//    public static void gatherPathwaysFromVCF() {
//
//        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
//        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows
//        HashSet<String> variantSet = new HashSet<String>();
//
//        // Validate that input file exists
//        if (!(new File(strMap.get(StrVars.input)).exists())) {
//            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
//            System.exit(1);
//        }
//
//        // Validate vepTablesPath
//        Preprocessor.validateVepTables();
//
//        // Create a set with all the requested variants
//        try {
//            BufferedReader br = getBufferedReader(strMap.get(StrVars.input));
//
//            for (String line; (line = br.readLine()) != null; ) {
//                if (matches_Vcf_Record(line)) {
//                    java.util.regex.Matcher matcher = InputPatterns.PATTERN_VCFRECORDFIRST4COLS.matcher(line);
//                    if (matcher.find()) {
//                        variantSet.add(matcher.group(1).trim().replace(".", "NA"));
//                    }
//                }
//            }
//        } catch (FileNotFoundException ex) {
//            PathwayMatcher.println("The Input file specified was not found: " + strMap.get(StrVars.input));
//            System.exit(1);
//        } catch (IOException ex) {
//            PathwayMatcher.println("There was a problem reading the Input file specified.");
//            System.exit(1);
//        }
//
//        // Traverse all the vepTables
//        for (int chr = 1; chr <= 22; chr++) {
//            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
//            try {
//                BufferedReader br = getBufferedReader(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
//                String[] fields = br.readLine().split(" ");
//                for (String line; (line = br.readLine()) != null; ) {
//                    Pair<String, String> recordAndProt = getRecordAndSwissProt(line);
//                    if (!recordAndProt.getRight().equals("NA")) {       //If there is any protein mapped to these variant
//                        if (variantSet.contains(recordAndProt.getLeft())) {
//                            String[] ids = recordAndProt.getRight().split(",");
//                            for (String id : ids) {
//                                if (!proteinList.containsKey(id)) {
//                                    proteinList.put(id, new TreeSet<>());
//                                }
//                                proteinList.get(id).add(recordAndProt.getLeft());
//                            }
//                        }
//                    }
//                }
//            } catch (FileNotFoundException ex) {
//                PathwayMatcher.println("There was a problem opening the vepTable for chromosome " + chr);
//                //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
//                System.exit(1);
//            } catch (IOException ex) {
//                PathwayMatcher.println("There was a problem reading the vepTable for chromosome " + chr);
//                //Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
//                System.exit(1);
//            }
//        }
//
//        // Search for the pathways of all the unique proteins
//        println("Number of proteins mapped: " + proteinList.size());
//        try {
//            FileWriter proteinsEncodedFile = new FileWriter("./proteinsEncoded.csv");
//            for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
//                proteinsEncodedFile.write(proteinEntry.getKey() + "\n");
//            }
//            proteinsEncodedFile.close();
//        } catch (IOException ex) {
//            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
//            System.exit(1);
//        }
//
//        print("Getting pathways and reactions...\n0% ");
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
//                print(newPercent + "% ");
//                if (newPercent % 10 == 0) {
//                    println("");
//                }
//                percent = newPercent;
//            }
//            cont++;
//        }
//        println("100% ");
//
//        // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
//        print("Writing result to file...\n0% ");
//        percent = 0;
//        cont = 0;
//        total = outputList.size();
//        FileWriter output;
//        try {
//            output = new FileWriter(strMap.get(StrVars.output));
//            if (boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
//                output.write("TopLevelPathwayId,TopLevelPathwayName,");
//            }
//            output.write("pathway,reaction,protein,rsid\n");
//            while (outputList.size() > 0) {
//                output.write(outputList.pollFirst() + "\n");
//                int newPercent = cont * 100 / total;
//                if (percent < newPercent) {
//                    print(newPercent + "% ");
//                    if (newPercent % 10 == 0) {
//                        println("");
//                    }
//                    percent = newPercent;
//                }
//                cont++;
//            }
//            output.close();
//        } catch (IOException ex) {
//            System.out.println("There was a problem writing to the output file " + strMap.get(StrVars.output));
//            System.exit(1);
//        }
//    }


}
