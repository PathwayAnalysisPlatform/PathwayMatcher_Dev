/*
INPUT: int position number and Protein fasta sequence
OUTPUT: the amino acid at the required position.
 */
package no.uib.pathwayminer.Tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class AminoAcidSelector {

    public static void main(String args[]) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:/Users/Francisco/Documents/NetBeansProjects/AnalizePhosphosites/src/main/java/Tools/fasta.txt"));
            int position = Integer.valueOf(br.readLine());
            String line = br.readLine();
            String sequence = "";
            while ((line = br.readLine()) != null) {
                sequence += line;
            }

            if (position - 2 > 0) {
                System.out.println("position " + (position - 1) + ":" + sequence.charAt(position - 2));
            }
            if (position - 1> 0) {
                System.out.println("position " + (position) + ":" + sequence.charAt(position - 1));
            }
            if (position > 0) {
                System.out.println("position " + (position + 1) + ":" + sequence.charAt(position));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AminoAcidSelector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AminoAcidSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
