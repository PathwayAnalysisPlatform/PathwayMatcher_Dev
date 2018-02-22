package no.uib.pap.pathwaymatcher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.io.Files;
import no.uib.pap.methods.analysis.ora.Analysis;
import no.uib.pap.methods.search.Search;
import no.uib.pap.model.*;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static no.uib.pap.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pap.model.Error.sendError;

public class PathwayMatcher {

    /**
     * The object to hold the command line arguments for PathwayMatcher.
     */
    static Options options = new Options();
    static CommandLine commandLine;

    static List<String> input;
    static FileWriter outputSearch;
    static FileWriter outputAnalysis;
    static String outputPath = "";
    static InputType inputType;
    static MatchType matchType = MatchType.FLEXIBLE;
    static Pair<List<String[]>, MessageStatus> searchResult;
    static Pair<TreeSet<Pathway>, MessageStatus> analysisResult;

    public static String separator = "\t";
    static Long margin = 0L;

    /**
     * Static mapping data structures
     */
    static ImmutableMap<String, String> iReactions;
    static ImmutableMap<String, Pathway> iPathways;
    static ImmutableSetMultimap<String, String> imapRsIdsToProteins;
    static ImmutableSetMultimap<String, String> imapChrBpToProteins;
    static ImmutableSetMultimap<String, String> imapGenesToProteins;
    static ImmutableSetMultimap<String, String> imapEnsemblToProteins;
    static ImmutableSetMultimap<String, Proteoform> imapProteinsToProteoforms;
    static ImmutableSetMultimap<Proteoform, String> imapProteoformsToReactions;
    static ImmutableSetMultimap<String, String> imapProteinsToReactions;
    static ImmutableSetMultimap<String, String> imapReactionsToPathways;
    static ImmutableSetMultimap<String, String> imapPathwaysToTopLevelPathways;

    static HashSet<String> hitPathways;
    static HashSet<String> inputProteins = new HashSet<>(); // These may not be in the reference data
    static HashSet<Proteoform> inputProteoforms = new HashSet<>(); // These may not be in the reference data
    static HashSet<String> hitProteins = new HashSet<>(); // These are in the reference data
    static HashSet<Proteoform> hitProteoforms = new HashSet<>(); // These are in the reference data

