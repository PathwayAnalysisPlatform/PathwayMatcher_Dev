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
import java.util.logging.Level;
import java.util.logging.Logger;
import static no.uib.pathwaymatcher.Conf.options;
import no.uib.pathwaymatcher.db.ConnectionNeo4j;
import no.uib.pathwaymatcher.stages.Filter;
import no.uib.pathwaymatcher.stages.Gatherer;
import no.uib.pathwaymatcher.stages.Matcher;
import no.uib.pathwaymatcher.stages.Preprocessor;
import no.uib.pathwaymatcher.stages.Reporter;
import no.uib.pathwaymatcher.model.ModifiedProtein;
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

        Option input = new Option("i", "inputPath", true, "input file path");
        input.setRequired(false);
        options.addOption(input);

        Option config = new Option("c", "configPath", true, "config file path");
        config.setRequired(false);
        options.addOption(config);

        Option output = new Option("o", "outputPath", true, "output file path");
        output.setRequired(false);
        options.addOption(output);

        Option max = new Option("m", "maxNumProt", true, "maximum number of indentifiers");
        max.setRequired(false);
        options.addOption(max);

        Option reactionsFile = new Option("r", "reactionsFile", false, "create a file with list of reactions containing the input");
        reactionsFile.setRequired(false);
        options.addOption(reactionsFile);

        Option pathwaysFile = new Option("p", "pathwaysFile", false, "create a file with list of pathways containing the input");
        pathwaysFile.setRequired(false);
        options.addOption(pathwaysFile);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        if (cmd.hasOption("configPath")) {
            Conf.setValue(Conf.strVars.configPath.toString(), cmd.getOptionValue("configPath"));
        }
        if (cmd.hasOption(Conf.strVars.inputPath.toString())) {
            Conf.setValue(Conf.strVars.inputPath.toString(), cmd.getOptionValue("inputPath"));
        }
        if (cmd.hasOption(Conf.strVars.outputPath.toString())) {
            Conf.setValue(Conf.strVars.outputPath.toString(), cmd.getOptionValue("outputPath"));
        }
        if (cmd.hasOption(Conf.intVars.maxNumProt.toString())) {
            Conf.setValue(Conf.intVars.maxNumProt.toString(), cmd.getOptionValue("maxNumProt"));
        }
        Conf.setValue(Conf.boolVars.reactionsFile.toString(), cmd.hasOption("reactionsFile"));
        Conf.setValue(Conf.boolVars.pathwaysFile.toString(), cmd.hasOption("pathwaysFile"));

        Iterator it = Conf.strMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            println(pair.getKey() + " = " + pair.getValue());
        }
        
        it = Conf.boolMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            println(pair.getKey() + " = " + pair.getValue());
        }
        
        it = Conf.intMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            println(pair.getKey() + " = " + pair.getValue());
        }

        //Read configuration options and initialize objects
        if (initialize() == 1) {
            return;
        }
        //System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //Read and convert input to standard format
        println("Preprocessing input file...");
        Preprocessor.standarizeFile();
        println("Preprocessing complete.");

        //Gather
        println("Candidate gathering started...");
        Gatherer.gatherCandidates();
        println("Candidate gathering complete.");

        //Match
        println("Candidate matching started....");
        Matcher.matchCandidates();
        println("Candidate matching complete.");

        //Filter pathways
        println("Filtering pathways and reactions....");
        Filter.getFilteredPathways();
        println("Filtering pathways and reactions complete.");

        Reporter.createReports();
    }

    private static int initialize() {

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Conf.strMap.get(Conf.strVars.configPath.toString())));

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
            System.out.println("Configuration file not found at: " + Conf.strMap.get(Conf.strVars.configPath.toString()));
            System.out.println(System.getProperty("user.dir") );
            Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(Conf.strVars.configPath.toString()));
            Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        MPs = new ArrayList<ModifiedProtein>(Conf.intMap.get(Conf.intVars.maxNumProt.toString()));

        ConnectionNeo4j.driver = GraphDatabase.driver(
                Conf.strMap.get(Conf.strVars.host.toString()),
                AuthTokens.basic(Conf.strMap.get(
                        Conf.strVars.username.toString()),
                        Conf.strMap.get(Conf.strVars.password.toString())
                )
        );

        return 0;
    }

    public static void println(String phrase) {
        if (Conf.boolMap.get(Conf.boolVars.verbose.toString())) {
            System.out.println(phrase);
        }
    }
}
