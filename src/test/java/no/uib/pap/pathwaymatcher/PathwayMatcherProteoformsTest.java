package no.uib.pap.pathwaymatcher;

import static no.uib.pap.pathwaymatcher.tools.ListDiff.anyContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class PathwayMatcherProteoformsTest {

    static String searchFile = "output/search.tsv";
    static String analysisFile = "output/analysis.tsv";

    @Test
    public void insulinTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/Insulin.txt",
                "-o", "output/proteoforms/insulinTest/"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File("output/proteoforms/insulinTest/search.tsv"), Charset.defaultCharset());
        assertEquals(102, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertTrue(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File("output/proteoforms/insulinTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void insulinTlpTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/Insulin.txt",
                "-o", "output/proteoforms/insulinTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File("output/proteoforms/insulinTest/search.tsv"), Charset.defaultCharset());
        assertEquals(112, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertTrue(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File("output/proteoforms/insulinTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void insulinSupersetMatchingTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/Insulin.txt",
                "-o", "output/proteoforms/insulinTest/",
                "-m", "superset",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File("output/proteoforms/insulinTest/search.tsv"), Charset.defaultCharset());
        assertEquals(118, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertFalse(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File("output/proteoforms/insulinTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(15, analysis.size());
    }

    @Test
    public void insulinWithMODTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/InsulinWithMOD.txt",
                "-o", "output/proteoforms/insulinWithMODTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File("output/proteoforms/insulinWithMODTest/search.tsv"), Charset.defaultCharset());
        assertEquals(112, search.size());

        List<String> analysis = Files.readLines(new File("output/proteoforms/insulinWithMODTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void allProteoformsTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/ReactomeAllProteoformsSimple.csv",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(376936, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(1903, analysis.size());
    }

    @Test
    public void set1Test() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/SIMPLE/Set1.csv",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(177, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(22, analysis.size());
    }

    @Test
    public void set2SubsetTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/SIMPLE/Set2.csv",
                "-o", "output/Proteoforms/set2SubsetTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File("output/Proteoforms/set2SubsetTest/search.tsv"), Charset.defaultCharset());
        assertEquals(1, search.size());

        List<String> analysis = Files.readLines(new File("output/Proteoforms/set2SubsetTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(1, analysis.size());
    }

    @Test
    public void set2SupersetTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/SIMPLE/Set2.csv",
                "-o", "output/Proteoforms/set2SupersetTest/",
                "-m", "SUPERSET",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File("output/Proteoforms/set2SupersetTest/search.tsv"), Charset.defaultCharset());
        assertEquals(66, search.size());

        List<String> analysis = Files.readLines(new File("output/Proteoforms/set2SupersetTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(15, analysis.size());
    }

    @Test
    public void set3Test() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/SIMPLE/Set3.csv",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(732, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(131, analysis.size());
    }

    @Test
    public void singleProteoformSearchSupersetTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/SingleProteoform.txt",
                "-o", "output/",
                "-tlp",
                "-m", "superset"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(115, search.size());
        search.remove(0);
        for(String line : search){
            assertTrue(line.startsWith("O43561-2;\tO43561") || line.startsWith("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226\tO43561"));
        }

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(12, analysis.size());
    }

    @Test
    public void singleProteoformSearchStrictTest() throws IOException {
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/SingleProteoform.txt",
                "-o", "output/",
                "-tlp",
                "-m", "strict"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(108, search.size());
        search.remove(0);
        for(String line : search){
            assertFalse(line.startsWith("O43561-2;\tO43561"));
            assertTrue(line.startsWith("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226\tO43561"));
        }

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(12, analysis.size());
    }

    @Test
    public void proteoformsCysticFibrosisTest() throws IOException {
        String[] args = {
                "-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/CysticFibrosis.txt",
                "-o", "output/proteoforms/CysticFibrosisTest/",
                "-g",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File("output/proteoforms/CysticFibrosisTest/search.tsv"), Charset.defaultCharset());
        assertEquals(553, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File("output/proteoforms/CysticFibrosisTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(105, analysis.size()); // Its 98 records + header
    }

}