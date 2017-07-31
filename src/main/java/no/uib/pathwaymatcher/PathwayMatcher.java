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
import no.uib.pathwaymatcher.Conf.BoolVars;
import no.uib.pathwaymatcher.Conf.InputType;
import no.uib.pathwaymatcher.Conf.InputTypeEnum;
import no.uib.pathwaymatcher.Conf.IntVars;
import static no.uib.pathwaymatcher.Conf.options;
import static no.uib.pathwaymatcher.Conf.setValue;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.stages.Reporter;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.stages.Filter;
import no.uib.pathwaymatcher.stages.Gatherer;
import no.uib.pathwaymatcher.stages.Matcher;
import no.uib.pathwaymatcher.stages.Preprocessor;
import static no.uib.pathwaymatcher.stages.Preprocessor.detectInputType;
import org.apache.commons.cli.*;
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

    //Parameters to run snpList in Netbeans: -i snpList005.csv -v ../ERC/vep_tables/ -t rsidList
    public static void main(String args[]) {

        Conf.setDefaultValues();
        // Define and parse command line options
        Conf.options = new Options();

        Option input = new Option("i", StrVars.input, true, "input file path");
        input.setRequired(false);
        options.addOption(input);

        Option inputType = new Option("t", StrVars.inputType, true, "Type of input file (" + InputType.rsidList + ", " + InputType.maxQuantMatrix + ", " + InputType.uniprotListAndModSites + ",...etc.)");
        inputType.setRequired(true);
        options.addOption(inputType);

        Option config = new Option("c", StrVars.conf, true, "config file path");
        config.setRequired(false);
        options.addOption(config);

        Option output = new Option("o", StrVars.output, true, "output file path");
        output.setRequired(false);
        options.addOption(output);

        Option max = new Option("m", IntVars.maxNumProt, true, "maximum number of indentifiers");
        max.setRequired(false);
        options.addOption(max);
        
        Option siteRange = new Option("r", IntVars.siteRange, true, "Allowed distance for PTM sites");
        siteRange.setRequired(false);
        options.addOption(siteRange);

        Option reactionsFile = new Option("rf", StrVars.reactionsFile, true, "create a file with list of reactions containing the input");
        reactionsFile.setRequired(false);
        options.addOption(reactionsFile);

        Option pathwaysFile = new Option("pf", StrVars.pathwaysFile, true, "create a file with list of pathways containing the input");
        pathwaysFile.setRequired(false);
        options.addOption(pathwaysFile);

        Option host = new Option(StrVars.host, true, "Url of the Neo4j database with Reactome");
        host.setRequired(false);
        options.addOption(host);

        Option username = new Option("u", StrVars.username, true, "Username to access the database with Reactome");
        username.setRequired(false);
        options.addOption(username);

        Option password = new Option("p", StrVars.password, true, "Password related to the username provided to access the database with Reactome");
        password.setRequired(false);
        options.addOption(password);

        Option vepTablesPathOption = new Option("v", StrVars.vepTablesPath, true, "The path of the folder containing the vep mapping tables. If the type of input is \"snpList\" then the parameter is required. It is not required otherwise.");
        vepTablesPathOption.setRequired(false);
        options.addOption(vepTablesPathOption);
        
        Option fastaFileOption = new Option("f", StrVars.fastaFile, true, "Path and name of the FASTA file with the possible protein sequences to search the peptides.");
        fastaFileOption.setRequired(false);
        options.addOption(fastaFileOption);

        Option showTopLevelPathways = new Option("tlp", BoolVars.showTopLevelPathways, false, "Set this flag to show the \"Top Level Pathways\" column in the output file.");
        showTopLevelPathways.setRequired(false);
        options.addOption(showTopLevelPathways);

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
            if (cmd.hasOption(StrVars.conf)) {
                Conf.setValue(StrVars.conf, cmd.getOptionValue(StrVars.conf).replace("\\", "/"));
            } else {
                setValue(StrVars.conf, "./Config.txt");
            }

            readConfigurationFromFile(); //Read configuration options from config.txt file

            if (cmd.hasOption(StrVars.input)) {
                Conf.setValue(StrVars.input, cmd.getOptionValue(StrVars.input).replace("\\", "/"));
            }

            if (cmd.hasOption(StrVars.output)) {
                Conf.setValue(StrVars.output, cmd.getOptionValue(StrVars.output).replace("\\", "/"));
            }
            if (cmd.hasOption(IntVars.maxNumProt)) {
                Conf.setValue(IntVars.maxNumProt, cmd.getOptionValue(IntVars.maxNumProt));
            }
            if (cmd.hasOption(IntVars.siteRange)) {
                Conf.setValue(IntVars.siteRange, cmd.getOptionValue(IntVars.siteRange));
            }
            if (cmd.hasOption(StrVars.reactionsFile)) {
                Conf.setValue(StrVars.reactionsFile, cmd.getOptionValue(StrVars.reactionsFile));
            }
            if (cmd.hasOption(StrVars.pathwaysFile)) {
                Conf.setValue(StrVars.pathwaysFile, cmd.getOptionValue(StrVars.pathwaysFile));
            }
            if (cmd.hasOption(StrVars.host)) {
                Conf.setValue(StrVars.host, cmd.getOptionValue(StrVars.host));
            }
            if (cmd.hasOption(StrVars.username)) {
                Conf.setValue(StrVars.username, cmd.getOptionValue(StrVars.username));
            }
            if (cmd.hasOption(StrVars.password)) {
                Conf.setValue(StrVars.password, cmd.getOptionValue(StrVars.password));
            }
            if (cmd.hasOption(BoolVars.showTopLevelPathways)) {
                Conf.setValue(BoolVars.showTopLevelPathways, Boolean.TRUE);
            }
            if (cmd.hasOption(StrVars.fastaFile)) {
                Conf.setValue(StrVars.fastaFile, cmd.getOptionValue(StrVars.fastaFile));
            }

            initialize();   //Initialize objects

            if (cmd.hasOption(StrVars.inputType)) {
                String inputTypeStr = cmd.getOptionValue("t");
                for (InputTypeEnum c : InputTypeEnum.values()) {
                    if (c.name().equals(inputTypeStr)) {
                        Conf.setValue(StrVars.inputType, inputTypeStr);
                        break;
                    }
                }
                if (strMap.get(StrVars.inputType).equals(InputType.unknown)) {
                    System.out.println("The specified input type is not supported: " + inputTypeStr);
                    System.exit(1);
                }
                if (strMap.get(StrVars.inputType).equals(InputType.rsid) || strMap.get(StrVars.inputType).equals(InputType.rsidList)) {
                    if (!cmd.hasOption(StrVars.input)) {
                        throw new ParseException(StrVars.input);
                    } else if (!cmd.hasOption(StrVars.vepTablesPath)) {
                        throw new ParseException(StrVars.vepTablesPath);
                    } else {
                        String path = cmd.getOptionValue(StrVars.vepTablesPath);
                        if (!path.endsWith("/")) {
                            path += "/";
                        }
                        Conf.setValue(StrVars.vepTablesPath, path);
                        if (strMap.get(StrVars.inputType).equals(InputType.rsid)) {     //Process a single rsId
                            Gatherer.gatherPathways(cmd.getOptionValue(StrVars.input));
                        } else {
                            Gatherer.gatherPathways(Boolean.TRUE);// Process a list of rsIds
                        }
                    }
                } else {
                    //System.out.println("Working Directory = " + System.getProperty("user.dir"));
                    if (strMap.get(StrVars.inputType) == Conf.InputType.unknown) {
                        try {
                            println("Detecting input type...");
                            String t = detectInputType();
                            setValue(StrVars.inputType, t);
                            println("Input type detected: " + strMap.get(StrVars.inputType));
                        } catch (IOException e) {
                            System.out.println("Failed to detect type input.");
                            System.exit(1);
                        }
                    }

                    if (strMap.get(StrVars.inputType) != Conf.InputType.rsidList) {
                        println("\nPreprocessing input file...");
                        if(!Preprocessor.standarizeFile()){
                            System.exit(1);
                        }
                        println("Preprocessing complete.");

                        //Gather: select all possible EWAS according to the input proteins
                        println("\nCandidate gathering started...");
                        Gatherer.gatherCandidateEwas();
                        println("Candidate gathering complete.");

                        //Match: choose which EWAS that match the substate of the proteins
                        switch (strMap.get(StrVars.inputType)) {
                            case Conf.InputType.maxQuantMatrix:
                            case Conf.InputType.peptideListAndSites:
                            case Conf.InputType.peptideListAndModSites:
                            case Conf.InputType.uniprotListAndSites:
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
                        Reporter.sortOutput();
                    }
                }
            }

        } catch (ParseException e) {

            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }
    }

    private static int initialize() {

        MPs = new ArrayList<ModifiedProtein>(Conf.intMap.get(IntVars.maxNumProt));

        if (strMap.get(StrVars.host).length() > 0) {
            ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(StrVars.host), AuthTokens.basic(strMap.get(StrVars.username), strMap.get(StrVars.password)));
        } else {
            ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(StrVars.host));
        }

        try {
            Session session = ConnectionNeo4j.driver.session();
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }

        return 0;
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

    private static int readConfigurationFromFile() {

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Conf.strMap.get(StrVars.conf)));

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

        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(StrVars.conf));
            //Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
            return 1;
        }
        return 0;
    }
}
