package no.uib.pap.pathwaymatcher.Analysis;

import com.google.common.collect.TreeMultimap;
import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.tools.PathwayStaticFactory;

import java.util.*;

/**
 * Performs statistical analysis on the pathway search results.
 *
 */

public abstract class Analyser {

    /**
     * Calculates statistical significance of each pathway in the search result.
     * <p>
     *     The p-value is calculated using a binomial distribution depending on the type of entities considered in
     *     the search: proteins or proteoforms.
     * </p>
     * @param result
     */
    public abstract void analyse(TreeMultimap<Proteoform, Reaction> result);

    /**
     * Benjamini-Hochberg adjustment for FDR at 0.05%
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
