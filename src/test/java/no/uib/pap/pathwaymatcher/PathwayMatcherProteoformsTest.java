package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
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
    static String verticesFile = "output/vertices.tsv";
    static String internalEdgesFile = "output/internalEdges.tsv";
    static String externalEdgesFile = "output/externalEdges.tsv";

    @Test
    public void insulinTest() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/Insulin.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(173, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(23, analysis.size());
    }

    @Test
    public void insulinWithMODTest() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/Simple/InsulinWithMOD.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(173, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(23, analysis.size());
    }

    @Test
    public void allProteoformsTest() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/ReactomeAllProteoformsSimple.csv",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(441276, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(1966, analysis.size());
    }

    @Test
    public void set1Test() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/SIMPLE/Set1.csv",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(284, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(38, analysis.size());
    }

    @Test
    public void set2Test() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "resources/input/Proteoforms/SIMPLE/Set2.csv",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(99, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(23, analysis.size());
    }

    @Test
    void createGraphSet2Test() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "resources/input/Proteins/UniProt/Set1.txt",
                "-o", "output/",
                "-tlp",
                "-g"};
        PathwayMatcher.main(args);

        List<String> vertices = Files.readLines(new File(verticesFile), Charset.defaultCharset());
        assertEquals(3, vertices.size());
        assertTrue(vertices.contains("P68871\tHemoglobin subunit beta"));
        assertTrue(vertices.contains("P69905\tHemoglobin subunit alpha"));

        List<String> internalEdges = Files.readLines(new File(internalEdgesFile), Charset.defaultCharset());
        assertTrue(internalEdges.contains("P68871\tP69905\tReaction"));
        assertTrue(internalEdges.contains("P69905\tP68871\tComplex"));

        List<String> externalEdges = Files.readLines(new File(externalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P68871\tP00738\tReaction"));
        assertTrue(externalEdges.contains("P68871\tQ86VB7\tReaction"));
        assertTrue(externalEdges.contains("P69905\tQ86VB7\tComplex"));
        assertTrue(externalEdges.contains("P68871\tP00738\tComplex"));
    }

    @Test
    void createGraphInsulinTest() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/",
                "-tlp",
                "-g"};
        PathwayMatcher.main(args);

        List<String> vertices = Files.readLines(new File(verticesFile), Charset.defaultCharset());
        assertEquals(2, vertices.size());
        assertTrue(vertices.contains("P01308\tInsulin"));

        List<String> internalEdges = Files.readLines(new File(internalEdgesFile), Charset.defaultCharset());
        assertEquals(1, internalEdges.size());

        List<String> externalEdges = Files.readLines(new File(externalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P01308\tP29120\tReaction"));
        assertTrue(externalEdges.contains("P01308\tP63027\tReaction"));
        assertTrue(externalEdges.contains("P01308\tP61764\tReaction"));
        assertTrue(externalEdges.contains("P01308\tP60880\tReaction"));
        assertTrue(externalEdges.contains("P01308\tQ9NY47\tReaction"));
        assertTrue(externalEdges.contains("P01308\tP54284\tReaction"));
        assertTrue(externalEdges.contains("P01308\tP63096\tReaction"));

        assertTrue(externalEdges.contains("P01308\tQ01484\tComplex"));
        assertTrue(externalEdges.contains("P01308\tQ12955\tComplex"));
        assertTrue(externalEdges.contains("P01308\tP35606\tComplex"));
    }

}