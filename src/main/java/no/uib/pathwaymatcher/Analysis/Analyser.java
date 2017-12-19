package no.uib.pathwaymatcher.Analysis;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.tools.PathwayStaticFactory;

import java.util.*;

/**
 * Class that performs statistical analysis on the search results.
 *
 * @author francisco
 */

public abstract class Analyser {
    public abstract void analyse(TreeMultimap<Proteoform, Reaction> result);

    /**
     * Benjamini-Hochberge adjustment for FDR at 0.05%
     */
    protected void adjustPValues() {

        // Sort pathways by pValue
        List<Pathway> sortedList = new ArrayList<>(PathwayStaticFactory.getPathwaySet());
        Collections.sort(sortedList, new Comparator<Pathway>() {
            public int compare(Pathway x, Pathway y) {
                return Double.compare(x.getPValue(), y.getPValue());
            }
        });

        // Count number of pathways with p-Values less than 0.05
        double n = 0;
        for (Pathway pathway : sortedList) {
            if (pathway.getPValue() < 0.05) {
                n++;
            } else {
                break;
            }
        }

        double rank = 1;
        for (Pathway pathway : sortedList) {
            double newPValue = pathway.getPValue() * n;
            newPValue /= rank;
            pathway.setEntitiesFDR(newPValue);
            rank++;
        }
    }
}
