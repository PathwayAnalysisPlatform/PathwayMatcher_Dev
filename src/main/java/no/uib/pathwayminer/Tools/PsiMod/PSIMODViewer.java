package no.uib.pathwayminer.Tools.PsiMod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class PSIMODViewer {

    public static void main(String args[]) throws IOException {
        
        BufferedReader arch_Tree = new BufferedReader(new FileReader("./src/main/resources/other/PSI-MOD.obo"));
        BufferedReader arch_Freq = new BufferedReader(new FileReader("./src/main/resources/csv/ReactomePTMs.csv"));
        FileWriter arch_Graph = new FileWriter("./src/main/resources/sif/ReactomePTMsFreqTree.csv");

        Modification[] PTMs = no.uib.pathwayminer.Tools.PsiMod.Modification[2000];
        int maxPsimod = 0;        
        
        //Read tree structure
        String line;
        while ((line = arch_Tree.readLine()) != null) {

            if (line.startsWith("[Term]")) {
                line = arch_Tree.readLine();
                Modification m = new Modification();
                m.id = Integer.valueOf(line.split(":")[2]);
                
                if(m.id > maxPsimod)
                    maxPsimod = m.id;
                
                while (line.length() > 0) {
                    if(line.startsWith("is_a")){
                        m.is_a.add(Integer.valueOf(line.substring(10, 14)));
                    }
                    line = arch_Tree.readLine();
                }
                PTMs.add(m);
            }
        }
        
        System.out.println(maxPsimod);

        //Initialize to zero frequency all of the nodes
        //Read how often every PTM occurs -> update node frequency
    }
}
