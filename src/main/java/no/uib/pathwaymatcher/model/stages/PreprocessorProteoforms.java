package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.model.Error.ERROR_WRITING_STANDARIZED_FILE;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Proteoform_Custom;

public class PreprocessorProteoforms extends Preprocessor {

    public PreprocessorProteoforms() {
        try {
            output = new FileWriter(Conf.strMap.get(Conf.StrVars.standardFilePath));
        } catch (IOException ex) {
            System.out.println("There was a problem writing temporary files.");
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(ERROR_WRITING_STANDARIZED_FILE.getCode());
        }
    }

    public Set<Proteoform> process(List<String> input) throws java.text.ParseException {
        Set<Proteoform> entities = new HashSet<>();
        try {
            int row = 1;
            for (String line : input) {
                line = line.trim();
                row++;
                if (matches_Proteoform_Custom(line)) {
                    output.write(line + "\n");
                } else if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                    System.out.println("Ignoring invalid row: " + row);
                } else {
                    throw new ParseException("Row " + row + " with wrong format", INVALID_ROW.getCode());
                }
            }
        } catch (IOException e) {
            System.out.println(ERROR_WRITING_STANDARIZED_FILE.getMessage());
            System.exit(ERROR_WRITING_STANDARIZED_FILE.getCode());
        }
        return entities;
    }
}
