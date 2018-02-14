package no.uib.pap.pathwaymatcher;

import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.pathwaymatcher.Conf.commandLine;
import static no.uib.pap.pathwaymatcher.Conf.initializeLog;
import static no.uib.pap.pathwaymatcher.Conf.options;
import static no.uib.pap.pathwaymatcher.Conf.readConfigurationFromFile;
import static no.uib.pap.pathwaymatcher.Conf.strMap;
import static no.uib.pap.pathwaymatcher.util.FileUtils.getInput;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.Conf.BoolVars;
import no.uib.pap.pathwaymatcher.Conf.StrVars;
import no.uib.pap.pathwaymatcher.Matching.Matcher;
import no.uib.pap.pathwaymatcher.Preprocessing.Preprocessor;
import no.uib.pap.pathwaymatcher.tools.PathwayStaticFactory;
import no.uib.pap.pathwaymatcher.tools.ReactionStaticFactory;

/**
 *
 * Gets the reactions and pathways related to the list of genetic variants, genes, peptides, proteins or proteoforms.
 * Start point class for the PathwayMatcher command line application.
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


    }
    
}
