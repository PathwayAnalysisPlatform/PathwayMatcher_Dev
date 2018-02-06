package no.uib.pap.pathwaymatcher;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.PathwayMatcher;
import no.uib.pap.pathwaymatcher.Preprocessing.PreprocessorSnps;
import no.uib.pap.pathwaymatcher.util.FileUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathwayMatcherGeneticVariantsTest {

    @Test
    public void GIANTTest() {
        String[] args = {"-t", "rsidList",
                "-o", "GIANTMapping.txt",
                "-i", "src/main/resources/input/GeneticVariants/RsId/GIANT.csv"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("GIANTMapping.txt");
        assertEquals(7279, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(628, statistics.size());
    }

    @Test
    public void cysticFibrosisTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/CysticFibrosis.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(975, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(188, statistics.size());
    }

    @Test
    public void cysticFibrosisWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/CysticFibrosis.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(979, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(188, statistics.size());
    }

    @Test
    public void diabetesTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Diabetes.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(6417, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(677, statistics.size());
    }

    @Test
    public void diabetesWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/Diabetes.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(6417, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(677, statistics.size());
    }

    @Test
    public void diabetesInYouthTest() {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/DiabetesInYouth.txt"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("Q9NQB0")));

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(103, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(20, statistics.size());
    }

    @Test
    public void diabetesInYouthWithChrAndBpTest() {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/DiabetesInYouth.txt"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("Q9NQB0")));

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(103, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(20, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/HuntingtonsDisease.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(350, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(114, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(350, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(114, statistics.size());
    }

    @Test
    public void HypoglycemiaTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P07550")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P23786")));

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(319, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(77, statistics.size());
    }

    @Test
    public void HypoglycemiaWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/Hypoglycemia.txt"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P07550")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P23786")));

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(319, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(77, statistics.size());
    }

    @Test
    public void HypoglycemiaTestWithTopLevelPathways() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher.entities.size());
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P07550")));
        assertTrue(PathwayMatcher.entities.contains(new Proteoform("P23786")));

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(321, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(77, statistics.size());
    }

    @Test
    public void UlcerativeColitisTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(7279, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(628, statistics.size());
    }

    @Test
    public void UlcerativeColitisWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(7279, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(628, statistics.size());
    }
}