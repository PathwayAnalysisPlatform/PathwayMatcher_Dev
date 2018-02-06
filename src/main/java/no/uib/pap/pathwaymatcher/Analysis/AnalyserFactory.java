package no.uib.pap.pathwaymatcher.Analysis;

import no.uib.pap.model.InputType;
import no.uib.pap.pathwaymatcher.Conf;

import static no.uib.pap.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.pathwaymatcher.Conf.isValidInputType;

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

        switch (InputType.valueOf(inputType)) {
            case UNIPROTLIST:
            case GENELIST:
            case ENSEMBLLIST:
            case PEPTIDELIST:
            case RSIDLIST:
            case VCF:
                return new AnalyserProteins();
            case PROTEOFORMS:
            case PEPTIDELISTANDMODSITES:
                return new AnalyserProteoforms();
            default:
                sendError(INVALID_INPUT_TYPE);
        }
        return null;
    }
}
