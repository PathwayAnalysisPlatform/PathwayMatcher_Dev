package no.uib.pap.pathwaymatcher;

import com.google.common.io.Files;
import no.uib.pap.model.ProteoformFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

import static no.uib.pap.pathwaymatcher.tools.ListDiff.anyContains;
import static no.uib.pap.pathwaymatcher.tools.ListDiff.anyMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathwayMatcherModifiedPeptidesTest {

    static String searchFile = "output/search.tsv";
    static String analysisFile = "output/analysis.tsv";
    static String fastaFile = "resources/uniprot-all.fasta";

    @Test
    public void insulinTest() throws IOException {
        String[] args = {"-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/Insulin.txt",
                "-o", "output/",
                "-f", fastaFile};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertTrue(anyContains("R-HSA-9006934", search));
        assertTrue(anyMatches("P01308", search));
        assertTrue(anyMatches("P01308;00798:95,00798:96,00798:100,00798:109\tP01308", search));
        assertTrue(anyMatches("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109\tP01308", search));

        assertEquals(102, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void insulinTlpTest() throws IOException {
        String[] args = {"-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/Insulin.txt",
                "-o", "output/",
                "-f", fastaFile,
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertTrue(anyContains("R-HSA-9006934", search));
        assertEquals(112, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void insulinSupersetMatchingTest() throws IOException {
        String[] args = {"-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/Insulin.txt",
                "-o", "output/modifiedPeptides/insulinSupersetMatchingTest/",
                "-m", "superset",
                "-f", fastaFile,
                "-tlp"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File("output/modifiedPeptides/insulinSupersetMatchingTest/search.tsv"), Charset.defaultCharset());
        assertEquals(118, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertFalse(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File("output/modifiedPeptides/insulinSupersetMatchingTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(15, analysis.size());
    }

    @Test
    public void insulinWithMODTest() throws IOException {
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/InsulinWithMOD.txt",
                "-o", "output/",
                "-f", fastaFile,
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(112, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void set1Test() throws IOException {
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/Set1.csv",
                "-o", "output/",
                "-f", fastaFile,
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());

        Assertions.assertFalse(anyContains("R-HSA-977225", search));

        assertTrue(anyMatches("P01308;00798:31,00798:43\tP01308\t.+\t.+\tR-HSA-392499", search));
        assertTrue(anyMatches("P01308;00798:31,00798:43\tP01308\tR-HSA-9023196\t.+\tR-HSA-264876", search));
        assertTrue(anyMatches("P01308;00798:31,00798:43\tP01308\tR-HSA-74711\t.+\tR-HSA-74751", search));
        assertTrue(anyMatches("P01308;\tP01308\tR-HSA-9023178\t.+\tR-HSA-2980736", search));
        assertTrue(anyMatches("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109\tP01308\tR-HSA-6809003\t.+\tR-HSA-199991", search));
        assertEquals(177, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(22, analysis.size());
    }

    @Test
    public void set2SubsetTest() throws IOException {
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/Set2.csv",
                "-o", "output/modifiedPeptides/set2SubsetTest/",
                "-f", fastaFile,
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File("output/modifiedPeptides/set2SubsetTest/search.tsv"), Charset.defaultCharset());
        assertEquals(1, search.size());

        List<String> analysis = Files.readLines(new File("output/modifiedPeptides/set2SubsetTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(1, analysis.size());
    }

    @Test
    public void set2SupersetTest() throws IOException {
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/Set2.csv",
                "-o", "output/modifiedPeptides/set2SupersetTest/",
                "-f", fastaFile,
                "-m", "superset",
                "-tlp"};
        PathwayMatcher.main(args);

        List<String> search = Files.readLines(new File("output/modifiedPeptides/set2SupersetTest/search.tsv"), Charset.defaultCharset());
        assertEquals(66, search.size());

        List<String> analysis = Files.readLines(new File("output/modifiedPeptides/set2SupersetTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(15, analysis.size());
    }

    @Test
    public void singleProteoformSearchSupersetTest() throws IOException {
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/SingleModifiedPeptide.txt",
                "-o", "output/",
                "-f", fastaFile,
                "-tlp",
                "-m", "superset"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(115, search.size());
        search.remove(0);
        for (String line : search) {
            assertTrue(line.startsWith("O43561-2;\tO43561") || line.startsWith("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226\tO43561"));
        }

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(12, analysis.size());
    }

    @Test
    public void singleProteoformSearchStrictTest() throws IOException {
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/SingleModifiedPeptide.txt",
                "-o", "output/",
                "-f", fastaFile,
                "-tlp",
                "-m", "strict"};
        PathwayMatcher.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(108, search.size());
        search.remove(0);
        for (String line : search) {
            assertFalse(line.startsWith("O43561-2;\tO43561"));
            assertTrue(line.startsWith("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226\tO43561"));
        }

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(12, analysis.size());
    }

    /*Tests that PathwayMatcher finds all possible combinations of PTMS when you put multiple peptides with 1+ ptms each. */

    @Test
    public void combinationsTest1() throws IOException {

        // It has the default matching type (SUBSET) to allow all combinations
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/O00141.txt",
                "-o", "output/",
                "-tlp",
                "-f", fastaFile};
        PathwayMatcher.main(args);

        // Check that there is a line with O00141;00046:422
        // And another line with O00141;00046:422,00047:256

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(29, search.size());

        boolean hasProteoform0 = false; //O00141
        boolean hasProteoform1 = false; //O00141;00046:422
        boolean hasProteoform2 = false; //O00141;00046:422,00047:256

        for (String line : search) {
            if (hasProteoform1 && hasProteoform2 && hasProteoform0) {
                break;
            }

            if(line.startsWith("O00141;\tO00141\t")){
                hasProteoform0 = true;
            }

            if (line.contains("O00141;00046:422\t")) {
                hasProteoform1 = true;
            }

            if (line.contains("O00141;00046:422,00047:256\t")) {
                hasProteoform2 = true;
            }
        }
        assertFalse(hasProteoform0);
        assertTrue(hasProteoform1);
        assertTrue(hasProteoform2);
    }

    /*O00203*/
    @Test
    public void combinationsTest2() throws IOException {

        // It has the default matching type (SUBSET) to allow all combinations
        String[] args = {
                "-t", "modifiedpeptide",
                "-i", "resources/input/ModifiedPeptides/P04049.txt",
                "-o", "output/",
                "-tlp",
                "-f", fastaFile};
        PathwayMatcher.main(args);

        // Check that there is a line with O00141;00046:422
        // And another line with O00141;00046:422,00047:256

        //Check the output file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());

        boolean hasProteoform0 = false; //P04049
        boolean hasProteoform1 = false; //P04049;00046:338
        boolean hasProteoform2 = false; //P04049;00046:621
        boolean hasProteoform3 = false; //P04049;00046:259,00046:621
        boolean hasProteoform4 = false; //P04049;00046:494,00046:621,00047:491,00048:341
        boolean hasProteoform5 = false; //P04049;00046:338,00046:494,00046:621,00047:491,00048:341
        boolean hasProteoform6 = false; //P04049;00046:29,00046:43,00046:259,00046:296,00046:301,00046:338,00046:494,00046:621,00046:642,00047:491,00048:341
        boolean hasProteoform7 = false; //P04049;00046:338,00048:340,00048:341

        for (String line : search) {
            if(line.startsWith("P04049\tP04049\t")){    // Proteoform 0
                hasProteoform0 = true;
            }

            if (line.contains("P04049;00046:338\t")) { // Proteoform 1
                hasProteoform1 = true;
            }

            if (line.contains("P04049;00046:621\t")) { // Proteoform 2
                hasProteoform2 = true;
            }

            if (line.contains("P04049;00046:259,00046:621\t")) { // Proteoform 3
                hasProteoform3 = true;
            }

            if (line.contains("P04049;00046:494,00046:621,00047:491,00048:341\t")) { // Proteoform 4
                hasProteoform4 = true;
            }

            if (line.contains("P04049;00046:338,00046:494,00046:621,00047:491,00048:341\t")) { // Proteoform 5
                hasProteoform5 = true;
            }

            if (line.contains("P04049;00046:29,00046:43,00046:259,00046:296,00046:301,00046:338,00046:494,00046:621,00046:642,00047:491,00048:341\t")) { // Proteoform 6
                hasProteoform6 = true;
            }

            if (line.contains("P04049;00046:338,00048:340,00048:341\t")) { // Proteoform 7
                hasProteoform7 = true;
            }
        }
        assertFalse(hasProteoform0);
        assertTrue(hasProteoform1);
        assertTrue(hasProteoform2);
        assertTrue(hasProteoform3);
        assertTrue(hasProteoform4);
        assertTrue(hasProteoform5);
        assertTrue(hasProteoform6);
        
        assertFalse(hasProteoform7);
    }

}