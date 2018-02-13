package no.uib.pap.pathwaymatcher.stages;

import static no.uib.pap.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.pathwaymatcher.Conf.boolMap;
import static no.uib.pap.pathwaymatcher.Conf.strMap;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.InputType;
import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.Conf.StrVars;
import no.uib.pap.pathwaymatcher.PathwayMatcher;
import no.uib.pap.pathwaymatcher.tools.PathwayStaticFactory;

public class Reporter {
    public static void reportSearchResults(TreeMultimap<Proteoform, Reaction> result) {
        logger.log(Level.FINE, "Writing results to file: " + strMap.get(Conf.StrVars.output));

        try {
            FileWriter resultsFile = new FileWriter(strMap.get(Conf.StrVars.output), false);

            int percentage = 0;
            int cont = 0;
            logger.log(Level.FINER, percentage + "% ");
            String sep = Conf.strMap.get(Conf.StrVars.colSep);

            // Write headers of the file
            resultsFile.write(
                    "UniProtAcc" + sep
                            + "Proteoform" + sep
                            + "Reaction StId" + sep
                            + "Reaction Name" + sep
                            + "Pathway StId" + sep
                            + "Pathway Name" + sep
                            + (Conf.boolMap.get(Conf.BoolVars.showTopLevelPathways) ? "TopLevelPathway StId" + sep + "TopLevelPathway Name" + sep : "")
                            + "\n"
            );

            // Write entries of the file
            for (Map.Entry<Proteoform, Reaction> entry : result.entries()) {

                Proteoform proteoform = entry.getKey();
                Reaction reaction = entry.getValue();

                for (Pathway pathway : reaction.getPathwaySet()) {
                    if (Conf.boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
                        for (Pathway tlp : pathway.getTopLevelPathwaySet()) {

                            resultsFile.write(proteoform.getUniProtAcc() + sep);
                            resultsFile.write(ProteoformFormat.SIMPLE.getString(proteoform) + sep);
                            resultsFile.write(pathway.getStId() + sep);
                            resultsFile.write(pathway.getDisplayName() + sep);
                            resultsFile.write(reaction.getStId() + sep);
                            resultsFile.write(reaction.getDisplayName() + sep);
                            resultsFile.write(tlp.getStId() + sep);
                            resultsFile.write(tlp.getDisplayName() + sep);
                            resultsFile.write("\n");
                        }
                    } else {
                        resultsFile.write(proteoform.getUniProtAcc() + sep);
                        resultsFile.write(ProteoformFormat.SIMPLE.getString(proteoform) + sep);
                        resultsFile.write(pathway.getStId() + sep);
                        resultsFile.write(pathway.getDisplayName() + sep);
                        resultsFile.write(reaction.getStId() + sep);
                        resultsFile.write(reaction.getDisplayName() + sep);
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

    public static void reportPathwayStatistics() {
        logger.log(Level.FINE, "Writing results to file: " + strMap.get(Conf.StrVars.pathwayStatistics));

        // Sort pathways by pValue
        List<Pathway> pathwayList = new ArrayList<>(PathwayStaticFactory.getPathwaySet());
        Collections.sort(pathwayList, new Comparator<Pathway>() {
            public int compare(Pathway x, Pathway y) {
                return Double.compare(x.getPValue(), y.getPValue());
            }
        });

        try {
            FileWriter statisticsFile = new FileWriter(strMap.get(Conf.StrVars.pathwayStatistics), false);
            String sep = Conf.strMap.get(Conf.StrVars.colSep);

            // Write headers of the file
            statisticsFile.write("Pathway StId" + sep
                    + "Pathway Name" + sep
                    + "# Entities Found" + sep
                    + "# Entities Total" + sep
                    + "Entities Ratio" + sep
                    + "Entities P-Value" + sep
                    + "Significant" + sep
                    + "Entities FDR" + sep
                    + "# Reactions Found" + sep
                    + "# Reactions Total" + sep
                    + "Reactions Ratio" + sep
                    + "Entities Found" + sep
                    + "Reactions Found" + sep
                    + "\n"
            );

            // For each pathway
            for (Pathway pathway : pathwayList) {
                // If top level pathways are required, then use print only the pathways that are low level
                if(boolMap.get(Conf.BoolVars.showTopLevelPathways) &&  pathway.getTopLevelPathwaySet().size() == 0){
                    continue;
                }
                statisticsFile.write(pathway.getStId() + sep
                        + "\"" + pathway.getDisplayName() + "\"" + sep
                        + pathway.getEntitiesFound().size() + sep
                        + pathway.getNumEntitiesTotal() + sep
                        + pathway.getEntitiesRatio() + sep
                        + pathway.getPValue() + sep
                        + (pathway.getPValue() < 0.05 ? "Yes" : "No") + sep
                        + pathway.getEntitiesFDR() + sep
                        + pathway.getReactionsFound().size() + sep
                        + pathway.getNumReactionsTotal() + sep
                        + pathway.getReactionsRatio() + sep
                        + pathway.getEntitiesFoundString(InputType.valueOf(Conf.strMap.get(Conf.StrVars.inputType))) + sep
                        + pathway.getReactionsFoundString() + sep + "\n");
            }

            statisticsFile.close();
        } catch (IOException ex) {
            sendError(ERROR_WITH_OUTPUT_FILE);
        }
    }

    public static List<String> getPathwayStatisticsList() {
        StringBuilder line = new StringBuilder();
        List<String> result = new ArrayList<>();

        logger.log(Level.FINE, "Writing results to file: " + strMap.get(Conf.StrVars.pathwayStatistics));

        // Sort pathways by pValue
        List<Pathway> pathwayList = new ArrayList<>(PathwayStaticFactory.getPathwaySet());
        Collections.sort(pathwayList, new Comparator<Pathway>() {
            public int compare(Pathway x, Pathway y) {
                return Double.compare(x.getPValue(), y.getPValue());
            }
        });

        String sep = Conf.strMap.get(Conf.StrVars.colSep);

        // For each pathway
        for (Pathway pathway : pathwayList) {
            if(pathway.getTopLevelPathwaySet().size() == 0){
                continue;
            }
            line = new StringBuilder();
            line.append(pathway.getStId() + sep
                    + "\"" + pathway.getDisplayName() + "\"" + sep
                    + pathway.getEntitiesFound().size() + sep
                    + pathway.getNumEntitiesTotal() + sep
                    + pathway.getEntitiesRatio() + sep
                    + pathway.getPValue() + sep
                    + (pathway.getPValue() < 0.05 ? "Yes" : "No") + sep
                    + pathway.getEntitiesFDR() + sep
                    + pathway.getReactionsFound().size() + sep
                    + pathway.getNumReactionsTotal() + sep
                    + pathway.getReactionsRatio() + sep
                    + pathway.getEntitiesFoundString(InputType.valueOf(strMap.get(StrVars.inputType))) + sep
                    + pathway.getReactionsFoundString() + sep);
            result.add(line.toString());
        }
        return result;
    }
}
