package no.uib.Prototype1.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ModifiedResidue {

    public int site;
//    String name;
//    Modification psimod; //TODO
    public String psimod;
    
    public ModifiedResidue(String psimod, int site) {
        this.site = site;
        this.psimod = psimod;
    }
    
    
    public String toString(){
        return psimod + ";" + site;
    }
}
