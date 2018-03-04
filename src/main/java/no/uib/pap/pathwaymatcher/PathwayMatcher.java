package no.uib.pap.pathwaymatcher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;
import no.uib.pap.methods.analysis.ora.Analysis;
import no.uib.pap.methods.search.Search;
import no.uib.pap.model.*;
import no.uib.pap.model.Error;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    static BufferedWriter outputSearch;
    static BufferedWriter outputAnalysis;
    static BufferedWriter outputVertices;
    static BufferedWriter outputInternalEdges;
    static BufferedWriter outputExternalEdges;
    static String outputPath = "";
    static InputType inputType;
    static MatchType matchType = MatchType.FLEXIBLE;
    static Pair<List<String[]>, MessageStatus> searchResult;
    static MessageStatus analysisResult;

    /**
     * The column separator.
     */
    public static final String separator = "\t";
    /**
     * The system specific end of file.
     */
    public static final String eol = System.lineSeparator();
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
        addOption("t", "inputType", true, "Input inputType: GENE|ENSEMBL|UNIPROT|PEPTIDE|RSID|PROTEOFORM", true);
        addOption("r", "range", true, "Ptm sites margin of error", false);
        addOption("tlp", "toplevelpathways", false, "Show Top Level Pathway columns", false);
        addOption("m", "matching", true, "Proteoform match criteria: EXACT|ONE|FLEXIBLE", false);
        addOption("i", "input", true, "Input file", true);
        addOption("o", "output", true, "Output path", false);
        addOption("g", "graph", false, "Create igraph file with connections of proteins", false);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("r")) {
                margin = Long.valueOf(commandLine.getOptionValue("r"));
            }
            if (commandLine.hasOption("m")) {
                String matchTypeValue = commandLine.getOptionValue("m").toUpperCase();
                if (MatchType.isValueOf(matchTypeValue)) {
                    matchType = MatchType.valueOf(matchTypeValue);
                } else {
                    System.out.println(Error.INVALID_MATCHING_TYPE.getMessage());
                    System.exit(Error.INVALID_MATCHING_TYPE.getCode());
                }
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
            if (args.length == 0) {
                System.exit(Error.NO_ARGUMENTS.getCode());
            }
            if (e.getMessage().startsWith("Missing required option: i")) {
                System.exit(Error.NO_INPUT.getCode());
            }
            if (e.getMessage().startsWith("Missing required option:")) {
                System.exit(Error.MISSING_ARGUMENT.getCode());
            }
            System.exit(Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
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
            System.exit(Error.COULD_NOT_READ_INPUT_FILE.getCode());
        }

        // ******** ******** Create output files ******** ********
        if (commandLine.hasOption("o")) {
            outputPath = commandLine.getOptionValue("o");
            outputPath = outputPath.endsWith("/") ? outputPath : outputPath + "/";
        }
        try {
            file = new File(outputPath + "search.tsv");
            if (outputPath.length() > 0) {
                file.getParentFile().mkdirs();
            }
            outputSearch = new BufferedWriter(new FileWriter(file));
            outputAnalysis = new BufferedWriter(new FileWriter(outputPath + "analysis.tsv"));

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
                System.exit(Error.INVALID_INPUT_TYPE.getCode());
            }

            inputType = InputType.valueOf(inputTypeValue);
            switch (inputType) {
                case GENE:
                case GENES:
                    imapGenesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapGenesToProteins.gz");
                    searchResult = Search.searchWithGene(input, iReactions, iPathways, imapGenesToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithGene(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case ENSEMBL:
                case ENSEMBLS:
                    imapEnsemblToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapEnsemblToProteins.gz");
                    searchResult = Search.searchWithEnsembl(input, iReactions, iPathways, imapEnsemblToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithEnsembl(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case UNIPROT:
                case UNIPROTS:
                    searchResult = Search.searchWithUniProt(input, iReactions, iPathways, imapProteinsToReactions,
                            imapReactionsToPathways, imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case PROTEOFORM:
                case PROTEOFORMS:
                    imapProteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapProteinsToProteoforms.gz");
                    imapProteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToReactions.gz");
                    searchResult = Search.searchWithProteoform(input, matchType, margin, iReactions, iPathways,
                            imapProteinsToProteoforms, imapProteoformsToReactions, imapReactionsToPathways,
                            imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithProteoform(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteoformsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case RSID:
                case RSIDS:
                    System.out.println("Loading data...");
                    imapRsIdsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapRsIdsToProteins.gz");
                    System.out.println("Matching input...");
                    searchResult = Search.searchWithRsId(input, iReactions, iPathways, imapRsIdsToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case CHRBP:
                case CHRBPS:
                    imapChrBpToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapChrBpToProteins.gz");
                    searchResult = Search.searchWithChrBp(input, iReactions, iPathways, imapChrBpToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case VCF:
                    imapChrBpToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapChrBpToProteins.gz");
                    searchResult = Search.searchWithVCF(input, iReactions, iPathways, imapChrBpToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case PEPTIDE:
                case PEPTIDES:
                    searchResult = Search.searchWithPeptide(input, iReactions, iPathways, imapProteinsToReactions,
                            imapReactionsToPathways, imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    break;
                case MODIFIEDPEPTIDE:
                case MODIFIEDPEPTIDES:
                    imapProteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapProteinsToProteoforms.gz");
                    imapProteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToReactions.gz");
                    searchResult = Search.searchWithModifiedPeptide(input, matchType, margin, iReactions, iPathways,
                            imapProteinsToProteoforms, imapProteoformsToReactions, imapReactionsToPathways,
                            imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithProteoform(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(
                            iPathways,
                            imapProteoformsToReactions.keySet().size(),
                            hitProteins,
                            hitPathways);
                    break;
                default:
                    System.out.println("Input inputType not supported.");
                    System.exit(1);
                    break;
            }

            writeAnalysisResult(hitPathways, iPathways);
            System.out.println("Analysis resoults writen to: " + outputPath + "analysis.csv");

            if (commandLine.hasOption("g")) {
                writeConnectionGraph(hitPathways, iPathways);
            }

            outputSearch.close();
            outputAnalysis.close();

        } catch (IOException e1) {
            System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage() + ": " + outputPath + "search.txt  " + eol + outputPath
                    + "analysis.txt");
            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
        }
    }

    private static void writeConnectionGraph(HashSet<String> hitPathways, ImmutableMap<String, Pathway> iPathways) throws IOException {
        System.out.println("Creating connection graph...");

        //Create output files
        outputVertices = new BufferedWriter(new FileWriter(outputPath + "vertices.tsv"));
        outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "internalEdges.tsv"));
        outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "externalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + separator + " name" + eol);
        outputInternalEdges.write("from" + separator + "to" + separator + "type" + eol);
        outputExternalEdges.write("from" + separator + "to" + separator + "type" + eol);

        // Load static mapping
        ImmutableMap<String, String> iProteins = (ImmutableMap<String, String>) getSerializedObject("iProteins.gz");
        ImmutableSetMultimap<String, String> imapReactionsToParticipants = (ImmutableSetMultimap<String, String>) getSerializedObject("imapReactionsToParticipants.gz");
        ImmutableSetMultimap<String, String> imapProteinsToComplexes = (ImmutableSetMultimap<String, String>) getSerializedObject("imapProteinsToComplexes.gz");
        ImmutableSetMultimap<String, String> imapComplexesToParticipants = (ImmutableSetMultimap<String, String>) getSerializedObject("imapComplexesToParticipants.gz");
        TreeMultimap<String, String> reactionEdges = TreeMultimap.create();
        TreeMultimap<String, String> complexEdges = TreeMultimap.create();

        // Write the vertices file
        for (String protein : hitProteins) {
            String line = String.join(separator, protein, iProteins.get(protein));
            outputVertices.write(line);
            outputVertices.newLine();
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "vertices.tsv");

        // Write edges among input proteins
        for (String protein : hitProteins) {
            // Output reaction neighbours
            for (String reaction : imapProteinsToReactions.get(protein)) {
                for (String participant : imapReactionsToParticipants.get(reaction)) {
                    if (!participant.equals(protein) && hitProteins.contains(participant)) {
                        reactionEdges.put(protein, participant);    // Added to a HashMap to eliminate duplicates
                    }
                }
            }
            // Output complex neighbours
            for (String complex : imapProteinsToComplexes.get(protein)) {
                for (String participant : imapComplexesToParticipants.get(complex)) {
                    if (!participant.equals(protein) && hitProteins.contains(participant)) {
                        complexEdges.put(protein, participant);
                    }
                }
            }
        }

        for (Map.Entry<String, String> edge : reactionEdges.entries()) {
            String line = String.join(separator, edge.getKey(), edge.getValue(), "Reaction");
            outputInternalEdges.write(line);
            outputInternalEdges.newLine();
        }
        for (Map.Entry<String, String> edge : complexEdges.entries()) {
            String line = String.join(separator, edge.getKey(), edge.getValue(), "Complex");
            outputInternalEdges.write(line);
            outputInternalEdges.newLine();
        }

        outputInternalEdges.close();
        System.out.println("Finished writing " + outputPath + "internalEdges.tsv");

        // Write edges among in and out proteins
        reactionEdges.clear();
        complexEdges.clear();
        for (String protein : hitProteins) {
            // Output reaction neighbours
            for (String reaction : imapProteinsToReactions.get(protein)) {
                for (String participant : imapReactionsToParticipants.get(reaction)) {
                    if (!participant.equals(protein) && !hitProteins.contains(participant)) {
                        reactionEdges.put(protein, participant);
                    }
                }
            }
            // Output complex neighbours
            for (String complex : imapProteinsToComplexes.get(protein)) {
                for (String participant : imapComplexesToParticipants.get(complex)) {
                    if (!participant.equals(protein) && !hitProteins.contains(participant)) {
                        complexEdges.put(protein, participant);
                    }
                }
            }
        }

        for (Map.Entry<String, String> edge : reactionEdges.entries()) {
            String line = String.join(separator, edge.getKey(), edge.getValue(), "Reaction");
            outputExternalEdges.write(line);
            outputExternalEdges.newLine();
        }
        for (Map.Entry<String, String> edge : complexEdges.entries()) {
            String line = String.join(separator, edge.getKey(), edge.getValue(), "Complex");
            outputExternalEdges.write(line);
            outputExternalEdges.newLine();
        }

        outputExternalEdges.close();
        System.out.println("Finished writing " + outputPath + "externalEdges.tsv");
    }

    private static void writeAnalysisResult(HashSet<String> hitPathways, ImmutableMap<String, Pathway> iPathways) {
        try {

            // Write headers of the file
            outputAnalysis.write("Pathway StId" + separator + "Pathway Name" + separator + "# Entities Found"
                    + separator + "# Entities Total" + separator + "Entities Ratio" + separator + "Entities P-Value"
                    + separator + "Significant" + separator + "Entities FDR" + separator + "# Reactions Found"
                    + separator + "# Reactions Total" + separator + "Reactions Ratio" + separator + "Entities Found"
                    + separator + "Reactions Found" + eol);

            // For each pathway
            for (String pathwayStId : hitPathways) {

                Pathway pathway = iPathways.get(pathwayStId);
                String line = String.join(separator,
                        pathway.getStId(),
                        String.join("", "\"", pathway.getDisplayName(), "\""),
                        Integer.toString(pathway.getEntitiesFound().size()),
                        Integer.toString(pathway.getNumEntitiesTotal()),
                        Double.toString(pathway.getEntitiesRatio()),
                        Double.toString(pathway.getPValue()),
                        (pathway.getPValue() < 0.05 ? "Yes" : "No"),
                        Double.toString(pathway.getEntitiesFDR()),
                        Integer.toString(pathway.getReactionsFound().size()),
                        Integer.toString(pathway.getNumReactionsTotal()),
                        Double.toString(pathway.getReactionsRatio()),
                        pathway.getEntitiesFoundString(inputType),
                        pathway.getReactionsFoundString());
                outputExternalEdges.write(line);
                outputExternalEdges.newLine();

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
        outputSearch.newLine();
        
        writeSearchResults(searchResult);
    }

    private static void outputSearchWithEnsembl(List<String[]> searchResult) throws IOException {

        outputSearch.write("ENSEMBL" + separator + "UNIPROT" + separator + "REACTION_STID" + separator
                + "REACTION_DISPLAY_NAME" + separator + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.newLine();

        writeSearchResults(searchResult);
    }

    private static void outputSearchWithUniProt(List<String[]> searchResult) throws IOException {

        outputSearch.write("UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
                + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.newLine();

        writeSearchResults(searchResult);
    }

    private static void outputSearchWithProteoform(List<String[]> searchResult) throws IOException {

        outputSearch.write("PROTEOFORM" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
                + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");
        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.newLine();

        writeSearchResults(searchResult);
    }
    
    private static void writeSearchResults(List<String[]> searchResult) throws IOException {
        
        for (String[] r : searchResult) {
            for (int i = 0; i < r.length; i++) {
                if (i > 0) {
                    outputSearch.write(separator);
                }
                outputSearch.write(r[i]);
            }
            outputSearch.newLine();
        }
    }

    /**
     * Adds a new command line option for the program.
     *
     * @param opt Short name
     * @param longOpt Long name
     * @param hasArg If requires a value argument
     * @param description Short text to explain the functionality of the option
     * @param required If the user has to specify this option each time the
     * program is run
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
