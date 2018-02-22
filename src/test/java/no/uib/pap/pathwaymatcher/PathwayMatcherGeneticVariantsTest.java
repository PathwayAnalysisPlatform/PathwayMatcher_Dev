package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;

import java.util.List;

import org.junit.jupiter.api.Test;

class PathwayMatcherGeneticVariantsTest {

    @Test
    public void GIANTTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-o", "output/GIANTTest/",
                "-i", "resources/input/GeneticVariants/RsId/GIANT.csv",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(369707, search.size());

        List<String> analysis = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(1964, analysis.size());
    }

    @Test
    public void cysticFibrosisTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(975, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(188, statistics.size());
    }

    @Test
    public void cysticFibrosisWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/Chr_Bp/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(979, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(188, statistics.size());
    }

    @Test
    public void diabetesTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/Diabetes.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(6417, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(677, statistics.size());
    }

    @Test
    public void diabetesWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/Chr_Bp/Diabetes.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(6417, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(677, statistics.size());
    }

    @Test
    public void diabetesInYouthTest() throws IOException {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/DiabetesInYouth.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("Q9NQB0"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(103, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(20, statistics.size());
    }

    @Test
    public void diabetesInYouthWithChrAndBpTest() throws IOException {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/Chr_Bp/DiabetesInYouth.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("Q9NQB0"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(103, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(20, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/HuntingtonsDisease.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(350, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(114, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(350, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(114, statistics.size());
    }

    @Test
    public void HypoglycemiaTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/Hypoglycemia.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("P07550"));
        assertTrue(PathwayMatcher.hitProteins.contains("P23786"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(319, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(77, statistics.size());
    }

    @Test
    public void HypoglycemiaWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/Chr_Bp/Hypoglycemia.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("P07550"));
        assertTrue(PathwayMatcher.hitProteins.contains("P23786"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(319, search);

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(77, statistics.size());
    }

    @Test
    public void HypoglycemiaTestWithTopLevelPathways() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/Hypoglycemia.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.hitProteoforms.size());
        assertTrue(PathwayMatcher.hitProteins.contains("P07550"));
        assertTrue(PathwayMatcher.hitProteins.contains("P23786"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(321, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(77, statistics.size());
    }

    @Test
    public void UlcerativeColitisTest() throws IOException {
        String[] args = {"-t", "rsid",
                "-i", "resources/input/GeneticVariants/RsId/UlcerativeColitis.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(7279, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(628, statistics.size());
    }

    @Test
    public void UlcerativeColitisWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/UlcerativeColitis.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.csv"), Charset.defaultCharset());
        assertEquals(7279, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.csv"), Charset.defaultCharset());
        assertEquals(628, statistics.size());
    }
}