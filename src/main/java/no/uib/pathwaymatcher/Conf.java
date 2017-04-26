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
        strMap.put(strVars.configPath.toString(), "./Config.txt");
        strMap.put(strVars.standardFilePath.toString(), "./standardFile.txt");
        strMap.put(strVars.inputPath.toString(), "./input.txt");
        strMap.put(strVars.inputType.toString(), InputTypeEnum.uniprotList.toString());

        strMap.put(strVars.outputPath.toString(), "./output.txt");
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
    
    public enum InputPatterns{
        maxQuantMatrix{
            public String toString() {
                return "Protein";
            }
        },
        peptideList{
            public String toString() {
                return "^[ARNDBCEQZGHILKMFPSTWYV]+$";
            }
        },
        peptideListAndSites{
            public String toString() {
                return "^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d+;)*\\d*$";
            }
        },
        peptideListAndModSites{
            public String toString() {
                return "^[ARNDBCEQZGHILKMFPSTWYV]+,(\\d{5}:\\d+;)*(\\d{5}:\\d+)?$";
            }
        },
        uniprotList{
            public String toString() {
                return "^\\p{Upper}\\p{Alnum}{5}$";
            }
        },
        uniprotListAndSites{
            public String toString() {
                return "^\\p{Upper}\\p{Alnum}{5},(\\d+;)*\\d*$";
            }
        },
        uniprotListAndModSites{
            public String toString() {
                return "\"^\\p{Upper}\\p{Alnum}{5},(\\\\d{5}:\\\\d+;)*\\\\d{5}:\\\\d*$\"";
            }
        },
        unknown{
            public String toString() {
                return "";
            }
        }
    }
}
