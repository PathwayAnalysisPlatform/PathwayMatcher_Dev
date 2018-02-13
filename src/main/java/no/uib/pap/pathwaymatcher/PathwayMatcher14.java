package no.uib.pap.pathwaymatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;

import no.uib.pap.model.InputType;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Pathway;

/**
 * Retrieves the pathways and reactions that contain the input entities as
 * participants.
 * 
 * @author Francisco
 *
 */
public class PathwayMatcher14 {

	/**
	 * The object to hold the command line arguments for PathwayMatcher.
	 */
	public static Options options;
	public static CommandLine commandLine;

	static List<String> input;
	static FileWriter outputSearch;
	static FileWriter outputAnalysis;
	static String outputPath = "";

	static String separator = "\t";

	static TreeMultimap<String, String> mapGenesToProteins;
	static TreeMultimap<String, String> mapEnsemblToProteins;
	static TreeMultimap<Proteoform, String> mapProteoformsToReactions;
	static TreeMultimap<String, String> mapProteinsToReactions;
	static TreeMultimap<String, String> mapReactionsToPathways;
	static TreeMultimap<String, String> mapPathwaysToTopLevelPathways;
	static HashMap<String, Pathway> pathways;
	static HashMap<String, String> reactions;

	public static void main(String[] args) {

		// ******** ******** Read and process command line arguments ******** ********
		options = new Options();

		addOption("t", "type", true, "Input type: GENES|ENSEMBL|UNIPROT|PEPTIDES|RSIDS|PROTEOFORMS", true);
		addOption("r", "range", true, "Ptm sites margin of error", false);
		addOption("tlp", "toplevelpathways", false, "Show \"Top Level Pathways\" in the output", false);
		addOption("m", "matching", true, "Proteoform match criteria: EXACT|ONE|FLEXIBLE", false);
		addOption("i", "input", true, "Input file", true);
		addOption("o", "output", true, "Output path", false);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		try {
			commandLine = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
		}

		// ******** ******** Read input ******** ********

		File file = new File(commandLine.getOptionValue("i"));
		try {
			input = Files.readLines(file, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("The input file was not found."); // TODO Error
		}

		// ******** ******** Create output files ******** ********
		if (commandLine.hasOption("o")) {
			outputPath = commandLine.getOptionValue("o");
			outputPath = outputPath.endsWith("/") ? outputPath : outputPath + "/";
		}
		try {
			file = new File(outputPath + "search.csv");
			file.getParentFile().mkdirs();
			outputSearch = new FileWriter(file);
			outputAnalysis = new FileWriter(outputPath + "analysis.csv");

			// ******** ******** Process the input ******** ********

			InputType type = InputType.valueOf(commandLine.getOptionValue("type").toUpperCase());
			switch (type) {
			case GENES:
				mapGenes();
				break;
			case ENSEMBL:
				break;
			case UNIPROT:
				mapProteins();
				break;
			case PROTEOFORMS:
				break;
			case RSIDS:
				BufferedReader br;
				try {
					br = getBufferedReaderFromResource("1.gz");
					for (int I = 0; I < 10; I++) {
						System.out.println(br.readLine());
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case VCF:
				break;
			case PEPTIDES:
				break;
			case MODIFIEDPEPTIDES:
				break;
			default:
				System.out.println("Input type not supported.");
				break;
			}
			outputSearch.close();
			outputAnalysis.close();
		} catch (IOException e1) {
			System.out.println("Could not create the output files: \n  " + outputPath + "search.txt\n  " + outputPath
					+ "analysis.txt"); // TODO Send correct code and message
			e1.printStackTrace();
		}
	}

	private static void mapProteins() {
		// TODO Auto-generated method stub

	}

	private static void mapGenes() throws IOException {

		// Load needed static maps
		reactions = (HashMap<String, String>) getSerializedObject("reactions.gz");
		pathways = (HashMap<String, Pathway>) getSerializedObject("pathways.gz");
		mapGenesToProteins = (TreeMultimap<String, String>) getSerializedObject("mapGenesToProteins.gz");
		mapProteinsToReactions = (TreeMultimap<String, String>) getSerializedObject("mapProteinsToReactions.gz");
		mapReactionsToPathways = (TreeMultimap<String, String>) getSerializedObject("mapReactionsToPathways.gz");
		mapPathwaysToTopLevelPathways = null;

		if (commandLine.hasOption("tlp")) {
			mapPathwaysToTopLevelPathways = (TreeMultimap<String, String>) getSerializedObject(
					"mapPathwaysToTopLevelPathways.gz");
		}

		// Generate search result
		outputSearch.write("GENE" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
				+ "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String gene : input) {
			// System.out.println(gene);
			for (String protein : mapGenesToProteins.get(gene)) {
				// System.out.println("--" + protein);
				for (String reaction : mapProteinsToReactions.get(protein)) {
					// System.out.println("----" + reaction);
					for (String pathway : mapReactionsToPathways.get(reaction)) {
						// System.out.println("------" + pathway);
						if (commandLine.hasOption("tlp")) {
							if (mapPathwaysToTopLevelPathways.get(pathway).size() > 0) {
								for (String topLevelPathway : mapPathwaysToTopLevelPathways.get(pathway)) {
									outputSearch.write(gene + separator + protein + separator + reaction + separator
											+ reactions.get(reaction) + separator + pathway + separator
											+ pathways.get(pathway).getDisplayName() + topLevelPathway + separator
											+ pathways.get(topLevelPathway).getDisplayName() + "\n");
								}
							} else {
								outputSearch.write(gene + separator + protein + separator + reaction + separator
										+ reactions.get(reaction) + separator + pathway + separator
										+ pathways.get(pathway).getDisplayName() + separator + pathway + separator
										+ pathways.get(pathway).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(gene + separator + protein + separator + reaction + separator
									+ reactions.get(reaction) + separator + pathway + separator
									+ pathways.get(pathway).getDisplayName() + "\n");
						}
					}
				}
			}
		}

		// Generate analysis result
	}

	/**
	 * Adds a new command line option for the program.
	 *
	 * @param opt
	 *            Short name
	 * @param longOpt
	 *            Long name
	 * @param hasArg
	 *            If requires a value argument
	 * @param description
	 *            Short text to explain the functionality of the option
	 * @param required
	 *            If the user has to specify this option each time the program is
	 *            run
	 */
	private static void addOption(String opt, String longOpt, boolean hasArg, String description, boolean required) {
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(required);
		options.addOption(option);
	}

	public static Object getSerializedObject(String fileName) {
		Object obj = null;
		try {
			InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
			GZIPInputStream gis = new GZIPInputStream(fileStream);
			ObjectInputStream ois = new ObjectInputStream(gis);
			obj = ois.readObject();
			ois.close();

		} catch (Exception ex) {
			System.out.println("Error loading file: " + fileName);
			ex.printStackTrace();
		}
		return obj;
	}

	static BufferedReader getBufferedReaderFromResource(String fileName) throws FileNotFoundException, IOException {

		BufferedReader br = null;
		InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream);
		br = new BufferedReader(decoder);

		return br;
	}

}
