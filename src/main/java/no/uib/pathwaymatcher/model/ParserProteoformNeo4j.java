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

public class ParserProteoformNeo4j extends Parser {

    public static final String PROTEOFORM_NEO4J = "\\\"{3}?([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?(\\\"{3})?,(-?\\d+|[Nn][Uu][Ll][Ll])?,(-?\\d+|[Nn][Uu][Ll][Ll])?,\\\"?\\[(\\\"\\\"\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll])\\\"\\\"(,\\\"\\\"\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll])\\\"\\\")*)?\\]\\\"?";
    private static final String ONELINE_MULTIPLE_PROTEOFORM_NEO4J = "^\\s*" + PROTEOFORM_NEO4J + "(\\s+" + PROTEOFORM_NEO4J + ")*\\s*$";
    private static final Pattern PATTERN_PROTEOFORM_NEO4J = Pattern.compile(PROTEOFORM_NEO4J);
    private static final Pattern PATTERN_PROTEOFORM_NEO4J_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_NEO4J + EXPRESSION_VALUES);
    private static final Pattern PATTERN_ONELINE_MULTIPLE_PROTEOFORM_NEO4J = Pattern.compile(ONELINE_MULTIPLE_PROTEOFORM_NEO4J);
    private static Logger logger = Logger.getLogger(ParserOriginal.class.getName());
    private boolean hasHeader = false;
    private int thresholdColumn = 0;            // Threshold number for columns, based on the first line we count columns. All the following lines must match this threshold.
    private int startOnLine = 0;                // Ignoring the initial blank lines and start parsing from the first valid line.

    public static boolean matches_Proteoform_Neo4j(String str) {
        Matcher m = PATTERN_PROTEOFORM_NEO4J.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Neo4j_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_NEO4J_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_oneLine_Neo4j_Proteoforms(String str) {
        Matcher m = PATTERN_ONELINE_MULTIPLE_PROTEOFORM_NEO4J.matcher(str);
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

    public static Proteoform getProteoform(String line) {
        return getProteoform(line, 0, new ArrayList<>());
    }

    /**
     * This method receives a line that has been validated to follow the structure of a simple proteoform with optional expression values.
     *
     * @param line
     * @param i
     * @param warningResponses
     * @return
     */
    public static Proteoform getProteoform(String line, int i, List<String> warningResponses) {

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

    public static String getString(Proteoform proteoform) {
        try {
            StringBuilder str = new StringBuilder();
            boolean isFirst = true;

            str.append(proteoform.getUniProtAcc());
            str.append(",");
            str.append(proteoform.getStartCoordinate());
            str.append(",");
            str.append(proteoform.getEndCoordinate());
            str.append(",");
            if (proteoform.getPTMs().size() > 0) {
                str.append("\"");
            }
            str.append("[");
            for (String mod : proteoform.getPTMs().keySet()) {
                for (Long coordinate : proteoform.getPTMs().getElements(mod)) {
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
            if (proteoform.getPTMs().size() > 0) {
                str.append("\"");
            }
            return str.toString();
        } catch (NullPointerException e) {
            System.out.println(proteoform.toString(Parser.ProteoformFormat.SIMPLE));
            System.out.println(e);
        }
        return null;
    }

    @Override
    public boolean flexibleCheck() {
        return false;
    }

    @Override
    public void parseData(String input) throws ParserException {
        String clean = input.trim();

        if (clean.equalsIgnoreCase("")) {
            // no data to be analysed
            errorResponses.add(Response.getMessage(Response.EMPTY_FILE));
        } else {
            // Split lines
            String[] lines = input.split("[\r\n]"); // Do not add + here. It will remove empty lines

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
                boolean isValidToken = matches_Proteoform_Neo4j(content[0]);
                if (isValidToken) {
                    Proteoform proteoform = getProteoform(content[0], 1, warningResponses);
                    if (proteoform != null) {
                        AnalysisIdentifier rtn = new AnalysisIdentifier(proteoform.getUniProtAcc());
                        rtn.setPtms(proteoform.getPTMs());
                        analysisIdentifierSet.add(rtn);
                    } else {
                        isValidToken = false;
                    }
                }
                if (!isValidToken) {
                    errorResponses.add(Response.getMessage(Response.INVALID_SINGLE_LINE, 1, thresholdColumn, tokens));
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
        ProteoformFormat proteoformType = checkForProteoformsWithExpressionValues(content, 0);

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
            if (matches_Proteoform_Neo4j_With_Expression_Values(line)) {
                analyseContentLineWithOneProteoform(line, i);
            } else {
                errorResponses.add(Response.getMessage(Response.INVALID_PROTEOFORM_LINE, i + 1, Parser.ProteoformFormat.SIMPLE));
                continue;
            }
        }
    }

    /**
     * Analyse a content line with one proteoform and possibly expression values.
     * Assumes that the line comes trimmed and the sets of consecutive spaces have been replaced for a single space.
     *
     * @param line The content line itself
     * @param i    Line number in the content of the input file
     */
    private void analyseContentLineWithOneProteoform(String line, int i) {

        Proteoform proteoform = getProteoform(line, i, warningResponses);

        // StringTokenizer has better performance than String.split().
        StringTokenizer st = new StringTokenizer(line); //space is default delimiter.

        int tokens = st.countTokens();
        if (tokens > 0) {
            // analyse if each line has the same amount of columns as the threshold based on first line
            if (thresholdColumn == tokens) {
                String first = st.nextToken();
                AnalysisIdentifier rtn = new AnalysisIdentifier(proteoform.getUniProtAcc());
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

}
