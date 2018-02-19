package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

class PathwayMatcherGenesTest {

	String [] args = { "-t", "genes", "-i", "resources/input/Genes/____.txt", "-o", "output/", "-tlp"};
	String searchFile = "output/search.txt";
	String analysisFile = "output/analysis.txt";
	
	@Test
	void genesDiabetesInYouthTest() throws IOException {
		args[3] = "resources/input/Genes/DiabetesInYouth.txt";
		PathwayMatcher14.main(args);

//		List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
//		assertEquals(44, search.size());
//
//		List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
//		assertEquals(17, analysis.size());
	}

}
