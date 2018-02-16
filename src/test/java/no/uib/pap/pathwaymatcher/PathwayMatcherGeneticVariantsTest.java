package no.uib.pap.PathwayMatcher14;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import com.google.common.io.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.uib.pap.model.Proteoform;
import no.uib.pap.PathwayMatcher14.Preprocessing.PreprocessorSnps;
import no.uib.pap.PathwayMatcher14.util.FileUtils;

class PathwayMatcher14GeneticVariantsTest {

    @Test
    public void GIANTTest() {
        String[] args = {"-t", "rsidList",
                "-o", "GIANTMapping.txt",
                "-i", "src/main/resources/input/GeneticVariants/RsId/GIANT.csv"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("GIANTMapping.txt"), Charset.defaultCharset());
        assertEquals(7279, output.size());

        List<String> statistics = Files.readLines( new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(628, statistics.size());
    }

    @Test
    public void cysticFibrosisTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/CysticFibrosis.txt",
                "-tlp"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(975, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(188, statistics.size());
    }

    @Test
    public void cysticFibrosisWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/CysticFibrosis.txt",
                "-tlp"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(979, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(188, statistics.size());
    }

    @Test
    public void diabetesTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Diabetes.txt",
                "-tlp"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(6417, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(677, statistics.size());
    }

    @Test
    public void diabetesWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/Diabetes.txt",
                "-tlp"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(6417, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(677, statistics.size());
    }

    @Test
    public void diabetesInYouthTest() {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/DiabetesInYouth.txt"};
        PathwayMatcher14.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher14.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher14.entities.size());
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("Q9NQB0")));

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(103, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(20, statistics.size());
    }

    @Test
    public void diabetesInYouthWithChrAndBpTest() {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/DiabetesInYouth.txt"};
        PathwayMatcher14.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher14.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(1, PathwayMatcher14.entities.size());
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("Q9NQB0")));

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(103, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(20, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/HuntingtonsDisease.txt"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(350, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(114, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(350, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(114, statistics.size());
    }

    @Test
    public void HypoglycemiaTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt"};
        PathwayMatcher14.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher14.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher14.entities.size());
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("P07550")));
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("P23786")));

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(319, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(77, statistics.size());
    }

    @Test
    public void HypoglycemiaWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/Hypoglycemia.txt"};
        PathwayMatcher14.main(args);

        // Verify the selected preprocessor is correct
        assertTrue(PathwayMatcher14.preprocessor.getClass().equals(PreprocessorSnps.class));

        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher14.entities.size());
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("P07550")));
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("P23786")));

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(319, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(77, statistics.size());
    }

    @Test
    public void HypoglycemiaTestWithTopLevelPathways() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt",
                "-tlp"};
        PathwayMatcher14.main(args);

       
        // Verify the proteins mapped are correct
        assertEquals(7, PathwayMatcher14.hitProteoforms.size());
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("P07550")));
        assertTrue(PathwayMatcher14.entities.contains(new Proteoform("P23786")));

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(321, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(77, statistics.size());
    }

    @Test
    public void UlcerativeColitisTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(7279, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(628, statistics.size());
    }

    @Test
    public void UlcerativeColitisWithChrAndBpTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt"};
        PathwayMatcher14.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(7279, output.size());

        List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(628, statistics.size());
    }
}