package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class PathwayMatcherGenesTest {

	String [] args = { "-t", "genes", "-i", "resources/input/Genes/____.txt", "-o", "output/", "-tlp"};
	String searchFile = "output/search.txt";
	String analysisFile = "output/analysis.txt";
	
	@Test
	void genesDiabetesInYouthTest() throws IOException {
		args[3] = "resources/input/Genes/DiabetesInYouth.txt";
		PathwayMatcher.main(args);

//		List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
//		assertEquals(44, search.size());
//
//		List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
//		assertEquals(17, analysis.size());
	}

}
