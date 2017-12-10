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
 * @author Luis Francisco Hernández Sánchez
 */
public class Conf {

    public static Options options;
    public static CommandLine commandLine;

    protected static final String LOG_FILE = "PathwayMatcher.log";
    protected static final Boolean APPEND_LOG = true;
    protected static Level LEVEL = Level.ALL;

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

    public interface IntVars {

        String maxNumProt = "maxNumProt";
        String margin = "margin";
        String rsidIndex = "rsidColumnIndex";     // Column indexes in the VEP tables
        String swissprotIndex = "swissprotColumnIndex";
        String nearestGeneIndex = "nearestGeneIndex";
        String ensemblIndex = "ensemblIndex";

        String percentageStep = "percentageStep";
    }

    public interface BoolVars {

        String verbose = "verbose";
        String inputHasPTMs = "inputHasPTMs";
        String showTopLevelPathways = "showTopLevelPathways";
        String useSubsequenceRanges = "useSubsequenceRanges";
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
        strMap.put(StrVars.outputType, OutputTypeEnum.fullTable);
        intMap.put(IntVars.maxNumProt, 21000);
        boolMap.put(BoolVars.verbose, Boolean.TRUE);
        strMap.put(StrVars.matchingType, MatchType.FLEXIBLE.toString());
        strMap.put(StrVars.peptideGrouping, PeptidePTMGrouping.none.toString());

        intMap.put(IntVars.rsidIndex, 2);
        intMap.put(IntVars.ensemblIndex, 4);
        intMap.put(IntVars.swissprotIndex, 5);
        intMap.put(IntVars.nearestGeneIndex, 7);

        intMap.put(IntVars.percentageStep, 5);
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

    // public static String input = "./src/main/resources/csv/listBjorn.csv";
    public enum ProteinType {
        ewas, uniprot
    }

    public interface InputType {

        String peptideList = "peptideList";
        String peptideListAndModSites = "peptideListAndModSites";
        String uniprotList = "uniprotList";
        String uniprotListAndModSites = "uniprotListAndModSites";
        String rsid = "rsid";
        String vcf = "vcf";
        String rsidList = "rsidList";
        String ensemblList = "ensemblList";
        String geneList = "geneList";
        String unknown = "unknown";
    }

    public enum InputTypeEnum {
        peptideList,
        peptideListAndModSites,
        uniprotList,
        uniprotListAndModSites,
        rsid,
        vcf,
        rsidList,
        ensemblList,
        geneList
    }

    public static enum ProteoformFormat {
        NONE,
        UNKNOWN,
        SIMPLE,
        PRO,
        PIR_ID,
        GPMDB,
        NEO4J
    }

    public interface OutputTypeEnum {

        String reactionsList = "reactionsList";
        String pathwaysList = "pathwaysList";
        String fullTable = "fullTable";
    }

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

    /*
    Reads all the configuration file and sets the values of the variables encountered. If the variable was defined in the
    command line as argument then it skips it.
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
