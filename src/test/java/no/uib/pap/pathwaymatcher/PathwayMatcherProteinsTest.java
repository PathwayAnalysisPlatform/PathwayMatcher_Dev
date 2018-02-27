package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

class PathwayMatcherProteinsTest {

    static String searchFile = "output/search.tsv";
    static String analysisFile = "output/analysis.tsv";

    @BeforeAll
    static void setUp() {
    }

    @Test
    public void singleProteinWithoutTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(147, output.size()); // Its 98 records + header
    }

    @Test
    public void singleProteinWithTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProtein.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(159, output.size());
    }

    @Test
    public void singleProteinWithIsoformTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(12 + 1, output.size());
    }

    @Test
    public void singleProteinWithIsoformAndTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", "output/",
                "-tlp"
        };
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(12 + 1, output.size());
    }

    @Test
    public void multipleProteinsTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/correctList.txt",
                "-o", "output/",
                "-tlp"
        };
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(365 + 1, output.size());
    }

    @Test
    public void multipleProteinsWithTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/correctList.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(377 + 1, output.size());
    }

    @Test
    public void hypoglycemiaProteinsTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "resources/input/Proteins/UniProt/Hypoglycemia.txt",
                "-o", "output/",
                "-m", "flexible",
                "-r", "3"};
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(79 + 1, output.size());
    }

    @Test
    void ensemblDiabetesInYouthTest() throws IOException {
        String[] args = {
                "-t", "ensembl",
                "-i", "resources/input/Proteins/Ensembl/DiabetesInYouth.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(230, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(17, analysis.size());
    }

    @Test
    public void ensemblCysticFibrosisTest() throws IOException {
        String[] args = {
                "-t", "ensembl",
                "-i", "resources/input/Proteins/Ensembl/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        PathwayMatcher.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(649, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(126, analysis.size()); // Its 98 records + header
    }

}