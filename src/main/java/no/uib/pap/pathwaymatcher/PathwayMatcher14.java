package no.uib.pap.pathwaymatcher;

import static no.uib.pap.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pap.model.Error.INVALID_INPUT_TYPE;
import static no.uib.pap.model.Error.sendError;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;

import no.uib.pap.model.InputType;
import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;

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
	static InputType type;

	static String separator = "\t";

	static TreeMultimap<String, String> mapGenesToProteins;
	static TreeMultimap<String, String> mapEnsemblToProteins;
	static TreeMultimap<Proteoform, String> mapProteoformsToReactions;
	static TreeMultimap<String, String> mapProteinsToReactions;
	static TreeMultimap<String, String> mapReactionsToPathways;
	static TreeMultimap<String, String> mapPathwaysToTopLevelPathways;
	static HashMap<String, Pathway> pathways;
	static HashSet<String> hitPathways;
	static TreeSet<Pathway> sortedPathways;
	static HashMap<String, String> reactions;
	static HashSet<String> inputProteins = new HashSet<>();
	static HashSet<Proteoform> inputProteoforms = new HashSet<>();

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

			reactions = (HashMap<String, String>) getSerializedObject("reactions.gz");
			hitPathways = new HashSet<>();
			pathways = (HashMap<String, Pathway>) getSerializedObject("pathways.gz");
			mapProteinsToReactions = (TreeMultimap<String, String>) getSerializedObject("mapProteinsToReactions.gz");
			mapPathwaysToTopLevelPathways = null;
			mapReactionsToPathways = (TreeMultimap<String, String>) getSerializedObject("mapReactionsToPathways.gz");
			if (commandLine.hasOption("tlp")) {
				mapPathwaysToTopLevelPathways = (TreeMultimap<String, String>) getSerializedObject(
						"mapPathwaysToTopLevelPathways.gz");
			}

			type = InputType.valueOf(commandLine.getOptionValue("type").toUpperCase());
			switch (type) {
			case GENES:
				mapGenes();
				break;
			case ENSEMBL:
				mapEnsembl();
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

			// Generate analysis result
			analyse();

			outputSearch.close();
			outputAnalysis.close();
		} catch (IOException e1) {
			System.out.println("Could not create the output files: \n  " + outputPath + "search.txt\n  " + outputPath
					+ "analysis.txt"); // TODO Send correct code and message
			e1.printStackTrace();
		}

	}

	private static void mapEnsembl() throws IOException {
		// Load needed static maps
		mapEnsemblToProteins = (TreeMultimap<String, String>) getSerializedObject("mapEnsemblToProteins.gz");

		// Generate search result
		outputSearch.write("ENSEMBL" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
				+ "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String ensembl : input) {

			for (String protein : mapEnsemblToProteins.get(ensembl.trim())) {

				inputProteins.add(protein);

				for (String reactionStId : mapProteinsToReactions.get(protein)) {

					hitPathways.addAll(mapReactionsToPathways.get(reactionStId));
					for (String pathwayStId : mapReactionsToPathways.get(reactionStId)) {

						// Add current protein to the fount entities of the pathway
						Pathway pathway = pathways.get(pathwayStId);
						pathway.getReactionsFound().add(reactionStId);
						pathway.getEntitiesFound().add(new Proteoform(protein));

						// Output the full result row
						if (commandLine.hasOption("tlp")) {
							if (mapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
								for (String topLevelPathway : mapPathwaysToTopLevelPathways.get(pathwayStId)) {
									outputSearch.write(ensembl + separator + protein + separator + reactionStId
											+ separator + reactions.get(reactionStId) + separator + pathwayStId
											+ separator + pathways.get(pathwayStId).getDisplayName() + topLevelPathway
											+ separator + pathways.get(topLevelPathway).getDisplayName() + "\n");
								}
							} else {
								outputSearch.write(ensembl + separator + protein + separator + reactionStId + separator
										+ reactions.get(reactionStId) + separator + pathwayStId + separator
										+ pathways.get(pathwayStId).getDisplayName() + separator + pathwayStId
										+ separator + pathways.get(pathwayStId).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(ensembl + separator + protein + separator + reactionStId + separator
									+ reactions.get(reactionStId) + separator + pathwayStId + separator
									+ pathways.get(pathwayStId).getDisplayName() + "\n");
						}
					}
				}
			}
		}

	}

	private static void mapProteins() throws IOException {

		// Generate search result
		outputSearch.write("UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
				+ "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String protein : input) {

			inputProteins.add(protein);
			for (String reactionStId : mapProteinsToReactions.get(protein)) {

				hitPathways.addAll(mapReactionsToPathways.get(reactionStId));
				for (String pathwayStId : mapReactionsToPathways.get(reactionStId)) {

					// Add current protein to the found entities of the pathway
					Pathway pathway = pathways.get(pathwayStId);
					pathway.getReactionsFound().add(reactionStId);
					pathway.getEntitiesFound().add(new Proteoform(protein));

					// Output the full result row
					if (commandLine.hasOption("tlp")) {
						if (mapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
							for (String topLevelPathway : mapPathwaysToTopLevelPathways.get(pathwayStId)) {
								outputSearch.write(separator + protein + separator + reactionStId + separator
										+ reactions.get(reactionStId) + separator + pathwayStId + separator
										+ pathways.get(pathwayStId).getDisplayName() + topLevelPathway + separator
										+ pathways.get(topLevelPathway).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(separator + protein + separator + reactionStId + separator
									+ reactions.get(reactionStId) + separator + pathwayStId + separator
									+ pathways.get(pathwayStId).getDisplayName() + separator + pathwayStId + separator
									+ pathways.get(pathwayStId).getDisplayName() + "\n");
						}
					} else {
						outputSearch.write(
								protein + separator + reactionStId + separator + reactions.get(reactionStId) + separator
										+ pathwayStId + separator + pathways.get(pathwayStId).getDisplayName() + "\n");
					}
				}
			}
		}
	}

	private static void mapGenes() throws IOException {

		// Load needed static maps
		mapGenesToProteins = (TreeMultimap<String, String>) getSerializedObject("mapGenesToProteins.gz");

		// Generate search result
		outputSearch.write("GENE" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
				+ "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String gene : input) {

			for (String protein : mapGenesToProteins.get(gene.trim())) {

				inputProteins.add(protein);

				for (String reactionStId : mapProteinsToReactions.get(protein)) {

					hitPathways.addAll(mapReactionsToPathways.get(reactionStId));
					for (String pathwayStId : mapReactionsToPathways.get(reactionStId)) {

						// Add current protein to the fount entities of the pathway
						Pathway pathway = pathways.get(pathwayStId);
						pathway.getReactionsFound().add(reactionStId);
						pathway.getEntitiesFound().add(new Proteoform(protein));

						// Output the full result row
						if (commandLine.hasOption("tlp")) {
							if (mapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
								for (String topLevelPathway : mapPathwaysToTopLevelPathways.get(pathwayStId)) {
									outputSearch.write(gene + separator + protein + separator + reactionStId + separator
											+ reactions.get(reactionStId) + separator + pathwayStId + separator
											+ pathways.get(pathwayStId).getDisplayName() + topLevelPathway + separator
											+ pathways.get(topLevelPathway).getDisplayName() + "\n");
								}
							} else {
								outputSearch.write(gene + separator + protein + separator + reactionStId + separator
										+ reactions.get(reactionStId) + separator + pathwayStId + separator
										+ pathways.get(pathwayStId).getDisplayName() + separator + pathwayStId
										+ separator + pathways.get(pathwayStId).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(gene + separator + protein + separator + reactionStId + separator
									+ reactions.get(reactionStId) + separator + pathwayStId + separator
									+ pathways.get(pathwayStId).getDisplayName() + "\n");
						}
					}
				}
			}
		}
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

	/**
	 * Calculates statistical significance of each pathway in the search result.
	 * <p>
	 * The p-value is calculated using a binomial distribution depending on the type
	 * of entities considered in the search: proteins or proteoforms.
	 * </p>
	 * 
	 * @param result
	 */
	public static void analyse() {

		int u;
		int n;

		switch (type) {
		case PROTEOFORMS:
		case MODIFIEDPEPTIDES:
			u = mapProteoformsToReactions.keySet().size();
			n = inputProteoforms.size();
		default:
			u = mapProteinsToReactions.keySet().size();// Total number of proteins without considering isoforms
			n = inputProteins.size(); // Sample size: # Proteins in the input
			break;
		}

		// Traverse all the pathways
		for (String stId : hitPathways) {

			Pathway pathway = pathways.get(stId);

			// Calculate entities and reactions ratio
			pathway.setEntitiesRatio(
					(double) pathway.getEntitiesFound().size() / (double) pathway.getNumEntitiesTotal());
			pathway.setReactionsRatio(
					(double) pathway.getReactionsFound().size() / (double) pathway.getNumReactionsTotal());

			// Calculate the entities pvalue

			int k = pathway.getEntitiesFound().size(); // Sucessful trials: Entities found participating in the pathway
			double p = pathway.getNumEntitiesTotal() / (double) u; // Probability of sucess in each trial: The entity is
																	// a participant in the pathway

			BinomialDistribution binomialDistribution = new BinomialDistributionImpl(n, p); // Given n trials with
																							// probability p of success
			pathway.setpValue(binomialDistribution.probability(k)); // Probability of k successful trials

		}
		adjustPValues();
		reportPathwayStatistics();
	}

	public static void analyseWithProteoforms() {

		// Query for the total number of proteins without considering isoforms
		int u = mapProteoformsToReactions.size();

		// Traverse all the pathways
		for (String stId : hitPathways) {

			Pathway pathway = pathways.get(stId);

			// Calculate entities and reactions ratio
			pathway.setEntitiesRatio(
					(double) pathway.getEntitiesFound().size() / (double) pathway.getNumEntitiesTotal());
			pathway.setReactionsRatio(
					(double) pathway.getReactionsFound().size() / (double) pathway.getNumReactionsTotal());

			// Calculate the entities pvalue
			int n = inputProteoforms.size(); // Sample size: # Proteins in the input
			int k = pathway.getEntitiesFound().size(); // Sucessful trials: Entities found participating in the pathway
			double p = pathway.getNumEntitiesTotal() / (double) u; // Probability of sucess in each trial: The entity is
																	// a participant in the pathway

			BinomialDistribution binomialDistribution = new BinomialDistributionImpl(n, p); // Given n trials with
																							// probability p of success
			pathway.setpValue(binomialDistribution.probability(k)); // Probability of k successful trials

		}
		adjustPValues();
		reportPathwayStatistics();
	}

	/**
	 * Benjamini-Hochberg adjustment for FDR at 0.05%
	 */
	private static void adjustPValues() {

		// Sort pathways by pValue
		Comparator<Pathway> comparator = new Comparator<Pathway>() {
			public int compare(Pathway x, Pathway y) {
				return Double.compare(x.getPValue(), y.getPValue());
			}
		};

		sortedPathways = new TreeSet<Pathway>(comparator);

		for (String stId : hitPathways) {
			sortedPathways.add(pathways.get(stId));
		}

		// Count number of pathways with p-Values less than 0.05
		double n = 0;
		for (Pathway pathway : sortedPathways) {
			if (pathway.getPValue() < 0.05) {
				n++;
			} else {
				break;
			}
		}

		double rank = 1;
		for (Pathway pathway : sortedPathways) {
			double newPValue = pathway.getPValue() * n;
			newPValue /= rank;
			pathway.setEntitiesFDR(newPValue);
			rank++;
		}
	}

	public static void reportPathwayStatistics() {

		try {

			// Write headers of the file
			outputAnalysis.write("Pathway StId" + separator + "Pathway Name" + separator + "# Entities Found"
					+ separator + "# Entities Total" + separator + "Entities Ratio" + separator + "Entities P-Value"
					+ separator + "Significant" + separator + "Entities FDR" + separator + "# Reactions Found"
					+ separator + "# Reactions Total" + separator + "Reactions Ratio" + separator + "Entities Found"
					+ separator + "Reactions Found" + separator + "\n");

			// For each pathway
			for (Pathway pathway : sortedPathways) {
				outputAnalysis.write(pathway.getStId() + separator + "\"" + pathway.getDisplayName() + "\"" + separator
						+ pathway.getEntitiesFound().size() + separator + pathway.getNumEntitiesTotal() + separator
						+ pathway.getEntitiesRatio() + separator + pathway.getPValue() + separator
						+ (pathway.getPValue() < 0.05 ? "Yes" : "No") + separator + pathway.getEntitiesFDR() + separator
						+ pathway.getReactionsFound().size() + separator + pathway.getNumReactionsTotal() + separator
						+ pathway.getReactionsRatio() + separator + pathway.getEntitiesFoundString(type) + separator
						+ pathway.getReactionsFoundString() + separator + "\n");
			}

			outputAnalysis.close();
		} catch (IOException ex) {
			sendError(ERROR_WITH_OUTPUT_FILE);
		}
	}
}
