package no.uib.PathwayMatcher.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Protein {

    public String id;                          //Uniprot Id. This is not unique across protein instances.
    public String name;
    public List<String> EWASs;                 //EWAS in Neo4j
    public int status;                         //Case respect found sites.

    public Protein(String id, String name, List<String> ewass, int s) {
        this.id = id;
        this.EWASs = ewass;
        this.status = s;
    }

    public Protein() {
        this.id = "";
        this.name = "";
        this.EWASs = new ArrayList<String>();
        this.status = 5;
    }
}
