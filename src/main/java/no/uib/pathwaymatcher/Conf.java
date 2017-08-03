package no.uib.pathwaymatcher;

import java.util.HashMap;
import org.apache.commons.cli.Options;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Conf {

    public static Options options;

    /**
     * Contains a map from a variable to its value
     */
    public static HashMap<String, String> strMap;
    public static HashMap<String, Boolean> boolMap;
    public static HashMap<String, Integer> intMap;

    public interface StrVars {


        String conf = "conf";
        String input = "input";
        String inputType = "inputType";
        String outputType = "outputType";
        String output = "output";
        String host = "host";
        String username = "username";
        String password = "password";
        String standardFilePath = "standardFilePath";
        String vepTablesPath = "vepTablesPath";
        String vepTableName = "vepTableName";
        String reactionsFile = "reactionsFile";
        String pathwaysFile = "pathwaysFile";
        String fastaFile = "fastaFile";
        String matchType = "matchType";
        String peptideGrouping = "peptideGrouping";
    }

    public interface IntVars {

        String maxNumProt = "maxNumProt";
        String siteRange = "siteRange";
        String rsidIndex = "rsidColumnIndex";     // Column indexes in the VEP tables
        String swissprotIndex = "swissprotColumnIndex";
        String nearestGeneIndex = "nearestGeneIndex";
        String ensemblIndex = "ensemblIndex";
        
        String percentageStep = "percentageStep";
    }

    public interface BoolVars {

        String ignoreMisformatedRows = "ignoreMisformatedRows";
        String verbose = "verbose";
        String inputHasPTMs = "inputHasPTMs";
        String showTopLevelPathways = "showTopLevelPathways";
    }

    public static boolean contains(String name) {

        if (strMap.containsKey(name)) {
            return true;
        }
        if (boolMap.containsKey(name)) {
            return true;
        }
        if (intMap.containsKey(name)) {
            return true;
        }

        return false;
    }

    public static void setValue(String name, String value) {
        if (strMap.containsKey(name)) {
            strMap.put(name, value);
        }
        if (boolMap.containsKey(name)) {
            boolMap.put(name, Boolean.valueOf(value));
        }
        if (intMap.containsKey(name)) {
            intMap.put(name, Integer.valueOf(value));
        }
    }

    public static void setValue(String name, Boolean value) {
        if (boolMap.containsKey(name)) {
            boolMap.put(name, Boolean.valueOf(value));
        }
    }
    
    public static String get(String name) {
        if (strMap.containsKey(name)) {
            return strMap.get(name);
        }
        return "";
    }
    
    public static void setDefaultValues() {
        intMap = new HashMap<String, Integer>();
        boolMap = new HashMap<String, Boolean>();
        strMap = new HashMap<String, String>();

        // Command line options
        strMap.put(StrVars.standardFilePath, "./standardFile.txt");
        strMap.put(StrVars.input, "./input.txt");
        strMap.put(StrVars.output, "./output.txt");
        strMap.put(StrVars.conf, "./Config.txt");
        strMap.put(StrVars.inputType, InputType.unknown);
        strMap.put(StrVars.vepTablesPath, "./vep/");
        strMap.put(StrVars.fastaFile, "./uniprot-all.fasta");
        boolMap.put(BoolVars.ignoreMisformatedRows, Boolean.FALSE);
        boolMap.put(BoolVars.showTopLevelPathways, Boolean.FALSE);
        
        
        // Extra configuration options (not published)
        strMap.put(StrVars.vepTableName, "XX.gz");
        boolMap.put(BoolVars.inputHasPTMs, Boolean.FALSE);
        strMap.put(StrVars.outputType, OutputTypeEnum.fullTable);
        intMap.put(IntVars.maxNumProt, 21000);
        boolMap.put(BoolVars.verbose, Boolean.TRUE);
        strMap.put(StrVars.matchType, MatchType.atLeastOneSite.toString());
        strMap.put(StrVars.peptideGrouping, PeptidePTMGrouping.none.toString());
        
        intMap.put(IntVars.rsidIndex, 2);
        intMap.put(IntVars.ensemblIndex, 4);
        intMap.put(IntVars.swissprotIndex, 5);
        intMap.put(IntVars.nearestGeneIndex, 7);
        
        intMap.put(IntVars.percentageStep, 5);
        
        strMap.put(StrVars.reactionsFile, "");
        strMap.put(StrVars.pathwaysFile, "");
        boolMap.put(BoolVars.ignoreMisformatedRows, Boolean.FALSE);
        
        boolMap.put(BoolVars.showTopLevelPathways, Boolean.FALSE);
        
        intMap.put(IntVars.siteRange, 0);

        //Database access
        strMap.put(StrVars.host, "bolt://localhost");
        strMap.put(StrVars.username, "");
        strMap.put(StrVars.password, "");
    }

    // public static String input = "./src/main/resources/csv/listBjorn.csv";
    public enum ProteinType {
        ewas, uniprot
    }

    public interface InputType {

        String maxQuantMatrix = "maxQuantMatrix";
        String peptideList = "peptideList";
        String peptideListAndSites = "peptideListAndSites";
        String peptideListAndModSites = "peptideListAndModSites";
        String uniprotList = "uniprotList";
        String uniprotListAndSites = "uniprotListAndSites";
        String uniprotListAndModSites = "uniprotListAndModSites";
        String rsid = "rsid";
        String rsidList = "rsidList";
        String ensemblList = "ensemblList";
        String geneList = "geneList";
        String unknown = "unknown";
    }

    public enum InputTypeEnum {
        maxQuantMatrix,
        peptideList,
        peptideListAndSites,
        peptideListAndModSites,
        uniprotList,
        uniprotListAndSites,
        uniprotListAndModSites,
        rsid,
        rsidList,
        ensemblList,
        geneList
    }

    public interface OutputTypeEnum {

        String reactionsList = "reactionsList";
        String pathwaysList = "pathwaysList";
        String fullTable = "fullTable";
    }

    public interface InputPatterns {

        String maxQuantMatrix = "Protein";
        String peptideList = "^[ARNDBCEQZGHILKMFPSTWYV]+$";
        String peptideListAndSites = "^[ARNDBCEQZGHILKMFPSTWYV]+(,\\d*)?(;\\d*)*$";
        String peptideListAndModSites = "^[ARNDBCEQZGHILKMFPSTWYV]+(,\\d{5}:\\d*)?(;\\d{5}:\\d*)*?$";
        String uniprotList = "^\\p{Upper}\\p{Alnum}{5}$";
        String uniprotListAndSites = "^\\p{Upper}\\p{Alnum}{5}(,\\d*)*(;\\d*)*$";
        String uniprotListAndModSites = "^\\p{Upper}\\p{Alnum}{5}(,\\d{5}:\\d*)?(;\\d{5}:\\d*)*$";
        String rsid = "^rs\\d*$";
        String unknown = "";
        String ensemblList = "^(\\p{Upper}{3,7})?\\d{1,11}$";
        String geneList = "^[-.\\p{Alnum}]*$";
    }
    
    public enum MatchType {
        atLeastOneSite,
        all
    }
    
    public enum PeptidePTMGrouping{
        none,
        byProtein
    }
}
