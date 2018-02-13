package no.uib.pap.pathwaymatcher;

import static no.uib.pap.model.Error.INPUT_PARSING_ERROR;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.pathwaymatcher.Conf.initializeLog;
import static no.uib.pap.pathwaymatcher.Conf.setValue;
import static no.uib.pap.pathwaymatcher.Conf.strMap;
import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.Analysis.Analyser;
import no.uib.pap.pathwaymatcher.Analysis.AnalyserFactory;
import no.uib.pap.pathwaymatcher.Matching.Matcher;
import no.uib.pap.pathwaymatcher.Matching.MatcherFactory;
import no.uib.pap.pathwaymatcher.Preprocessing.Preprocessor;
import no.uib.pap.pathwaymatcher.Preprocessing.PreprocessorFactory;
import no.uib.pap.pathwaymatcher.Search.Finder;
import no.uib.pap.pathwaymatcher.stages.Reporter;

/**
 * Allows the execution of PathwayMatcher as with a limited number of parameters for the REST service.
 */
public class PathwayMatcherService {

    public final static Logger logger = Logger.getLogger(PathwayMatcher.class.getName());

    /**
     * Gets the reactions and pathways for a list of indentifiers in the input.
     * @param input The list of identifiers
     * @param type  The type of the objects in the input
     * @param margin The margin of error for the modification sites.
     * @param showTopLevelPathways If top level pathways should also be shown in the output
     * @param matchingType The matching criteria to decide a proteoform in the input is a database proteoform
     * @return
     */
    public List<String> match(List<String> input, String type, int margin, Boolean showTopLevelPathways, String matchingType) {
        Set<Proteoform> entities = new HashSet<>();
        Preprocessor preprocessor;
        Matcher matcher;

        Conf.setDefaultValues();
        setValue("margin", margin);
        setValue(Conf.BoolVars.showTopLevelPathways, showTopLevelPathways);
        setValue(Conf.StrVars.matchingType, matchingType);
        initializeNeo4j(strMap.get(Conf.StrVars.host), strMap.get(Conf.StrVars.username), strMap.get(Conf.StrVars.password));
        initializeLog();

        preprocessor = PreprocessorFactory.getPreprocessor(type);

        try {
            entities = preprocessor.process(input);
        } catch (java.text.ParseException e) {
            sendError(INPUT_PARSING_ERROR);
        }

        logger.log(Level.INFO, "Preprocessing complete.");

        logger.log(Level.INFO, "\nMatching input entities...");
        matcher = MatcherFactory.getMatcher(type, matchingType);
        TreeMultimap<Proteoform, String> mapping = matcher.match(entities);
        logger.log(Level.INFO, "Matching complete.");

        logger.log(Level.INFO, "\nFiltering pathways and reactions....");
        TreeMultimap<Proteoform, Reaction> result = Finder.search(mapping);
        logger.log(Level.INFO, "Filtering pathways and reactions complete.");

        Analyser analyser = AnalyserFactory.getAnalyser(type);
        analyser.analyse(result);

        return Reporter.getPathwayStatisticsList();
    }
}
