package no.uib.PathwayMatcher.Model;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Reaction {

    public String stId;                          
    public String name;
    public no.uib.PathwayMatcher.Model.Pathway pathway;

    public Reaction(String id, String name, String pathwayStId, String pathwayName) {
        this.stId = id;
        this.name = name;
        this.pathway = new Pathway(pathwayStId, pathwayName);
    }

    public Reaction() {
        this.stId = "";
        this.name = "";
    }
}
