package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;

import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class FactoryPreprocessor {

    public static Preprocessor getPreprocessor(String typeSuggested) {

        logger.log(Level.INFO,"\nPreprocessing input file...");

        // Type check
        if (!isValidInputType(typeSuggested)) {
            sendError(INVALID_INPUT_TYPE);
        }

        Conf.InputTypeEnum type = Conf.InputTypeEnum.valueOf(typeSuggested);

        Preprocessor preprocessor = null;
        switch (type) {
            case geneList:
                break;
            case uniprotList:
                preprocessor = new PreprocessorProteins();
                break;
            case ensemblList:
                preprocessor = new PreprocessorEnsembl();
                break;
            case uniprotListAndModSites:
                preprocessor = new PreprocessorProteoforms();
                break;
            case peptideList:
                preprocessor = new PreprocessorPeptides();
                break;
            case peptideListAndModSites:
                preprocessor = new PreprocessorModifiedPeptides();
                break;
            case rsidList:
                preprocessor = new PreprocessorSnps();
                break;
            case vcf:
                preprocessor = new PreprocessorVCF();
                break;
        }

        return preprocessor;
    }
}
