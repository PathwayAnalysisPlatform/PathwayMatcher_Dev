package no.uib.pathwaymatcher;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import no.uib.pathwaymatcher.Conf.BoolVars;
import no.uib.pathwaymatcher.Conf.InputType;
import no.uib.pathwaymatcher.Conf.IntVars;

import static no.uib.pathwaymatcher.Conf.options;
import static no.uib.pathwaymatcher.Conf.commandLine;
import static no.uib.pathwaymatcher.Conf.setValue;
import static no.uib.pathwaymatcher.Conf.strMap;

import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.model.Error;
import no.uib.pathwaymatcher.model.stages.*;
import no.uib.pathwaymatcher.model.Proteoform;

import static no.uib.pathwaymatcher.model.Error.*;
import static no.uib.pathwaymatcher.util.FileUtils.getInput;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Configuration_Variable;

import no.uib.pathwaymatcher.model.stages.PreprocessorFactory;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

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

    //Parameters to run snpList in Netbeans: -i snpList005.csv -v ../ERC/vep_tables/ -t rsidList
    public static void main(String args[]) {

        Conf.setDefaultValues();

        // If there are no arguments and there is no configuration file in the same directory

        if (args.length == 0) {
            File file = new File(strMap.get(StrVars.conf));
            if (!file.exists() && !file.isDirectory()) {
                System.exit(Error.NO_ARGUMENTS.getCode());
            }
        }

        // Read and set configuration values
        options = new Options();

        addOption("t", StrVars.inputType, true, "Type of input file (" + InputType.peptideList + ", " + InputType.rsidList + ", " + InputType.uniprotListAndModSites + ",...etc.)", true);
        addOption("i", StrVars.input, true, "input file path", false);
        addOption("o", StrVars.output, true, "output file path", false);
        addOption("c", StrVars.conf, true, "config file path and name", false);
        addOption("m", IntVars.maxNumProt, true, "maximum number of indentifiers", false);
        addOption("r", IntVars.siteRange, true, "Allowed distance for PTM sites", false);
        addOption("rf", StrVars.reactionsFile, true, "create a file with list of reactions containing the input", false);
        addOption("pf", StrVars.pathwaysFile, true, "create a file with list of pathways containing the input", false);
        addOption("h", StrVars.host, true, "Url of the Neo4j database with Reactome", false);
        addOption("u", StrVars.username, true, "Username to access the database with Reactome", false);
        addOption("p", StrVars.password, true, "Password related to the username provided to access the database with Reactome", false);
        addOption("vep", StrVars.vepTablesPath, true, "The path of the folder containing the vep mapping tables. If the type of input is \"snpList\" then the parameter is required. It is not required otherwise.", false);
        addOption("f", StrVars.fastaFile, true, "Path and name of the FASTA file with the possible protein sequences to search the peptides.", false);
        addOption("tlp", BoolVars.showTopLevelPathways, false, "Set this flag to show the \"Top Level Pathways\" column in the output file.", false);
        addOption("imr", BoolVars.ignoreMisformatedRows, false, "Ignore input lines with wrong format.", false);
        addOption("pg", StrVars.peptideGrouping.toString(), false, "Group PTM of peptides mapped to same protein", false);
        addOption("mt", StrVars.matchingType.toString(), false, "Type of criteria used to decide if two proteoforms are equivalent.", false);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        }

        //Set all command line arguments provided
        for (String option : commandLine.getArgs()) {
            if (commandLine.hasOption(option)) {
                Conf.setValue(option, commandLine.getOptionValue(option));
            }
        }

        // Set values from configuration file
        readConfigurationFromFile();

        initialize();   //Initialize objects


        // Get the appropriate preprocessor according to the input type
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(strMap.get(StrVars.inputType));
        List<String> input = getInput(strMap.get(StrVars.input));

        try{
             List<Object> entities = preprocessor.process(input);
        } catch (java.text.ParseException e) {
            System.out.println("Error parsing the input file.");
            System.exit(INPUT_PARSING_ERROR.getCode());
        }
        println("Preprocessing complete.");

        // At this stage, the entities to search are in memory (proteins or proteoforms)
        Matcher matcher = MatcherFactory.getMatcher(strMap.get(StrVars.inputType));

         matcher.match(entities);

        println("\nCandidate gathering started...");
        Gatherer.gatherCandidates();
        println("Candidate gathering complete.");

        //Match: choose which EWAS that match the substate of the proteins
        switch (strMap.get(StrVars.inputType)) {
            case Conf.InputType.peptideListAndModSites:
            case Conf.InputType.uniprotListAndModSites:
                println("\nCandidate matching started....");
                Matcher.matchCandidates();
                println("Candidate matching complete.");
                break;
        }

        //Filter pathways
        println("\nFiltering pathways and reactions....");
        Filter.getFilteredPathways();
        println("Filtering pathways and reactions complete.");

        Reporter.createReports();
        println("\nProcess complete.");


        // If rsid, rsidList, vcf should contain VEP tables

        // Add the "/" to the vepTabesPath variable



        if (strMap.get(StrVars.inputType).equals(InputType.rsid)
                || strMap.get(StrVars.inputType).equals(InputType.rsidList)
                || strMap.get(StrVars.inputType).equals(InputType.vcf)) {
            if (!commandLine.hasOption(StrVars.vepTablesPath)) {
                throw new ParseException("Missing argument " + StrVars.vepTablesPath);
            } else {
                String path = commandLine.getOptionValue(StrVars.vepTablesPath);
                if (!path.endsWith("/") || !path.endsWith("\\")) {
                    path += "/";
                }
                Conf.setValue(StrVars.vepTablesPath, path);
                if (strMap.get(StrVars.inputType).equals(InputType.rsid)) {     //Process a single rsId
                    if (!commandLine.hasOption(StrVars.input)) {
                        throw new ParseException(StrVars.input);
                    }
                    Gatherer.gatherPathways(commandLine.getOptionValue(StrVars.input));
                } else if (strMap.get(StrVars.inputType).equals(InputType.rsidList)) {
                    Gatherer.gatherPathwaysFromGeneticVariants(Boolean.TRUE);// Process a list of rsIds
                } else {
                    println("\nPreprocessing input file...");
                    if (!Preprocessor.standarizeFile()) {
                        System.exit(1);
                    }
                    println("Preprocessing complete.");
                    Gatherer.gatherPathwaysFromVCF();                       // Process a list of genetic variations
                }
            }
        } else {

            if (strMap.get(StrVars.inputType) != Conf.InputType.rsidList) {

                if (strMap.get(StrVars.inputType).startsWith("peptide")) {
                    if (!commandLine.hasOption(StrVars.fastaFile)) {
                        throw new ParseException("Missing argument " + StrVars.fastaFile);
                    }
                }

                println("\nPreprocessing input file...");
                if (!Preprocessor.standarizeFile()) {
                    System.exit(1);
                }
                println("Preprocessing complete.");

                //Gather: select all possible EWAS according to the input
                println("\nCandidate gathering started...");
                Gatherer.gatherCandidates();
                println("Candidate gathering complete.");

                //Match: choose which EWAS that match the substate of the proteins
                switch (strMap.get(StrVars.inputType)) {
                    case Conf.InputType.peptideListAndModSites:
                    case Conf.InputType.uniprotListAndModSites:
                        println("\nCandidate matching started....");
                        Matcher.matchCandidates();
                        println("Candidate matching complete.");
                        break;
                }

                //Filter pathways
                println("\nFiltering pathways and reactions....");
                Filter.getFilteredPathways();
                println("Filtering pathways and reactions complete.");

                Reporter.createReports();
                println("\nProcess complete.");
            } else {
                Gatherer.gatherPathways();
            }
        }
    }

    private static void addOption(String opt, String longOpt, boolean hasArg, String description, boolean required) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        options.addOption(option);
    }

    /**
     * Create data structure to hold the proteoforms and initialize the connection to Neo4j.
     * This method needs that the command line arguments have been parsed and set, also the variables from the
     * configuration file.
     *
     * @return
     */
    private static void initialize() {

        MPs = new ArrayList<Proteoform>(Conf.intMap.get(IntVars.maxNumProt));

        if (strMap.get(StrVars.username).length() > 0) {
            ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(StrVars.host), AuthTokens.basic(strMap.get(StrVars.username), strMap.get(StrVars.password)));
        } else {
            ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(StrVars.host));
        }

        try {
            Session session = ConnectionNeo4j.driver.session();
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(e);
            System.out.println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(COULD_NOT_CONNECT_TO_NEO4j.getCode());
        }
    }

    public static void println(String phrase) {
        if (Conf.boolMap.get(BoolVars.verbose)) {
            System.out.println(phrase);
        }
    }

    public static void print(String phrase) {
        if (Conf.boolMap.get(BoolVars.verbose)) {
            System.out.print(phrase);
        }
    }

    /*
    Reads all the configuration file and sets the values of the variables encountered. If the variable was defined in the
    command line as argument then it skips it.
     */
    private static void readConfigurationFromFile() {

        try {
            //Read and set configuration values from file
            LineIterator it = FileUtils.lineIterator(new File(strMap.get(StrVars.conf)), "UTF-8");

            //For every valid variable found in the config.txt file, the variable value gets updated
            String line;
            while (it.hasNext()) {
                line = it.nextLine().trim();

                if (matches_Configuration_Variable(line)) {
                    String[] parts = line.split("=");
                    String name = parts[0];
                    String value = parts[1];
                    if (Conf.contains(name)) {
                        if (!commandLine.hasOption(name)) {
                            Conf.setValue(name, value);
                        }
                    }
                }
            }

            LineIterator.closeQuietly(it);
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(StrVars.conf));
            System.exit(Error.COULD_NOT_READ_CONF_FILE.getCode());
        }
    }
}
