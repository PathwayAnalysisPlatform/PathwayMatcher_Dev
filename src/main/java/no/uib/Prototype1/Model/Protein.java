package no.UiB.Prototype1.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Protein {

    public String id;                          //Uniprot Id. This is not unique across protein instances.
    public String name;
    public List<ModifiedProtein> inputMPs;     //ModifiedProteins requested
    public List<ModifiedProtein> knownMPs;     //EWAS in Neo4j
    public int status;                         //Case respect found sites.

    public Protein(String id, String name, List<ModifiedProtein> imps, List<ModifiedProtein> kmps, int s) {
        this.id = id;
        this.inputMPs = imps;
        this.knownMPs = kmps;
        this.status = s;
    }

    public Protein() {
        this.id = "";
        this.name = "";
        this.inputMPs = new ArrayList<ModifiedProtein>();
        this.knownMPs = new ArrayList<ModifiedProtein>();
        this.status = 5;
    }
}
