package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static no.uib.pathwaymatcher.model.Error.ERROR_WRITING_STANDARIZED_FILE;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Peptite_And_Mod_Sites;

public class PreprocessorModifiedPeptides extends Preprocessor {

    @Override
    public Set<Proteoform> process(List<String> input) throws ParseException {
        //Note: In this function, the duplicate protein identifiers are NOT removed, since every row might show a protein with a different modified version.

        println("\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
            System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
        }
        println("\nLoading peptide mapper complete.");

        Set<Proteoform> entities = new HashSet<>();
        int row = 1;
        try {
            for (String line : input) {
                line = line.trim();
                row++;
                if (matches_Peptite_And_Mod_Sites(line)) {
                    //Process line
                    String[] parts = line.split(",");
                    for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(parts[0])) {
                        output.write(id + "," + parts[1] + "\n");   //TODO create proteoform
                    }
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
