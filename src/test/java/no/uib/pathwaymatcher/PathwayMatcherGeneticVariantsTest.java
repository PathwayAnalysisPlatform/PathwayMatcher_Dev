package no.uib.pathwaymatcher;

import no.uib.pathwaymatcher.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherGeneticVariantsTest {

    @Test
    public void cysticFibrosisTest() {
        String[] args = {"-t", "rsidList",
                         "-i", "src/main/resources/input/GeneticVariants/RsId/CysticFibrosis.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(978 + 1, output.size());    //Its 98 records + header

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(187+1, statistics.size());
    }

    @Test
    public void diabetesTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Diabetes.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(6281 + 1, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(676+1, statistics.size());
    }

    @Test
    public void diabetesInYouthTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/DiabetesInYouth.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(102 + 1, output.size());

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(19+1, statistics.size());
    }

    @Test
    public void huntingtonsDiseaseTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/HuntingtonsDisease.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(978 + 1, output.size());    //Its 98 records + header

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(187+1, statistics.size());
    }

    @Test
    public void HypoglycemiaTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(978 + 1, output.size());    //Its 98 records + header

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(187+1, statistics.size());
    }

    @Test
    public void UlcerativeColitisTest() {
        String[] args = {"-t", "rsidList",
                "-i", "src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(978 + 1, output.size());    //Its 98 records + header

        List<String> statistics = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(187+1, statistics.size());
    }
}