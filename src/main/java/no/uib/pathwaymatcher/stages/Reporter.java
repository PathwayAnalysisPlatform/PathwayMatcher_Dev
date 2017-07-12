package no.uib.pathwaymatcher.stages;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.BoolVars;
import no.uib.pathwaymatcher.Conf.StrVars;
import static no.uib.pathwaymatcher.Conf.strMap;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.ModifiedProtein;
import no.uib.pathwaymatcher.model.Reaction;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;
import static no.uib.pathwaymatcher.PathwayMatcher.print;
import static no.uib.pathwaymatcher.PathwayMatcher.println;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Reporter {
    //The purpose of this class is to write the results of the whole process to files. It can be in several formats or different content.

    public static void createReports() {

        createOutputFile();
        if (strMap.get(StrVars.pathwaysFile).length() > 0) {
            createPathwaysFile();
        }
        if (strMap.get(StrVars.reactionsFile).length() > 0) {
            createReactionsFile();
        }
    }

    private static void createOutputFile() {
        println("Writing results to file: " + strMap.get(StrVars.output));

        try {
            FileWriter FWPMPR = new FileWriter(strMap.get(Conf.StrVars.output));

            int percentage = 0;
            print(percentage + "% ");
            FWPMPR.write("UniprotId" + "," + "PTMs" + "," + "PathwayStId" + "," + "PathwayDisplayName" + "," + "ReactionStId" + "," + "ReactionDisplayName" + "\n");
            for (int I = 0; I < MPs.size(); I++) {
                ModifiedProtein mp = MPs.get(I);
                for (EWAS e : mp.EWASs) {
                    if (e.matched) {
                        for (Reaction r : e.reactionsList) {
                            FWPMPR.write(mp.baseProtein.id + "," + mp.PTMs.toString() + "," + r.pathway.stId + "," + r.pathway.displayName + "," + r.stId + "," + r.name + "\n");
                        }
                    }
                }
                int newPercentage = I * 100 / MPs.size();
                if (newPercentage > percentage) {
                    percentage = newPercentage;
                    print(percentage + "% ");
                }
            }
            if (percentage == 100) {
                println("");
            } else {
                println("100%");
            }
            FWPMPR.close();

        } catch (IOException ex) {
            println("Failed to create the output file.");
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void createPathwaysFile() {
        println("Creating Pathways file: " + strMap.get(StrVars.pathwaysFile));
        TreeSet<String> pathwaySet = new TreeSet<>();

        try {
            FileWriter FWPathways = new FileWriter(strMap.get(StrVars.pathwaysFile));

            println("Eliminating repeated pathways...");
            int percentage = 0;
            print(percentage + "% ");
            for (int I = 0; I < MPs.size(); I++) {
                ModifiedProtein mp = MPs.get(I);
                for (EWAS e : mp.EWASs) {
                    if (e.matched) {
                        for (Reaction r : e.reactionsList) {
                            pathwaySet.add(r.pathway.toString());
                        }
                    }
                }
                int newPercentage = I * 100 / pathwaySet.size();
                if (newPercentage > percentage) {
                    percentage = newPercentage;
                    print(percentage + "% ");
                }
            }
            if (percentage == 100) {
                println("");
            } else {
                println("100%");
            }
            
            if(pathwaySet.size() == 0){
                println("No pathways to write.");
            }
            else{
                println("Writing set to file...");
                percentage = 0;
                print(percentage + "% ");
                int I = 0;
                for (String p : pathwaySet) {
                    FWPathways.write(p + "\n");
                    I++;
                    int newPercentage = I * 100 / pathwaySet.size();
                    if (newPercentage > percentage) {
                        percentage = newPercentage;
                        print(percentage + "% ");
                    }
                }
                if (percentage == 100) {
                    println("");
                } else {
                    println("100%");
                }
            }
            FWPathways.close();

        } catch (IOException ex) {
            println("Failed to create the pathways file.");
            //Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createReactionsFile() {
        println("Reactions file requested: " + strMap.get(StrVars.reactionsFile));
        TreeSet<String> reactionSet = new TreeSet<>();

        try {
            FileWriter FWReactions = new FileWriter(strMap.get(StrVars.reactionsFile));

            println("Eliminating repeated reactions...");
            int percentage = 0;
            print(percentage + "% ");
            for (int I = 0; I < MPs.size(); I++) {
                ModifiedProtein mp = MPs.get(I);
                for (EWAS e : mp.EWASs) {
                    if (e.matched) {
                        for (Reaction r : e.reactionsList) {
                            reactionSet.add(r.toString());
                        }
                    }
                }
                int newPercentage = I * 100 / reactionSet.size();
                if (newPercentage > percentage) {
                    percentage = newPercentage;
                    print(percentage + "% ");
                }
            }
            if (percentage == 100) {
                println("");
            } else {
                println("100%");
            }

            if(reactionSet.size() == 0){
                println("No reactions to write.");
            }
            else{
                println("Writing set to file...");
                percentage = 0;
                print(percentage + "% ");
                int I = 0;
                for (String r : reactionSet) {
                    FWReactions.write(r + "\n");
                    I++;
                    int newPercentage = I * 100 / reactionSet.size();
                    if (newPercentage > percentage) {
                        percentage = newPercentage;
                        print(percentage + "% ");
                    }
                }
                if (percentage == 100) {
                    println("");
                } else {
                    println("100%");
                }
            }

            FWReactions.close();

        } catch (IOException ex) {
            println("Failed to create the reactions file.");
            //Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sortOutput() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
