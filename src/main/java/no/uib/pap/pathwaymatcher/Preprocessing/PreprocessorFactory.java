package no.uib.pap.pathwaymatcher.Preprocessing;

import static no.uib.pap.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pap.pathwaymatcher.Conf.isValidInputType;

import no.uib.pap.model.InputType;

public class PreprocessorFactory {

    public static Preprocessor getPreprocessor(String typeSuggested) throws IllegalArgumentException {

        // Type check
        if (!isValidInputType(typeSuggested)) {
            throw new IllegalArgumentException(INVALID_INPUT_TYPE + " : " + typeSuggested);
        }

        InputType type = InputType.valueOf(typeSuggested);

        Preprocessor preprocessor = null;
        switch (type) {
            case GENES:
                preprocessor = new PreprocessorGenes();
                break;
            case UNIPROT:
                preprocessor = new PreprocessorProteins();
                break;
            case ENSEMBL:
                preprocessor = new PreprocessorEnsembl();
                break;
            case PEPTIDES:
                preprocessor = new PreprocessorPeptides();
                break;
            case RSIDS:
                preprocessor = new PreprocessorSnps();
                break;
            case VCF:
                preprocessor = new PreprocessorVCF();
                break;
            case PROTEOFORMS:
                preprocessor = new PreprocessorProteoforms();
                break;
            case MODIFIEDPEPTIDES:
                preprocessor = new PreprocessorModifiedPeptides();
                break;
            case UNKNOWN:
            	break;
        }

        return preprocessor;
    }
}
