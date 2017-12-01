package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FactoryPreprocessorTest {

    @Test
    void getProteinsPreprocessorForEnsemblTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.ensemblList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorEnsembl.class));
    }

    @Test
    void getProteinsPreprocessorForGenesTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.geneList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorGenes.class));
    }

    @Test
    void getProteinsPreprocessorForUniProtTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.uniprotList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorProteins.class));
    }

    @Test
    void getProteinsPreprocessorForSNPTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.rsidList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorSnps.class));
    }

    @Test
    void getProteinsPreprocessorForPeptidesTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.peptideList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorPeptides.class));
    }

    @Test
    void getProteformsPreprocessorForUniProtWithModsTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.uniprotListAndModSites.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorProteoforms.class));
    }

    @Test
    void getProteformsPreprocessorForPeptidesWithModsTest() {
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor(Conf.InputTypeEnum.peptideListAndModSites.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorModifiedPeptides.class));
    }

    @Test
    void invalidInputTest() {
        try{
            Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("SomethingInvalid");
            fail("Should send IllegalArgumentException because the Input type is not valid.");
        } catch (IllegalArgumentException e){
            assertTrue(e.getMessage().equals("Error 6: The input type is invalid. : SomethingInvalid"));
        }
    }

}