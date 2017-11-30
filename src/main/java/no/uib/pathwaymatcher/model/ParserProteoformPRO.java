package org.reactome.server.analysis.parser;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.core.model.Proteoform;
import org.reactome.server.analysis.parser.exception.ParserException;
import org.reactome.server.analysis.parser.response.Response;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reactome.server.analysis.parser.tools.InputPatterns.*;
import static org.reactome.server.analysis.parser.tools.ProteoformsProcessor.checkForProteoformsWithExpressionValues;

public class ParserProteoformPRO extends Parser {

    /**
     * Format rules:
     * - One proteoform per line
     * - Consists of a sequence block and optional modification blocks
     * - The only mandatory part is the accession number.
     * - There are one or more optional modification blocks
     * - Sequence blocks consist of a UniProtKB accession with an optional isoform indicated by a dash, followed
     * by a comma. And an optional subsequence range separated with a comma.
     * - Each modification block is presented in order from the N-terminal-most amino acid specified.
     * - Within a modification block there are one or more amino acids listed by type and position.
     * - Multiple amino-acids within a block are separated by forward slashes.
     * - Positions of modification are relative to the full length of the isoform.
     * - Missing a subsequence section indicates that the class encompasses either multiple species or isoforms.
     * - Missing modification blocks with a subsequence indicates that the class is defined by subsequence only.
     * - If there is a subsequence then the comma separator is added, otherwise is not added.
     * - If there is at least one modification block, an extra comma separator is added
     * - The line never ends in comma.
     * - There string ",," is never found.
     * - NOTE: In our casse we will only use the accession numbers and set of post translational modifications
     * to identify a particular proteoform, to make our analysis consistent with the rest of the formats.
     * - We allow the position to be null, so that it is also consistent with the rest.
     * - The missing coordinates are represented as "?" or "null" or "NULL", never left blank.
     * <p>
     * The draft of the format is at: doi: 10.1093/nar/gkw1075
     */

