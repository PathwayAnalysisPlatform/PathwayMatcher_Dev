package no.uib.pathwaymatcher.stages;

import com.sun.org.glassfish.gmbal.Description;
import no.uib.pathwaymatcher.Conf;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.stages.Preprocessor.readInput;
import static no.uib.pathwaymatcher.stages.Preprocessor.validateVepTables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PreprocessorTest {

    private static final String PATH = "src/test/resources/Generic/";

    @Test
    @Description("Test reading a file with Unix line endings LF")
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

    @Test
    @Description("Test reading a file with Windows line endings CRLF")
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

    @Test
    @Description("Test reading a file with Mac line endings CR")
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

    @Test
    void vepDirectoryDoesNotExistTest(){
        try {
            validateVepTables("src/test/resources/SomeWrongDirectory/");
            fail("Should have sent NoSuchFileException for not finding the directory.");
        } catch (FileNotFoundException e) {
            fail("Should have sent NoSuchFileException for not finding the directory.");
        } catch (NoSuchFileException e) {

        }
    }

    @Test
    void vepTable1DoesNotExistTest(){
        try {
            Conf.setDefaultValues();
            validateVepTables("src/test/resources/Generic/GeneticVariants/broken23");
            fail("Should have sent FileNotFoundException for not finding table for chr 1.");
        } catch (FileNotFoundException e) {
            assertEquals(e.getMessage(), "The vep table for chromosome 1 was not found. Expected: src/test/resources/Generic/GeneticVariants/broken23/1.gz");
        } catch (NoSuchFileException e) {
            fail("Should have sent FileNotFoundException for not finding table for chr 1.");
        }
    }

    @Test
    void vepTable4DoesNotExistTest(){
        try {
            Conf.setDefaultValues();
            validateVepTables("src/test/resources/Generic/GeneticVariants/broken123");
            fail("Should have sent FileNotFoundException for not finding table for chr 4.");
        } catch (FileNotFoundException e) {
            assertEquals(e.getMessage(), "The vep table for chromosome 4 was not found. Expected: src/test/resources/Generic/GeneticVariants/broken123/4.gz");
        } catch (NoSuchFileException e) {
            fail("Should have sent FileNotFoundException for not finding table for chr 4.");
        }
    }

    @Test
    void vepTablesAreFineTest(){
        Conf.setDefaultValues();
        try {
            validateVepTables("src/main/resources/vep");
        } catch (FileNotFoundException e) {
            fail("Should find all the tables fine.");
        } catch (NoSuchFileException e) {
            fail("Should find all the tables fine.");
        }
    }

    @Test
    void pathHasEndSlash(){
        Conf.setDefaultValues();
        try {
            validateVepTables("src/main/resources/vep/");
        } catch (FileNotFoundException e) {
            fail("Should find all the tables fine.");
        } catch (NoSuchFileException e) {
            fail("Should find all the tables fine.");
        }
    }

    @Test
    void pathHasNoEndSlash(){
        Conf.setDefaultValues();
        try {
            validateVepTables("src/main/resources/vep");
        } catch (FileNotFoundException e) {
            fail("Should find all the tables fine.");
        } catch (NoSuchFileException e) {
            fail("Should find all the tables fine.");
        }
    }

}