package no.uib.pathwaymatcher.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserProteoformNeo4j extends Parser {

    public static final String PROTEOFORM_NEO4J = "\\\"{3}?([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?(\\\"{3})?,(-?\\d+|[Nn][Uu][Ll][Ll])?,(-?\\d+|[Nn][Uu][Ll][Ll])?,\\\"?\\[(\\\"\\\"\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll])\\\"\\\"(,\\\"\\\"\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll])\\\"\\\")*)?\\]\\\"?";
    private static final Pattern PATTERN_PROTEOFORM_NEO4J = Pattern.compile(PROTEOFORM_NEO4J);

    public static boolean matches_Proteoform_Neo4j(String str) {
        Matcher m = PATTERN_PROTEOFORM_NEO4J.matcher(str);
        return m.matches();
    }

    /**
     * Goes through the line to check if it contains at least a part that could uniquely identify the input to a specific format.
     *
     * @param input
     * @return
     */
    public static boolean check(String input) {
        Matcher m = PATTERN_PROTEOFORM_NEO4J.matcher(input);
        return m.find();
    }

    @Override
    public Proteoform getProteoform(String line) {
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
    public Proteoform getProteoform(String line, int i) {

        Proteoform proteoform = new Proteoform("");
        StringBuilder protein = new StringBuilder();
        StringBuilder coordinate = null;
        StringBuilder mod = null;

        // Get the identifier
        int pos = 0;
        char c = line.charAt(pos);
        while (c != ',') {
            if (c != '\"') {
                protein.append(c);
            }
            c = line.charAt(++pos);
        }
        proteoform.setUniProtAcc(protein.toString());
        //Here the pos points to the first comma
        c = line.charAt(++pos);
        // Here the pos points to the start coordinate content or its final comma
        // Get the start coordinate
        coordinate = new StringBuilder();
        while (c != ',') {
            coordinate.append(c);
            c = line.charAt(++pos);
        }
        proteoform.setStartCoordinate(interpretCoordinateString(coordinate.toString()));
        c = line.charAt(++pos);
        // Here the pos points to the end coordinate content or its final comma
        // Get the end coordinate
        coordinate = new StringBuilder();
        while (c != ',') {
            coordinate.append(c);
            c = line.charAt(++pos);
        }
        proteoform.setEndCoordinate(interpretCoordinateString(coordinate.toString()));
        // Here the pos points to the end coordinate field final comma
        //Get the PTMs
        while (c != ']') {

            if (Character.isDigit(c)) {   // Found a digit of a PSI-MOD id
                // Capture the 5 digits
                mod = new StringBuilder();
                for (int I = 0; I < 5; I++) {
                    mod.append(c);
                    c = line.charAt(++pos);
                }
                c = line.charAt(++pos);
                // Capture the coordinate
                coordinate = new StringBuilder();
                while (c != '\"') {
                    coordinate.append(c);
                    c = line.charAt(++pos);
                }
                // Add the PTM
                proteoform.addPtm(mod.toString(), (coordinate.toString().toLowerCase().equals("null") ? null : Long.valueOf(coordinate.toString())));
            }

            c = line.charAt(++pos);
        }

        return proteoform;
    }

    @Override
    public String getString(Proteoform proteoform) {
        try {
            StringBuilder str = new StringBuilder();
            boolean isFirst = true;

            str.append(proteoform.getUniProtAcc());
            str.append(",");
            str.append(proteoform.getStartCoordinate());
            str.append(",");
            str.append(proteoform.getEndCoordinate());
            str.append(",");
            if (proteoform.getPtms().keySet().size() > 0) {
                str.append("\"");
            }
            str.append("[");
            for (String mod : proteoform.getPtms().keySet()) {
                for (Long coordinate : proteoform.getPtms().get(mod)) {
                    if (!isFirst) {
                        str.append(',');
                    }
                    str.append("\"\"");
                    str.append(mod + ":" + coordinate);
                    str.append("\"\"");
                    isFirst = false;
                }
            }
            str.append("]");
            if (proteoform.getPtms().keySet().size() > 0) {
                str.append("\"");
            }
            return str.toString();
        } catch (NullPointerException e) {
            System.out.println(proteoform.toString(Parser.ProteoformFormat.SIMPLE));
            System.out.println(e);
        }
        return null;
    }

}
