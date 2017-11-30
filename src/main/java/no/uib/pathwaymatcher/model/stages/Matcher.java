package no.uib.pathwaymatcher.model.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.model.Proteoform;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public abstract class Matcher {

    /**
     * Performs a match between the entities received as parameter and the ones in the database.
     * @param entities
     * @return List of
     */
    public abstract SetMultimap<Proteoform, String> match(Object entities);

}
