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
    void correctListTest() {

        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Valid/correctList.txt"));
            Assert.assertEquals(10, entities.size());
            Assert.assertTrue(entities.contains(new Proteoform("P06213")));
            Assert.assertTrue(entities.contains(new Proteoform("P01308")));
            Assert.assertTrue(entities.contains(new Proteoform("P30518")));
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }
    }

    @Test
    void singleProteinTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Valid/singleProtein.txt"));
            Assert.assertEquals(1, entities.size());
            Assert.assertTrue(entities.contains(new Proteoform("P01308")));
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }
    }

    @Test
    void singleProteinWithIsoformTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Valid/singleProteinWithIsoform.txt"));
            Assert.assertEquals(1, entities.size());
            Assert.assertTrue(entities.contains(new Proteoform("Q9Y6P5-3")));
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }
    }

    @Test
    void singleProteinWithBrokenIsoformTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Invalid/singleProteinWithBrokenIsoform.txt"));
            Assert.assertEquals(0, entities.size());
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly. But not get any entity.");
        }
    }

    @Test
    void listWithIsoformsTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Valid/listWithIsoforms.txt"));
            Assert.assertEquals(11, entities.size());
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }
    }

    @Test
    void listWithBrokenIsoformsTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Invalid/listWithBrokenIsoforms.txt"));
            Assert.assertEquals(7, entities.size());
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly. But not parse all of them.");
        }
    }

    @Test
    void listWithSpacesTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Valid/listWithSpaces.txt"));
            Assert.assertEquals(11, entities.size());
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }
    }

    @Test
    void emptyListTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            Set<Proteoform> entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Invalid/empty.txt"));
            Assert.assertEquals(0, entities.size());
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }
    }

}