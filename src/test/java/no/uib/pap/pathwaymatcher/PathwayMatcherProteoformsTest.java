package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import no.uib.pap.pathwaymatcher.util.FileUtils;

public class PathwayMatcherProteoformsTest {

    @Test
    public void insulinTest() {
        String[] args = {"-t", "proteoforms",
                "-i", "src/main/resources/input/Proteoforms/Simple/Insulin.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(109 + 1, output.size());

        List<String> stats = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(18 + 1, stats.size());
    }

    @Test
    public void insulinWithMODTest() {
        String[] args = {"-t", "proteoforms",
                "-i", "src/main/resources/input/Proteoforms/Simple/InsulinWithMOD.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(109 + 1, output.size());

        List<String> stats = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(18 + 1, stats.size());
    }


}