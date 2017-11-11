package no.uib.pathwaymatcher.stages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Pair;
import no.uib.pathwaymatcher.Conf;
import static no.uib.pathwaymatcher.Conf.*;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.PathwayMatcher;
import static no.uib.pathwaymatcher.PathwayMatcher.print;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.PathwayMatcher.uniprotSet;
import static no.uib.pathwaymatcher.util.InputPatterns.*;

import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Preprocessor {

    private static FileWriter output;

    /* This Class should transform any type of file to the standard format of representing the Modified Proteins. */
    public static Boolean standarizeFile() {

        Boolean parseResult = false;
        try {
            output = new FileWriter(Conf.strMap.get(Conf.StrVars.standardFilePath));
        } catch (IOException ex) {
            System.out.println("The output file standarized has a problem.");
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        try {
            switch (Conf.InputTypeEnum.valueOf(strMap.get(StrVars.inputType))) {
                case uniprotList:
                    parseResult = parseFormat_uniprotList();
                    break;
                case uniprotListAndSites:
                    parseResult = parseFormat_uniprotListAndSites();
                    break;
                case uniprotListAndModSites:
                    parseResult = parseFormat_uniprotListAndModSites();
                    break;
                case maxQuantMatrix:
                    parseResult = parseFormat_maxQuantMatrix();
                    break;
                case peptideList:
                    parseResult = parseFormat_peptideList();
                    break;
                case peptideListAndSites:
                    parseResult = parseFormat_peptideListAndSites();
                    break;
                case peptideListAndModSites:
                    parseResult = parseFormat_peptideListAndModSites();
                    break;
                case rsidList:
                    parseResult = parseFormat_snpList();
                    break;
                case rsid:
                    break;
                case ensemblList:
                    parseResult = parseFormat_ensemblList2();
                    break;
                case geneList:
                    parseResult = parseFormat_geneList();
                    break;
                case vcf:
                    parseResult = parseFormat_vcf();
                    break;
            }
        } catch (java.text.ParseException e) {

        } catch (IOException e) {
            System.out.println("There was a problem reading the input file.");
            System.exit(1);
        }
        try {
            output.close();
        } catch (IOException ex) {
            if (Conf.boolMap.get(Conf.BoolVars.verbose)) {
                System.out.println("\nThe output file standarized has a problem.");
            }
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (parseResult) {
            println("\nFile parsed correctly!");
        } else {
            System.out.println("\nThe format of the file is incorrect.");
        }
        return parseResult;
    }

    //Detect the type of input
    public static String detectInputType() throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String firstLine = reader.readLine();
            if (firstLine.trim().startsWith(MAXQUANT)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.maxQuantMatrix;
            } else if (matches_Peptite(firstLine)) {
                return InputType.peptideList;
            } else if (matches_Peptite_And_Sites(firstLine)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.peptideListAndSites;
            } else if (matches_Peptite_And_Mod_Sites(firstLine)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.peptideListAndModSites;
            } else if (matches_Protein_Uniprot(firstLine)) {
                return InputType.uniprotList;
            } else if (matches_Protein_Uniprot_And_Sites(firstLine)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.uniprotListAndSites;
            } else if (matches_Proteoform_Custom(firstLine)) {
                setValue(Conf.BoolVars.inputHasPTMs, Boolean.TRUE);
                return InputType.uniprotListAndModSites;
            } else if (matches_Rsid(firstLine)) {
                return InputType.rsidList;
            }
        } catch (FileNotFoundException ex) {
            System.out.println("The Input file specified was not found: " + Conf.strMap.get(Conf.StrVars.input));
            System.out.println("The starting location is: " + System.getProperty("user.dir"));
            //Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return InputType.unknown;
    }

    /**
     * ********** The parser methods transform the original input file to the
     * standard Modified proteins standard file. **************
     */
    //If all the file was converted properly they return true.
    //If the file does not follow the format they return false. 
    public static Boolean parseFormat_maxQuantMatrix() throws java.text.ParseException, IOException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;

        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();        //Read header line; the first row of the file

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Maxquant(line)) {
                        printMaxQuantLine(line);            //Process line
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }

                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        return parsedCorrectly;
    }

    private static void printMaxQuantLine(String line) throws IOException {
        String[] sections = line.split("\t");
        String[] ids = sections[0].split(";");
        String[] sites = sections[1].split(";");

        for (int P = 0; P < ids.length; P++) {
            output.write(ids[P] + ",00000:" + sites[P] + "\n");
        }
    }

    public static Boolean parseFormat_peptideList() throws java.text.ParseException, IOException {
        //Note: In this function the duplicate protein identifiers are removed by adding the whole input list to a set.

        println("\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            return false;
        }
        println("\nLoading peptide mapper complete.");

        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Peptite(line)) {
                        //Process line
                        for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(line)) {
                            uniprotSet.add(id);
                        }
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }

                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
