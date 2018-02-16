package no.uib.pap.pathwaymatcher;

import static no.uib.pap.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Proteoform_Simple;

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
import java.text.ParseException;
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
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;

import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;

import no.uib.pap.model.InputType;
import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.pathwaymatcher.Matching.PeptideMatcher;
import no.uib.pap.pathwaymatcher.Matching.ProteoformMatcher;
import no.uib.pap.pathwaymatcher.Matching.ProteoformMatcherFlexible;
import no.uib.pap.pathwaymatcher.Matching.ProteoformMatcherOne;
import no.uib.pap.pathwaymatcher.Matching.ProteoformMatcherStrict;
import no.uib.pap.pathwaymatcher.Matching.VariantMatcher;

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

	public static List<String> input;
	public static FileWriter outputSearch;
	public static FileWriter outputAnalysis;
	public static String outputPath = "";
	public static InputType inputType;

	static PathwayMatcher14.MatchingType matchingType = MatchingType.FLEXIBLE;
	public static int margin = 0;
	public static final String fasta = "uniprot-all.fasta";

	public static int rsidColumnIndex = 2;
	public static int ensemblColumnIndex = 4;
	public static int swissprotColumnIndex = 5;
	public static int nearestGeneColumnIndex = 7;

	public static String separator = "\t";

	static TreeMultimap<String, String> mapGenesToProteins;
	static TreeMultimap<String, String> mapEnsemblToProteins;
	static TreeMultimap<String, Proteoform> mapProteinsToProteoforms;
	public static TreeMultimap<Proteoform, String> mapProteoformsToReactions;
	public static TreeMultimap<String, String> mapProteinsToReactions;
	public static TreeMultimap<String, String> mapReactionsToPathways;
	public static TreeMultimap<String, String> mapPathwaysToTopLevelPathways;
	public static HashMap<String, Pathway> pathways;
	public static HashSet<String> hitPathways;
	static TreeSet<Pathway> sortedPathways;
	public static HashMap<String, String> reactions;
	public static HashSet<String> inputProteins = new HashSet<>(); // These may not be in the reference data
	static HashSet<Proteoform> inputProteoforms = new HashSet<>(); // These may not be in the reference data
	public static HashSet<String> hitProteins = new HashSet<>(); // These are in the reference data
	public static HashSet<Proteoform> hitProteoforms = new HashSet<>(); // These are in the reference data

	public static void main(String[] args) {

		// ******** ******** Read and process command line arguments ******** ********
		options = new Options();

		addOption("t", "inputType", true, "Input inputType: GENES|ENSEMBL|UNIPROT|PEPTIDES|RSIDS|PROTEOFORMS", true);
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

			inputType = InputType.valueOf(commandLine.getOptionValue("inputType").toUpperCase());
			switch (inputType) {
			
			case GENES:
				mapGenes();
				break;
				
			case ENSEMBL:
				mapEnsembl();
				break;
				
			case UNIPROT:
				for(String line : input) {
					hitProteins.add(line.trim());
				}
				mapProteins();
				break;
				
			case PROTEOFORMS:
				
				int row = 1;
				for (String line : input) {
					row++;
					if (matches_Proteoform_Simple(line.trim())) {
						Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform(line, row);
						inputProteoforms.add(proteoform);
					} else {
						if (line.isEmpty())
							sendWarning(EMPTY_ROW, row);
						else
							sendWarning(INVALID_ROW, row);
					}
				}

				if (!commandLine.hasOption("m")) {
					throw new MissingArgumentException(options.getOption("m"));
				}
				matchingType = MatchingType.valueOf(commandLine.getOptionValue("m"));

				mapProteoforms();
				break;

			case RSIDS:
			case VCF:
				VariantMatcher.mapVariants();
				break;
			case PEPTIDES:
				PeptideMatcher.mapPeptides();
				break;
			case MODIFIEDPEPTIDES:
				PeptideMatcher.mapModifiedPeptides();
				break;
			default:
				System.out.println("Input inputType not supported.");
				break;
			}

			analyse();

			outputSearch.close();
			outputAnalysis.close();

		} catch (IOException e1) {
			System.out.println("Could not create the output files: \n  " + outputPath + "search.txt\n  " + outputPath
					+ "analysis.txt"); // TODO Send correct code and message
		} catch (MissingArgumentException e) {
			formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
			e.printStackTrace();
		} catch (ParseException e) {
			System.out.println("Error reading the input.");
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

	public static void mapProteins() throws IOException {

		// Generate search result
		outputSearch.write("UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
				+ "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String protein : inputProteins) {

			for (String reactionStId : mapProteinsToReactions.get(protein)) {

				hitProteins.add(protein);
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

	private static void mapProteoforms() throws IOException {
		
		mapProteinsToProteoforms = (TreeMultimap<String, Proteoform>) getSerializedObject("mapProteinsToProteoforms.gz");
		ProteoformMatcher matcher = null;
		switch(matchingType) {
		case FLEXIBLE:
			matcher = new ProteoformMatcherFlexible();
			break;
		case ONE:
			matcher = new ProteoformMatcherOne();
			break;
		case STRICT:
			matcher = new ProteoformMatcherStrict();
			break;
		}
		
		for(Proteoform proteoform : inputProteoforms) {
			for(Proteoform refProteoform : mapProteinsToProteoforms.get(proteoform.getUniProtAcc())) {
				if(matcher.matches(proteoform, refProteoform)) {
					hitProteoforms.add(refProteoform);
				}
			}
		}

		// Generate search result
		outputSearch.write("PROTEOFORM" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
				+ "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (Proteoform proteoform : inputProteoforms) {

			for (String reactionStId : mapProteoformsToReactions.get(proteoform)) {

				hitProteoforms.add(proteoform);
				hitPathways.addAll(mapReactionsToPathways.get(reactionStId));
				for (String pathwayStId : mapReactionsToPathways.get(reactionStId)) {

					// Add current protein to the found entities of the pathway
					Pathway pathway = pathways.get(pathwayStId);
					pathway.getReactionsFound().add(reactionStId);
					pathway.getEntitiesFound().add(proteoform);

					// Output the full result row
					if (commandLine.hasOption("tlp")) {
						if (mapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
							for (String topLevelPathway : mapPathwaysToTopLevelPathways.get(pathwayStId)) {
								outputSearch.write(separator + proteoform.toString(ProteoformFormat.SIMPLE) + separator
										+ reactionStId + separator + reactions.get(reactionStId) + separator
										+ pathwayStId + separator + pathways.get(pathwayStId).getDisplayName()
										+ topLevelPathway + separator + pathways.get(topLevelPathway).getDisplayName()
										+ "\n");
							}
						} else {
							outputSearch.write(separator + proteoform.toString(ProteoformFormat.SIMPLE) + separator
									+ reactionStId + separator + reactions.get(reactionStId) + separator + pathwayStId
									+ separator + pathways.get(pathwayStId).getDisplayName() + separator + pathwayStId
									+ separator + pathways.get(pathwayStId).getDisplayName() + "\n");
						}
					} else {
						outputSearch.write(proteoform.toString(ProteoformFormat.SIMPLE) + separator + reactionStId
								+ separator + reactions.get(reactionStId) + separator + pathwayStId + separator
								+ pathways.get(pathwayStId).getDisplayName() + "\n");
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

	/**
	 * Calculates statistical significance of each pathway in the search result.
	 * <p>
	 * The p-value is calculated using a binomial distribution depending on the
	 * inputType of entities considered in the search: proteins or proteoforms.
	 * </p>
	 * 
	 * @param result
	 */
	public static void analyse() {

		int u, n;

		switch (inputType) {
		case PROTEOFORMS:
		case MODIFIEDPEPTIDES:
			u = mapProteoformsToReactions.keySet().size(); // Total number of proteoforms
			n = hitProteoforms.size(); // Sample size: # Proteoforms in the input that really exist in the reference
										// data
		default:
			u = mapProteinsToReactions.keySet().size();// Total number of proteins without considering isoforms
			n = hitProteins.size(); // Sample size: # Proteins in the input that really exist in the reference data
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
						+ pathway.getReactionsRatio() + separator + pathway.getEntitiesFoundString(inputType)
						+ separator + pathway.getReactionsFoundString() + separator + "\n");
			}

			outputAnalysis.close();
		} catch (IOException ex) {
			sendError(ERROR_WITH_OUTPUT_FILE);
		}
	}

	public enum MatchingType {
		STRICT, FLEXIBLE, ONE
	}
}
