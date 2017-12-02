package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.Conf;

public class ParserFactory {

    public static Parser createParser(String input) {
        if (ParserProteoformPRO.check(input)) {
            return new ParserProteoformPRO();
        } else if (ParserProteoformSimple.check(input)) {
            return new ParserProteoformSimple();
        }
        return new ParserProteoformNeo4j();
    }

    public static Parser createParser(Conf.ProteoformFormat type) {
        switch (type) {
            case NEO4J:
                return new ParserProteoformNeo4j();
            case PRO:
                return new ParserProteoformPRO();
            case SIMPLE:
                return new ParserProteoformSimple();
            default:
                return null;
        }
    }
}
