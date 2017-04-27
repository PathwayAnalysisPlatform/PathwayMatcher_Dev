package no.uib.pathwaymatcher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.uib.pathwaymatcher.Conf.boolVars;
import no.uib.pathwaymatcher.Conf.intVars;
import static no.uib.pathwaymatcher.Conf.options;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.strVars;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.stages.Reporter;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.stages.Filter;
import no.uib.pathwaymatcher.stages.Gatherer;
import no.uib.pathwaymatcher.stages.Matcher;
import no.uib.pathwaymatcher.stages.Preprocessor;
import org.apache.commons.cli.*;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * @author Luis Francisco Hernández Sánchez
 * @author Marc Vaudel
 */

/*
    Overview

    - Data Input
    - Data Preprocessing
    - Data gathering
    - Search:
        - Matching
        - Filtering

 */

 /*
    // Data Preprocessing
        //Detect type of input: MaxQuant, fasta, standard format list
        //Parse to standard format.

    // Data Gathering
        //Take list of Modified Proteins (MPs) in my standard format: UniprotId,[psimod:site;psimod:site...;psimod:site]
        //Every row specifies a Modified Protein (MP), which is a protein in a specific configuration of PTMs.
        //A UniprotId can appear in many rows. But they appear in contiguous rows, since the list is ordered by UniprotId in the first column.
        //For every Protein in the MPs list, get the possible configurations

    // Matching the Input MPs with the available reference MPs.
        //In this data model means selecting a set of EWAS for every input MP.

    // Filtering
        //Find all Events (Reactions/Pathways) containg the selected EWASes.

    // Analysis
        // Do maths and statistics to score pathways according to their significance.
        // Statistics on the matching partners of the proteins
 */
public class PathwayMatcher {

    public static List<ModifiedProtein> MPs;
    public static Set<String> uniprotSet = new HashSet<String>();

