package no.uib.pap.pathwaymatcher.Preprocessing;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.ProteoformFormat;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pap.model.Warning.*;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pap.pathwaymatcher.util.InputPatterns.matches_Proteoform_Simple;

public class PreprocessorProteoforms extends Preprocessor {

    public TreeSet<Proteoform> process(List<String> input) throws java.text.ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Proteoform_Simple(line)) {
                
                Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform(line, row);
                entities.add(proteoform);
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }
        }
        return entities;
    }
}
