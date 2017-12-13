package no.uib.pathwaymatcher.Analysis;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.tools.PathwayStaticFactory;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.neo4j.driver.v1.Values;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.getSingleValue;

public class AnalyserProteoforms extends Analyser{
    @Override
    public void analyse(TreeMultimap<Proteoform, Reaction> result) {

        // Query for the total number of proteins without considering isoforms
        int u = getSingleValue(ReactomeQueries.getCountAllProteoforms, "count");      // Total number of possible entities

        // Traverse all the pathways
        for (Pathway pathway : PathwayStaticFactory.getPathwaySet()) {

            // Query for the total number of entities and reactions of the pathway
            pathway.setNumEntitiesTotal(getSingleValue(ReactomeQueries.getCountEntitesInPathwayDistinguishingProteoforms, "count", Values.parameters("stId", pathway.getStId())));
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
