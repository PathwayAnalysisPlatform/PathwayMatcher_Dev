package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.model.Proteoform;

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

    protected static Long interpretCoordinateFromStringToLong(String s) {
        if (s == null) {
            return -1L;
        }
        if (s.length() == 0) {
            return -1L;
        }
        if (s.equals("?")) {
            return -1L;
        }
        if(s.toLowerCase().equals("null")){
            return -1L;
        }
        return Long.valueOf(s);
    }

    protected static String interpretCoordinateFromLongToString(Long l){
        if(l == null || l == -1L){
            return "null";
        }
        else{
            return l.toString();
        }
    }
}
