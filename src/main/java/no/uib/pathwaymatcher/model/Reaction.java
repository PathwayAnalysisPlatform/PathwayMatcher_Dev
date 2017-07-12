package no.uib.pathwaymatcher.model;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Reaction {

    public String stId;                          
    public String name;
    public no.uib.pathwaymatcher.model.Pathway pathway;

    public Reaction(String id, String name, String pathwayStId, String pathwayName) {
        this.stId = id;
        this.name = name;
        this.pathway = new Pathway(pathwayStId, pathwayName);
    }

    public Reaction() {
        this.stId = "";
        this.name = "";
    }
    
    @Override
    public String toString(){
        return this.stId + "," + this.name;
    }
}
