package no.UiB.Prototype1.Stages;

import no.UiB.Prototype1.Model.ModifiedProtein;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import no.UiB.Prototype1.Configuration;
import no.UiB.Prototype1.Model.ModifiedResidue;
import no.UiB.Prototype1.Model.Protein;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Gatherer {

    private static Driver driver = GraphDatabase.driver(Configuration.host, AuthTokens.basic(Configuration.username, Configuration.password));
    
    public static List<ModifiedProtein> getInputList() {
        int cont = 0;
        List<ModifiedProtein> inputList = new ArrayList<ModifiedProtein>();
        try {

            BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/csv/inputList.csv"));
            br.readLine();

            //Read list of Modified Proteins (MPs) in my standard format: UniprotId,[psimod:site;psimod:site...;psimod:site]

            for (String ModifiedProtein; (ModifiedProtein = br.readLine()) != null && cont < Configuration.maxNumberOfProteins; cont++) {
                String[] parts = ModifiedProtein.split(",");
                String[] modifiedResidues = (parts.length > 1) ? parts[1].split(";") : new String[0];
                Protein p = new Protein();
                ModifiedProtein mp = new ModifiedProtein();
                p.id = parts[0];

                for (int I = 0; I < modifiedResidues.length; I++) {
                    String[] mr = modifiedResidues[I].split(":");
                    //mp.PTMConfiguration.add(new ModifiedResidue(Integer.valueOf(mr[0]), Integer.valueOf(mr[1])));
                }
            }
            
        } catch (IOException e) {
        } 
        
        return inputList;
    }
}
