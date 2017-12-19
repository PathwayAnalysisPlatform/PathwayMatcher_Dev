package no.uib.pathwaymatcher.model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class PsiModTerm {

    int id;
    String label;
    PsiModTerm parent;
    PsiModTerm children;
    
    public PsiModTerm(int id) {
        this.id = id;
    }
}
