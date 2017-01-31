package no.uib.Prototype1.Stages;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.Prototype1.Configuration;
import no.uib.Prototype1.Model.EWAS;
import no.uib.Prototype1.Model.ModifiedProtein;
import no.uib.Prototype1.Model.Reaction;
import static no.uib.Prototype1.Prototype1.MPs;
import static no.uib.Prototype1.Prototype1.println;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Reporter {
    //The purpose of this class is to write the results of the whole process to files. It can be in several formats or different content.

    public static void createReports() {

        FileWriter FWPathways = null, FWReactions = null, FWPMPR = null;
        try {
            if (Configuration.createHitPathwayFile) {
                FWPathways = new FileWriter(Configuration.outputFilePathways);
            }
            if (Configuration.createHitReactionFile) {
                FWReactions = new FileWriter(Configuration.outputFileReactions);
            }
            if (Configuration.createPMPRTableFile) {
                FWPMPR = new FileWriter(Configuration.outputFilePMPR);
            }

            for (ModifiedProtein mp : MPs) {
                for (EWAS e : mp.EWASs) {
                    if (e.matched) {
                        for (Reaction r : e.reactionsList) {
                            if(Configuration.createHitReactionFile){
                                FWReactions.write(r.stId + "\n");
                            }
                            if (Configuration.createHitPathwayFile) {
                                FWPathways.write(r.name + "\n");
                            }
                            if (Configuration.createPMPRTableFile) {
                                FWPMPR.write(mp.baseProtein.id + "," + mp.PTMs.toString() + "," + r.pathway.stId + "," + r.pathway.displayName + "," + r.stId + "," + r.name + "\n");
                            }
                        }
                    }
                }
            }

            if (Configuration.createHitPathwayFile) {
                FWPathways.close();
            }
            if (Configuration.createHitReactionFile) {
                FWReactions.close();
            }
            if (Configuration.createPMPRTableFile) {
                FWPMPR.close();
            }
        } catch (IOException ex) {
            println("Failed to create a report file.");
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
