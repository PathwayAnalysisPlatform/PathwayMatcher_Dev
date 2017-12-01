package no.uib.pathwaymatcher.model.stages;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class Reporter {
    public static void reportSearchResults(TreeBasedTable<Proteoform, Pathway, Reaction> result) {
        logger.log(Level.FINE, "Writing results to file: " + strMap.get(Conf.StrVars.output));

        try {
            FileWriter resultsFile = new FileWriter(strMap.get(Conf.StrVars.output));

            int percentage = 0;
            logger.log(Level.FINER, percentage + "% ");
            resultsFile.write(
                    "UniprotId" + Conf.strMap.get(Conf.StrVars.colSep)
                            + "PTMs" + Conf.strMap.get(Conf.StrVars.colSep)
                            + (Conf.boolMap.get(Conf.BoolVars.showTopLevelPathways) ? "TopLevelPathwayStId" + Conf.strMap.get(Conf.StrVars.colSep) + "TopLevelPathwayDisplayName" + Conf.strMap.get(Conf.StrVars.colSep) : "")
                            + "PathwayStId" + Conf.strMap.get(Conf.StrVars.colSep)
                            + "PathwayDisplayName" + Conf.strMap.get(Conf.StrVars.colSep)
                            + "ReactionStId" + Conf.strMap.get(Conf.StrVars.colSep)
                            + "ReactionDisplayName" + "\n"
            );

//            for (Table.Cell<Proteoform, Pathway, Reaction> entry : result.cellSet()) {
//                resultsFile.write(entry.getRowKey().toString() + Conf.strMap.get(Conf.StrVars.colSep)
//                        + entry.getColumnKey().toString() + Conf.strMap.get(Conf.StrVars.colSep)
//                        + r.printEntry(Conf.boolMap.get(Conf.BoolVars.showTopLevelPathways)) + "\n");
//            }
            resultsFile.close();

        } catch (IOException ex) {
            sendError(ERROR_WITH_OUTPUT_FILE);
        }
/*************************************************/
        // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
//        logger.log(Level.FINE, "Writing result to file...\n0% ");
//        percent = 0;
//        cont = 0;
//        total = outputList.size();
//        FileWriter output;
//        try {
//            output = new FileWriter(strMap.get(Conf.StrVars.output));
//            if (boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
//                output.write("TopLevelPathwayId,TopLevelPathwayName,");
//            }
//            output.write("pathway,reaction,protein,rsid\n");
//            while (outputList.size() > 0) {
//                output.write(outputList.pollFirst() + "\n");
//                int newPercent = cont * 100 / total;
//                if (percent < newPercent) {
//                    logger.log(Level.FINER, newPercent + "% ");
//                    if (newPercent % 10 == 0) {
//                        logger.log(Level.FINER, "");
//                    }
//                    percent = newPercent;
//                }
//                cont++;
//            }
//            output.close();
//        } catch (IOException ex) {
//            System.out.println("There was a problem writing to the output file " + strMap.get(Conf.StrVars.output));
//            System.exit(1);
//        }
    }
}
