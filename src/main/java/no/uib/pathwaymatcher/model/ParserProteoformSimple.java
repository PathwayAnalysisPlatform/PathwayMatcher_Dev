package org.reactome.server.analysis.parser;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.core.model.Proteoform;
import org.reactome.server.analysis.core.util.MapList;
import org.reactome.server.analysis.parser.exception.ParserException;
import org.reactome.server.analysis.parser.response.Response;
import org.reactome.server.analysis.parser.tools.InputPatterns;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reactome.server.analysis.parser.tools.InputPatterns.*;
import static org.reactome.server.analysis.parser.tools.ProteoformsProcessor.checkForProteoformsWithExpressionValues;

public class ParserProteoformSimple extends Parser {

    public static final String PROTEOFORM_SIMPLE = "([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?;(\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))?(,\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))*";
    private static final String ONELINE_MULTIPLE_PROTEOFORM_SIMPLE = "^\\s*" + PROTEOFORM_SIMPLE + "(\\s+" + PROTEOFORM_SIMPLE + ")*\\s*$";
    private static final Pattern PATTERN_PROTEOFORM_SIMPLE = Pattern.compile(PROTEOFORM_SIMPLE);
    private static final Pattern PATTERN_PROTEOFORM_SIMPLE_WITH_EXPRESSION_VALUES = Pattern.compile(PROTEOFORM_SIMPLE + EXPRESSION_VALUES);
    private static final Pattern PATTERN_ONELINE_PROTEOFORM_SIMPLE = Pattern.compile(ONELINE_MULTIPLE_PROTEOFORM_SIMPLE);
    private static Logger logger = Logger.getLogger(ParserOriginal.class.getName());
    private boolean hasHeader = false;
    private int thresholdColumn = 0;            // Threshold number for columns, based on the first line we count columns. All the following lines must match this threshold.
    private int startOnLine = 0;                // Ignoring the initial blank lines and start parsing from the first valid line.

    public static boolean matches_Proteoform_Simple(String str) {
        Matcher m = PATTERN_PROTEOFORM_SIMPLE.matcher(str);
        return m.matches();
    }

    public static boolean matches_Proteoform_Simple_With_Expression_Values(String str) {
        Matcher m = PATTERN_PROTEOFORM_SIMPLE_WITH_EXPRESSION_VALUES.matcher(str);
        return m.matches();
    }

    public static boolean matches_oneLine_Simple_Proteoforms(String str) {
        Matcher m = PATTERN_ONELINE_PROTEOFORM_SIMPLE.matcher(str);
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

        StringBuilder protein = new StringBuilder();
        StringBuilder coordinate = null;
        StringBuilder mod = null;
        MapList<String, Long> ptms = new MapList<>();

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
            warningResponses.add(Response.getMessage(Response.INLINE_PROBLEM, i + 1, 1));
            throw new RuntimeException("Problem parsing line " + line);
        }

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
            ptms.add(mod.toString(), (coordinate.toString().toLowerCase().equals("null") ? null : Long.valueOf(coordinate.toString())));
            if (c != ',') {
                break;
            }
            pos++;
        }

        return new Proteoform(protein.toString(), ptms);
    }

    public static String getString(Proteoform proteoform) {
        StringBuilder str = new StringBuilder();
        str.append(proteoform.getUniProtAcc() + ";");
        String[] mods = proteoform.getPTMs().keySet().stream().toArray(String[]::new);
        for (int M = 0; M < mods.length; M++) {
            Long[] sites = new Long[proteoform.getPTMs().getElements(mods[M]).size()];
            sites = proteoform.getPTMs().getElements(mods[M]).toArray(sites);
            for (int S = 0; S < sites.length; S++) {
                if (M != 0 || S != 0) {
                    str.append(",");
                }
                str.append(mods[M] + ":" + sites[S]);
            }
        }
        return str.toString();
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
                boolean isValidToken = matches_Proteoform_Simple(content[0]);
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
            if (matches_Proteoform_Simple_With_Expression_Values(line)) {
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
