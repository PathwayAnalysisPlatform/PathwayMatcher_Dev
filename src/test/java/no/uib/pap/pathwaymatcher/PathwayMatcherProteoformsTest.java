package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class PathwayMatcherProteoformsTest {

    @Test
    public void insulinTest() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "src/main/resources/input/Proteoforms/Simple/Insulin.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(109 + 1, output.size());

        List<String> stats = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(18 + 1, stats.size());
    }

    @Test
    public void insulinWithMODTest() throws IOException{
        String[] args = {"-t", "proteoforms",
                "-i", "src/main/resources/input/Proteoforms/Simple/InsulinWithMOD.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
        assertEquals(109 + 1, output.size());

        List<String> stats = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
        assertEquals(18 + 1, stats.size());
    }


}