package no.uib.pap.pathwaymatcher.stages;

import static no.uib.pap.pathwaymatcher.Preprocessing.Preprocessor.readInput;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.uib.pap.pathwaymatcher.Preprocessing.Preprocessor;
import no.uib.pap.pathwaymatcher.Preprocessing.PreprocessorSnps;

class PreprocessorTest {

    private static final String PATH = "src/test/resources/Generic/";
    private static Preprocessor preprocessorSnps;

    @BeforeAll
    static void setUp(){
        preprocessorSnps = new PreprocessorSnps();
    }

    @Test  //Test reading a file with Unix line endings LF
    void readInputlineEndUnixTest() {
        System.out.println();
        List<String> lines = null;
        try {
            lines = readInput(PATH + "lineEndUnix.txt");
        } catch (IOException e) {
            e.printStackTrace();
            fail("The file should be read without errors.");
        }
        assertEquals(10, lines.size());
    }

    @Test	//Test reading a file with Windows line endings CRLF
    void readInputlineEndWindowsTest() {
        List<String> lines = null;
        try {
            lines = readInput(PATH + "lineEndWindows.txt");
        } catch (IOException e) {
            e.printStackTrace();
            fail("The file should be read without errors.");
        }
        assertEquals(10, lines.size());
    }

    @Test	//Test reading a file with Mac line endings CR
    void readInputlineEndMacTest() {
        List<String> lines = null;
        try {
            lines = readInput(PATH + "lineEndMac.txt");
        } catch (IOException e) {
            e.printStackTrace();
            fail("The file should be read without errors.");
        }
        assertEquals(10, lines.size());
    }

}