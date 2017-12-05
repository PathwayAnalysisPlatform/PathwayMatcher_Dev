package no.uib.pathwaymatcher.stages;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import org.apache.commons.lang.NotImplementedException;

public class AnalyserProteoforms extends Analyser{
    @Override
    public void analyse(TreeMultimap<Proteoform, Reaction> result) {
        throw new NotImplementedException();
    }
}
