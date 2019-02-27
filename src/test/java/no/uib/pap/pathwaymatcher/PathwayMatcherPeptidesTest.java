package no.uib.pap.pathwaymatcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static no.uib.pap.pathwaymatcher.tools.ListDiff.anyContains;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PathwayMatcherPeptidesTest {

    private static String searchFile = "output/search.tsv";
    private static String analysisFile = "output/analysis.tsv";
    private static String fastaFile = "resources/uniprot-all.fasta";

    @Test
    void insulinTest() throws IOException {
        String[] args = {
                "-t", "peptide",
                "-i", "resources/input/Peptides/insulinSignalPeptide.txt",
                "-o", "output/",
                "-tlp",
                "-f", fastaFile
        };
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(116, output.size());
        assertFalse(anyContains("F8WCM5", output));
        assertTrue(anyContains("P01308", output));

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(22, stats.size());
    }

    @Test
    void insulinRelatedSignalPeptidesTest() throws IOException {
        String[] args = {
                "-t", "peptides",
                "-i", "resources/input/Peptides/insulinRelatedSignalPeptides.txt",
                "-o", "output/",
                "-tlp",
                "-g",
                "-f", fastaFile
        };
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(125, output.size());
        assertTrue(anyContains("Q16270", output));
        assertTrue(anyContains("P35858", output));
        assertTrue(anyContains("P17936", output));
        assertFalse(anyContains("P17936-2", output));
        assertTrue(anyContains("P08069", output));
        assertFalse(anyContains("Q16270-2", output));

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(26, stats.size());
    }

    @Test
    void searchWithPeptideFillHitsTest1() throws IOException {

        String[] args = {
                "-t", "peptides",
                "-i", "resources/input/Peptides/singlePeptide.txt",
                "-o", "output/",
                "-tlp",
                "-g",
                "-f", fastaFile
        };
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertTrue(anyContains("P01308", output));
        assertTrue(anyContains("R-HSA-264876", output));
        assertTrue(anyContains("R-HSA-74749", output));
    }

    @Test
    void searchWithPeptidesFillHitsTest2() throws IOException {

        String[] args = {
                "-t", "peptides",
                "-i", "resources/input/Peptides/peptideList2.txt",
                "-o", "output/",
                "-tlp",
                "-g",
                "-f", fastaFile
        };
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(539, output.size());
        assertTrue(anyContains("P37088", output));
        assertTrue(anyContains("P01137", output));
        assertTrue(anyContains("R-HSA-2672351", output));
        assertTrue(anyContains("R-HSA-76002", output));
        assertTrue(anyContains("R-HSA-449147", output));

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(105, stats.size());

    }

}