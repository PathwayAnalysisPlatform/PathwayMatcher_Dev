package no.uib.pathwaymatcher.Analysis;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.tools.PathwayStaticFactory;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.neo4j.driver.v1.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.getSingleValue;

public class AnalyserProteins extends Analyser {

    @Override
    public void analyse(TreeMultimap<Proteoform, Reaction> result) {

        // Query for the total number of proteins without considering isoforms
        int u = getSingleValue(ReactomeQueries.getCountAllProteins, "count");      // Total number of possible entities

        // Traverse all the pathways
        for (Pathway pathway : PathwayStaticFactory.getPathwaySet()) {

            // Query for the total number of entities and reactions of the pathway
            pathway.setNumEntitiesTotal(getSingleValue(ReactomeQueries.getCountEntitiesInPathway, "count", Values.parameters("stId", pathway.getStId())));
            pathway.setNumReactionsTotal(getSingleValue(ReactomeQueries.getCountReactionsInPathway, "count", Values.parameters("stId", pathway.getStId())));

            // Calculate entities and reactions ratio
            pathway.setEntitiesRatio((double) pathway.getEntitiesFound().size() / (double) pathway.getNumEntitiesTotal());
            pathway.setReactionsRatio((double) pathway.getReactionsFound().size() / (double) pathway.getNumReactionsTotal());

            // Calculate the entities pvalue
            int n = result.keySet().size();                     // Sample size: # Proteins in the input
            int k = pathway.getEntitiesFound().size();          // Sucessful trials: Entities found participating in the pathway
            double p = pathway.getNumEntitiesTotal() / (double)u;        // Probability of sucess in each trial: The entity is a participant in the pathway

            BinomialDistribution binomialDistribution = new BinomialDistributionImpl(n, p);     //Given n trials with probability p of success
            pathway.setpValue(binomialDistribution.probability(k));           //Probability of k successful trials

        }
        adjustPValues();
    }

    /**
     * Benjamini-Hochberge adjustment for FDR at 0.05%
     */
    private void adjustPValues() {

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
