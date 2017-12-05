package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;

import static no.uib.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class FactoryAnalyser {

    public static Analyser getAnalyser(String inputType) {

        if (!isValidInputType(inputType)) {
            System.out.println("Invalid input type: " + inputType);
            System.exit(INVALID_INPUT_TYPE.getCode());
        }

        switch (Conf.InputTypeEnum.valueOf(inputType)) {
            case uniprotList:
            case geneList:
            case ensemblList:
            case peptideList:
            case rsid:
            case rsidList:
            case vcf:
                return new AnalyserProteins();
            case uniprotListAndModSites:
            case peptideListAndModSites:
                return new AnalyserProteoforms();
            default:
                sendError(INVALID_INPUT_TYPE);
        }
        return null;
    }
}
