package no.uib.pap.pathwaymatcher.Preprocessing;

import no.uib.pap.model.InputType;
import no.uib.pap.pathwaymatcher.Conf;

import static no.uib.pap.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pap.pathwaymatcher.Conf.isValidInputType;

public class PreprocessorFactory {

    public static Preprocessor getPreprocessor(String typeSuggested) throws IllegalArgumentException {

        // Type check
        if (!isValidInputType(typeSuggested)) {
            throw new IllegalArgumentException(INVALID_INPUT_TYPE + " : " + typeSuggested);
        }

        InputType type = InputType.valueOf(typeSuggested);

        Preprocessor preprocessor = null;
        switch (type) {
            case GENELIST:
                preprocessor = new PreprocessorGenes();
                break;
            case UNIPROTLIST:
                preprocessor = new PreprocessorProteins();
                break;
            case ENSEMBLLIST:
                preprocessor = new PreprocessorEnsembl();
                break;
            case PEPTIDELIST:
                preprocessor = new PreprocessorPeptides();
                break;
            case RSIDLIST:
                preprocessor = new PreprocessorSnps();
                break;
            case VCF:
                preprocessor = new PreprocessorVCF();
                break;
            case PROTEOFORMS:
                preprocessor = new PreprocessorProteoforms();
                break;
            case PEPTIDELISTANDMODSITES:
                preprocessor = new PreprocessorModifiedPeptides();
                break;
            case UNKNOWN:
            	break;
        }

        return preprocessor;
    }
}
