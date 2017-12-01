package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Warning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Peptite;

public class PreprocessorPeptides extends Preprocessor {

    public TreeSet<Proteoform> process(List<String> input) throws java.text.ParseException {
        //Note: In this function the duplicate protein identifiers are removed by adding the whole input list to a set.

        PathwayMatcher.logger.log(Level.INFO, "\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
            System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
        }
        PathwayMatcher.logger.log(Level.INFO, "\nLoading peptide mapper complete.");

        TreeSet<Proteoform> entities = new TreeSet<>();
        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Peptite(line)) {
                //Process line
                for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(line)) {
                    entities.add(new Proteoform(id));
                }
            } else {
                logger.log(Level.WARNING, "Row " + row + " with wrong format", Warning.INVALID_ROW.getCode());
            }
        }
        return entities;
    }
}
