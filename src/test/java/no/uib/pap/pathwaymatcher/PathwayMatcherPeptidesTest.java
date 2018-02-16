package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.Matching.PeptideMatcher;

class PathwayMatcherPeptidesTest {

	@Test
	void insulinTest() throws IOException{
		String[] args = { "-t", "peptideList", "-i", "src/main/resources/input/Peptides/insulinSignalPeptide.txt", "-f",
				"src/main/resources/input/Peptides/insulin.fasta", "-tlp" };
		PathwayMatcher14.main(args);

		// Verify the proteins mapped are correct
		assertEquals(2, PathwayMatcher14.hitProteins.size());
		assertTrue(PathwayMatcher14.hitProteins.contains("F8WCM5"));
		assertTrue(PathwayMatcher14.hitProteins.contains("P01308"));

		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(111, output.size());

		List<String> stats = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
		assertEquals(25, stats.size());
	}

	@Test
	void insulinSignalPeptideTest() throws IOException {
		String[] args = { "-t", "peptideList", "-i", "src/main/resources/input/Peptides/insulinSignalPeptide.txt", "-f",
				"src/test/resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta",
				"-tlp" };
		PathwayMatcher14.main(args);

		// Verify the proteins mapped are correct
		assertEquals(2, PathwayMatcher14.hitProteins.size());
		assertTrue(PathwayMatcher14.hitProteins.contains("F8WCM5"));
		assertTrue(PathwayMatcher14.hitProteins.contains("P01308"));

		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(111, output.size());

		List<String> stats = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
		assertEquals(25, stats.size());
	}

	@Test
	void insulinRelatedSignalPeptidesTest() throws IOException {
		String[] args = { "-t", "peptideList", "-i",
				"src/main/resources/input/Peptides/insulinRelatedSignalPeptides.txt", "-f",
				"src/test/resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta",
				"-tlp" };
		PathwayMatcher14.main(args);

		// Verify the proteins mapped are correct
		assertEquals(6, PathwayMatcher14.hitProteins.size());
		assertTrue(PathwayMatcher14.hitProteins.contains("Q16270"));
		assertTrue(PathwayMatcher14.hitProteins.contains("P35858"));
		assertTrue(PathwayMatcher14.hitProteins.contains("P17936"));
		assertTrue(PathwayMatcher14.hitProteins.contains("P17936-2"));
		assertTrue(PathwayMatcher14.hitProteins.contains("P08069"));
		assertTrue(PathwayMatcher14.hitProteins.contains("Q16270-2"));

		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(62, output.size());

		List<String> stats = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
		assertEquals(17, stats.size());
	}

}