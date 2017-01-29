package no.UiB.Prototype1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.UiB.Prototype1.Model.ModifiedProtein;
import no.UiB.Prototype1.Stages.Filter;
import no.UiB.Prototype1.Stages.Gatherer;
import no.UiB.Prototype1.Stages.Matcher;
import no.UiB.Prototype1.Stages.Preprocessor;
import no.uib.Prototype1.Stages.Reporter;

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
    - Data Analysis
    - Visualization

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
public class Prototype1 {

    public static List<ModifiedProtein> MPs;
    public static Set<String> matchedEWAS;
    public static Set<String> hitPathways = new HashSet<String>();
    public static Set<String> hitReactions = new HashSet<String>();

    public static void main(String args[]) throws IOException {

        //Read configuration options and initialize objects
        if (initialize() == 1) {
            return;
        }
        //System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //Read and convert input to standard format
        Preprocessor.standarizeFile();

        //Gather
        Gatherer.gatherCandidates();

        //Match
        Matcher.matchCandidates();

        //Filter pathways
        Filter.getFilteredPathways();

        //Analyze
        //TODO
        //Output
        Reporter.reportPathways();
        Reporter.reportReactions();
    }

    private static int initialize() {

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Configuration.configPath));

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
                if (parts[0].equals("inputListFile")) {
                    Configuration.inputListFile = parts[1].replace("\\", "/");
                } else if (parts[0].equals("standarizedFile")) {
                    Configuration.standarizedFile = parts[1].replace("\\", "/");
                } else if (parts[0].equals("outputFilePathways")) {
                    Configuration.outputFilePathways = parts[1].replace("\\", "/");
                } else if (parts[0].equals("outputFileReactions")) {
                    Configuration.outputFileReactions = parts[1].replace("\\", "/");
                } else if (parts[0].equals("maxNumberOfProteins")) {
                    Configuration.maxNumberOfProteins = Integer.valueOf(parts[1]);
                } else if (parts[0].equals("createProteinStatusFile")) {
                    Configuration.createProteinStatusFile = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("createProteinsNotFoundFile")) {
                    Configuration.createProteinsNotFoundFile = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("createProteinsWithMissingSitesFile")) {
                    Configuration.createProteinsWithMissingSitesFile = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("createHitPathwayFile")) {
                    Configuration.createHitPathwayFile = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("ignoreMisformatedRows")) {
                    Configuration.ignoreMisformatedRows = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("host")) {
                    Configuration.host = parts[1];
                } else if (parts[0].equals("username")) {
                    Configuration.username = parts[1];
                } else if (parts[0].equals("password")) {
                    Configuration.password = parts[1];
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Configuration file not found at: " + Configuration.configPath);
            Logger.getLogger(Prototype1.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Configuration.configPath);
            Logger.getLogger(Prototype1.class.getName()).log(Level.SEVERE, null, ex);
        }

        MPs = new ArrayList<ModifiedProtein>(Configuration.maxNumberOfProteins);
        matchedEWAS = new HashSet<String>(Configuration.maxNumberOfProteins);

        return 0;
    }
}
