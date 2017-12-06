package no.uib.pathwaymatcher.Analysis;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.db.ReactomeQueries;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.getSingleValue;
import static no.uib.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.Assert.assertEquals;

public class AnalyserProteinsTest {

    @BeforeAll
    static void setUpAll() {
        Conf.setDefaultValues();
        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
    }


    @Test
    public void getSingleValueTest() {
        int u = getSingleValue(ReactomeQueries.getCountAllProteins, "count");
        assertEquals(10710, u);

        int n = 184;                // Sample size: # Proteins in the input
        int k = 25;         // Sucessful trials: Entities found participating in the pathway
        int t = 90;         // Total number of entities in the pathway
        double p = (double) t / (double) u;        // Probability of sucess in each trial: The entity is a participant in the pathway

        BinomialDistribution binomialDistribution = new BinomialDistributionImpl(n, p);     //Given n trials with probability p of success
        double pValue = binomialDistribution.probability(k);                                //Probability of k successful trials
        System.out.println(pValue);

//        u = ConnectionNeo4j.getSingleValue(ReactomeQueries.getCountAllProteinsWithIsoforms, null, "count");
//        assertEquals(10876, u);
    }
}