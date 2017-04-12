package no.uib.PathwayMatcher;

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
        configPath, inputPath, inputType, outputType, outputPath, host, username, password, standardFilePath
    }

    public enum intVars {
        maxNumProt
    }

    public enum boolVars {
        ignoreMisformatedRows, verbose, reactionsFile, pathwaysFile
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
        strMap.put(Conf.strVars.configPath.toString(), "./Config.txt");
        strMap.put(Conf.strVars.standardFilePath.toString(), "./standardFile.txt");
        strMap.put(Conf.strVars.inputPath.toString(), "./input.txt");
        strMap.put(Conf.strVars.inputType.toString(), InputTypeEnum.uniprotList.toString());

        strMap.put(Conf.strVars.outputPath.toString(), "./output.txt");
        strMap.put(Conf.strVars.outputType.toString(), OutputTypeEnum.fullTable.toString());

        boolMap.put(Conf.boolVars.verbose.toString(), Boolean.TRUE);
        boolMap.put(Conf.boolVars.reactionsFile.toString(), Boolean.FALSE);
        boolMap.put(Conf.boolVars.pathwaysFile.toString(), Boolean.FALSE);
        boolMap.put(Conf.boolVars.ignoreMisformatedRows.toString(), Boolean.FALSE);
        intMap.put(Conf.intVars.maxNumProt.toString(), 21000);

        //Database access
        strMap.put(Conf.strVars.host.toString(), "bolt://localhost");
        strMap.put(Conf.strVars.username.toString(), "neo4j");
        strMap.put(Conf.strVars.password.toString(), "neo4j2");
    }

    // public static String inputPath = "./src/main/resources/csv/listBjorn.csv";
    public enum ProteinType {
        ewas, uniprot
    }

    public enum InputTypeEnum {
        maxQuantMatrix,
        peptideList,
        peptideListAndSites,
        peptideListAndModSites,
        uniprotList,
        uniprotListAndSites,
        uniprotListAndModSites,
        unknown
    }

    public enum OutputTypeEnum {
        reactionsList,
        pathwaysList,
        fullTable
    }
}
