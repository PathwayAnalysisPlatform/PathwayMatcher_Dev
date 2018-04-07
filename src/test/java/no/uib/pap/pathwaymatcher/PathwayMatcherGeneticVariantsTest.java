package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PathwayMatcherGeneticVariantsTest {

    @Test
    public void GIANTTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-o", "output/",
                "-i", "resources/input/GeneticVariants/RsId/GIANT.csv"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.tsv"), Charset.defaultCharset());
        assertEquals(369707, search.size());

        List<String> analysis = Files.readLines(new File("output/analysis.tsv"), Charset.defaultCharset());
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
        List<String> search = Files.readLines(new File("output/search.tsv"), Charset.defaultCharset());
        assertEquals(6104, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.tsv"), Charset.defaultCharset());
        assertEquals(206, statistics.size());
    }

    @Test
    public void cysticFibrosisWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "CHRBPS",
                "-i", "resources/input/GeneticVariants/Chr_Bp/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.tsv"), Charset.defaultCharset());
        assertEquals(6104, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.tsv"), Charset.defaultCharset());
        assertEquals(206, statistics.size());
    }

    @Test
    public void cysticFibrosisWithVCFTest() throws IOException {
        String[] args = {"-t", "vcf",
                "-i", "resources/input/GeneticVariants/VCF/CysticFibrosis.txt",
                "-o", "output/cysticFibrosisWithVCFTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/cysticFibrosisWithVCFTest/search.tsv"), Charset.defaultCharset());
        assertEquals(6103, search.size());

        List<String> statistics = Files.readLines(new File("output/cysticFibrosisWithVCFTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(205, statistics.size());
    }

    @Test
    public void diabetesTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/Diabetes.txt",
                "-o", "output/diabetesTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesTest/search.tsv"), Charset.defaultCharset());
        assertEquals(12030, search.size());

        List<String> statistics = Files.readLines(new File("output/diabetesTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(717, statistics.size());
    }

    @Test
    public void diabetesWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbp",
                "-i", "resources/input/GeneticVariants/Chr_Bp/Diabetes.txt",
                "-o", "output/diabetesWithChrAndBpTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(11921, search.size());

        List<String> statistics = Files.readLines(new File("output/diabetesWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(717, statistics.size());
    }

    @Test
    public void diabetesInYouthTest() throws IOException {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/DiabetesInYouth.txt",
                "-o", "output/diabetesInYouthTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("Q9NQB0"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesInYouthTest/search.tsv"), Charset.defaultCharset());
        assertEquals(267, search.size());

        List<String> statistics = Files.readLines(new File("output/diabetesInYouthTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(24, statistics.size());
    }

    @Test
    public void diabetesInYouthWithChrAndBpTest() throws IOException {
        // Execute the full pathway matcher
        String[] args = {"-t", "chrbps",
                "-i", "resources/input/GeneticVariants/Chr_Bp/DiabetesInYouth.txt",
                "-o", "output/diabetesInYouthWithChrAndBpTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("Q9NQB0"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesInYouthWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(267, search.size());

        List<String> statistics = Files.readLines(new File("output/diabetesInYouthWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(24, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/HuntingtonsDisease.txt",
                "-o", "output/huntingtonsDiseaseTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/huntingtonsDiseaseTest/search.tsv"), Charset.defaultCharset());
        assertEquals(487, search.size());

        List<String> statistics = Files.readLines(new File("output/huntingtonsDiseaseTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(130, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbps",
                "-i", "resources/input/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt",
                "-o", "output/huntingtonsDiseaseWithChrAndBpTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/huntingtonsDiseaseWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(487, search.size());

        List<String> statistics = Files.readLines(new File("output/huntingtonsDiseaseWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(130, statistics.size());
    }

    @Test
    public void HypoglycemiaTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "resources/input/GeneticVariants/RsId/Hypoglycemia.txt",
                "-o", "output/HypoglycemiaTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("P07550"));
        assertTrue(PathwayMatcher.hitProteins.contains("P23786"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/HypoglycemiaTest/search.tsv"), Charset.defaultCharset());
        assertEquals(512, search.size());

        List<String> statistics = Files.readLines(new File("output/HypoglycemiaTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(83, statistics.size());
    }

    @Test
    public void HypoglycemiaWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbps",
                "-i", "resources/input/GeneticVariants/Chr_Bp/Hypoglycemia.txt",
                "-o", "output/HypoglycemiaWithChrAndBpTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.hitProteins.size());
        assertTrue(PathwayMatcher.hitProteins.contains("P07550"));
        assertTrue(PathwayMatcher.hitProteins.contains("P23786"));

        // Check the output file
        List<String> search = Files.readLines(new File("output/HypoglycemiaWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(512, search.size());

        List<String> statistics = Files.readLines(new File("output/HypoglycemiaWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(83, statistics.size());
    }

    @Test
    public void UlcerativeColitisTest() throws IOException {
        String[] args = {"-t", "rsid",
                "-i", "resources/input/GeneticVariants/RsId/UlcerativeColitis.txt",
                "-o", "output/UlcerativeColitisTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/UlcerativeColitisTest/search.tsv"), Charset.defaultCharset());
        assertEquals(17266, search.size());

        List<String> statistics = Files.readLines(new File("output/UlcerativeColitisTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(665, statistics.size());
    }

    @Test
    public void UlcerativeColitisWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbps",
                "-i", "resources/input/GeneticVariants/Chr_Bp/UlcerativeColitis.txt",
                "-o", "output/UlcerativeColitisWithChrAndBpTest/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/UlcerativeColitisWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(17193, search.size());

        List<String> statistics = Files.readLines(new File("output/UlcerativeColitisWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(665, statistics.size());
    }
}