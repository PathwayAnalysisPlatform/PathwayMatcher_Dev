package no.uib.pathwaymatcher;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.Analysis.Analyser;
import no.uib.pathwaymatcher.Analysis.AnalyserFactory;
import no.uib.pathwaymatcher.Conf.*;
import no.uib.pathwaymatcher.Matching.Matcher;
import no.uib.pathwaymatcher.Matching.MatcherFactory;
import no.uib.pathwaymatcher.Preprocessing.Preprocessor;
import no.uib.pathwaymatcher.Preprocessing.PreprocessorFactory;
import no.uib.pathwaymatcher.Search.Finder;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.stages.Reporter;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.Conf.*;
import static no.uib.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static no.uib.pathwaymatcher.model.Error.*;
import static no.uib.pathwaymatcher.util.FileUtils.getInput;

/**
 * // PREPROCESS: Verify consistency and standarize
 * Convert peptides, proteins, gene names, gene variants to a set of proteoforms
 * <p>
 * // MATCH: Input to Reference entitites
 * Get a mapping from the input proteoforms to EntityWithAccessionedSequence stIds
 * <p>
 * // SEARCH:
 * Find all Reactions/Pathways that have the selected EWASes as participants
 * <p>
 * // ANALYSE:
 * Do maths and statistics to score pathways according to their significance.
 * Statistics on the matching partners of the proteins
 * <p>
 * // REPORT:
 * Write search result file
 * Write analysis result file
 *
 * @author Luis Francisco Hernández Sánchez
 * @author Marc Vaudel
 */
public class PathwayMatcher {

    public final static Logger logger = Logger.getLogger(PathwayMatcher.class.getName());

    public static void main(String args[]) {

        initializeLog();

        Set<Proteoform> entities = new HashSet<>();
        Preprocessor preprocessor;
        Matcher matcher;

        Conf.setDefaultValues();

        // If there are no arguments and there is no configuration file in the same directory

        if (args.length == 0) {
            File file = new File(strMap.get(StrVars.conf));
            if (!file.exists() && !file.isDirectory()) {
                sendError(NO_ARGUMENTS);
            }
        }

        // Read and set configuration values
        options = new Options();

        addOption("t", StrVars.inputType, true, "Type of input file (" + InputType.peptideList + ", " + InputType.snpList + ", " + InputType.proteoforms + ",...etc.)", true);
        addOption("r", IntVars.margin, true, "Allowed distance for PTM sites", false);
        addOption("tlp", BoolVars.showTopLevelPathways, false, "Set this flag to show the \"Top Level Pathways\" column in the output file.", false);
        addOption("mt", StrVars.matchingType.toString(), false, "Type of criteria used to decide if two proteoforms are equivalent.", false);

        addOption("f", StrVars.fastaFile, true, "Path and name of the FASTA file with the possible protein sequences to search the peptides.", false);
        addOption("i", StrVars.input, true, "input file path", false);
        addOption("o", StrVars.output, true, "output file path", false);
        addOption("c", StrVars.conf, true, "config file path and name", false);
        addOption("h", StrVars.host, true, "Url of the Neo4j database with Reactome", false);
        addOption("u", StrVars.username, true, "Username to access the database with Reactome", false);
        addOption("p", StrVars.password, true, "Password related to the username provided to access the database with Reactome", false);
        addOption("vep", StrVars.vepTablesPath, true, "The path of the folder containing the vep mapping tables. If the type of input is \"snpList\" then the parameter is required. It is not required otherwise.", false);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        }

        //Set all command line arguments provided
        for (Option option : commandLine.getOptions()) {
            if(option.hasArg()) {
                Conf.setValue(option.getLongOpt(), commandLine.getOptionValue(option.getOpt()));
            }
            else{
                Conf.setValue(option.getLongOpt(), Boolean.TRUE);
            }
        }

        try {
            readConfigurationFromFile();
        } catch (IOException ex) {
            if(commandLine.hasOption(StrVars.conf)){
                sendError(COULD_NOT_READ_CONF_FILE);
            }
        }

        initializeNeo4j(strMap.get(StrVars.host), strMap.get(StrVars.username), strMap.get(StrVars.password));

        preprocessor = PreprocessorFactory.getPreprocessor(strMap.get(StrVars.inputType));

        try {
            entities = preprocessor.process(getInput(strMap.get(StrVars.input)));
        } catch (java.text.ParseException e) {
            sendError(INPUT_PARSING_ERROR);
        }
        logger.log(Level.INFO, "Preprocessing complete.");

        logger.log(Level.INFO, "\nMatching input entities...");
        matcher = MatcherFactory.getMatcher(strMap.get(StrVars.inputType), strMap.get(StrVars.matchingType));
        TreeMultimap<Proteoform, String> mapping = matcher.match(entities);
        logger.log(Level.INFO, "Matching complete.");

        logger.log(Level.INFO, "\nFiltering pathways and reactions....");
        TreeMultimap<Proteoform, Reaction> result = Finder.search(mapping);
        logger.log(Level.INFO, "Filtering pathways and reactions complete.");
        Reporter.reportSearchResults(result);

        Analyser analyser = AnalyserFactory.getAnalyser(strMap.get(StrVars.inputType));
        analyser.analyse(result);
        Reporter.reportPathwayStatistics();

        logger.log(Level.INFO, "\nProcess complete.");

    }

    private static void addOption(String opt, String longOpt, boolean hasArg, String description, boolean required) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        options.addOption(option);
    }
}
