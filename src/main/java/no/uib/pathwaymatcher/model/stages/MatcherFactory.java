package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;

import static no.uib.pathwaymatcher.model.Error.INVALID_INPUT_TYPE;

public class MatcherFactory {

    public static Matcher getMatcher(String type) {

        switch (Conf.InputTypeEnum.valueOf(type)) {
            case uniprotList:
            case geneList:
            case ensemblList:
            case peptideList:
            case rsid:
            case rsidList:
            case vcf:
                return new MatcherProteins();
            break;
            case uniprotListAndModSites:
            case peptideListAndModSites:
                return new MatcherProteoforms();
            break;
            default:
                System.out.println(INVALID_INPUT_TYPE.getMessage());
                System.exit(INVALID_INPUT_TYPE.getCode());
                break;
        }
        return null;
    }
}
