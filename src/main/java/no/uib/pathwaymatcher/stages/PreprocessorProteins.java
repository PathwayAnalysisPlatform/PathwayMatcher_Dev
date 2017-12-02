package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.model.Proteoform;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Warning.EMPTY_ROW;
import static no.uib.pathwaymatcher.model.Warning.INVALID_ROW;
import static no.uib.pathwaymatcher.model.Warning.sendWarning;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Protein_Uniprot;

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
                if(line.isEmpty()) sendWarning(EMPTY_ROW, row); else sendWarning(INVALID_ROW,row);
            }
        }

        return entities;
    }
}
