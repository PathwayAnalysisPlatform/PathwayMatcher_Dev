package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

class PathwayMatcherProteinsTest {

	@Test
	public void singleProteinWithoutTopLevelPathwaysTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/singleProtein.txt" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(98 + 1, output.size()); // Its 98 records + header
	}

	@Test
	public void singleProteinWithTopLevelPathwaysTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/singleProtein.txt",
				"-o", "singleProteinWithTopLevelPathwaysTest.txt", "-tlp" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("singleProteinWithTopLevelPathwaysTest.txt"), Charset.defaultCharset());
		assertEquals(110 + 1, output.size());
	}

	@Test
	public void singleProteinWithIsoformTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i",
				"src/test/resources/Generic/Proteins/Valid/singleProteinWithIsoform.txt" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(12 + 1, output.size());
	}

	@Test
	public void singleProteinWithIsoformAndTopLevelPathwaysTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i",
				"src/test/resources/Generic/Proteins/Valid/singleProteinWithIsoform.txt", "-tlp" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(12 + 1, output.size());
	}

	@Test
	public void multipleProteinsTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/correctList.txt" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(365 + 1, output.size());
	}

	@Test
	public void multipleProteinsWithTopLevelPathwaysTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i", "src/test/resources/Generic/Proteins/Valid/correctList.txt",
				"-tlp" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
		assertEquals(377 + 1, output.size());
	}

	@Test
	public void hypoglycemiaProteinsTest() throws IOException {
		String[] args = { "-t", "uniprotList", "-i", "src/main/resources/input/Proteins/UniProt/Hypoglycemia.txt",
				"-tlp", "-mt", "flexible", "-r", "3" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
		assertEquals(79 + 1, output.size());
	}

}