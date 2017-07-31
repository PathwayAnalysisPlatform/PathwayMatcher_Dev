package no.uib.pathwaymatcher.model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Modification {

    public int site;
    public String psimod;
    
    public Modification(String psimod, int site) {
        this.site = site;
        this.psimod = psimod;
    }
    
    
    public String toString(){
        return psimod + ":" + site;
    }
}
