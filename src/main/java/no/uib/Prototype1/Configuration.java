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

}
