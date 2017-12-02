package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.tools.Parser;
import no.uib.pathwaymatcher.tools.ParserProteoformSimple;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Warning;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Warning.EMPTY_ROW;
import static no.uib.pathwaymatcher.model.Warning.INVALID_ROW;
import static no.uib.pathwaymatcher.model.Warning.sendWarning;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Proteoform_Custom;

public class PreprocessorProteoforms extends Preprocessor {

    public TreeSet<Proteoform> process(List<String> input) throws java.text.ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Proteoform_Custom(line)) {
                Parser parser = new ParserProteoformSimple();
                Proteoform proteoform = parser.getProteoform(line, row);
                entities.add(proteoform);
            } else {
                if(line.isEmpty()) sendWarning(EMPTY_ROW, row); else sendWarning(INVALID_ROW,row);
            }
        }
        return entities;
    }
}
