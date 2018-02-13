package no.uib.pap.pathwaymatcher.Matching;

import static no.uib.pap.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pap.model.Error.INVALID_MATCHING_TYPE;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pap.pathwaymatcher.Conf.isValidMatchingType;

import no.uib.pap.model.InputType;
import no.uib.pap.pathwaymatcher.Conf;

public class MatcherFactory {

    public static Matcher getMatcher(String inputType, String matchingType) {

        // Type check
        if (!isValidInputType(inputType)) {
            System.out.println("Invalid input type: " + inputType);
            System.exit(INVALID_INPUT_TYPE.getCode());
        }

        switch (InputType.valueOf(inputType)) {
            case UNIPROT:
            case GENES:
            case ENSEMBL:
            case PEPTIDES:
            case RSIDS:
            case VCF:
                return new MatcherProteins();
            case PROTEOFORMS:
            case MODIFIEDPEPTIDES:
                if(!isValidMatchingType(matchingType)){
                    sendError(INVALID_MATCHING_TYPE);
                }
                switch ( Conf.MatchType.valueOf(matchingType.toUpperCase())){
                    case STRICT:
                        return new MatcherProteoformsStrict();
                    case ONE:
                        return new MatcherProteoformsOne();
                    case FLEXIBLE:
                        return new MatcherProteoformsFlexible();
                }
            break;
            default:
                sendError(INVALID_INPUT_TYPE);
        }
        return null;
    }
}
