package no.uib.pathwaymatcher.stages;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.BoolVars;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.model.Reaction;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;
import static no.uib.pathwaymatcher.PathwayMatcher.println;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Reporter {
    //The purpose of this class is to write the results of the whole process to files. It can be in several formats or different content.

    public static void createReports() {

        FileWriter FWPathways = null, FWReactions = null, FWPMPR = null;
        try {
            if (Conf.boolMap.get(BoolVars.pathwaysFile)) {
                FWPathways = new FileWriter("./Pathways.txt");
            }
            if (Conf.boolMap.get(BoolVars.reactionsFile)) {
                FWReactions = new FileWriter("./Reactions.txt");
            }

            FWPMPR = new FileWriter(Conf.strMap.get(Conf.StrVars.output));

            for (ModifiedProtein mp : MPs) {
                for (EWAS e : mp.EWASs) {
                    if (e.matched) {
                        for (Reaction r : e.reactionsList) {
                            if (Conf.boolMap.get(BoolVars.pathwaysFile)) {
                                FWPathways.write(r.name + "\n");
                            }
                            if (Conf.boolMap.get(BoolVars.reactionsFile)) {
                                FWReactions.write(r.stId + "\n");
                            }

                            FWPMPR.write(mp.baseProtein.id + "," + mp.PTMs.toString() + "," + r.pathway.stId + "," + r.pathway.displayName + "," + r.stId + "," + r.name + "\n");
                        }
                    }
                }
            }

            if (Conf.boolMap.get(BoolVars.pathwaysFile)) {
                FWPathways.close();
            }
            if (Conf.boolMap.get(BoolVars.reactionsFile)) {
                FWReactions.close();
            }

            FWPMPR.close();

        } catch (IOException ex) {
            println("Failed to create a report file.");
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sortOutput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
