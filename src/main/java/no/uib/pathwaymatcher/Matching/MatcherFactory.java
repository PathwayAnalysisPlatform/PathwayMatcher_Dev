package no.uib.pathwaymatcher.Matching;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.stages.*;

import static no.uib.pathwaymatcher.Conf.isValidInputType;
import static no.uib.pathwaymatcher.Conf.isValidMatchingType;
import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pathwaymatcher.model.Error.INVALID_MATCHING_TYPE;
import static no.uib.pathwaymatcher.model.Error.sendError;

public class MatcherFactory {

    public static Matcher getMatcher(String inputType, String matchingType) {

        // Type check
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
                return new MatcherProteins();
            case uniprotListAndModSites:
            case peptideListAndModSites:
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
