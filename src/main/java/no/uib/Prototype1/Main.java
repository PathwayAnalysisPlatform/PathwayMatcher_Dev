package no.uib.Prototype1;

import java.io.IOException;
import java.text.ParseException;
import no.UiB.Prototype1.Stages.Preprocessor;

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
public class Main {
    public static void main(String args[]) throws IOException, ParseException
    {
        /***************** Data Input ****************/
        
        //Verify the type of input
        String inputPath = "./src/main/java/no/uib/Prototype1/Input/Input1.txt";
        int inputType = Preprocessor.detectInputType(inputPath);
        
        switch(inputType)
        {
            case 1:     //MaxQuant format   
                System.out.println("Parsing ended correctly: " + Preprocessor.parseFormat1_MaxQuant(inputPath));
                break;
            case 2:     //Uniprot Ids with generic phosphosites
                Preprocessor.parseFormat2_UniprotsWithSites(inputPath);
                break;
            case 3:     //Peptides with generic phosphosites
                Preprocessor.parseFormat3_PeptidesWithSites(inputPath);
                break;
        }
        
        System.out.println("\nFormato: " + inputType);
        //Convert input to standard format
    }
}
