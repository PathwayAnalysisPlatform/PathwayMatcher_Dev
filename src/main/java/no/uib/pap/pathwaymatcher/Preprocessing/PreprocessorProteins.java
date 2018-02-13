package no.uib.pap.pathwaymatcher.Preprocessing;

import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pap.pathwaymatcher.util.InputPatterns.matches_Protein_Uniprot;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import no.uib.pap.model.Proteoform;

/**
 * Class to process the input list of proteins to convert to proteoform set of the valid entries.
 */
public class PreprocessorProteins extends Preprocessor {

    public TreeSet<Proteoform> process(List<String> input) throws java.text.ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Protein_Uniprot(line)) {
                entities.add(new Proteoform(line));
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }
        }

        return entities;
    }
}
