package no.uib.pap.pathwaymatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import com.google.common.io.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.PathwayMatcher14;

class PathwayMatcherGeneticVariantsTest {

	@Test
	public void GIANTTest() throws IOException {
		String[] args = { "-t", "rsids", "-o", "output/GIANTTest/", "-i",
				"resources/input/GeneticVariants/RsId/GIANT.csv", "-tlp" };
		PathwayMatcher14.main(args);

		// Check the output file
		List<String> output = PathwayMatcher14.readFileFromResources("output/GIANTTest/search.txt");
		assertEquals(7279, output.size());

		List<String> statistics = Files.readLines(new File("output/GIANTTest/analysis.txt"), Charset.defaultCharset());
		assertEquals(628, statistics.size());
	}

//	@Test
//	public void cysticFibrosisTest() throws IOException {
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/RsId/CysticFibrosis.txt",
//				"-tlp" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(975, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(188, statistics.size());
//	}
//
//	@Test
//	public void cysticFibrosisWithChrAndBpTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/CysticFibrosis.txt",
//				"-tlp" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(979, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(188, statistics.size());
//	}
//
//	@Test
//	public void diabetesTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/RsId/Diabetes.txt",
//				"-tlp" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(6417, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(677, statistics.size());
//	}
//
//	@Test
//	public void diabetesWithChrAndBpTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/Diabetes.txt",
//				"-tlp" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(6417, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(677, statistics.size());
//	}
//
//	@Test
//	public void diabetesInYouthTest() throws IOException {
//		// Execute the full pathway matcher
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/RsId/DiabetesInYouth.txt" };
//		PathwayMatcher14.main(args);
//
//		// Verify the proteins mapped are correct
//		assertEquals(1, PathwayMatcher14.hitProteins.size());
//		assertTrue(PathwayMatcher14.hitProteins.contains("Q9NQB0"));
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(103, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(20, statistics.size());
//	}
//
//	@Test
//	public void diabetesInYouthWithChrAndBpTest() throws IOException{
//		// Execute the full pathway matcher
//		String[] args = { "-t", "rsidList", "-i",
//				"src/main/resources/input/GeneticVariants/Chr_Bp/DiabetesInYouth.txt" };
//		PathwayMatcher14.main(args);
//
//		// Verify the proteins mapped are correct
//		assertEquals(1, PathwayMatcher14.hitProteins.size());
//		assertTrue(PathwayMatcher14.hitProteins.contains("Q9NQB0"));
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(103, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(20, statistics.size());
//	}
//
//	@Test
//	public void huntingtonsDiseaseTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i",
//				"src/main/resources/input/GeneticVariants/RsId/HuntingtonsDisease.txt" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(350, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(114, statistics.size());
//	}
//
//	@Test
//	public void huntingtonsDiseaseWithChrAndBpTest() throws IOException {
//		String[] args = { "-t", "rsidList", "-i",
//				"src/main/resources/input/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(350, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(114, statistics.size());
//	}
//
//	@Test
//	public void HypoglycemiaTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt" };
//		PathwayMatcher14.main(args);
//
//		// Verify the proteins mapped are correct
//		assertEquals(7, PathwayMatcher14.hitProteins.size());
//		assertTrue(PathwayMatcher14.hitProteins.contains("P07550"));
//		assertTrue(PathwayMatcher14.hitProteins.contains("P23786"));
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(319, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(77, statistics.size());
//	}
//
//	@Test
//	public void HypoglycemiaWithChrAndBpTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/Chr_Bp/Hypoglycemia.txt" };
//		PathwayMatcher14.main(args);
//
//		// Verify the proteins mapped are correct
//		assertEquals(7, PathwayMatcher14.hitProteins.size());
//		assertTrue(PathwayMatcher14.hitProteins.contains("P07550"));
//		assertTrue(PathwayMatcher14.hitProteins.contains("P23786"));
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(319, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(77, statistics.size());
//	}
//
//	@Test
//	public void HypoglycemiaTestWithTopLevelPathways() throws IOException{
//		String[] args = { "-t", "rsidList", "-i", "src/main/resources/input/GeneticVariants/RsId/Hypoglycemia.txt",
//				"-tlp" };
//		PathwayMatcher14.main(args);
//
//		// Verify the proteins mapped are correct
//		assertEquals(7, PathwayMatcher14.hitProteoforms.size());
//		assertTrue(PathwayMatcher14.hitProteins.contains("P07550"));
//		assertTrue(PathwayMatcher14.hitProteins.contains("P23786"));
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(321, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(77, statistics.size());
//	}
//
//	@Test
//	public void UlcerativeColitisTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i",
//				"src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(7279, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(628, statistics.size());
//	}
//
//	@Test
//	public void UlcerativeColitisWithChrAndBpTest() throws IOException{
//		String[] args = { "-t", "rsidList", "-i",
//				"src/main/resources/input/GeneticVariants/RsId/UlcerativeColitis.txt" };
//		PathwayMatcher14.main(args);
//
//		// Check the output file
//		List<String> output = Files.readLines(new File("output.txt"), Charset.defaultCharset());
//		assertEquals(7279, output.size());
//
//		List<String> statistics = Files.readLines(new File("pathwayStatistics.csv"), Charset.defaultCharset());
//		assertEquals(628, statistics.size());
//	}
}