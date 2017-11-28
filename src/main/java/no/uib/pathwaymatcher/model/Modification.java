package no.uib.pathwaymatcher.model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Modification {

    public Integer site;
    public String psimod;
    
    public Modification(String psimod, Integer site) {
        this.site = site;
        this.psimod = psimod;
    }
    
    
    public String toString(){
        return psimod + ":" + site;
    }
}