    public static void main(String args[]) throws IOException {

        Conf.setDefaultValues();
        // Define and parse command line options
        Conf.options = new Options();

        Option input = new Option("i", strVars.input.toString(), true, "input file path");
        input.setRequired(false);
        options.addOption(input);

        Option inputType = new Option("t", strVars.inputType.toString(), true, "Type of input file (uniprot list, SNP list,...etc.)");
        inputType.setRequired(false);
        options.addOption(inputType);

        Option config = new Option("c", strVars.conf.toString(), true, "config file path");
        config.setRequired(false);
        options.addOption(config);

        Option output = new Option("o", strVars.conf.toString(), true, "output file path");
        output.setRequired(false);
        options.addOption(output);

        Option max = new Option("m", intVars.maxNumProt.toString(), true, "maximum number of indentifiers");
        max.setRequired(false);
        options.addOption(max);

        Option reactionsFile = new Option("r", boolVars.reactionsFile.toString(), false, "create a file with list of reactions containing the input");
        reactionsFile.setRequired(false);
        options.addOption(reactionsFile);

        Option pathwaysFile = new Option("p", boolVars.pathwaysFile.toString(), false, "create a file with list of pathways containing the input");
        pathwaysFile.setRequired(false);
        options.addOption(pathwaysFile);

        Option host = new Option("h", strVars.host.toString(), true, "Url of the Neo4j database with Reactome");
        host.setRequired(false);
        options.addOption(host);

        Option username = new Option("u", strVars.username.toString(), true, "Username to access the database with Reactome");
        username.setRequired(false);
        options.addOption(username);

        Option password = new Option("p", strVars.password.toString(), true, "Password related to the username provided to access the database with Reactome");
        password.setRequired(false);
        options.addOption(password);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            // If an option is specified in the command line and in the configuration file, the command line value is used
            /* For the configuration path
                - If a command line value is sent, it is used
                - If a command line value is not sent, it searches in the current folder for config.txt
                - If a command line value es not sent and there is no file in the current folder, then the program finishes
             */
            if (cmd.hasOption(strVars.conf.toString())) {
                Conf.setValue(strVars.conf.toString(), cmd.getOptionValue(strVars.conf.toString()).replace("\\", "/"));
            }

            readConfigurationFromFile(); //Read configuration options from config.txt file

            if (cmd.hasOption(strVars.input.toString())) {
                Conf.setValue(strVars.input.toString(), cmd.getOptionValue(strVars.input.toString()).replace("\\", "/"));
            }
            if (cmd.hasOption(strVars.input.toString())) {
                Conf.setValue(strVars.input.toString(), cmd.getOptionValue(strVars.inputType.toString()).replace("\\", "/"));
            }
            if (cmd.hasOption(strVars.output.toString())) {
                Conf.setValue(strVars.output.toString(), cmd.getOptionValue(strVars.output.toString()).replace("\\", "/"));
            }
            if (cmd.hasOption(intVars.maxNumProt.toString())) {
                Conf.setValue(intVars.maxNumProt.toString(), cmd.getOptionValue(intVars.maxNumProt.toString()));
            }
            Conf.setValue(boolVars.reactionsFile.toString(), cmd.hasOption(boolVars.pathwaysFile.toString()));
            Conf.setValue(boolVars.pathwaysFile.toString(), cmd.hasOption(boolVars.reactionsFile.toString()));

            if (cmd.hasOption(strVars.host.toString())) {
                Conf.setValue(strVars.host.toString(), cmd.getOptionValue(strVars.host.toString()));
            }
            if (cmd.hasOption(strVars.username.toString())) {
                Conf.setValue(strVars.username.toString(), cmd.getOptionValue(strVars.username.toString()));
            }
            if (cmd.hasOption(strVars.password.toString())) {
                Conf.setValue(strVars.password.toString(), cmd.getOptionValue(strVars.password.toString()));
            }

        } catch (ParseException e) {

            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

//        Iterator it = Conf.strMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            println(pair.getKey() + " = " + pair.getValue());
//        }
//
//        it = Conf.boolMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            println(pair.getKey() + " = " + pair.getValue());
//        }
//
//        it = Conf.intMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            println(pair.getKey() + " = " + pair.getValue());
//        }
        initialize();   //Initialize objects

        //System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //Read and convert input to standard format
        println("Preprocessing input file...");
        Preprocessor.standarizeFile();
        println("Preprocessing complete.");

        //Gather: select all possible EWAS according to the input proteins
        println("Candidate gathering started...");
        Gatherer.gatherCandidateEwas();
        println("Candidate gathering complete.");

        //Match: choose which EWAS that match the substate of the proteins
        switch (strMap.get(strVars.inputType.toString())) {
            case Conf.InputType.maxQuantMatrix:
            case Conf.InputType.peptideListAndSites:
            case Conf.InputType.peptideListAndModSites:
            case Conf.InputType.uniprotListAndSites:
            case Conf.InputType.uniprotListAndModSites:
                println("Candidate matching started....");
                Matcher.matchCandidates();
                println("Candidate matching complete.");
                break;
        }

        //Filter pathways
        println("Filtering pathways and reactions....");
        Filter.getFilteredPathways();
        println("Filtering pathways and reactions complete.");
        Reporter.createReports();
    }

    private static int initialize() {

        MPs = new ArrayList<ModifiedProtein>(Conf.intMap.get(intVars.maxNumProt.toString()));

        ConnectionNeo4j.driver = GraphDatabase.driver(
                Conf.strMap.get(strVars.host.toString()),
                AuthTokens.basic(Conf.strMap.get(
                        strVars.username.toString()),
                        Conf.strMap.get(strVars.password.toString())
                )
        );

        return 0;
    }

    public static void println(String phrase) {
        if (Conf.boolMap.get(boolVars.verbose.toString())) {
            System.out.println(phrase);
        }
    }

    private static int readConfigurationFromFile() {

        if (Conf.strMap.get(strVars.conf.toString()).endsWith("/")) {
            System.out.println("The name for the configuration file was not specified.\n\n Examples: ./folder1/folder2/fileName.txt");
            System.exit(1);
        }

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Conf.strMap.get(strVars.conf.toString())));

            //For every valid variable found in the config.txt file, the variable value gets updated
            String line;
            while ((line = configBR.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("//")) {
                    continue;
                }
                if (!line.contains("=")) {
                    continue;
                }
                String[] parts = line.split("=");
                if (Conf.contains(parts[0])) {
                    Conf.setValue(parts[0], parts[1]);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("The Configuration file specified was not found: " + Conf.strMap.get(strVars.conf.toString()));
            System.out.println("The starting location is: " + System.getProperty("user.dir"));
            //Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(strVars.conf.toString()));
            //Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
            return 1;
        }
        return 0;
    }
}
