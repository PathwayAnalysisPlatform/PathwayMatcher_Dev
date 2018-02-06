package no.uib.pap.pathwaymatcher.Matching;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Protein;
import no.uib.pap.model.Proteoform;

import java.util.Set;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public abstract class Matcher {

    /**
     * Performs a match between the entities received as parameter and the ones in the database, using a specific set of criteria.
     * @param entities
     * @return List of
     */
    public abstract TreeMultimap<no.uib.pap.model.Proteoform, String> match(Set<no.uib.pap.model.Proteoform> entities);

    public abstract Boolean matches(Proteoform iP, Proteoform rP);
}
