package no.uib.pathwaymatcher.model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ModOnthologyTerm {

    int id;
    String label;
    ModOnthologyTerm parent;
    ModOnthologyTerm children;
    
    public ModOnthologyTerm(int id) {
        this.id = id;
    }
}