    public static void main(String args[]) {

        // ******** ******** Read and process command line arguments ******** ********
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
            if (commandLine.hasOption("r")) {
                margin = Long.valueOf(commandLine.getOptionValue("r"));
            }
            if (commandLine.hasOption("m")) {
                String matchTypeValue = commandLine.getOptionValue("m");
                if (MatchType.isValueOf(matchTypeValue)) {
                    matchType = MatchType.valueOf(matchTypeValue);
                }
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
            System.exit(1);
        }

        // ******** ******** Read input ******** ********

        File file = new File(commandLine.getOptionValue("i"));
        try {
            input = Files.readLines(file, Charset.defaultCharset());
            for (String line : input) {
                line = line.trim();
            }
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

            // Load static structures needed for all the cases
            iReactions = (ImmutableMap<String, String>) getSerializedObject("iReactions.gz");
            iPathways = (ImmutableMap<String, Pathway>) getSerializedObject("iPathways.gz");
            imapProteinsToReactions = (ImmutableSetMultimap<String, String>) getSerializedObject(
                    "imapProteinsToReactions.gz");
            imapReactionsToPathways = (ImmutableSetMultimap<String, String>) getSerializedObject(
                    "imapReactionsToPathways.gz");
            imapPathwaysToTopLevelPathways = null;
            if (commandLine.hasOption("tlp")) {
                imapPathwaysToTopLevelPathways = (ImmutableSetMultimap<String, String>) getSerializedObject(
                        "imapPathwaysToTopLevelPathways.gz");
            }
            hitPathways = new HashSet<>();

            String inputTypeValue = commandLine.getOptionValue("inputType").toUpperCase();
            if (!InputType.isValueOf(inputTypeValue)) {
                System.out.println("Invalid input type: " + inputTypeValue);
                System.exit(1);
            }

            inputType = InputType.valueOf(inputTypeValue);
            switch (inputType) {
                case GENE:
                case GENES:
                    searchResult = Search.searchWithGene(input, iReactions, iPathways, imapGenesToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"));
                    outputSearchWithGene(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case ENSEMBL:
                case ENSEMBLS:
                    searchResult = Search.searchWithEnsembl(input, iReactions, iPathways, imapEnsemblToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"));
                    outputSearchWithEnsembl(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case UNIPROT:
                case UNIPROTS:
                    searchResult = Search.searchWithUniProt(input, iReactions, iPathways, imapProteinsToReactions,
                            imapReactionsToPathways, imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"));
                    outputSearchWithUniProt(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case PROTEOFORM:
                case PROTEOFORMS:
                    searchResult = Search.searchWithProteoform(input, matchType, margin, iReactions, iPathways,
                            imapProteinsToProteoforms, imapProteoformsToReactions, imapReactionsToPathways,
                            imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"));
                    outputSearchWithProteoform(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteoformsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case RSID:
                case RSIDS:
                    System.out.println("Loading data...");
                    imapRsIdsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapRsIdsToProteins.gz");
                    System.out.println("Matching input...");
                    searchResult = Search.searchWithRsId(input, iReactions, iPathways, imapRsIdsToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"));
                    outputSearchWithUniProt(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case CHRBP:
                case CHRBPS:
                    searchResult = Search.searchWithChrBp(input, iReactions, iPathways, imapChrBpToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"));
                    outputSearchWithUniProt(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case VCF:
                    searchResult = Search.searchWithVCF(input, iReactions, iPathways, imapChrBpToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"));
                    outputSearchWithUniProt(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case PEPTIDE:
                case PEPTIDES:
                    searchResult = Search.searchWithPeptide(input, iReactions, iPathways, imapProteinsToReactions,
                            imapReactionsToPathways, imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"));
                    outputSearchWithUniProt(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                case MODIFIEDPEPTIDE:
                case MODIFIEDPEPTIDES:
                    searchResult = Search.searchWithModifiedPeptide(input, matchType, margin, iReactions, iPathways,
                            imapProteinsToProteoforms, imapProteoformsToReactions, imapReactionsToPathways,
                            imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"));
                    outputSearchWithProteoform(searchResult.getKey());
                    analysisResult = Analysis.analysis(iPathways, imapProteoformsToReactions.keySet().size(),
                            searchResult.getKey());
                    break;
                default:
                    System.out.println("Input inputType not supported.");
                    System.exit(1);
                    break;
            }

            writeAnalysisResult(analysisResult.getKey());

            System.out.println("Matching finished.");

            outputSearch.close();
            outputAnalysis.close();

        } catch (IOException e1) {
            System.out.println("Could not create the output files: \n  " + outputPath + "search.txt\n  " + outputPath
                    + "analysis.txt"); // TODO Send correct code and message
        } /*
         * catch (MissingArgumentException e) {
         * formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
         * e.printStackTrace(); } catch (java.text.ParseException e) {
         * System.out.println("Error reading the input."); }
         */
    }

    private static void writeAnalysisResult(TreeSet<Pathway> sortedPathways) {
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

    private static void outputSearchWithGene(List<String[]> searchResult) throws IOException {

        outputSearch.write("GENE" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
                + "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.write("\n");

        for (String[] r : searchResult) {
            for (int I = 0; I < r.length; I++) {
                outputSearch.write(r[I] + separator);
            }
            outputSearch.write("\n");
        }
    }

    private static void outputSearchWithEnsembl(List<String[]> searchResult) throws IOException {

        outputSearch.write("ENSEMBL" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
                + "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME\n");
        }
        outputSearch.write("\n");

        for (String[] r : searchResult) {
            for (int I = 0; I < r.length; I++) {
                outputSearch.write(r[I] + separator);
            }
            outputSearch.write("\n");
        }
    }

    private static void outputSearchWithUniProt(List<String[]> searchResult) throws IOException {

        outputSearch.write("UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
                + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME\n");
        }
        outputSearch.write("\n");

        for (String[] r : searchResult) {
            for (int I = 0; I < r.length; I++) {
                outputSearch.write(r[I] + separator);
            }
            outputSearch.write("\n");
        }
    }

    private static void outputSearchWithProteoform(List<String[]> searchResult) throws IOException {

        outputSearch.write("PROTEOFORM" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
                + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.write("\n");

        for (String[] r : searchResult) {
            for (int I = 0; I < r.length; I++) {
                outputSearch.write(r[I] + separator);
            }
            outputSearch.write("\n");
        }
    }

    /**
     * Adds a new command line option for the program.
     *
     * @param opt         Short name
     * @param longOpt     Long name
     * @param hasArg      If requires a value argument
     * @param description Short text to explain the functionality of the option
     * @param required    If the user has to specify this option each time the program is
     *                    run
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
}
