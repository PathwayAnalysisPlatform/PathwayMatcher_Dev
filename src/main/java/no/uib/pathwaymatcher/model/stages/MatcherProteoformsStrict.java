package no.uib.pathwaymatcher.model.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;
import java.util.TreeSet;

import static java.lang.Math.abs;
import static no.uib.pathwaymatcher.Conf.intMap;
import static no.uib.pathwaymatcher.Conf.strMap;

public class MatcherProteoformsStrict extends MatcherProteoforms {

    @Override
    public SetMultimap<Proteoform, String> match(Set<Proteoform> entities) {
        throw new NotImplementedException();
    }

}
