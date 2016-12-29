package no.UiB.Prototype1;

import no.UiB.Prototype1.Model.ModifiedProtein;
import no.UiB.Prototype1.Stages.Gatherer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
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
    - Data Analysis
    - Visualization

 */

 /*
    // Data Input
        //Either peptide list, MaxQuant file, standard format list

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
 */
public class Prototype1 {

    public static void main(String args[]) throws IOException {

        TreeSet<String> pathways = new TreeSet<String>();
        List<ModifiedProtein> inputList;

        //File streams for result files
        FileWriter ProteinsNotFoundStream = new FileWriter("./src/main/resources/csv/ProteinsNotFound.csv");                     // Saves the list of uniprot ids when proteins where not found
        FileWriter ProteinsWithMissingSitesStream = new FileWriter("./src/main/resources/csv/ProteinsWithMissingSites.csv");     // Saves the list of uniprot ids when proteins do not have registered sites
        FileWriter ProteinStatusStream = new FileWriter("./src/main/resources/csv/ProteinStatus.csv");                           // Saves the list of uniptot ids, case, sites expected, reactome Ids, sites found, displayName. When something is missing it is left blank.
        FileWriter hitPathwayStream = new FileWriter("./src/main/resources/csv/HitPathway.csv");                                 // <Uniprot Id, Reactome Id, pathwat with dotted route>, sorted according to the three columns

        /*
         * ****************************************************************************************************
         */
        inputList = Gatherer.getInputList();
    }
}
