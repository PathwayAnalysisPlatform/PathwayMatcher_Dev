package no.uib.pap.pathwaymatcher;

import com.google.common.io.Files;
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
    static String verticesFile = "output/vertices.tsv";
    static String edgesFile = "output/vertices.tsv";

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
                "-t", "uniprotList",
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
                "-m", "flexible"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(159, output.size());
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
    public void createConnectionGraphTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(159, output.size());
    }

    @Test
    public void couldNotWriteToOutputTest() {
        exit.expectSystemExitWithStatus(3);
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "???/",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }
}