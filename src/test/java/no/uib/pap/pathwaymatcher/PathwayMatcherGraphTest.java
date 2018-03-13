package no.uib.pap.pathwaymatcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherGraphTest {

    static String verticesFile = "vertices.tsv";
    static String internalEdgesFile = "internalEdges.tsv";
    static String externalEdgesFile = "externalEdges.tsv";

    @BeforeEach
    void beforeEach() {
        try {
            java.nio.file.Files.deleteIfExists(Paths.get(verticesFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void verticesTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/verticesTest/",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);

        List<String> lines = Files.readLines(new File("output/verticesTest/" + verticesFile), Charset.defaultCharset());
        assertEquals(2, lines.size());
        assertTrue(lines.contains("P01308\tInsulin"));
    }

    @Test
    public void verticesTest2() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/verticesTest2/",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);

        List<String> lines = Files.readLines(new File("output/verticesTest2/" + verticesFile), Charset.defaultCharset());
        assertEquals(2, lines.size());
        assertTrue(lines.contains("P01308\tInsulin"));
    }

    @Test
    void createGraphSet2Test() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "resources/input/Proteins/UniProt/Set1.txt",
                "-o", "output/createGraphSet2Test/",
                "-tlp",
                "-g"};
        PathwayMatcher.main(args);

        List<String> vertices = Files.readLines(new File("output/createGraphSet2Test/" + verticesFile), Charset.defaultCharset());
        assertEquals(3, vertices.size());
        assertTrue(vertices.contains("P68871\tHemoglobin subunit beta"));
        assertTrue(vertices.contains("P69905\tHemoglobin subunit alpha"));

        List<String> internalEdges = Files.readLines(new File("output/createGraphSet2Test/" + internalEdgesFile), Charset.defaultCharset());
        assertTrue(internalEdges.contains("P68871\tP69905\tReaction\tR-HSA-2168885\toutput\tinput"));
        assertTrue(internalEdges.contains("P68871\tP69905\tComplex\tR-HSA-1237320\tcomponent\tcomponent"));

        List<String> externalEdges = Files.readLines(new File("output/createGraphSet2Test/" + externalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P00738\tP68871\tReaction\tR-HSA-6798745\tinput\tinput"));
        assertFalse(externalEdges.contains("P68871\tP00738\tReaction\tR-HSA-6798745\tinput\tinput"));
        assertTrue(externalEdges.contains("P68871\tQ86VB7\tReaction\tR-HSA-2230938\toutput\tinput"));
        assertTrue(externalEdges.contains("P69905\tQ86VB7\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
        assertFalse(externalEdges.contains("Q86VB7\tP69905\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P00738\tP68871\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
        assertFalse(externalEdges.contains("P68871\tP00738\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
    }

    @Test
    void createGraphInsulinTest() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/createGraphInsulinTest/",
                "-tlp",
                "-g"};
        PathwayMatcher.main(args);

        List<String> vertices = Files.readLines(new File("output/createGraphInsulinTest/" + verticesFile), Charset.defaultCharset());
        assertEquals(2, vertices.size());
        assertTrue(vertices.contains("P01308\tInsulin"));

        List<String> internalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + internalEdgesFile), Charset.defaultCharset());
        assertEquals(1, internalEdges.size());

        List<String> externalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + externalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P01308\tP29120\tReaction\tR-HSA-9023196\tinput\tcatalyst"));
        assertTrue(externalEdges.contains("P01308\tP63027\tReaction\tR-HSA-9023173\toutput\tcatalyst"));
        assertTrue(externalEdges.contains("P01308\tP63027\tReaction\tR-HSA-9023173\toutput\tcatalyst"));
        assertTrue(externalEdges.contains("P01308\tP60880\tReaction\tR-HSA-265166\tinput\tinput"));
        assertTrue(externalEdges.contains("P01308\tQ9NY47\tReaction\tR-HSA-265166\tinput\tregulator"));
        assertTrue(externalEdges.contains("P01308\tP54284\tReaction\tR-HSA-265166\tinput\tregulator"));
        assertTrue(externalEdges.contains("P01308\tP63096\tReaction\tR-HSA-265166\toutput\tregulator"));

        assertTrue(externalEdges.contains("P01308\tQ01484\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tQ12955\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tP35606\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
    }

    @Test
    void reactionNeighboursTest() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/createGraphInsulinTest/",
                "-tlp",
                "-g"};
        PathwayMatcher.main(args);

        List<String> vertices = Files.readLines(new File("output/createGraphInsulinTest/" + verticesFile), Charset.defaultCharset());
        assertEquals(2, vertices.size());
        assertTrue(vertices.contains("P01308\tInsulin"));

        List<String> internalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + internalEdgesFile), Charset.defaultCharset());
        assertEquals(1, internalEdges.size());

        List<String> externalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + externalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P01308\tP29120\tReaction\tR-HSA-9023196\tinput\tcatalyst"));
        assertTrue(externalEdges.contains("P01308\tP63027\tReaction\tR-HSA-9023173\toutput\tcatalyst"));
        assertTrue(externalEdges.contains("P01308\tP63027\tReaction\tR-HSA-9023173\toutput\tcatalyst"));
        assertTrue(externalEdges.contains("P01308\tP60880\tReaction\tR-HSA-265166\tinput\tinput"));
        assertTrue(externalEdges.contains("P01308\tQ9NY47\tReaction\tR-HSA-265166\tinput\tregulator"));
        assertTrue(externalEdges.contains("P01308\tP54284\tReaction\tR-HSA-265166\tinput\tregulator"));
        assertTrue(externalEdges.contains("P01308\tP63096\tReaction\tR-HSA-265166\toutput\tregulator"));

        assertTrue(externalEdges.contains("P01308\tQ01484\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tQ12955\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tP35606\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
    }

    @Test
    void complexNeighboursTest() {

    }

    @Test
    void setNeighboursTest() {

    }


}