//        //Print all uniprot ids to the standarized file
//        for (String id : uniprotSet) {
//            output.write(id + ",\n");
//        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_peptideListAndSites() throws java.text.ParseException {
        //Note: In this function, the duplicate protein identifiers are NOT removed, since every row might show a protein with a different modified version.
        println("\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            return false;
        }
        println("\nLoading peptide mapper complete.");

        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            switch (PeptidePTMGrouping.valueOf(strMap.get(StrVars.peptideGrouping))) {
                case byProtein:
                    HashMap<String, HashSet<Integer>> ProtSitesMap = new HashMap<>();

                    //Read all peptides with their sites and make a set of proteins with related PTM sites
                    while ((line = reader.readLine()) != null) {
                        row++;
                        try {

                            if (matches_Peptite_And_Sites(line)) {
                                //Process line
                                String[] parts = line.split(",");

                                for (Pair<String, Integer> peptideProteinMap : compomics.utilities.PeptideMapping.getPeptideMappingWithIndex(parts[0])) {
                                    ProtSitesMap.putIfAbsent(peptideProteinMap.getLeft(), new HashSet<>());
                                    if (parts.length > 1) {
                                        String[] sites = parts[1].split(";");
                                        for (int S = 0; S < sites.length; S++) {
                                            ProtSitesMap.get(peptideProteinMap.getLeft()).add(Integer.valueOf(sites[S]) + peptideProteinMap.getRight() + 1);
                                        }
                                    }
                                }
                            } else {
                                if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                                    System.out.println("Ignoring missformatted row: " + row);
                                } else {
                                    throw new ParseException("Row " + row + " with wrong format", 0);
                                }
                            }
                        } catch (ParseException e) {
                            System.out.println(e.getMessage());
                            parsedCorrectly = false;
                            System.exit(0);
                        }
                    }

                    // Send all the proteins with PTM sites to the standard file
                    for (Entry<String, HashSet<Integer>> protSites : ProtSitesMap.entrySet()) {
                        output.write(protSites.getKey());
                        if (protSites.getValue().size() > 0) {
                            output.write(",");
                        } else {
                            output.write("\n");
                        }
                        int cont = 0;
                        for (Integer site : protSites.getValue()) {
                            output.write("00000:" + site);       //Notice that the index of the peptide inside the protein is 0-based and the index of the PTM-site is 1-based
                            cont++;
                            if (cont >= protSites.getValue().size()) {
                                output.write("\n");
                            } else {
                                output.write(";");
                            }
                        }
                    }
                    break;
                case none:
                default:
                    while ((line = reader.readLine()) != null) {
                        row++;
                        try {
                            if (matches_Peptite_And_Sites(line)) {
                                //Process line
                                String[] parts = line.split(",");

                                for (Pair<String, Integer> peptideProteinMap : compomics.utilities.PeptideMapping.getPeptideMappingWithIndex(parts[0])) {
                                    output.write(peptideProteinMap.getLeft());
                                    if (parts.length > 1) {
                                        output.write(",");
                                        String[] sites = parts[1].split(";");
                                        for (int S = 0; S < sites.length; S++) {
                                            if (S > 0) {
                                                output.write(";");
                                            }
                                            output.write("00000:" + (Integer.valueOf(sites[S]) + peptideProteinMap.getRight() + 1));       //Notice that the index of the peptide inside the protein is 0-based and the index of the PTM-site is 1-based
                                        }
                                    }

                                    output.write("\n");
                                }
                            } else {
                                if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                                    System.out.println("Ignoring missformatted row: " + row);
                                } else {
                                    throw new ParseException("Row " + row + " with wrong format", 0);
                                }
                            }
                        } catch (ParseException e) {
                            System.out.println(e.getMessage());
                            parsedCorrectly = false;
                            System.exit(0);
                        }
                    }
                    break;
            }

            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_peptideListAndModSites() throws java.text.ParseException {
        //Note: In this function, the duplicate protein identifiers are NOT removed, since every row might show a protein with a different modified version.

        println("\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            return false;
        }
        println("\nLoading peptide mapper complete.");

        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Peptite_And_Mod_Sites(line)) {
                        //Process line
                        String[] parts = line.split(",");
                        for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(parts[0])) {
                            output.write(id + "," + parts[1] + "\n");
                        }
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_uniprotList() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line;

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Protein_Uniprot(line)) {
                        uniprotSet.add(line);
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();

            //        //Print all uniprot ids to the standarized file
            //        for (String id : uniprotSet) {
            //            output.write(id + ",\n");
            //        }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_uniprotListAndSites() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Protein_Uniprot_And_Sites(line)) {
                        //Process line
                        String[] parts = line.split(";");
                        if (parts.length > 1) {
                            String[] sites = parts[1].split(",");
                            output.write(parts[0] + ",");
                            for (int S = 0; S < sites.length; S++) {
                                if (S > 0) {
                                    output.write(",");
                                }
                                output.write("00000:" + sites[S]);
                            }
                            output.write("\n");
                        }
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_uniprotListAndModSites() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Proteoform_Custom(line)) {
                        //Process line
                        output.write(line + "\n");
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    private static Boolean parseFormat_snpList() {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {    //Every line is a SNP
                row++;
                try {
                    if (matches_Rsid(line)) {
                        //Get the Gene name of the current SNP
                        List<String> geneList = new ArrayList();

                        //Get the possible Proteins
                        List<String> proteinList = new ArrayList();

                        //Add proteins to a set (to filter duplicates)
                        uniprotSet.addAll(proteinList);

                    } else {
                        throw new ParseException("Row " + row + " with wrong format", 0);
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    if (boolMap.get(BoolVars.ignoreMisformatedRows) == false) {
                        System.exit(3);
                    }
                }
            }
            reader.close();

            //Optionally: Print all the possible proteins to a file
            //        //Print all uniprot ids to the standarized file
            //        for (String id : uniprotSet) {
            //            output.write(id + ",\n");
            //        }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_ensemblList() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        HashSet<String> ensemblSet = new HashSet<>();

        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Protein_Ensembl(line)) {
                        ensemblSet.add(line);
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();

            // Convert the ensembl ids to uniprot accessions
            println("Converting Ensembl ids to UniProt accessions");
            int cont = 0;
            int percentage = 0;
            for (String ensemblId : ensemblSet) {
                for (String uniprotAccession : getUniprotAccessionByEnsembl(ensemblId)) {
                    uniprotSet.add(uniprotAccession);
                }
                cont++;
                int newPercentage = cont * 100 / ensemblSet.size();
                if (newPercentage - percentage >= Conf.intMap.get(IntVars.percentageStep)) {
                    percentage = newPercentage;
                    print(percentage + "% ");
                }
            }
            if (percentage == 100) {
                println("");
            } else {
                println("100%");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    public static Boolean parseFormat_ensemblList2() throws java.text.ParseException {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        HashSet<String> ensemblSet = new HashSet<>();
        HashMap<String, HashSet<String>> ensemblMapping = new HashMap<>();

        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Protein_Ensembl(line)) {
                        ensemblSet.add(line);
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();

            // Get all the mapping from ensembl to uniprot from Reactome
            ensemblMapping = getAllUniprotAccessionToEnsemblMapping();

            // Convert the ensembl ids to uniprot accessions
            println("Converting Ensembl ids to UniProt accessions");
            int cont = 0;
            int percentage = 0;
            for (String ensemblId : ensemblSet) {
                if (ensemblMapping.containsKey(ensemblId)) {
                    for (String uniprotAccession : ensemblMapping.get(ensemblId)) {
                        uniprotSet.add(uniprotAccession);
                    }
                }
                cont++;
                int newPercentage = cont * 100 / ensemblSet.size();
                if (newPercentage - percentage >= Conf.intMap.get(IntVars.percentageStep)) {
                    percentage = newPercentage;
                    print(percentage + "% ");
                }
            }
            if (percentage == 100) {
                println("");
            } else {
                println("100%");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    static Boolean validateVepTables() {
        File vepDirectory = new File(strMap.get(StrVars.vepTablesPath));
        if (!vepDirectory.exists()) {
            PathwayMatcher.println("The vepTablesPath provided does not exist.");
            System.exit(1);
        } else {
            for (int chr = 1; chr <= 22; chr++) {
                if (!(new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + "")).exists())) {
                    PathwayMatcher.println("The vep table for chromosome " + chr + " was not found. Expected: " + strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                    System.exit(1);
                }
            }
        }
        return true;
    }

    private static HashSet<String> getUniprotAccessionByEnsembl(String ensemblId) {

        HashSet<String> uniprotAccessionsResult = new HashSet<>();
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = ReactomeQueries.getUniprotAccessionByEnsembl;
            StatementResult queryResult;

            queryResult = session.run(query, Values.parameters("id", ensemblId));

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                uniprotAccessionsResult.add(record.get("uniprotAccession").asString());
            }

            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
        return uniprotAccessionsResult;
    }

    private static HashMap<String, HashSet<String>> getAllUniprotAccessionToEnsemblMapping() {

        HashMap<String, HashSet<String>> mapping = new HashMap<>();
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = ReactomeQueries.getAllUniprotAccessionToEnsembl;
            StatementResult queryResult;

            queryResult = session.run(query);

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                String ensemblId = record.get("ensemblId").asString();
                String uniprotAccession = record.get("uniprotAccession").asString();
                mapping.putIfAbsent(ensemblId, new HashSet<>());
                mapping.get(ensemblId).add(uniprotAccession);
            }

            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
        return mapping;
    }

    private static HashMap<String, HashSet<String>> getAllUniprotAccessionToGeneNameMapping() {

        HashMap<String, HashSet<String>> mapping = new HashMap<>();
        try {
            Session session = ConnectionNeo4j.driver.session();

            String query = ReactomeQueries.getAllUniprotAccessionToGeneName;
            StatementResult queryResult;

            queryResult = session.run(query);

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                String ensemblId = record.get("gene").asString();
                String uniprotAccession = record.get("uniprotAccession").asString();
                mapping.putIfAbsent(ensemblId, new HashSet<>());
                mapping.get(ensemblId).add(uniprotAccession);
            }

            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }
        return mapping;
    }

    private static Boolean parseFormat_geneList() {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;
        HashSet<String> geneSet = new HashSet<>();
        HashMap<String, HashSet<String>> geneMapping = new HashMap<>();

        try {
            int row = 1;
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                row++;
                try {
                    if (matches_Gene(line)) {
                        geneSet.add(line);
                    } else {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + row);
                        } else {
                            throw new ParseException("Row " + row + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            }
            reader.close();

            // Get all the mapping from ensembl to uniprot from Reactome
            geneMapping = getAllUniprotAccessionToGeneNameMapping();

            // Convert the ensembl ids to uniprot accessions
            println("Converting Gene Names to UniProt accessions");
            int cont = 0;
            int percentage = 0;
            for (String geneName : geneSet) {
                if (geneMapping.containsKey(geneName)) {
                    for (String uniprotAccession : geneMapping.get(geneName)) {
                        uniprotSet.add(uniprotAccession);
                    }
                }
                cont++;
                int newPercentage = cont * 100 / geneSet.size();
                if (newPercentage - percentage >= Conf.intMap.get(IntVars.percentageStep)) {
                    percentage = newPercentage;
                    print(percentage + "% ");
                }
            }
            if (percentage == 100) {
                println("");
            } else {
                println("100%");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }

    private static Boolean parseFormat_vcf() {
        Boolean parsedCorrectly = true;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(Conf.strMap.get(Conf.StrVars.input)));
            String line = reader.readLine();

            // Skip all the comment lines at the beginning of the file
            while (line != null) {
                if (!line.startsWith("#")) {
                    break;
                }
                line = reader.readLine();
            }

            // Read all data record lines from the file
            do {
                try {
                    if (!matches_Vcf_Record(line)) {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring missformatted row: " + line);
                        } else {
                            throw new ParseException("Row " + line + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
            } while ((line = reader.readLine()) != null);

            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }
}
