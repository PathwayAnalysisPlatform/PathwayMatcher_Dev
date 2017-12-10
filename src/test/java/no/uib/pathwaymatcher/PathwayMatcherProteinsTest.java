package no.uib.pathwaymatcher;

import no.uib.pathwaymatcher.util.FileUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

class PathwayMatcherProteinsTest {

    @Test
    public void singleProteinWithoutTopLevelPathwaysTest() {
        String[] args = {"-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/singleProtein.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(98 + 1, output.size());    //Its 98 records + header
    }

    @Test
    public void singleProteinWithTopLevelPathwaysTest() {
        String[] args = {"-t", "uniprotList",
                "-i", "src/test/resources/Generic/Proteins/Valid/singleProtein.txt",
                "-o", "singleProteinWithTopLevelPathwaysTest.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("singleProteinWithTopLevelPathwaysTest.txt");
        assertEquals(110 + 1, output.size());
    }

    @Test
    public void singleProteinWithIsoformTest() {
        String[] args = {"-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/singleProteinWithIsoform.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(12 + 1, output.size());
    }

    @Test
    public void singleProteinWithIsoformAndTopLevelPathwaysTest() {
        Conf.setValue(Conf.BoolVars.showTopLevelPathways, true);
        String[] args = {"-t", "uniprotList",
                "-i", "src/test/resources/Generic/Proteins/Valid/singleProteinWithIsoform.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(12 + 1, output.size());
    }

    @Test
    public void multipleProteinsTest() {
        String[] args = {"-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/correctList.txt"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(365 + 1, output.size());
    }

    @Test
    public void multipleProteinsWithTopLevelPathwaysTest() {
        String[] args = {"-t", "uniprotList",
                "-i", "src/test/resources/Generic/Proteins/Valid/correctList.txt",
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("output.txt");
        assertEquals(377 + 1, output.size());
    }

    @Test
    public void hypoglycemiaProteinsTest() {
        String[] args = {"-t", "uniprotList",
                "-i", "src/main/resources/input/Proteins/UniProt/Hypoglycemia.txt",
                "-tlp",
                "-mt", "flexible",
                "-r", "3"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> output = FileUtils.getInput("pathwayStatistics.csv");
        assertEquals(93 + 1, output.size());
    }

}