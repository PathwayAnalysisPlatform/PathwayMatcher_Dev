package no.uib.Prototype1.Stages;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.UiB.Prototype1.Configuration;
import no.UiB.Prototype1.Prototype1;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Reporter {
    //The purpose of this class is to write the results of the whole process to files. It can be in several formats or different content.
    
    public static void reportPathways(){
        try {
            FileWriter output = new FileWriter(Configuration.outputFilePathways);
            for(String p : Prototype1.hitPathways){
                output.write(p + "\n");
            }
            output.close();
        } catch (IOException ex) {
            System.out.println("Failed to create the output file for the pathways: " + Configuration.outputFilePathways);
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void reportReactions(){
        try {
            FileWriter output = new FileWriter(Configuration.outputFileReactions);
            for(String r : Prototype1.hitReactions){
                output.write(r + "\n");
            }
            output.close();
        } catch (IOException ex) {
            System.out.println("Failed to create the output file for the reactions: " + Configuration.outputFilePathways);
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
