package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.model.Error.INPUT_PARSING_ERROR;
import static no.uib.pathwaymatcher.util.FileUtils.getInput;
import static org.junit.jupiter.api.Assertions.*;

class PreprocessorProteinsTest {

    @Test
    void singleProteinTest() {

        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput(strMap.get(Conf.StrVars.input)));
            Assert.assertEquals(1, entities.size());
            Assert.assertTrue(entities.contains("P01308"));
        } catch (java.text.ParseException e) {
            System.out.println("Error parsing the input file.");
            System.exit(INPUT_PARSING_ERROR.getCode());
        }
    }

    @Test
    void singleProteinWithIsoformTest() {
    }

}