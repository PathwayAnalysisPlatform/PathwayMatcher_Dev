package no.uib.pap.pathwaymatcher.Analysis;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Reaction;
import no.uib.pap.pathwaymatcher.db.ReactomeQueries;
import no.uib.pap.pathwaymatcher.tools.PathwayStaticFactory;

import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.neo4j.driver.v1.Values;

import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.getSingleValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnalyserProteins extends Analyser {

    @Override
    /**
     * Calculates the statistical significance for each pathway containing participants in the input data.
     * For each pathway the data it needs: Total number of proteins, # participants found, # total number of participants
     */
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
}
