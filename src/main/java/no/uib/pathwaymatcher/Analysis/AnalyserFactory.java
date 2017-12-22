package no.uib.pathwaymatcher.Analysis;

import no.uib.pathwaymatcher.Conf;

import static no.uib.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pathwaymatcher.model.Error.sendError;

/**
 * Creates and instance of the appropriate Analyser ( {@link AnalyserProteins} or {@link AnalyserProteoforms}) depending
 * on the type of input.
 */
public class AnalyserFactory {

    /**
     * Returns an instance of {@link AnalyserProteins} or {@link AnalyserProteoforms}.
     * @param inputType The string describing the input type.
     * @return The instance of a type of {@link Analyser}
     */
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
            case proteoforms:
            case peptideListAndModSites:
                return new AnalyserProteoforms();
            default:
                sendError(INVALID_INPUT_TYPE);
        }
        return null;
    }
}
