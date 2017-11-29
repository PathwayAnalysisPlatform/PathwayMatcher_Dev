package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.ReactionResultEntry;

import java.util.Set;
import java.util.TreeSet;

public class MatcherProteoformsStrict extends Matcher {
    @Override
    public TreeSet<ReactionResultEntry> match(Set<Proteoform> entities) {
        return null;
    }
}
