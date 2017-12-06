package no.uib.pathwaymatcher.Preprocessing;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Preprocessing.PreprocessorModifiedPeptides;
import no.uib.pathwaymatcher.Preprocessing.PreprocessorPeptides;
import no.uib.pathwaymatcher.Preprocessing.PreprocessorProteoforms;
import no.uib.pathwaymatcher.Preprocessing.PreprocessorSnps;

import static no.uib.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;

public class PreprocessorFactory {

    public static Preprocessor getPreprocessor(String typeSuggested) throws IllegalArgumentException {

        // Type check
        if (!isValidInputType(typeSuggested)) {
            throw new IllegalArgumentException(INVALID_INPUT_TYPE + " : " + typeSuggested);
        }

        Conf.InputTypeEnum type = Conf.InputTypeEnum.valueOf(typeSuggested);

        Preprocessor preprocessor = null;
        switch (type) {
            case geneList:
                preprocessor = new PreprocessorGenes();
                break;
            case uniprotList:
                preprocessor = new PreprocessorProteins();
                break;
            case ensemblList:
                preprocessor = new PreprocessorEnsembl();
                break;
            case peptideList:
                preprocessor = new PreprocessorPeptides();
                break;
            case rsidList:
                preprocessor = new PreprocessorSnps();
                break;
            case vcf:
                preprocessor = new PreprocessorVCF();
                break;
            case uniprotListAndModSites:
                preprocessor = new PreprocessorProteoforms();
                break;
            case peptideListAndModSites:
                preprocessor = new PreprocessorModifiedPeptides();
                break;
        }

        return preprocessor;
    }
}
