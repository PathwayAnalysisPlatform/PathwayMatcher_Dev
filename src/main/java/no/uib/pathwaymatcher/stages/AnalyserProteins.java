package no.uib.pathwaymatcher.stages;

import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.tools.PathwayStaticFactory;

import java.util.TreeSet;

public class AnalyserProteins extends Analyser{

    @Override
    public void analyse(TreeMultimap<Proteoform, Reaction> result) {

        // Traverse all the pathways
        for(Pathway pathway : PathwayStaticFactory.getPathwaySet()){
            // Query for the total number of entities and reactions of the pathway

            // Calculate entities and reactions ratio

            // Calculate the entities pvalue

        }

    }

}
