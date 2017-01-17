/*
 * INPUT: A list of proteins with a specific phosphosite set
 * OUTPUT: A list of the pahways containing them

Strict mode:
Only hits with the Reactome Proteins that have the isoform variant.
Only hits with the Reactome Proteins that contain at least one of the phosphosites.

Flexible mode:
If the specific isoform is not found, then it hits using the general form.
If all requested phosphosites are found, then only use those Reactome proteins containing them to hit pathways                                                    
If at least one of the requested sites is not found, then hits with the general form.

 */
package no.uib.Prototype1.Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import no.uib.Prototype1.db.ConnectionNeo4j;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ProteinWithPhosphositesPathwayMapper {
    private static Driver driver;
    public static void main(String args[]) throws IOException
    {
        //Read input list
        
        //Verify the status of every protein (row)
        
        //Query for the pathways of each protein
        
        //Merge all pathways
        
        //Sort them by dot structure name        
        
        driver = GraphDatabase.driver(ConnectionNeo4j.host, AuthTokens.basic(ConnectionNeo4j.username, ConnectionNeo4j.password));
        HashMap<String, HashSet<Integer>> expectedSites = new HashMap<String, HashSet<Integer>>();   //<Uniprot Id, list of phosphosites>
        int cont = 0;
        BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/csv/listFile.csv"));
        try {
            br.readLine();

            //First traverse all the list and merge all the occurrences of the same protein with their sites.
            for (String line; (line = br.readLine()) != null && cont < 200000000; cont++) {
                String[] parts = line.split(",");

                System.out.println(cont + "\tReading: " + parts[0]);
                        
                
                if(parts.length > 0)
                {
                    if(parts[0].length() == 0)
                        continue;
                    if (parts[0].contains("_"))
                        continue;
                    
                    if (!expectedSites.containsKey(parts[0])) 
                        expectedSites.put(parts[0], new HashSet<Integer>());
                    
                    if (parts.length > 1) {
                        String[] sites = parts[1].split(";");

                        for (int s = 0; s < sites.length; s++) 
                            expectedSites.get(parts[0]).add(Integer.valueOf(sites[s]));   
                    }
                }
            }
            
            //For all the set of unique proteins, cathegorize them according to their annotated status
            /*
            1: Protein found, 
            */
            
        } finally {
            br.close();
        }
    }
}
