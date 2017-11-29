package no.uib.pathwaymatcher.model.stages;

import com.sun.org.glassfish.gmbal.Description;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.uib.pathwaymatcher.model.stages.Preprocessor.readInput;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PreprocessorTest {

    private static final String PATH = "src/test/resources/Preprocessor/Generic/";

    @Test
    @Description("Test reading a file with Unix line endings LF")
    void lineEndUnixTest() {
        System.out.println();
        List<String> lines = readInput(PATH + "lineEndUnix.txt");
        assertEquals(10, lines.size());
    }

    @Test
    @Description("Test reading a file with Windows line endings CRLF")
    void lineEndWindowsTest() {
        List<String> lines = readInput(PATH + "lineEndWindows.txt");
        assertEquals(10, lines.size());
    }

    @Test
    @Description("Test reading a file with Mac line endings CR")
    void lineEndMacTest() {
        List<String> lines = readInput(PATH + "lineEndMac.txt");
        assertEquals(10, lines.size());
    }

}