package no.uib.pathwaymatcher.model;

import java.text.ParseException;

public abstract class Parser {

    public static enum ProteoformFormat {
        NONE,
        UNKNOWN,
        SIMPLE,
        PRO,
        PIR_ID,
        GPMDB,
        NEO4J
    }

    public abstract Proteoform getProteoform(String line) throws ParseException;

    public abstract Proteoform getProteoform(String line, int row) throws ParseException;

    public abstract String getString(Proteoform proteoform);

    protected static Long interpretCoordinateString(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return null;
        }
        if (s.equals("?")) {
            return null;
        }
        if(s.toLowerCase().equals("null")){
            return null;
        }
        return Long.valueOf(s);
    }
}
