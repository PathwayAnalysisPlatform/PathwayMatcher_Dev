package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.MatchType;
import no.uib.pathwaymatcher.model.EWAS;
import no.uib.pathwaymatcher.model.Modification;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.ReactionResultEntry;

import java.util.Set;
import java.util.TreeSet;

import static java.lang.Math.abs;
import static no.uib.pathwaymatcher.Conf.intMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.MPs;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public abstract class Matcher {

    public abstract TreeSet<ReactionResultEntry> match(Set<?> entities);

}
