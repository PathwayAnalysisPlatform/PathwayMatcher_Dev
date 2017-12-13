package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Preprocessing.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PreprocessorFactoryTest {

    @Test
    void getProteinsPreprocessorForEnsemblTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.ensemblList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorEnsembl.class));
    }

    @Test
    void getProteinsPreprocessorForGenesTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.geneList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorGenes.class));
    }

    @Test
    void getProteinsPreprocessorForUniProtTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.uniprotList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorProteins.class));
    }

    @Test
    void getProteinsPreprocessorForSNPTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.rsidList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorSnps.class));
    }

    @Test
    void getProteinsPreprocessorForPeptidesTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.peptideList.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorPeptides.class));
    }

    @Test
    void getProteformsPreprocessorForUniProtWithModsTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.proteoforms.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorProteoforms.class));
    }

    @Test
    void getProteformsPreprocessorForPeptidesWithModsTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(Conf.InputTypeEnum.peptideListAndModSites.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorModifiedPeptides.class));
    }

    @Test
    void invalidInputTest() {
        try{
            Preprocessor preprocessor = PreprocessorFactory.getPreprocessor("SomethingInvalid");
            fail("Should send IllegalArgumentException because the Input type is not valid.");
        } catch (IllegalArgumentException e){
            assertTrue(e.getMessage().equals("Error 6: The input type is invalid. : SomethingInvalid"));
        }
    }

}