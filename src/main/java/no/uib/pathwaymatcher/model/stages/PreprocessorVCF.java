package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Vcf_Record;

public class PreprocessorVCF extends Preprocessor {

    @Override
    public Set<?> process(List<String> input) throws ParseException {

        Set<List> entities = new HashSet<>();

        Preprocessor.validateVepTables();

        int row = 0;

        for (String line : input) {
            row++;
            line = line.trim();

            if (!line.startsWith("#")) {
                continue;
            }

            if (!matches_Vcf_Record(line)) {
                if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                    System.out.println("Ignoring invalid row " + row + ": " + line);
                } else {
                    throw new ParseException("Row " + row + " with wrong format", INVALID_ROW.getCode());
                }
            }

            //Map variant to proteins

        }
        return entities;
    }
}
