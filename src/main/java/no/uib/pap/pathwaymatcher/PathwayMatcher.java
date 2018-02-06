package no.uib.pap.pathwaymatcher;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.model.InputType;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.Analysis.Analyser;
import no.uib.pap.pathwaymatcher.Analysis.AnalyserFactory;
import no.uib.pap.pathwaymatcher.Conf.*;
import no.uib.pap.pathwaymatcher.Matching.Matcher;
import no.uib.pap.pathwaymatcher.Matching.MatcherFactory;
import no.uib.pap.pathwaymatcher.Preprocessing.Preprocessor;
import no.uib.pap.pathwaymatcher.Preprocessing.PreprocessorFactory;
import no.uib.pap.pathwaymatcher.Search.Finder;
import no.uib.pap.pathwaymatcher.stages.Reporter;
import no.uib.pap.pathwaymatcher.tools.PathwayStaticFactory;
import no.uib.pap.pathwaymatcher.tools.ReactionStaticFactory;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pap.model.Error.*;
import static no.uib.pap.pathwaymatcher.Conf.*;
import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static no.uib.pap.pathwaymatcher.util.FileUtils.getInput;

/**
 *
 * Gets the reactions and pathways related to the list of genetic variants, genes, peptides, proteins or proteoforms.
 * Start point class for the PathwayMatcher command line application.
 * <p><ul>
 *     <li>PREPROCESS: Verifies format and translate to proteoforms
 *     <li>MATCH: Decides which input proteoforms correspond to which EntityWithAccessionedSequence stIds in the database
 *     <li>SEARCH:Find Reactions/Pathways that have the selected EWASes as participants
 *     <li>ANALYSE: Calculate p-Values for each of the mapped pathways.
 *     <li>REPORT: Write the results to a file.
 * </ul><p>
 *
 * @author Luis Francisco Hernández Sánchez
 * @author Marc Vaudel
 */
public class PathwayMatcher {

    public final static Logger logger = Logger.getLogger(PathwayMatcher.class.getName());

    /**
     * Set of proteoforms equivalent to the entities in the input
     */
    public static Set<Proteoform> entities;

    /**
     * Instance of the {@link Preprocessor} to check the consistency of the file and convert any type of input to proteoforms.
     */
    public static Preprocessor preprocessor;

    /**
     * Instance of the {@link Matcher} to connect proteoforms in the input to EntityWithAccessionSequences in the database.
     */
    public static Matcher matcher;

    /**
     * Sets the starting point for all PathwayMatcher.
     * @param args
     */
    public static void main(String args[]) {

        initializeLog();
        Conf.setDefaultValues();

        // If there are no arguments and there is no configuration file in the same directory

        /*if (args.length == 0) {
            File file = new File(strMap.get(StrVars.conf));
            if (!file.exists() && !file.isDirectory()) {
                sendError(NO_ARGUMENTS);
            }
        }*/

        // Read and set configuration values
        options = new Options();

        addOption("t", StrVars.inputType, true, "Type of input file (" + InputType.PEPTIDELIST + ", " + InputType.RSIDLIST + ", " + InputType.PROTEOFORMS + ",...etc.)", true);
        addOption("r", IntVars.margin, true, "Allowed distance for PTM sites", false);
        addOption("tlp", BoolVars.showTopLevelPathways, false, "Set this flag to show the \"Top Level Pathways\" column in the output file.", false);
        addOption("mt", StrVars.matchingType.toString(), false, "Type of criteria used to decide if two proteoforms are equivalent.", false);

        addOption("f", StrVars.fastaFile, true, "Path and name of the FASTA file with the possible protein sequences to search the peptides.", false);
        addOption("i", StrVars.input, true, "input file path", true);
        addOption("o", StrVars.output, true, "output file path", false);
        addOption("c", StrVars.conf, true, "config file path and name", false);
        addOption("h", StrVars.host, true, "Url of the Neo4j database with Reactome", false);
        addOption("u", StrVars.username, true, "Username to access the database with Reactome", false);
        addOption("p", StrVars.password, true, "Password related to the username provided to access the database with Reactome", false);
        addOption("vep", StrVars.vepTablesPath, true, "The path of the folder containing the vep mapping tables. If the type of input is \"rsidList\" then the parameter is required. It is not required otherwise.", false);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
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
                sendError(no.uib.pap.model.Error.COULD_NOT_READ_CONF_FILE);
            }
        }

        initializeNeo4j(strMap.get(StrVars.host), strMap.get(StrVars.username), strMap.get(StrVars.password));
        PathwayStaticFactory.initialize();
        ReactionStaticFactory.initialize();

        preprocessor = PreprocessorFactory.getPreprocessor(strMap.get(StrVars.inputType));

        try {
            entities = preprocessor.process(getInput(strMap.get(StrVars.input)));
        } catch (java.text.ParseException e) {
            sendError(no.uib.pap.model.Error.INPUT_PARSING_ERROR);
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

    /**
     * Adds a new command line option for the program.
     *
     * @param opt Short name
     * @param longOpt Long name
     * @param hasArg    If requires a value argument
     * @param description   Short text to explain the functionality of the option
     * @param required  If the user has to specify this option each time the program is run
     */
    private static void addOption(String opt, String longOpt, boolean hasArg, String description, boolean required) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        options.addOption(option);
    }
}
