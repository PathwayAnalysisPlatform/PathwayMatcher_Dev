package no.uib.pathwaymatcher.Analysis;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;

import java.util.TreeSet;

/**
 * Class that performs statistical analysis on the search results.
 *
 * @author francisco
 */

public abstract class Analyser {
    public abstract void analyse(TreeMultimap<Proteoform, Reaction> result);
}
