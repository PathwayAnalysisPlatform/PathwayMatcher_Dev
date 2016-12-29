package no.UiB.Prototype1.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ModifiedResidue {

    int site;
    String name;
    Modification psimod;
    
    public ModifiedResidue(int site, Modification psimod) {
        this.site = site;
        this.psimod = psimod;
    }
}