    private static final String COORDINATE = "(\\d{1,11}|([Nn][Uu][Ll][Ll])|\\?)";
    private static final String PROTEOFORM_PRO = "UniProtKB:([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?(," + COORDINATE + "-" + COORDINATE + ")?(,\\p{Alpha}{3}-(\\d{1,11}|[Nn][Uu][Ll][Ll])(\\/\\p{Alpha}{3}-(\\d{1,11}|([Nn][Uu][Ll][Ll])))*,MOD:\\d{5}(\\|\\p{Alpha}{3}-(\\d{1,11}|[Nn][Uu][Ll][Ll])(\\/\\p{Alpha}{3}-(\\d{1,11}|[Nn][Uu][Ll][Ll]))*,MOD:\\d{5})*)?";
    private static final String ONELINE_MULTIPLE_PROTEOFORM_PRO = "^\\s*" + PROTEOFORM_PRO + "(\\s+" + PROTEOFORM_PRO + ")*\\s*$";
    private static final Pattern PATTERN_PROTEOFORM_PRO = Pattern.compile(PROTEOFORM_PRO);
    private static final Pattern PATTERN_PROTEOFORM_PRO_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_PRO + EXPRESSION_VALUES);
    private static Logger logger = Logger.getLogger(ParserOriginal.class.getName());
    private boolean hasHeader = false;
    private int thresholdColumn = 0;            // Threshold number for columns, based on the first line we count columns. All the following lines must match this threshold.
    private int startOnLine = 0;                // Ignoring the initial blank lines and start parsing from the first valid line.

    public static boolean matches_Proteoform_Pro(String str) {
        Matcher m = PATTERN_PROTEOFORM_PRO.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Pro_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_PRO_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean check(String str) {
        Matcher m = PATTERN_PROTEOFORM_PRO.matcher(str);
        return m.find();
    }

    @Override
    public boolean flexibleCheck() {
        return false;
    }

    /**
     * Receives a trimmed line that has already been proved to follow the regex for PRO Proteoform.
     * <p>
     * The proteoform consists of five attributes: uniprot accession, isoform, start and end coordinates
     * and post-translational modifications.
     *
     * @param line
     * @param i
     * @return
     */
    public static Proteoform getProteoform(String line, int i) {

        Proteoform proteoform = new Proteoform("");
        StringBuilder protein = new StringBuilder();
        StringBuilder coordinate = null;
        List<Long> coordinateList = new ArrayList<>();
        StringBuilder mod = null;
        final int lineLength = line.length();

        int pos = 0;
        char c = line.charAt(pos);
        while (c != ':') {        // Read the database name section "UniProtKB:"
            c = line.charAt(++pos);
        }
        c = line.charAt(++pos);
        while (true) {        // Read the accession section
            protein.append(c);
            pos++;
            if (pos >= lineLength) {
                break;
            }
            c = line.charAt(pos);
            if (c == ',' || c == ' ' || c == '\t') {
                break;
            }
        }           // The proteoform should come at least until here
        proteoform.setUniProtAcc(protein.toString());
        if (c == ',') {
            pos++;      // Advance after the comma of the accession or out of the string
            if (pos < lineLength) {        // If there are still characters
                c = line.charAt(pos);

                // Read the next piece of text until the next '-'
                StringBuilder str = new StringBuilder();
                while (c != '-') {
                    str.append(c);
                    c = line.charAt(++pos);
                }

                // If it is a start coordinate
                if (StringUtils.isNumeric(str.toString()) || str.toString().equals("?") || str.toString().toLowerCase().equals("null")) {
                    proteoform.setStartCoordinate(interpretCoordinateString(str.toString()));
                    c = line.charAt(++pos);

                    //Read the endCoordinate
                    coordinate = new StringBuilder();
                    while (Character.isAlphabetic(c) || Character.isDigit(c) || c == '?') {
                        coordinate.append(c);
                        pos++;
                        if (pos >= lineLength) {
                            break;
                        }
                        c = line.charAt(pos);
                    }
                    proteoform.setEndCoordinate(interpretCoordinateString(coordinate.toString()));
                    pos++;
                }
                // If it was a PTM modified residue
                else {
                    proteoform.setStartCoordinate(null);
                    proteoform.setEndCoordinate(null);
                    pos -= str.toString().length();
                }
                // Here pos should e pointing at the comma or the next position after the end
                if (pos < lineLength) {
                    if (line.charAt(pos - 1) != ',') {
                        return proteoform;
                    }
                }
                // Here the pos should point to the comma; either after the uniprot accession or the subsequence ranges.
                while (pos < lineLength) {       //Read the post-translational modifications section
                    c = line.charAt(pos);           //While there are characters to read expect: \w{3}-\d+/PTM/PTM,MOD:#####

                    if (c == '|') {
                        c = line.charAt(++pos);
                        coordinateList = new ArrayList<>();
                    }
                    if (c == ' ' || c == '\t') {
                        break;
                    }
                    while (c != ',') {
                        while (c != '-') {
                            c = line.charAt(++pos);
                        }
                        c = line.charAt(++pos);
                        coordinate = new StringBuilder();
                        while (c != ',' && c != '/') {
                            coordinate.append(c);
                            c = line.charAt(++pos);
                        }
                        coordinateList.add(interpretCoordinateString(coordinate.toString()));
                    }
                    while (c != ':') {    // Skip the "MOD:"
                        c = line.charAt(++pos);
                    }
                    mod = new StringBuilder();
                    for (int I = 0; I < 5; I++) {
                        mod.append(line.charAt(++pos));
                    }
                    for (Long site : coordinateList) {
                        proteoform.addPtm(mod.toString(), site);
                    }
                    pos++;
                }
            }
        }

        return proteoform;
    }

    public static Proteoform getProteoform(String line) {
        return getProteoform(line, 0);
    }

    public static String getString(Proteoform proteoform) {
        StringBuilder str = new StringBuilder();

        //Print the protein accession
        str.append("UniProtKB:" + proteoform.getUniProtAcc());

        // Print the subsequence range
        if (proteoform.getStartCoordinate() != null || proteoform.getEndCoordinate() != null) {
            Long start = proteoform.getStartCoordinate();
            Long end = proteoform.getEndCoordinate();
            str.append("," + (start != null ? start : "?") + "-" + (end != null ? end : "?"));
        }

        if (proteoform.getPTMs().values().size() > 0) {
            str.append(",");
            String[] mods = proteoform.getPTMs().keySet().stream().toArray(String[]::new);
            for (int M = 0; M < mods.length; M++) {
                if (M != 0) {
                    str.append("|");
                }
                Long[] sites = new Long[proteoform.getPTMs().getElements(mods[M]).size()];
                sites = proteoform.getPTMs().getElements(mods[M]).toArray(sites);
                for (int S = 0; S < sites.length; S++) {
                    if (S != 0) {
                        str.append("/");
                    }
                    str.append(getResidue(mods[M]) + "-" + sites[S]);
                }
                str.append(",MOD:" + mods[M]);
            }
        }
        return str.toString();
    }

    private static String getResidue(String mod) {
        switch (mod) {
            case "00010":
            case "01631":
                return "Ala";
            case "00092":
            case "00012":
                return "Asn";
            case "00011":
            case "01632":
                return "Arg";
            case "00113":
            case "00014":
            case "01635":
            case "00094":
            case "00798":
                return "Cys";
            case "01637":
                return "Gln";
            case "00015":
            case "00041":
                return "Glu";
            case "01638":
                return "Gly";
            case "00018":
                return "His";
            case "00019":
                return "Ile";
            case "01641":
                return "Leu";
            case "00037":
            case "00130":
            case "00162":
            case "01148":
            case "01914":
            case "00083":
            case "01149":
            case "00064":
            case "00087":
                return "Lys";
            case "00023":
                return "Phe";
            case "00038":
            case "00039":
            case "01645":
            case "00024":
                return "Pro";
            case "00046":
            case "01646":
            case "00025":
                return "Ser";
            case "00047":
            case "00813":
            case "00026":
                return "Thr";
            case "00027":
            case "01648":
                return "Trp";
            case "00048":
                return "Tyr";
            case "01650":
                return "Val";
            default:
                return "XXX";
        }
    }

    @Override
    public void parseData(String input) throws ParserException {
        String clean = input.trim();

        if (clean.equalsIgnoreCase("")) {
            // no data to be analysed
            errorResponses.add(Response.getMessage(Response.EMPTY_FILE));
        } else {
            // Split lines
            String[] lines = input.split("\\R"); // Do not add + here. It will remove empty lines

            // check and parser whether one line file is present.
            int uniqueLineRow = isOneLineFile(lines);
            if (uniqueLineRow >= 0) {
                hasHeader = false;
                analyseOneLineFile(lines[uniqueLineRow]);
            } else {
                analyseHeaderColumns(lines);                    // Prepare header
                analyseContent(lines);                          // Prepare content
            }
        }

        if (errorResponses.size() >= 1) {
            logger.error("Error analysing your data");
            throw new ParserException("Error analysing your data", errorResponses);
        }
    }

    /**
     * For single line files the rules are:
     * 1- No Expressions Values
     * 2- Cannot start with #, comments
     * 3- Must match this "regular expression" (\delim)?ID((\delimID)* | (\delimNUMBER)* )
     * 4- where \delim can be space, comma, colon, semi-colon or tab.
     * <p>
     * In case it contains proteoforms:
     * 1, 2 still apply
     * Must match the format (\delim)?Proteoform((\delimProteoform)*)
     * Delimiters can be space or tab.
     */
    private void analyseOneLineFile(String line) {
        line = line.trim();
        // Line cannot start with # or //
        if (line.startsWith("#") || line.startsWith("//")) {
            errorResponses.add(Response.getMessage(Response.START_WITH_HASH));
            return;
        }

        line = PATTERN_SPACES.matcher(line).replaceAll(" "); // Remove all extra spaces
        StringTokenizer st = new StringTokenizer(line); //space is default delimiter.

        int tokens = st.countTokens();
        if (tokens > 0) {
            headerColumnNames.add(DEFAULT_IDENTIFIER_HEADER);
            while (st.hasMoreTokens()) {
                String[] content = {st.nextToken().trim()};
                boolean isValidToken = matches_Proteoform_Pro(content[0]);
                if (isValidToken) {
                    Proteoform proteoform = getProteoform(content[0], 1);
                    if (proteoform != null) {
                        AnalysisIdentifier rtn = new AnalysisIdentifier(proteoform.getUniProtAcc());
                        rtn.setPtms(proteoform.getPTMs());
                        rtn.setStartCoordinate(proteoform.getStartCoordinate());
                        rtn.setEndCoordinate(proteoform.getEndCoordinate());
                        analysisIdentifierSet.add(rtn);
                    } else {
                        isValidToken = false;
                    }
                }
                if (!isValidToken) {
                    errorResponses.add(Response.getMessage(Response.INVALID_TOKEN, content[0], "PRO"));
                    continue;
                }
            }
        }
    }

    /**
     * Analyse header based on the first line.
     * Headers must start with # or //
     * Note that the lines already come trimmed. Therefore the method  startsWithHeaderSign(headerLine) works.
     *
     * @param data is the file lines
     */
    private void analyseHeaderColumns(String[] data) {
        /**
         * Verify in which line the file content starts. Some cases, file has a bunch of blank line in the firsts lines.
         * StartOnLine will be important in the content analysis. Having this attribute we don't to iterate and ignore
         * blank lines in the beginning.
         */
        String firstLine = "";
        for (int R = 0; R < data.length; R++) {
            if (StringUtils.isNotEmpty(data[R])) {
                firstLine = data[R];
                startOnLine = R;
                break;
            }
        }

        hasHeader = firstLine.startsWith("#") || firstLine.startsWith("//");
        if (hasHeader) {
            headerColumnNames = getHeaderLabels(firstLine);
            thresholdColumn = headerColumnNames.size();
        } else {
            //warningResponses.add(Response.getMessage(Response.MALFORMED_HEADER));
            predictFirstLineAsHeader(firstLine);
        }
    }

    /**
     * Analyse all the data itself.
     * Replace any character like space, comma, semicolon, tab into a space and then replace split by space.
     *
     * @param content line array
     */
    private void analyseContent(String[] content) {
        if (hasHeader) {
            startOnLine += 1;
        }

        for (int i = startOnLine; i < content.length; ++i) {
            String line = content[i].trim();
            if (line.isEmpty()) {
                warningResponses.add(Response.getMessage(Response.EMPTY_LINE, i + 1));
                continue;
            }

            line = PATTERN_SPACES.matcher(line).replaceAll(" ");
            if (matches_Proteoform_Pro_With_Expression_Values(line)) {
                analyseContentLineWithOneProteoform(line, i);
            } else {
                errorResponses.add(Response.getMessage(Response.INVALID_PROTEOFORM_LINE, i + 1, Parser.ProteoformFormat.PRO));
                continue;
            }
        }
    }

    /**
     * Get header labels and also define a standard pattern in the column length
     *
     * @param line The line to be analysed as a header
     */
    private List<String> getHeaderLabels(String line) {

        List<String> headerLabelsList = new ArrayList<>();

        // remove chars which categorizes a comment.
        line = line.replaceAll("^(#|//)", "");

        // Split header line by our known delimiters
        String[] cols = line.split(SPACES);

        for (String columnName : cols) {
            headerLabelsList.add(StringEscapeUtils.escapeJava(columnName.trim()));
        }
        return headerLabelsList;
    }

    /**
     * Analyse a content line with one proteoform and possibly expression values.
     * Assumes that the line comes trimmed and the sets of consecutive spaces have been replaced for a single space.
     *
     * @param line The content line itself
     * @param i    Line number in the content of the input file
     */
    private void analyseContentLineWithOneProteoform(String line, int i) {

        Proteoform proteoform = getProteoform(line, i);

        // StringTokenizer has better performance than String.split().
        StringTokenizer st = new StringTokenizer(line); //space is default delimiter.

        int tokens = st.countTokens();
        if (tokens > 0) {
            // analyse if each line has the same amount of columns as the threshold based on first line
            if (thresholdColumn == tokens) {
                String first = st.nextToken();
                AnalysisIdentifier rtn = new AnalysisIdentifier(proteoform.getUniProtAcc());
                rtn.setStartCoordinate(proteoform.getStartCoordinate());
                rtn.setEndCoordinate(proteoform.getEndCoordinate());
                rtn.setPtms(proteoform.getPTMs());

                int j = 1;
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (isNumeric(token)) {
                        rtn.add(Double.valueOf(token));
                    } else {
                        warningResponses.add(Response.getMessage(Response.INLINE_PROBLEM, i + 1, j + 1));
                    }
                    j++;
                }
                analysisIdentifierSet.add(rtn);
            } else {
                errorResponses.add(Response.getMessage(Response.COLUMN_MISMATCH, i + 1, thresholdColumn, tokens));
            }
        }
    }

    /**
     * There're files which may have a header line but malformed.
     * This method analyse the first line and if the columns are not numeric
     * a potential header is present and the user will be notified.
     * At this point it was already checked if the header contained #|// at the beginning, so we are unsure if it is a header.
     *
     * @param firstLine potential header
     */
    private void predictFirstLineAsHeader(String firstLine) {

        int errorInARow = 0;
        List<String> columnNames = new LinkedList<>();

        firstLine = firstLine.replaceAll("^(#|//)", "");

        // Check if it is a regular line or a proteoform line
        String[] content = {firstLine};
        Parser.ProteoformFormat proteoformType = checkForProteoformsWithExpressionValues(content, 0);

        // Split the line in chunks of characters
        String[] chunks = firstLine.split(SPACES);

        // Count the number of chunks without only digits
        // Add each chunk to the list of column names.
        if (chunks.length > 0) {
            for (String col : chunks) {
                columnNames.add(col.trim());
                if (!isNumeric(col.trim())) {
                    errorInARow++;
                }
            }
        }

        thresholdColumn = chunks.length;

        // If 2 or more chunks of the row contain at least one letter, send warning that this may be a header
        // and use the first row as header.
        if (errorInARow >= 2) {
            hasHeader = true;
            warningResponses.add(Response.getMessage(Response.POTENTIAL_HEADER));
            headerColumnNames = columnNames;
        } else {
            // just skip the predictable header and use the default one
            hasHeader = false;
            warningResponses.add(Response.getMessage(Response.NO_HEADER));
            buildDefaultHeader(chunks.length);
        }
    }

    /**
     * The default header will be built based on the first line.
     * Example: For colsLength 5 the result would be:
     * ["", "col1", "col2", "col3", "col4"]
     *
     * @param colsLength
     */
    private void buildDefaultHeader(Integer colsLength) {
        thresholdColumn = colsLength;

        headerColumnNames.add(DEFAULT_IDENTIFIER_HEADER);
        for (int i = 1; i < colsLength; i++) {
            headerColumnNames.add(DEFAULT_EXPRESSION_HEADER + i);
        }
    }
}
