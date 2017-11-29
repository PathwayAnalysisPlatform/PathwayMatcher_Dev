package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.stages.*;

import static no.uib.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;

public class PreprocessorFactory {

    public static Preprocessor getPreprocessor(String typeSuggested) {

        println("\nPreprocessing input file...");

        // Type check
        if (!isValidInputType(typeSuggested)) {
            System.out.println("Invalid input type: " + typeSuggested);
            System.exit(INVALID_INPUT_TYPE.getCode());
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
            case rsid:
                preprocessor = new PreprocessorRsid();
                break;
            case rsidList:
                break;
            case vcf:
                break;
        }



        return preprocessor;
    }
}
