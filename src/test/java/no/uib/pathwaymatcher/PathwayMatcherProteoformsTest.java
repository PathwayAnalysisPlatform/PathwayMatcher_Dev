package no.uib.pathwaymatcher;

import no.uib.pathwaymatcher.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.List;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_READ_CONF_FILE;
import static org.junit.Assert.*;

public class PathwayMatcherProteoformsTest {

    @Before
    public void setUp() throws Exception {
        Conf.setDefaultValues();
    }

    @After
    public void tearDown() throws Exception {
    }

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

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
}