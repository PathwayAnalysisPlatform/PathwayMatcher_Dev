package no.UiB.Prototype1.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Modification {

    int id;
    String label;
    Modification parent;
    Modification children;
    
    public Modification(int id) {
        this.id = id;
    }
}
