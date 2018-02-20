package no.uib.pap.pathwaymatcher;

import static no.uib.pap.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Proteoform_Simple;
import static no.uib.pap.pathwaymatcher.PathwayMatcher14.imapRsIdsToProteins;

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
import java.util.ArrayList;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
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
 * Retrieves the Pathways and Reactions that contain the input proteoformSet as
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

	static ImmutableSetMultimap<String, String> imapGenesToProteins;
	public static ImmutableSetMultimap<String, String> imapRsIdsToProteins;
	public static ImmutableSetMultimap<String, String> imapChrBpToProteins;
	static ImmutableSetMultimap<String, String> imapEnsemblToProteins;
	static ImmutableSetMultimap<String, Proteoform> imapProteinsToProteoforms;
	public static ImmutableSetMultimap<Proteoform, String> imapProteoformsToReactions;
	public static ImmutableSetMultimap<String, String> imapProteinsToReactions;
	public static ImmutableSetMultimap<String, String> imapReactionsToPathways;
	public static ImmutableSetMultimap<String, String> imapPathwaysToTopLevelPathways;
	public static ImmutableMap<String, Pathway> iPathways;
	public static ImmutableMap<String, String> iReactions;
	public static HashSet<String> hitPathways;
	static TreeSet<Pathway> sortedPathways;
	public static HashSet<String> inputProteins = new HashSet<>(); // These may not be in the reference data
	static HashSet<Proteoform> inputProteoforms = new HashSet<>(); // These may not be in the reference data
	public static HashSet<String> hitProteins = new HashSet<>(); // These are in the reference data
	public static HashSet<Proteoform> hitProteoforms = new HashSet<>(); // These are in the reference data

	public static void main(String[] args) {
		
		System.out.println(System.getProperty("user.dir"));

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
			System.out.println(e.getMessage());
			formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
			System.exit(1);
		}

		// ******** ******** Read input ******** ********

		File file = new File(commandLine.getOptionValue("i"));
		try {
			input = Files.readLines(file, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("The input file: " + commandLine.getOptionValue("i") + " was not found."); // TODO Error
			System.exit(1);
		}

		// ******** ******** Create output files ******** ********
		if (commandLine.hasOption("o")) {
			outputPath = commandLine.getOptionValue("o");
			outputPath = outputPath.endsWith("/") ? outputPath : outputPath + "/";
		}
		try {
			file = new File(outputPath + "search.csv");
			if (outputPath.length() > 0) {
				file.getParentFile().mkdirs();
			}
			outputSearch = new FileWriter(file);
			outputAnalysis = new FileWriter(outputPath + "analysis.csv");

			// ******** ******** Process the input ******** ********

			iReactions = (ImmutableMap<String, String>) getSerializedObject("iReactions.gz");
			hitPathways = new HashSet<>();
			iPathways = (ImmutableMap<String, Pathway>) getSerializedObject("iPathways.gz");
			imapProteinsToReactions = (ImmutableSetMultimap<String, String>) getSerializedObject(
					"imapProteinsToReactions.gz");
			imapPathwaysToTopLevelPathways = null;
			imapReactionsToPathways = (ImmutableSetMultimap<String, String>) getSerializedObject(
					"imapReactionsToPathways.gz");
			if (commandLine.hasOption("tlp")) {
				imapPathwaysToTopLevelPathways = (ImmutableSetMultimap<String, String>) getSerializedObject(
						"imapPathwaysToTopLevelPathways.gz");
			}

			String inputTypeValue = commandLine.getOptionValue("inputType").toUpperCase();
			if (!InputType.isValueOf(inputTypeValue)) {
				System.out.println("Invalid input type: " + inputTypeValue);
				System.exit(1);
			}
			inputType = InputType.valueOf(inputTypeValue);
			switch (inputType) {

			case GENES:
				mapGenes();
				break;

			case ENSEMBL:
				mapEnsembl();
				break;

			case UNIPROT:
				for (String line : input) {
					if (imapProteinsToReactions.containsKey(line)) {
						hitProteins.add(line.trim());
					}
				}
				System.out.println("Requested " + hitProteins.size() + " proteins.");
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
				VariantMatcher.mapRsIds();
				break;
			case CHRBPS:
				VariantMatcher.mapChrBp();
				break;
			case VCF:
				VariantMatcher.mapVCF();
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

			System.out.println("Matching finished.");

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
		imapGenesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapGenesToProteins.gz");

		// Generate search result
		outputSearch.write("GENE" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
				+ "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String gene : input) {

			for (String protein : imapGenesToProteins.get(gene.trim())) {
				hitProteins.add(protein);
				for (String reactionStId : imapProteinsToReactions.get(protein)) {

					for (String pathwayStId : imapReactionsToPathways.get(reactionStId)) {
						hitPathways.add(pathwayStId);

						// Add current protein to the fount proteoformSet of the pathway
						Pathway pathway = iPathways.get(pathwayStId);
						pathway.getReactionsFound().add(reactionStId);
						pathway.getEntitiesFound().add(new Proteoform(protein));

						// Output the full result row
						if (commandLine.hasOption("tlp")) {
							if (imapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
								for (String topLevelPathway : imapPathwaysToTopLevelPathways.get(pathwayStId)) {
									outputSearch.write(gene + separator + protein + separator + reactionStId + separator
											+ iReactions.get(reactionStId) + separator + pathwayStId + separator
											+ iPathways.get(pathwayStId).getDisplayName() + topLevelPathway + separator
											+ iPathways.get(topLevelPathway).getDisplayName() + "\n");
								}
							} else {
								outputSearch.write(gene + separator + protein + separator + reactionStId + separator
										+ iReactions.get(reactionStId) + separator + pathwayStId + separator
										+ iPathways.get(pathwayStId).getDisplayName() + separator + pathwayStId
										+ separator + iPathways.get(pathwayStId).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(gene + separator + protein + separator + reactionStId + separator
									+ iReactions.get(reactionStId) + separator + pathwayStId + separator
									+ iPathways.get(pathwayStId).getDisplayName() + "\n");
						}
					}
				}
			}
		}
	}

	private static void mapEnsembl() throws IOException {
		// Load needed static maps
		imapEnsemblToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapEnsemblToProteins.gz");

		// Generate search result
		outputSearch.write("ENSEMBL" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
				+ "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
		if (commandLine.hasOption("tlp")) {
			outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
		}
		outputSearch.write("\n");

		for (String ensembl : input) {
			ensembl = ensembl.trim();

			for (String protein : imapEnsemblToProteins.get(ensembl)) {
				hitProteins.add(protein);
				for (String reactionStId : imapProteinsToReactions.get(protein)) {

					for (String pathwayStId : imapReactionsToPathways.get(reactionStId)) {
						hitPathways.add(pathwayStId);

						// Add current protein to the fount proteoformSet of the pathway
						Pathway pathway = iPathways.get(pathwayStId);
						pathway.getReactionsFound().add(reactionStId);
						pathway.getEntitiesFound().add(new Proteoform(protein));

						// Output the full result row
						if (commandLine.hasOption("tlp")) {
							if (imapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
								for (String topLevelPathway : imapPathwaysToTopLevelPathways.get(pathwayStId)) {
									outputSearch.write(ensembl + separator + protein + separator + reactionStId
											+ separator + iReactions.get(reactionStId) + separator + pathwayStId
											+ separator + iPathways.get(pathwayStId).getDisplayName() + topLevelPathway
											+ separator + iPathways.get(topLevelPathway).getDisplayName() + "\n");
								}
							} else {
								outputSearch.write(ensembl + separator + protein + separator + reactionStId + separator
										+ iReactions.get(reactionStId) + separator + pathwayStId + separator
										+ iPathways.get(pathwayStId).getDisplayName() + separator + pathwayStId
										+ separator + iPathways.get(pathwayStId).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(ensembl + separator + protein + separator + reactionStId + separator
									+ iReactions.get(reactionStId) + separator + pathwayStId + separator
									+ iPathways.get(pathwayStId).getDisplayName() + "\n");
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

		// System.out.println("The possible pathways are: " + (new
		// HashSet(imapReactionsToPathways.values())).size());

		for (String protein : hitProteins) {
			for (String reaction : imapProteinsToReactions.get(protein)) {

				for (String pathwayStId : imapReactionsToPathways.get(reaction)) {
					hitPathways.add(pathwayStId);

					// Add current protein to the fount proteoformSet of the pathway
					Pathway pathway = iPathways.get(pathwayStId);
					pathway.getReactionsFound().add(reaction);
					pathway.getEntitiesFound().add(new Proteoform(protein));

					// Output the full result row
					if (commandLine.hasOption("tlp")) {
						if (imapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
							for (String topLevelPathway : imapPathwaysToTopLevelPathways.get(pathwayStId)) {
								outputSearch.write(protein + separator + reaction + separator + iReactions.get(reaction)
										+ separator + pathwayStId + separator
										+ iPathways.get(pathwayStId).getDisplayName() + topLevelPathway + separator
										+ iPathways.get(topLevelPathway).getDisplayName() + "\n");
							}
						} else {
							outputSearch.write(protein + separator + reaction + separator + iReactions.get(reaction)
									+ separator + pathwayStId + separator + iPathways.get(pathwayStId).getDisplayName()
									+ separator + pathwayStId + separator + iPathways.get(pathwayStId).getDisplayName()
									+ "\n");
						}
					} else {
						outputSearch
								.write(protein + separator + reaction + separator + iReactions.get(reaction) + separator
										+ pathwayStId + separator + iPathways.get(pathwayStId).getDisplayName() + "\n");
					}
				}
			}
		}

		System.out.println("The number of hit pathways is: " + hitPathways.size());
	}

	private static void mapProteoforms() throws IOException {

		imapProteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject(
				"imapProteinsToProteoforms.gz");
		ProteoformMatcher matcher = null;
		switch (matchingType) {
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

		for (Proteoform proteoform : inputProteoforms) {
			for (Proteoform refProteoform : imapProteinsToProteoforms.get(proteoform.getUniProtAcc())) {
				if (matcher.matches(proteoform, refProteoform)) {
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

			for (String reactionStId : imapProteoformsToReactions.get(proteoform)) {

				hitProteoforms.add(proteoform);
				hitPathways.addAll(imapReactionsToPathways.get(reactionStId));
				for (String pathwayStId : imapReactionsToPathways.get(reactionStId)) {

					// Add current protein to the found proteoformSet of the pathway
					Pathway pathway = iPathways.get(pathwayStId);
					pathway.getReactionsFound().add(reactionStId);
					pathway.getEntitiesFound().add(proteoform);

					// Output the full result row
					if (commandLine.hasOption("tlp")) {
						if (imapPathwaysToTopLevelPathways.get(pathwayStId).size() > 0) {
							for (String topLevelPathway : imapPathwaysToTopLevelPathways.get(pathwayStId)) {
								outputSearch.write(separator + proteoform.toString(ProteoformFormat.SIMPLE) + separator
										+ reactionStId + separator + iReactions.get(reactionStId) + separator
										+ pathwayStId + separator + iPathways.get(pathwayStId).getDisplayName()
										+ topLevelPathway + separator + iPathways.get(topLevelPathway).getDisplayName()
										+ "\n");
							}
						} else {
							outputSearch.write(separator + proteoform.toString(ProteoformFormat.SIMPLE) + separator
									+ reactionStId + separator + iReactions.get(reactionStId) + separator + pathwayStId
									+ separator + iPathways.get(pathwayStId).getDisplayName() + separator + pathwayStId
									+ separator + iPathways.get(pathwayStId).getDisplayName() + "\n");
						}
					} else {
						outputSearch.write(proteoform.toString(ProteoformFormat.SIMPLE) + separator + reactionStId
								+ separator + iReactions.get(reactionStId) + separator + pathwayStId + separator
								+ iPathways.get(pathwayStId).getDisplayName() + "\n");
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
	 * inputType of proteoformSet considered in the search: proteins or proteoforms.
	 * </p>
	 * 
	 * @param result
	 */
	public static void analyse() {

		int u, n;

		switch (inputType) {
		case PROTEOFORMS:
		case MODIFIEDPEPTIDES:
			u = imapProteoformsToReactions.keySet().size(); // Total number of proteoforms
			n = hitProteoforms.size(); // Sample size: # Proteoforms in the input that really exist in the reference
										// data
		default: // All the other types of input
			u = imapProteinsToReactions.keySet().size();// Total number of proteins without considering isoforms
			n = hitProteins.size(); // Sample size: # Proteins in the input that really exist in the reference data
			break;
		}

		// System.out.println("The number of pathways with data is: " +
		// iPathways.size());

		// Traverse all the iPathways
		for (String stId : hitPathways) {

			Pathway pathway = iPathways.get(stId);

			// Calculate proteoformSet and iReactions ratio
			pathway.setEntitiesRatio(
					(double) pathway.getEntitiesFound().size() / (double) pathway.getNumEntitiesTotal());
			pathway.setReactionsRatio(
					(double) pathway.getReactionsFound().size() / (double) pathway.getNumReactionsTotal());

			// Calculate the proteoformSet pvalue

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

		// Sort iPathways by pValue
		Comparator<Pathway> comparator = new Comparator<Pathway>() {
			public int compare(Pathway x, Pathway y) {

				if (x.equals(y))
					return 0;

				if (x.getPValue() != y.getPValue()) {
					return Double.compare(x.getPValue(), y.getPValue());
				}

				// First by displayName
				if (!x.getDisplayName().equals(y.getDisplayName())) {
					return x.getDisplayName().compareTo(y.getDisplayName());
				}

				// Second by stId
				if (!x.getStId().equals(y.getStId())) {
					return x.getStId().compareTo(y.getStId());
				}

				return 0;
			}
		};

		sortedPathways = new TreeSet<Pathway>(comparator);

		// System.out.println("The number of Pathway stIds is: " +
		// iPathways.keySet().size());
		for (String stId : hitPathways) {
			sortedPathways.add(iPathways.get(stId));
		}
		// System.out.println("The number of pathways to be analysed is: " +
		// sortedPathways.size());
		// Count number of iPathways with p-Values less than 0.05
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
		System.out.println("The number of analysed pathways is: " + sortedPathways.size());
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

	public static List<String> readFileFromResources(String fileName) {
		File file = new File(ClassLoader.getSystemResource(fileName).getFile());
		
		List<String> lines = new ArrayList<>();
		try {
			lines = Files.readLines(file, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("Resource file: " + file + " was not found.");
			e.printStackTrace();
		}
		
		return lines;
	}
}
