package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.model.Proteoform;

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
    public abstract SetMultimap<Proteoform, String> match(Set<Proteoform> entities);

    public abstract Boolean matches(Proteoform iP, Proteoform rP);
}
