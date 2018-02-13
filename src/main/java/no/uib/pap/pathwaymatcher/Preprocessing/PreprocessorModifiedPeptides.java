package no.uib.pap.pathwaymatcher.Preprocessing;

import static no.uib.pap.model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pap.pathwaymatcher.util.InputPatterns.matches_Peptite_And_Mod_Sites;

import java.text.ParseException;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.PathwayMatcher;

public class PreprocessorModifiedPeptides extends PreprocessorPeptides {

    @Override
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");

        PathwayMatcher.logger.log(Level.INFO, "\nLoading peptide mapper...");
        if (!initializePeptideMapper()) {
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
                for (String id : getPeptideMapping(parts[0])) {
                    entities.add(new Proteoform(id));
                }
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }
        }
        return entities;
    }
}
