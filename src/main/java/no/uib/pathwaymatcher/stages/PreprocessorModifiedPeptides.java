package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Warning;

import java.text.ParseException;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Peptite_And_Mod_Sites;

public class PreprocessorModifiedPeptides extends Preprocessor {

    @Override
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");

        PathwayMatcher.logger.log(Level.INFO, "\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
            System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
        }
        logger.log(Level.INFO, "\nLoading peptide mapper complete.");

        TreeSet<Proteoform> entities = new TreeSet<>();
        int row = 1;

        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Peptite_And_Mod_Sites(line)) {
                String[] parts = line.split(",");
                for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(parts[0])) {
                    entities.add(new Proteoform(id));
                }
            } else {
                logger.log(Level.WARNING, "Row " + row + " with wrong format", Warning.INVALID_ROW.getCode());
            }
        }
        return entities;
    }
}
