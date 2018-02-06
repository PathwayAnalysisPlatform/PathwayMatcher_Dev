package no.uib.pap.pathwaymatcher.stages;

import no.uib.pap.model.InputType;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.Preprocessing.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PreprocessorFactoryTest {

    @Test
    void getProteinsPreprocessorForEnsemblTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.ENSEMBLLIST.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorEnsembl.class));
    }

    @Test
    void getProteinsPreprocessorForGenesTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.GENELIST.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorGenes.class));
    }

    @Test
    void getProteinsPreprocessorForUniProtTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.UNIPROTLIST.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorProteins.class));
    }

    @Test
    void getProteinsPreprocessorForSNPTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.RSIDLIST.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorSnps.class));
    }

    @Test
    void getProteinsPreprocessorForPeptidesTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.PEPTIDELIST.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorPeptides.class));
    }

    @Test
    void getProteformsPreprocessorForUniProtWithModsTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.PROTEOFORMS.toString());
        assertTrue(preprocessor.getClass().equals(PreprocessorProteoforms.class));
    }

    @Test
    void getProteformsPreprocessorForPeptidesWithModsTest() {
        Preprocessor preprocessor = PreprocessorFactory.getPreprocessor(InputType.PEPTIDELISTANDMODSITES.toString());
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