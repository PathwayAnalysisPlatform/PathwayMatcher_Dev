package no.uib.pathwaymatcher.model.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;

public class MatcherProteoformsFlexible extends MatcherProteoforms {
    @Override
    public SetMultimap<Proteoform, String> match(Set<Proteoform> entities) {
        throw new NotImplementedException();
    }
}
