package no.uib.pap.pathwaymatcher;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.PathwayMatcher;
import no.uib.pap.pathwaymatcher.Preprocessing.PreprocessorPeptides;
import no.uib.pap.pathwaymatcher.Preprocessing.PreprocessorSnps;
import no.uib.pap.pathwaymatcher.util.FileUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherPeptidesTest {

    @Test
    void insulinTest() {
        String[] args = {"-t", "peptideList",
                "-i", "src/main/resources/input/Peptides/insulinSignalPeptide.txt",
                "-f", "src/main/resources/input/Peptides/insulin.fasta",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorPeptides.class));

        // Verify the proteins mapped are correct
        assertEquals(2, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("F8WCM5")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P01308")));

        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(111, output.size());

        List<String> stats = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(25, stats.size());
    }

    @Test
    void insulinSignalPeptideTest() {
        String[] args = {"-t", "peptideList",
                "-i", "src/main/resources/input/Peptides/insulinSignalPeptide.txt",
                "-f", "src/test/resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorPeptides.class));

        // Verify the proteins mapped are correct
        assertEquals(2, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("F8WCM5")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P01308")));

        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(111, output.size());

        List<String> stats = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(25, stats.size());
    }

    @Test
    void insulinRelatedSignalPeptidesTest() {
        String[] args = {"-t", "peptideList",
                "-i", "src/main/resources/input/Peptides/insulinRelatedSignalPeptides.txt",
                "-f", "src/test/resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorPeptides.class));

        // Verify the proteins mapped are correct
        assertEquals(6, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("Q16270")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P35858")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P17936")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P17936-2")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P08069")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("Q16270-2")));


        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(62, output.size());

        List<String> stats = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(17, stats.size());
    }

}