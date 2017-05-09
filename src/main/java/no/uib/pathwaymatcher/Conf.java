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
    }
    
    public interface IntVars {
        String maxNumProt = "";
    }

    public interface BoolVars {
        String ignoreMisformatedRows = "ignoreMisformatedRows";
        String verbose = "verbose";
        String reactionsFile = "reactionsFile";
        String pathwaysFile = "pathwaysFile";
        String inputHasPTMs = "inputHasPTMs";
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

    public static void setDefaultValues() {
        intMap = new HashMap<String, Integer>();
        boolMap = new HashMap<String, Boolean>();
        strMap = new HashMap<String, String>();

        // Set general configuration
        strMap.put(StrVars.conf, "./Config.txt");
        strMap.put(StrVars.standardFilePath, "./standardFile.txt");
        strMap.put(StrVars.input, "./input.txt");
        strMap.put(StrVars.inputType, InputType.unknown);
        boolMap.put(BoolVars.inputHasPTMs, Boolean.FALSE);
        strMap.put(StrVars.vepTablesPath, ".");
        strMap.put(StrVars.vepTableName, "chrXX_processed.txt.gz");

        strMap.put(StrVars.output, "./output.txt");
        strMap.put(StrVars.outputType, OutputTypeEnum.fullTable);

        boolMap.put(BoolVars.verbose, Boolean.TRUE);
        boolMap.put(BoolVars.reactionsFile, Boolean.FALSE);
        boolMap.put(BoolVars.pathwaysFile, Boolean.FALSE);
        boolMap.put(BoolVars.ignoreMisformatedRows, Boolean.FALSE);
        intMap.put(IntVars.maxNumProt, 21000);

        //Database access
        strMap.put(StrVars.host, "bolt://localhost");
        strMap.put(StrVars.username, "neo4j");
        strMap.put(StrVars.password, "neo4j2");
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
        String unknown = "unknown";
    }

    public interface OutputTypeEnum {
        String reactionsList = "reactionsList";
        String pathwaysList = "pathwaysList";
        String fullTable = "fullTable";
    }

    public interface InputPatterns {

        String maxQuantMatrix = "Protein";
        String peptideList = "^[ARNDBCEQZGHILKMFPSTWYV]+$";
        String peptideListAndSites = "^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d+;)*\\d*$";
        String peptideListAndModSites = "^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d{5}:\\d+;)*(\\d{5}:\\d+)?$";
        String uniprotList = "^\\p{Upper}\\p{Alnum}{5}$";
        String uniprotListAndSites = "^\\p{Upper}\\p{Alnum}{5},(\\d+;)*\\d*$";
        String uniprotListAndModSites = "^\\p{Upper}\\p{Alnum}{5},(\\\\d{5}:\\\\d+;)*\\\\d{5}:\\\\d*$";
        String rsid = "^rs\\d*$";
        String unknown = "";
    }
}
