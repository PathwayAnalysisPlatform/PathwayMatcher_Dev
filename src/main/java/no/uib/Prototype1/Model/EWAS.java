package no.UiB.Prototype1.Model;

import java.util.List;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class EWAS {
    String stId;
    String name;
    String displayName;
    List<ModifiedResidue> PTMConfiguration; //An EWAS contains only ONE PTMConfiguration; a ModifiedProtein can have MANY PTMConfigurations
}
