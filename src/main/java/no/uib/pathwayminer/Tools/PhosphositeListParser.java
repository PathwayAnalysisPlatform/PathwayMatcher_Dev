package no.uib.pathwayminer.Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class PhosphositeListParser {
    public static void main(String args[]) throws IOException {
        
        //Process once the whole list to create the set of unique uniprot ids to query.
        //Send the list of uniprot ids and sites to the 
        
        HashMap<String, HashSet<Integer>> expectedSites = new HashMap<String, HashSet<Integer>>();   //<Uniprot Id, list of phosphosites>
        BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/txt/Phospho (STY)Sites.txt"));
        FileWriter resultFile = new FileWriter("./src/main/resources/csv/listFilePhosphosites.csv");
        int cont = 0;
        br.readLine();
        for (String line; (line = br.readLine()) != null && cont < 200000; cont++) {
            String[] parts = line.split("\t");

            System.out.println(cont + "\tReading: " + parts[0]);

            if (parts[0].length() == 0 || parts[1].length() == 0) {
                continue;
            }

            String[] ids = parts[0].split(";");
            String[] sites = parts[1].split(";");

            //For each uniprot Id in the row
            for (int p = 0; p < ids.length; p++) {
                if (ids[p].contains("_")) {
                    continue;
                }
                if (!expectedSites.containsKey(ids[p])) {
                    expectedSites.put(ids[p], new HashSet<Integer>());
                }
                expectedSites.get(ids[p]).add(Integer.valueOf(sites[p]));
            }
            System.out.println(cont + "\tReading: " + parts[0]);
        }
        
        Iterator iterator = expectedSites.entrySet().iterator();
        cont = 0;
        while (iterator.hasNext()) {
            
            Map.Entry e = (Map.Entry) iterator.next();
            System.out.println(cont + "\t" + e.getKey());
            cont++;
            resultFile.write(e.getKey() + "," + e.getValue().toString().replace(",", ";").replace("[", "").replace("]", "").replace(" ", "")  + "\n");
        }
        
        br.close();
        resultFile.close();
    }
}
