package no.uib.PathwayMatcher;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Configuration {

    //General configuration
    public static String configPath = "./Config.txt";
    
    public static String inputListFile = "./src/main/resources/csv/listFileUniprot.csv";      //Input to create a json graph
//    public static String inputListFile = "./src/main/resources/csv/listBjorn.csv";              //Input to create a json graph
    public static String standarizedFile = "./stantarizedFile.csv";
    public static String outputFilePathways = "./Pathways.csv";
    public static String outputFileReactions = "./Reactions.csv";
    public static String outputFilePMPR = "./result.csv";

    //Functional configuration
    public static int maxNumberOfProteins = 5;
    public static boolean createProteinStatusFile = false;
    public static boolean createProteinsNotFoundFile = false;
    public static boolean createProteinsWithMissingSitesFile = false;
    public static boolean createHitPathwayFile = false;
    public static boolean createHitReactionFile = false;
    public static boolean createPMPRTableFile = true;
    
    //Input configuration
    public static Boolean ignoreMisformatedRows = true;

    //Database access
    public static String host = "bolt://localhost";
    public static String username = "neo4j";
    public static String password = "neo4j2";
    
    //***************************
    //***** Graph Generator *****
    //***************************
    public static boolean verboseConsole = true;
    public static ProteinType unitType = ProteinType.uniprot;
    public static String configGraphPath = ".";
    
        //Nodes 
    public static int maxNumProt = 2000;
    public static boolean onlyNeighborsInList = false;
    public static boolean onlyOrderedEdges = false;
    public static boolean showMissingProteins = false;
    
        //Relations 
    public static boolean reactionNeighbors = true;
    public static boolean complexNeighbors = true;
    public static boolean entityNeighbors = false;
    public static boolean candidateNeighbors = false;
    public static boolean topLevelPathwayNeighbors = false;
    public static boolean pathwayNeighbors = false;
    
        //Results
    public static GraphType outputGraphFileType = GraphType.json;
    public static String outputGraphFilePath = "C:/Users/Francisco/Documents/PhD uib/Projects/ProteinsExplorer/public/resources/";
    public static String outputFileName = "ProteomeReactions";

    // public static String inputListFile = "./src/main/resources/csv/listBjorn.csv";
    public enum ProteinType {
        ewas, uniprot
    }

    public enum InputType {
        maxQuantMatrix, 
        peptideList, 
        peptideListAndSites, 
        peptideListAndModSites, 
        uniprotList, 
        uniprotListAndSites,
        uniprotListAndModSites, 
        unknown
    }

    public enum GraphType {
        json {
            public String toString() {
                return "json";
            }
        },
        graphviz {
            public String toString() {
                return "graphviz";
            }
        },
        sif {
            public String toString() {
                return "sif";
            }
        }
    }
}
