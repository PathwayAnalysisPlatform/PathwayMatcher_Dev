package org.reactome.server.analysis.parser;

import org.apache.commons.lang.StringUtils;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.parser.exception.ParserException;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_WINDOWS;

public abstract class Parser {

    protected List<String> headerColumnNames = new LinkedList<>();
    protected Set<AnalysisIdentifier> analysisIdentifierSet = new LinkedHashSet<>();
    protected List<String> warningResponses = new LinkedList<>();
    protected List<String> errorResponses = new LinkedList<>();

    /**
     * This is the default header for oneline file and multiple line file
     * Changing here will propagate in both.
     */
    protected static final String DEFAULT_IDENTIFIER_HEADER = "";
    protected static final String DEFAULT_EXPRESSION_HEADER = "col";

    public static enum ProteoformFormat {
        NONE,
        UNKNOWN,
        SIMPLE,
        PRO,
        PIR_ID,
        GPMDB,
        NEO4J
    }

    public List<String> getHeaderColumnNames() {
        return headerColumnNames;
    }

    public void setHeaderColumnNames(List<String> headerColumnNames) {
        this.headerColumnNames = headerColumnNames;
    }

    public Set<AnalysisIdentifier> getAnalysisIdentifierSet() {
        return analysisIdentifierSet;
    }

    public void setAnalysisIdentifierSet(Set<AnalysisIdentifier> analysisIdentifierSet) {
        this.analysisIdentifierSet = analysisIdentifierSet;
    }

    public List<String> getWarningResponses() {
        return warningResponses;
    }

    public void setWarningResponses(List<String> warningResponses) {
        this.warningResponses = warningResponses;
    }

    /**
     * Verifies if the input contains some part that can indentify it to a specific format allowing some minor errors.
     * @return
     */
    public abstract boolean flexibleCheck();

    /**
     * Fills the data structures above to send to the analysis methods.
     * @param input The contents of the user input file.
     */
    public abstract void parseData(String input) throws ParserException;

    /**
     * ---- FOR VERY SPECIFIC CASES, BUT VERY USEFUL FOR REACTOME ----
     * There're cases where the user inputs a file with one single line to be analysed
     * This method performs a quick view into the file and count the lines. It stops if file has more than one line.
     * p.s empty lines are always ignored
     * To avoid many iteration to the same file, during counting lines the main attributes are being set and used in the
     * analyse content method.
     * This method ignores blank lines,spaces, tabs and so on.
     *
     * @param input the file
     * @return If the file has one valid line returns the number of line. Otherwise returns a negative number.
     */
    protected int isOneLineFile(String[] input) {

        int lineNumber = -1;
        int countNonEmptyLines = 0;
        for (int N = 0; N < input.length; N++) {

            // Cleaning the line in other to eliminate blank or spaces spread in the file
            String cleanLine = input[N].trim();

            if (StringUtils.isNotEmpty(cleanLine) || StringUtils.isNotBlank(cleanLine)) {
                countNonEmptyLines++;
                lineNumber = N;

                // We don't need to keep counting...
                if (countNonEmptyLines > 1) {
                    lineNumber = -1;
                    break;
                }
            }
        }
        return lineNumber;
    }

}
