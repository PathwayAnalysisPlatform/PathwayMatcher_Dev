package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.model.Proteoform;

import java.text.ParseException;

public abstract class Parser {

    public abstract Proteoform getProteoform(String line) throws ParseException;

    public abstract Proteoform getProteoform(String line, int row) throws ParseException;

    public abstract String getString(Proteoform proteoform);

    public static Long interpretCoordinateFromStringToLong(String s) {
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

    public static String interpretCoordinateFromLongToString(Long l){
        if(l == null || l == -1L){
            return "null";
        }
        else{
            return l.toString();
        }
    }
}
