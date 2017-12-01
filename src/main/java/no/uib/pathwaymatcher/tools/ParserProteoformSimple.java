package no.uib.pathwaymatcher.tools;

import com.google.common.base.CharMatcher;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.tools.Parser;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static no.uib.pathwaymatcher.model.Warning.INVALID_ROW;
import static no.uib.pathwaymatcher.model.Warning.sendWarning;

public class ParserProteoformSimple extends Parser {

    public static final String PROTEOFORM_SIMPLE = "([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?;(\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))?(,\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))*";
    private static final Pattern PATTERN_PROTEOFORM_SIMPLE = Pattern.compile(PROTEOFORM_SIMPLE);

    public static boolean matches_Proteoform_Simple(String str) {
        Matcher m = PATTERN_PROTEOFORM_SIMPLE.matcher(str);
        return m.matches();
    }

    /**
     * Goes through the line to check if it contains at least a part that could uniquely identify the input to a specific format.
     *
     * @param input
     * @return
     */
    public static boolean check(String input) {
        Matcher m = PATTERN_PROTEOFORM_SIMPLE.matcher(input);
        return m.find();
    }

    @Override
    public Proteoform getProteoform(String line) throws ParseException {
        return getProteoform(line, 0);
    }

    /**
     * This method receives a line that has been validated to follow the structure of a simple proteoform with optional expression values.
     *
     * @param line
     * @param i
     * @return
     */
    @Override
    public Proteoform getProteoform(String line, int i) throws ParseException{

        Proteoform proteoform = new Proteoform("");
        StringBuilder protein = new StringBuilder();
        StringBuilder coordinate = null;
        StringBuilder mod = null;

        // Get the identifier
        // Read until end of line or semicolon
        int pos = 0;
        char c = line.charAt(pos);
        while (c != ';') {
            protein.append(c);
            pos++;
            if (pos == line.length())
                break;
            c = line.charAt(pos);
        }
        pos++;
        if (protein.length() == 0) {
            sendWarning(INVALID_ROW, i);
            throw new ParseException(INVALID_ROW.getMessage(), INVALID_ROW.getCode());
        }
        proteoform.setUniProtAcc(protein.toString());
        // Get ptms one by one
        //While there are characters

        while (pos < line.length()) {
            c = line.charAt(pos);
            if (!Character.isDigit(c)) {
                break;
            }
            coordinate = new StringBuilder();
            mod = new StringBuilder();
            //Read a ptm
            while (c != ':') {
                mod.append(c);
                pos++;
                c = line.charAt(pos);
            }
            pos++;
            c = line.charAt(pos);
            while (Character.isDigit(c) || CharMatcher.anyOf("nulNUL").matches(c)) {
                coordinate.append(c);
                pos++;
                if (pos == line.length())
                    break;
                c = line.charAt(pos);
            }
            proteoform.addPtm(mod.toString(), interpretCoordinateFromStringToLong(coordinate.toString()));
            if (c != ',') {
                break;
            }
            pos++;
        }

        return proteoform;
    }

    @Override
    public String getString(Proteoform proteoform) {
        StringBuilder str = new StringBuilder();
        str.append(proteoform.getUniProtAcc() + ";");
        String[] mods = proteoform.getPtms().keySet().stream().toArray(String[]::new);
        for (int M = 0; M < mods.length; M++) {
            Long[] sites = new Long[proteoform.getPtms().get(mods[M]).size()];
            sites = proteoform.getPtms().get(mods[M]).toArray(sites);
            for (int S = 0; S < sites.length; S++) {
                if (M != 0 || S != 0) {
                    str.append(",");
                }
                str.append(mods[M] + ":" +  interpretCoordinateFromLongToString(sites[S]));
            }
        }
        return str.toString();
    }

}
