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

    static String[] args = {"-t", "uniprot", "-i", "resources/SampleInputs/Proteins/Valid/singleProtein.txt", "-o", "output/"};
    static String outputFile = "output/search.csv";

    static final String GET_MAPPING_BY_PROTEIN_LIST = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:ReactionLikeEvent{speciesName:\"Homo sapiens\"}),\n" +
            "      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"}),\n" +
            "      (pe)-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "      WHERE re.identifier IN [\"P01308\"]\n" +
            "RETURN DISTINCT re.identifier, rle.stId, rle.displayName, p.stId, p.displayName\n" +
            "ORDER BY rle.stId";

    static final String GET_MAPPING_BY_PROTEIN_LIST_WITH_TLP = "";

    @BeforeAll
    static void setUp() {
    }

    @Test
    public void singleProteinWithoutTopLevelPathwaysTest() throws IOException {
        args[3] = "resources/input/Proteins/Valid/singleProtein.txt";
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
        assertEquals(147, output.size()); // Its 98 records + header
    }

    @Test
    public void singleProteinWithTopLevelPathwaysTest() throws IOException {
        PathwayMatcher.main(new String[]{"-t", "uniprot", "-i", "resources/input/Proteins/Valid/singleProtein.txt", "-o", "output/", "-tlp"});

        // Check the output file
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
        assertEquals(159, output.size());
    }

    @Test
    public void singleProteinWithIsoformTest() throws IOException {
        args[3] = "resources/input/Proteins/Valid/singleProteinWithIsoform.txt";
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
        assertEquals(12 + 1, output.size());
    }

    @Test
    public void singleProteinWithIsoformAndTopLevelPathwaysTest() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "resources/input/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", "output/",
                "-tlp"
        };
        PathwayMatcher.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
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
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
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
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
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
        List<String> output = Files.readLines(new File(outputFile), Charset.defaultCharset());
        assertEquals(79 + 1, output.size());
    }

}