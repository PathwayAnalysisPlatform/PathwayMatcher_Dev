package no.UiB.Prototype1;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Configuration {

    //Functional configuration
    public static int maxNumberOfProteins = 5;
    public static Boolean createProteinStatusFile = true;
    public static Boolean createProteinsNotFoundFile = true;
    public static Boolean createProteinsWithMissingSitesFile = false;
    public static Boolean createHitPathwayFile = true;

    //Database access
    public static String host = "bolt://localhost";
    public static String username = "neo4j";
    public static String password = "neo4j2";

    // Graph Generator
    
    public static boolean verboseConsole = true; 
    public static ProteinType unitType = ProteinType.uniprot;
    
    public static int maxNumProt = 100;
    public static boolean onlyNeighborsInList = false;
    public static boolean onlyOrderedEdges = true;
    public static boolean showMissingProteins = true;
    
    public static boolean reactionNeighbors = true;
    public static boolean complexNeighbors = true;
    public static boolean entityNeighbors = false;
    public static boolean candidateNeighbors = false;
    public static boolean topLevelPathwayNeighbors = false;
    public static boolean pathwayNeighbors = false;

    public static GraphType outputGraphFileType = GraphType.json;
    public static String outputGraphFilePath = "C:/Users/Francisco/Documents/PhD UiB/Projects/PathwayAdventure/public/resources/";
    public static String inputListFile = "./src/main/resources/csv/listFileUniprot.csv";
//    public static String inputListFile = "./src/main/resources/csv/listBjorn.csv";
    

    public enum ProteinType{
        ewas, uniprot
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
