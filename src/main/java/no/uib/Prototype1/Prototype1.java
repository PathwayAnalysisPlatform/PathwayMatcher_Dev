package no.UiB.Prototype1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        
        initialize();
        
        //Read configuration options
        //TODO Create text file with configuration. Read the file and populate the configuration variables.
        //TODO If the file of configuration does not exist then take default values.
//        System.out.println("Working Directory = " + System.getProperty("user.dir"));
            
        //Read and convert input to standard format
        Preprocessor.standarizeFile();
        
        //Gather
        Gatherer.gatherCandidates();
        //Match
        Matcher.matchEWAS();
        
        //Filter pathways
        Filter.getFilteredPathways();
        
        //Analyze
        //TODO
        
        //Output
        Reporter.reportPathways();
        Reporter.reportReactions();
    }

    private static void initialize() {
        
        //Read and set configuration values from file
        //TODO 
        
        MPs = new ArrayList<ModifiedProtein>(Configuration.maxNumberOfProteins);
        matchedEWAS = new HashSet<String>(Configuration.maxNumberOfProteins);
    }
}
