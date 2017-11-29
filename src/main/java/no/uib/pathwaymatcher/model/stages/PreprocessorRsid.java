package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Rsid;

public class PreprocessorRsid extends Preprocessor {

    public Set<String> process(List<String> input) throws ParseException {

        Set<String> entities = new HashSet<>();

        // Validate vepTablesPath
        Preprocessor.validateVepTables();

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Rsid(line)) {
                if (matches_Rsid(line)) {
                    entities.add(line);
                } else if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                    System.out.println("Ignoring invalid row: " + row);
                } else {
                    throw new ParseException("Row " + row + " with wrong format", INVALID_ROW.getCode());
                }
            }
        }

        return entities;
    }
}
