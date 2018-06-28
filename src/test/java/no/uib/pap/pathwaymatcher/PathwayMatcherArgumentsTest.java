package no.uib.pap.pathwaymatcher;

import com.google.common.io.Files;
import com.sun.org.glassfish.gmbal.Description;
import no.uib.pap.model.Error;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathwayMatcherArgumentsTest {

    static String analysisFile = "output/analysis.tsv";
    static String searchFile = "output/search.tsv";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void mainWithNoArgumentsTest() throws Exception {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.NO_ARGUMENTS.getCode());
        PathwayMatcher.main(new String[0]);
    }

    @Test
    public void missingRequiredOption_t_Test() {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-i", "input.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_t_Test() {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "-i", "input.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingRequiredOption_i_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(Error.NO_INPUT.getCode());
        String[] args = {
                "-t", "uniprotList", "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void inputFileNotFound_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COULD_NOT_READ_INPUT_FILE.getCode());
        String[] args = {
                "-t", "uniprot",
                "-i", "blabla.csv",
                "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_i_Test() {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-i"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_o_Test() {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-o", "-i", "resources/input/Proteins/UniProt/uniprot-all.list"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_r_Test() {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-r"};
        PathwayMatcher.main(args);
    }

    @Test
    public void matchingTypeTest() throws IOException {
        String[] args = {
                "-t", "proteoform",
                "-i", "resources/input/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", "output/",
                "-m", "superset"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(121, output.size());
    }

    @Test
    public void invalidMatchingTypeTest() {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.INVALID_MATCHING_TYPE.getCode());
        String[] args = {
                "-t", "proteoform",
                "-i", "resources/input/Proteoforms/multipleLinesWithIsoforms.txt",
                "-m", "blabla"};
        PathwayMatcher.main(args);
    }

    @Test
    public void couldNotWriteToOutputTest() {
        exit.expectSystemExitWithStatus(3);
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    public void printHelpTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-h"
        };
        PathwayMatcher.main(args);
    }

    @Test
    public void printHelpLongTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "--help",
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    @Description("Should ignore the help request.")
    public void printHelpNotFirstArgumentTest() {
        exit.expectSystemExitWithStatus(2);
        String[] args = {
                "-t", "uniprot",
                "--help",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    public void printHelpWithOtherArgumentsTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-h",
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    public void printVersionShortTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-v"
        };
        PathwayMatcher.main(args);
    }

    @Test
    public void printVersionLongTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "--version"
        };
        PathwayMatcher.main(args);
    }

    @Test
    public void printVersionLongFailTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-version"
        };
        PathwayMatcher.main(args);
    }
}