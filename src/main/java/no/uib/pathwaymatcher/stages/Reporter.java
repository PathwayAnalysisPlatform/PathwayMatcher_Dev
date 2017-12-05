package no.uib.pathwaymatcher.stages;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.tools.Parser;
import no.uib.pathwaymatcher.tools.ParserProteoformSimple;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class Reporter {
    public static void reportSearchResults(TreeMultimap<Proteoform, Reaction> result) {
        logger.log(Level.FINE, "Writing results to file: " + strMap.get(Conf.StrVars.output));

        try {
            FileWriter resultsFile = new FileWriter(strMap.get(Conf.StrVars.output), false);

            int percentage = 0;
            int cont = 0;
            logger.log(Level.FINER, percentage + "% ");
            Parser parser = new ParserProteoformSimple();
            String sep = Conf.strMap.get(Conf.StrVars.colSep);

            // Write headers of the file
            resultsFile.write(
                    "UniProtAcc" +sep
                            + "Proteoform" +sep
                            + "ReactionStId" +sep
                            + "ReactionDisplayName" +sep
                            + "PathwayStId" +sep
                            + "PathwayDisplayName" +sep
                            + (Conf.boolMap.get(Conf.BoolVars.showTopLevelPathways) ? "TopLevelPathwayStId" +sep + "TopLevelPathwayDisplayName" +sep : "")
                            + "\n"
            );

            // Write entries of the file
            for (Map.Entry<Proteoform, Reaction> entry : result.entries()) {

                Proteoform proteoform = entry.getKey();
                Reaction reaction = entry.getValue();

                for (Pathway pathway : reaction.getPathwaySet()) {
                    if (Conf.boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
                        for (Pathway tlp : pathway.getTopLevelPathwaySet()) {
                            resultsFile.write(parser.getString(proteoform) +sep);
                            resultsFile.write(proteoform.getUniProtAcc() +sep);
                            resultsFile.write(pathway.getStId() +sep);
                            resultsFile.write(pathway.getDisplayName() +sep);
                            resultsFile.write(reaction.getStId() +sep);
                            resultsFile.write(reaction.getDisplayName() +sep);
                            resultsFile.write(tlp.getStId() +sep);
                            resultsFile.write(tlp.getDisplayName() +sep);
                            resultsFile.write("\n");
                        }
                    } else {
                        resultsFile.write(proteoform.getUniProtAcc() +sep);
                        resultsFile.write(parser.getString(proteoform) +sep);
                        resultsFile.write(pathway.getStId() +sep);
                        resultsFile.write(pathway.getDisplayName() +sep);
                        resultsFile.write(reaction.getStId() +sep);
                        resultsFile.write(reaction.getDisplayName() +sep);
                        resultsFile.write("\n");
                    }
                }

                int newPercent = cont * 100 / result.entries().size();
                if (percentage < newPercent) {
                    logger.log(Level.FINER, newPercent + "% ");
                    if (newPercent > percentage + Conf.intMap.get(Conf.IntVars.percentageStep)) {
                        percentage = newPercent;
                        PathwayMatcher.logger.log(Level.FINE, percentage + "% ");
                    }
                }
                if (percentage != 100) {
                    PathwayMatcher.logger.log(Level.FINE, "100%");
                }
            }
            resultsFile.close();

        } catch (IOException ex) {
            sendError(ERROR_WITH_OUTPUT_FILE);
        }
    }
}
