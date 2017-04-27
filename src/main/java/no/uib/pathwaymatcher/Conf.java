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

    public enum strVars {
        conf, input, inputType, outputType, output, host, username, password, standardFilePath
    }

    public enum intVars {
        maxNumProt
    }

    public enum boolVars {
        ignoreMisformatedRows, verbose, reactionsFile, pathwaysFile, inputHasPTMs
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

    public static void setValue(strVars var, String value) {
        if (strMap.containsKey(var.toString())) {
            strMap.put(var.toString(), value);
        }
    }

    public static void setValue(boolVars var, boolean value) {
        if (boolMap.containsKey(var.toString())) {
            boolMap.put(var.toString(), value);
        }
    }

    public static void setValue(intVars var, int value) {
        if (intMap.containsKey(var.toString())) {
            intMap.put(var.toString(), value);
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
        strMap.put(strVars.conf.toString(), "./Config.txt");
        strMap.put(strVars.standardFilePath.toString(), "./standardFile.txt");
        strMap.put(strVars.input.toString(), "./input.txt");
        strMap.put(strVars.inputType.toString(), InputType.unknown.toString());
        boolMap.put(boolVars.inputHasPTMs.toString(), Boolean.FALSE);

        strMap.put(strVars.output.toString(), "./output.txt");
        strMap.put(strVars.outputType.toString(), OutputTypeEnum.fullTable.toString());

        boolMap.put(boolVars.verbose.toString(), Boolean.TRUE);
        boolMap.put(boolVars.reactionsFile.toString(), Boolean.FALSE);
        boolMap.put(boolVars.pathwaysFile.toString(), Boolean.FALSE);
        boolMap.put(boolVars.ignoreMisformatedRows.toString(), Boolean.FALSE);
        intMap.put(intVars.maxNumProt.toString(), 21000);

        //Database access
        strMap.put(strVars.host.toString(), "bolt://localhost");
        strMap.put(strVars.username.toString(), "neo4j");
        strMap.put(strVars.password.toString(), "neo4j2");
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
        String snpList = "snpList";
        String unknown = "unknown";
    }

    public enum OutputTypeEnum {
        reactionsList,
        pathwaysList,
        fullTable
    }

    public interface InputPatterns {

        String maxQuantMatrix = "Protein";
        String peptideList = "^[ARNDBCEQZGHILKMFPSTWYV]+$";
        String peptideListAndSites = "^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d+;)*\\d*$";
        String peptideListAndModSites = "^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d{5}:\\d+;)*(\\d{5}:\\d+)?$";
        String uniprotList = "^\\p{Upper}\\p{Alnum}{5}$";
        String uniprotListAndSites = "^\\p{Upper}\\p{Alnum}{5},(\\d+;)*\\d*$";
        String uniprotListAndModSites = "\"^\\p{Upper}\\p{Alnum}{5},(\\\\d{5}:\\\\d+;)*\\\\d{5}:\\\\d*$\"";
        String snpList = "\"^rsid\\d*$\"";
        String unknown = "";
    }
}
