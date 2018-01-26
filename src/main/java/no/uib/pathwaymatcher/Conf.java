package no.uib.pathwaymatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import no.uib.pathwaymatcher.model.Pathway;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.sendError;
import static no.uib.pathwaymatcher.model.Warning.COULD_NOT_CREATE_LOG_FILE;
import static no.uib.pathwaymatcher.model.Warning.sendWarning;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Configuration_Variable;

/**
 * Holds the configuration values for the PathwayMatcher application.
 * <p>Gets and sets the default values and the ones specified by the user.
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Conf {

    /**
     * The object to hold the command line arguments for PathwayMatcher.
     */
    public static Options options;
    public static CommandLine commandLine;

    /**
     * The path and name of the file for the log output.
     */
    protected static final String LOG_FILE = "PathwayMatcher.log";

    /**
     * If the log file should be erased before each run.
     */
    protected static final Boolean APPEND_LOG = true;

    /**
     * The level of the messages shown by the log.
     */
    protected static Level LEVEL = Level.ALL;

    /**
     * Contains the mapping of string configuration variables to their values.
     */
    public static HashMap<String, String> strMap;

    /**
     * Contains the mapping of boolean configuration variables to their values.
     */
    public static HashMap<String, Boolean> boolMap;

    /**
     * Contains the mapping of integer configuration variables to their values.
     */
    public static HashMap<String, Integer> intMap;

    /**
     * Specifies the available string configuration variables and their names.
     */
    public interface StrVars {

        String conf = "conf";
        String input = "input";
        String inputType = "inputType";
        String outputType = "outputType";
        String output = "output";
        String pathwayStatistics = "statisticsFile";
        String host = "host";
        String username = "username";
        String password = "password";
        String standardFilePath = "standardFilePath";
        String vepTablesPath = "vepTablesPath";
        String vepTableName = "vepTableName";
        String fastaFile = "fastaFile";
        String peptideGrouping = "peptideGrouping";
        String colSep = "colSep";
        String ptmColSep = "ptmColSep";
        String matchingType = "matchingType";
    }

    /**
     * Specifies the available integer configuration variables and their names.
     */
    public interface IntVars {

        String maxNumProt = "maxNumProt";
        String margin = "margin";
        String rsidIndex = "rsidColumnIndex";     // Column indexes in the VEP tables
        String swissprotIndex = "swissprotColumnIndex";
        String nearestGeneIndex = "nearestGeneIndex";
        String ensemblIndex = "ensemblIndex";

        String percentageStep = "percentageStep";
    }

    /**
     * Specifies the available boolean configuration variables and their names.
     */
    public interface BoolVars {

        String verbose = "verbose";
        String inputHasPTMs = "inputHasPTMs";
        String showTopLevelPathways = "showTopLevelPathways";
        String useSubsequenceRanges = "useSubsequenceRanges";
    }

    /**
     * Verifies if a configuration variable with a specific name is available.
     * @param name The name of the variable
     * @return
     */
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

    /**
     * Sets the value of a configuration variable of types string, integer or boolean.
     * @param name The name of the variable
     * @param value The value for the variable
     */
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

    public static void setValue(String name, int value) {
        if (intMap.containsKey(name)) {
            intMap.put(name, value);
        }
    }

    public static String get(String name) {
        if (strMap.containsKey(name)) {
            return strMap.get(name);
        }
        return "";
    }

    /**
     * Sets default values for all the configuration variables.
     */
    public static void setDefaultValues() {
        intMap = new HashMap<String, Integer>();
        boolMap = new HashMap<String, Boolean>();
        strMap = new HashMap<String, String>();

        // Command line options
        strMap.put(StrVars.input, "./input.txt");
        strMap.put(StrVars.output, "output.txt");
        strMap.put(StrVars.pathwayStatistics, "pathwayStatistics.csv");
        strMap.put(StrVars.conf, "./Config.txt");
        strMap.put(StrVars.inputType, InputType.unknown);
        strMap.put(StrVars.vepTablesPath, "vep/");
        strMap.put(StrVars.fastaFile, "");
        boolMap.put(BoolVars.showTopLevelPathways, Boolean.FALSE);


        // Extra configuration options (not published)
        strMap.put(StrVars.vepTableName, "simpleXX.gz");
        boolMap.put(BoolVars.inputHasPTMs, Boolean.FALSE);
        intMap.put(IntVars.maxNumProt, 21000);
        boolMap.put(BoolVars.verbose, Boolean.TRUE);
        strMap.put(StrVars.matchingType, MatchType.FLEXIBLE.toString());
        strMap.put(StrVars.peptideGrouping, PeptidePTMGrouping.none.toString());

        intMap.put(IntVars.rsidIndex, 2);
        intMap.put(IntVars.ensemblIndex, 4);
        intMap.put(IntVars.swissprotIndex, 5);
        intMap.put(IntVars.nearestGeneIndex, 7);

        intMap.put(IntVars.percentageStep, 1);
        boolMap.put(BoolVars.showTopLevelPathways, Boolean.FALSE);

        intMap.put(IntVars.margin, 3);
        strMap.put(StrVars.colSep, "|");
        strMap.put(StrVars.ptmColSep, ";");

        //Database access
        strMap.put(StrVars.host, "bolt://127.0.0.1:7687");
        strMap.put(StrVars.username, "");
        strMap.put(StrVars.password, "");

        // Extras
        boolMap.put(BoolVars.useSubsequenceRanges, Boolean.FALSE);

        PathwayMatcher.logger.setLevel(Level.ALL);
    }

    /**
     * Specifies the possible input types for PathwayMatcher
     */
    public interface InputType {

        String peptideList = "peptideList";
        String peptideListAndModSites = "peptideListAndModSites";
        String uniprotList = "uniprotList";
        String proteoforms = "proteoforms";
        String rsid = "rsid";
        String vcf = "vcf";
        String snpList = "snpList";
        String ensemblList = "ensemblList";
        String geneList = "geneList";
        String unknown = "unknown";
    }

    /**
     * Specifies the possible input types for PathwayMatcher as an enum
     */
    public enum InputTypeEnum {
        peptideList,
        peptideListAndModSites,
        uniprotList,
        proteoforms,
        rsid,
        vcf,
        rsidList,
        ensemblList,
        geneList
    }

    /**
     * Specifies the possible proteoform formats
     */
    public static enum ProteoformFormat {
        NONE,
        UNKNOWN,
        SIMPLE,
        PRO,
        PIR_ID,
        GPMDB,
        NEO4J
    }

    /**
     * Specifies the possible matching types
     */
    public enum MatchType {
        STRICT,
        FLEXIBLE,
        ONE
    }

    public enum PeptidePTMGrouping {
        none,
        byProtein
    }

    public static boolean isValidInputType(String type) {

        for (InputTypeEnum c : InputTypeEnum.values()) {
            if (c.name().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidMatchingType(String type) {

        for (MatchType t : MatchType.values()) {
            if (t.name().equals(type.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize logger for all the PathwayMatcher
     */
    protected static void initializeLog() {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, APPEND_LOG);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(LEVEL);
        } catch (IOException e) {
            sendWarning(COULD_NOT_CREATE_LOG_FILE);
        }
    }

    /**
     *
     * Reads all the configuration file and sets the values of the variables encountered. If the variable was defined in the
     * command line as argument then it skips it.
     */
    protected static void readConfigurationFromFile() throws IOException {

        try {
            //Read and set configuration values from file
            LineIterator it = FileUtils.lineIterator(new File(strMap.get(StrVars.conf)), "UTF-8");

            //For every valid variable found in the config.txt file, the variable value gets updated
            String line;
            while (it.hasNext()) {
                line = it.nextLine().trim();

                if (matches_Configuration_Variable(line)) {
                    String[] parts = line.split("=");
                    String name = parts[0];
                    String value = parts[1];
                    if (Conf.contains(name)) {
                        if (!commandLine.hasOption(name)) {
                            Conf.setValue(name, value);
                        }
                    }
                }
            }

            LineIterator.closeQuietly(it);
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
