package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;

class PathwayMatcherGenesTest {

	String [] args = { "-t", "genes", "-i", "resources/input/Genes/____.txt", "-o", "output/", "-tlp"};
	String searchFile = "output/search.tsv";
	String analysisFile = "output/analysis.tsv";
	
	@Test
	void genesDiabetesInYouthTest() throws IOException {
		args[3] = "resources/input/Genes/DiabetesInYouth.txt";
		PathwayMatcher.main(args);

		List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
		assertEquals(34, search.size());

		List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
		assertEquals(10, analysis.size());
	}

	@Test
	public void genesCysticFibrosisTest() throws IOException {
		args[3] = "resources/input/Genes/CysticFibrosis.txt";
		PathwayMatcher.main(args);

		// Check the search file
		List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
		assertEquals(539, search.size()); // Its 98 records + header

		List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
		assertEquals(105, analysis.size()); // Its 98 records + header
	}

}
