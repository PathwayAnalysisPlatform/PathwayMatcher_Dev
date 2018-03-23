package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import no.uib.pap.methods.search.Search;
import no.uib.pap.model.MessageStatus;
import no.uib.pap.model.ProteoformFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

class PathwayMatcherPeptidesTest {

    static String searchFile = "output/search.tsv";
    static String analysisFile = "output/analysis.tsv";

    @Test
    void insulinTest() throws IOException {
        String[] args = {
                "-t", "peptide",
                "-i", "resources/input/Peptides/insulinSignalPeptide.txt",
                "-o", "output/",
                "-tlp"
        };
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(2, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("F8WCM5"));
        assertTrue(PathwayMatcher.hitProteins.contains("P01308"));

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(159, output.size());

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(30, stats.size());
    }

    @Test
    void insulinRelatedSignalPeptidesTest() throws IOException {
        String[] args = {
                "-t", "peptides",
                "-i", "resources/input/Peptides/insulinRelatedSignalPeptides.txt",
                "-o", "output/",
                "-tlp",
                "-g"
        };
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(4, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("Q16270"));
        assertTrue(PathwayMatcher.hitProteins.contains("P35858"));
        assertTrue(PathwayMatcher.hitProteins.contains("P17936"));
        assertFalse(PathwayMatcher.hitProteins.contains("P17936-2"));
        assertTrue(PathwayMatcher.hitProteins.contains("P08069"));
        assertFalse(PathwayMatcher.hitProteins.contains("Q16270-2"));

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(89, output.size());

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(21, stats.size());
    }

    @Test
    void searchWithPeptideFillHitsTest1() {

        String[] args = {
                "-t", "peptides",
                "-i", "resources/input/Peptides/singlePeptide.txt",
                "-o", "output/",
                "-tlp",
                "-g"
        };
        PathwayMatcher.main(args);

        assertTrue(PathwayMatcher.hitProteins.contains("P01308"));
        Assertions.assertEquals(29, PathwayMatcher.hitPathways.size());
        assertTrue(PathwayMatcher.hitPathways.contains("R-HSA-264876"));
        assertTrue(PathwayMatcher.hitPathways.contains("R-HSA-74749"));
    }

    @Test
    void searchWithPeptidesFillHitsTest2() throws ParseException, IOException {

        String[] args = {
                "-t", "peptides",
                "-i", "resources/input/Peptides/peptideList2.txt",
                "-o", "output/",
                "-tlp",
                "-g"
        };
        PathwayMatcher.main(args);

        Assertions.assertEquals(11, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("P37088"));
        assertTrue(PathwayMatcher.hitProteins.contains("P01137"));
        Assertions.assertEquals(125, PathwayMatcher.hitPathways.size());
        assertTrue(PathwayMatcher.hitPathways.contains("R-HSA-2672351"));
        assertTrue(PathwayMatcher.hitPathways.contains("R-HSA-76002"));
        assertTrue(PathwayMatcher.hitPathways.contains("R-HSA-449147"));

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(649, output.size());

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(126, stats.size());

    }

}