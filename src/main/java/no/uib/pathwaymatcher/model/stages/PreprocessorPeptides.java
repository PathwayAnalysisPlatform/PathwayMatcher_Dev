package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Peptite;

public class PreprocessorPeptides extends Preprocessor {

    public Set<String> process(List<String> input) throws java.text.ParseException {
        //Note: In this function the duplicate protein identifiers are removed by adding the whole input list to a set.

        println("\nLoading peptide mapper...");
        if (!compomics.utilities.PeptideMapping.initializePeptideMapper()) {
            System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
            System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
        }
        println("\nLoading peptide mapper complete.");

        Set<String> entities = new HashSet<>();
        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Peptite(line)) {
                //Process line
                for (String id : compomics.utilities.PeptideMapping.getPeptideMapping(line)) {
                    entities.add(id);
                }
            } else if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                System.out.println("Ignoring invalid row: " + row);
            } else {
                throw new ParseException("Row " + row + " with wrong format", INVALID_ROW.getCode());
            }
        }
        return entities;
    }
}
