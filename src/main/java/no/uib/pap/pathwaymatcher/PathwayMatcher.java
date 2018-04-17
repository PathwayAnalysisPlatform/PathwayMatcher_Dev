package no.uib.pap.pathwaymatcher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import static no.uib.pap.model.Error.ERROR_WITH_OUTPUT_FILE;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.model.InputPatterns.matches_ChrBp;
import static no.uib.pap.model.InputPatterns.matches_Rsid;
import static no.uib.pap.model.InputPatterns.matches_Vcf_Record;
import static no.uib.pap.model.InputType.GENE;
import static no.uib.pap.model.InputType.GENES;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;

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
    static MatchType matchType = MatchType.SUPERSET;
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
    static ImmutableMap<String, String> iProteins;
    static ImmutableMap<String, Reaction> imapReactions;
    static ImmutableMap<String, Pathway> iPathways;
    static ImmutableSetMultimap<String, String> imapRsIdsToProteins;
    static ImmutableSetMultimap<Long, String> imapChrBpToProteins;
    static ImmutableSetMultimap<String, String> imapGenesToProteins;
    static ImmutableSetMultimap<String, String> imapEnsemblToProteins;
    static ImmutableSetMultimap<String, Proteoform> imapProteinsToProteoforms;
    static ImmutableSetMultimap<Proteoform, String> imapProteoformsToReactions;
    static ImmutableSetMultimap<String, String> imapProteinsToReactions;
    static ImmutableSetMultimap<String, String> imapReactionsToPathways;
    static ImmutableSetMultimap<String, String> imapPathwaysToTopLevelPathways;

    static HashSet<String> hitPathways;
    static TreeSet<String> hitGenes = new TreeSet<String>(); // These are in the reference data
    static TreeMultimap<String, String> mapProteinsToGenes = TreeMultimap.create();
    static TreeSet<String> hitProteins = new TreeSet<String>(); // These are in the reference data
    static HashSet<Proteoform> hitProteoforms = new HashSet<>(); // These are in the reference data
    static HashSet<String> inputProteins = new HashSet<>(); // These may not be in the reference data
    static HashSet<Proteoform> inputProteoforms = new HashSet<>(); // These may not be in the reference data

    public static void main(String args[]) {

        // ******** ******** Read and process command line arguments ******** ********
        addOption("t", "inputType", true, "Input inputType: GENE|ENSEMBL|UNIPROT|PEPTIDE|RSID|PROTEOFORM", true);
        addOption("r", "range", true, "Ptm sites margin of error", false);
        addOption("tlp", "toplevelpathways", false, "Show Top Level Pathway columns", false);
        addOption("m", "matchType", true, "Proteoform match criteria: STRICT|ONE|SUPERSET|SUBSET", false);
        addOption("i", "input", true, "Input file", true);
        addOption("o", "output", true, "Output path", false);
        addOption("g", "graph", false, "Create connection graph", false);
        addOption("gu", "graphUniprot", false, "Create protein connection graph", false);
        addOption("gp", "graphProteoform", false, "Create proteoform connection graph", false);
        addOption("gg", "graphGene", false, "Create gene connection graph", false);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("r")) {
                margin = Long.valueOf(commandLine.getOptionValue("r"));
            }

                String inputTypeValue = commandLine.getOptionValue("inputType").toUpperCase();
                if (!InputType.isValueOf(inputTypeValue)) {
                    System.out.println("Invalid input type: " + inputTypeValue);
                    System.exit(Error.INVALID_INPUT_TYPE.getCode());
                }

                inputType = InputType.valueOf(inputTypeValue);

                switch (inputType){
                    case PROTEOFORM:
                    case PROTEOFORMS:
                    case MODIFIEDPEPTIDE:
                    case MODIFIEDPEPTIDES:
                        if (commandLine.hasOption("m")) {
                            String matchTypeValue = commandLine.getOptionValue("m").toUpperCase();
                            if (MatchType.isValueOf(matchTypeValue)) {
                                matchType = MatchType.valueOf(matchTypeValue);
                            } else {
                                System.out.println(Error.INVALID_MATCHING_TYPE.getMessage());
                                System.exit(Error.INVALID_MATCHING_TYPE.getCode());
                            }
                        }
                        break;
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
            imapReactions = (ImmutableMap<String, Reaction>) getSerializedObject("imapReactions.gz");
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

            switch (inputType) {
                case GENE:
                case GENES:
                    imapGenesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapGenesToProteins.gz");
                    searchResult = Search.searchWithGene(input, imapReactions, iPathways, imapGenesToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways, hitGenes);
                    outputSearchWithGene(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteoforms();
                    break;
                case ENSEMBL:
                case ENSEMBLS:
                    imapEnsemblToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapEnsemblToProteins.gz");
                    searchResult = Search.searchWithEnsembl(input, imapReactions, iPathways, imapEnsemblToProteins,
                            imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                            commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithEnsembl(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteoforms();
                    break;
                case UNIPROT:
                case UNIPROTS:
                    searchResult = Search.searchWithUniProt(input, imapReactions, iPathways, imapProteinsToReactions,
                            imapReactionsToPathways, imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteoforms();
                    break;
                case PROTEOFORM:
                case PROTEOFORMS:
                    imapProteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapProteinsToProteoforms.gz");
                    imapProteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToReactions.gz");
                    searchResult = Search.searchWithProteoform(input, matchType, margin, imapReactions, iPathways,
                            imapProteinsToProteoforms, imapProteoformsToReactions, imapReactionsToPathways,
                            imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithProteoform(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteoformsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteins();
                    break;
                case RSID:
                case RSIDS:
                    HashSet<String> rsIdSet = new HashSet<>();
                    // Get the unique set of Variants
                    int row = 0;
                    for (String rsid : input) {
                        row++;
                        if (rsid.isEmpty()) {
                            sendWarning(EMPTY_ROW, row);
                            continue;
                        }
                        if (!matches_Rsid(rsid)) {
                            sendWarning(INVALID_ROW, row);
                            continue;
                        }
                        rsIdSet.add(rsid);
                    }
                    outputSearchWithRsidHeader();
                    for (int chr = 1; chr <= 22; chr++) {
                        System.out.println("Loading data for chromosome " + chr);
                        imapRsIdsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapRsIdsToProteins" + chr + ".gz");
                        System.out.println("Matching...");
                        searchResult = Search.searchWithRsId(rsIdSet, imapReactions, iPathways, imapRsIdsToProteins,
                                imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                                commandLine.hasOption("tlp"), hitProteins, hitPathways);
                        writeSearchResults(searchResult.getKey());
                    }

                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteoforms();
                    break;
                case CHRBP:
                case CHRBPS:
                case VCF:
                    TreeMultimap<Integer, Long> chrBpMap = TreeMultimap.create();
                    Snp snp = null;
                    row = 0;
                    for (String line : input) {
                        row++;
                        if (line.isEmpty()) {
                            sendWarning(EMPTY_ROW, row);
                            continue;
                        }
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (!matches_ChrBp(line) && !matches_Vcf_Record(line)) {
                            sendWarning(INVALID_ROW, row);
                            continue;
                        }
                        snp = getSnpFromChrBp(line);
                        chrBpMap.put(snp.getChr(), snp.getBp());
                    }
                    outputSearchWithChrBpHeader();
                    for (int chr : chrBpMap.keySet()) {
                        System.out.println("Loading data for chromosome " + chr);
                        imapChrBpToProteins = (ImmutableSetMultimap<Long, String>) getSerializedObject("imapChrBpToProteins" + chr + ".gz");
                        searchResult = Search.searchWithChrBp(chr, chrBpMap.get(chr), imapReactions, iPathways, imapChrBpToProteins,
                                imapProteinsToReactions, imapReactionsToPathways, imapPathwaysToTopLevelPathways,
                                commandLine.hasOption("tlp"), hitProteins, hitPathways);
                        writeSearchResults(searchResult.getKey());
                    }
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteoforms();
                    break;
                case PEPTIDE:
                case PEPTIDES:
                    searchResult = Search.searchWithPeptide(input, imapReactions, iPathways, imapProteinsToReactions,
                            imapReactionsToPathways, imapPathwaysToTopLevelPathways, commandLine.hasOption("tlp"), hitProteins, hitPathways);
                    outputSearchWithUniProt(searchResult.getKey());
                    System.out.println("Matching results writen to: " + outputPath + "search.csv");
                    System.out.println("Starting ORA analysis...");
                    analysisResult = Analysis.analysis(iPathways, imapProteinsToReactions.keySet().size(),
                            hitProteins, hitPathways);
                    getHitProteoforms();
                    break;
                case MODIFIEDPEPTIDE:
                case MODIFIEDPEPTIDES:
                    imapProteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapProteinsToProteoforms.gz");
                    imapProteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToReactions.gz");
                    searchResult = Search.searchWithModifiedPeptide(input, matchType, margin, imapReactions, iPathways,
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
                    getHitProteins();
                    break;
                default:
                    System.out.println("Input inputType not supported.");
                    System.exit(1);
                    break;
            }

            writeAnalysisResult(hitPathways, iPathways);
            System.out.println("Analysis results writen to: " + outputPath + "analysis.csv");

            boolean doProteinGraph = false;
            boolean doProteoformGraph = false;
            boolean doGeneGraph = false;

            if (commandLine.hasOption("g")) {
                switch (inputType) {
                    case PROTEOFORMS:
                    case PROTEOFORM:
                    case MODIFIEDPEPTIDE:
                    case MODIFIEDPEPTIDES:
                        doProteoformGraph = true;
                        break;
                    default:
                        doProteinGraph = true;
                        getHitProteoforms();
                        break;
                }
            }
            if (commandLine.hasOption("gu") || doProteinGraph) {
                writeProteinGraph();
            }
            if (commandLine.hasOption("gp") || doProteoformGraph) {
                writeProteoformGraph();
            }
            if (commandLine.hasOption("gg")) {
                if(!inputType.equals(GENE) && !inputType.equals(GENES)){
                    getHitGenes();
                }
                writeGeneGraph();
            }

            outputSearch.close();
            outputAnalysis.close();

        } catch (IOException e1) {
            System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage() + ": " + outputPath + "search.txt  " + eol + outputPath
                    + "analysis.txt");
            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
        }
    }

    private static void getHitProteins() {
        if(imapProteoformsToReactions == null){
            imapProteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToReactions.gz");
        }
        for (Proteoform proteoform : hitProteoforms) {
            hitProteins.add(proteoform.getUniProtAcc());
        }
    }

    private static void getHitProteoforms() {
        if(imapProteinsToProteoforms == null){
            imapProteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapProteinsToProteoforms.gz");
        }
        for (String protein : hitProteins) {
            for (Proteoform proteoform : imapProteinsToProteoforms.get(protein)) {
                hitProteoforms.add(proteoform);
            }
        }
    }

    private static void getHitGenes() {
        if(imapGenesToProteins == null){
            imapGenesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapGenesToProteins.gz");
        }

        for(Map.Entry<String, String> entry : imapGenesToProteins.entries()){
            String gene = entry.getKey();
            String protein = entry.getValue();
            if (hitProteins.contains(protein)) {
                hitGenes.add(gene);
                mapProteinsToGenes.put(protein, gene);
            }
        }
    }

    private static void writeProteinGraph() throws IOException {

        System.out.println("Creating protein connection graph...");

        ImmutableSetMultimap<String, String> imapProteinsToComplexes = (ImmutableSetMultimap<String, String>) getSerializedObject("imapProteinsToComplexes.gz");
        ImmutableSetMultimap<String, String> imapComplexesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapComplexesToProteins.gz");
        ImmutableSetMultimap<String, String> imapSetsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapSetsToProteins.gz");
        ImmutableSetMultimap<String, String> imapProteinsToSets = (ImmutableSetMultimap<String, String>) getSerializedObject("imapProteinsToSets.gz");
        HashSet<String> checkedComplexes = new HashSet<>();
        HashSet<String> checkedReactions = new HashSet<>();
        HashSet<String> checkedSets = new HashSet<>();

        //Create output files
        outputVertices = new BufferedWriter(new FileWriter(outputPath + "proteinVertices.tsv"));
        outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteinInternalEdges.tsv"));
        outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteinExternalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + separator + " name" + eol);
        outputInternalEdges.write("id1" + separator + "id2" + separator + "type" + separator + "container_id" + separator + "role1" + separator + "role2" + eol);
        outputExternalEdges.write("id1" + separator + "id2" + separator + "type" + separator + "container_id" + separator + "role1" + separator + "role2" + eol);

        // Load static mapping
        iProteins = (ImmutableMap<String, String>) getSerializedObject("iProteins.gz");

        // Write the vertices file
        for (String protein : hitProteins) {
            String line = String.join(separator, protein, iProteins.get(protein));
            outputVertices.write(line);
            outputVertices.newLine();
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "proteinVertices.tsv");

        // Write edges among input proteins

        for (String protein : hitProteins) {
            // Output reaction neighbours
            for (String reaction : imapProteinsToReactions.get(protein)) {

                // Avoid adding edges related to the same reaction
                if (checkedReactions.contains(reaction)) {
                    continue;
                }
                checkedReactions.add(reaction);
                for (Map.Entry<String, Role> from_participant : imapReactions.get(reaction).getProteinParticipants().entries()) {
                    for (Map.Entry<String, Role> to_participant : imapReactions.get(reaction).getProteinParticipants().entries()) {
                        if (from_participant.getKey().compareTo(to_participant.getKey()) < 0) {   // Only different and ordered pairs to avoid duplicate edges
                            if (hitProteins.contains(from_participant.getKey()) || hitProteins.contains(to_participant.getKey())) {
                                String line = String.join(
                                        separator,
                                        from_participant.getKey(),
                                        to_participant.getKey(),
                                        "Reaction",
                                        reaction,
                                        from_participant.getValue().toString(),
                                        to_participant.getValue().toString());
                                if (hitProteins.contains(from_participant.getKey()) && hitProteins.contains(to_participant.getKey())) {
                                    outputInternalEdges.write(line);
                                    outputInternalEdges.newLine();
                                } else {
                                    outputExternalEdges.write(line);
                                    outputExternalEdges.newLine();
                                }
                            }
                        }
                    }
                }
            }

            // Output complex neighbours
            for (String complex : imapProteinsToComplexes.get(protein)) {

                // Avoid adding edges related to the same complex
                if (checkedComplexes.contains(complex)) {
                    continue;
                }
                checkedComplexes.add(complex);

                // For each pair of components in this complex
                for (String from_component : imapComplexesToProteins.get(complex)) {
                    for (String to_component : imapComplexesToProteins.get(complex)) {
                        if (from_component.compareTo(to_component) < 0) {
                            if (hitProteins.contains(from_component) || hitProteins.contains(to_component)) {
                                String line = String.join(separator, from_component, to_component, "Complex", complex, "component", "component");
                                if (hitProteins.contains(from_component) && hitProteins.contains(to_component)) {
                                    outputInternalEdges.write(line);
                                    outputInternalEdges.newLine();
                                } else {
                                    outputExternalEdges.write(line);
                                    outputExternalEdges.newLine();
                                }
                            }
                        }
                    }
                }
            }

            // Output set neighbours
            for (String set : imapProteinsToSets.get(protein)) {

                // Avoid adding edges related to the same complex
                if (checkedSets.contains(set)) {
                    continue;
                }
                checkedSets.add(set);

                // For each pair of members of this set
                for (String from_member : imapSetsToProteins.get(set)) {
                    for (String to_member : imapSetsToProteins.get(set)) {
                        if (from_member.compareTo(to_member) < 0) {
                            if (hitProteins.contains(from_member) || hitProteins.contains(to_member)) {
                                String line = String.join(separator, protein, to_member, "Set", set, "member/candidate", "member/candidate");
                                if (hitProteins.contains(from_member) && hitProteins.contains(to_member)) {
                                    outputInternalEdges.write(line);
                                    outputInternalEdges.newLine();
                                } else {
                                    outputExternalEdges.write(line);
                                    outputExternalEdges.newLine();
                                }
                            }
                        }
                    }
                }
            }
        }
        outputInternalEdges.close();
        outputExternalEdges.close();
        System.out.println("Finished writing edges files: \n" + outputPath + "proteinInternalEdges.tsv\n" + outputPath + "proteinExternalEdges.tsv");
    }

    private static void writeGeneGraph() throws IOException {
        System.out.println("Creating gene connection graph...");

        ImmutableSetMultimap<String, String> imapProteinsToComplexes = (ImmutableSetMultimap<String, String>) getSerializedObject("imapProteinsToComplexes.gz");
        ImmutableSetMultimap<String, String> imapComplexesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapComplexesToProteins.gz");
        ImmutableSetMultimap<String, String> imapSetsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("imapSetsToProteins.gz");
        ImmutableSetMultimap<String, String> imapProteinsToSets = (ImmutableSetMultimap<String, String>) getSerializedObject("imapProteinsToSets.gz");
        HashSet<String> checkedComplexes = new HashSet<>();
        HashSet<String> checkedReactions = new HashSet<>();
        HashSet<String> checkedSets = new HashSet<>();
        TreeMultimap<String, String> addedEdges = TreeMultimap.create();

        //Create output files
        outputVertices = new BufferedWriter(new FileWriter(outputPath + "geneVertices.tsv"));
        outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "geneInternalEdges.tsv"));
        outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "geneExternalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + separator + " name" + eol);
        outputInternalEdges.write("id1" + separator + "id2" + separator + "type" + separator + "container_id" + separator + "role1" + separator + "role2" + eol);
        outputExternalEdges.write("id1" + separator + "id2" + separator + "type" + separator + "container_id" + separator + "role1" + separator + "role2" + eol);

        // Load static mapping
        iProteins = (ImmutableMap<String, String>) getSerializedObject("iProteins.gz");

        // Write the vertices file
        for (String gene : hitGenes) {
            for(String protein : imapGenesToProteins.get(gene)){
                String line = String.join(separator, gene, iProteins.get(protein));
                outputVertices.write(line);
                outputVertices.newLine();
            }
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "geneVertices.tsv");

        // Write edges among input genes

        for (String protein : hitProteins) {
            // Output reaction neighbours
            for (String reaction : imapProteinsToReactions.get(protein)) {

                // Avoid adding edges related to the same reaction
                if (checkedReactions.contains(reaction)) {
                    continue;
                }
                checkedReactions.add(reaction);
                for (Map.Entry<String, Role> from_participant : imapReactions.get(reaction).getProteinParticipants().entries()) {
                    for (Map.Entry<String, Role> to_participant : imapReactions.get(reaction).getProteinParticipants().entries()) {
                        if (hitProteins.contains(from_participant.getKey()) || hitProteins.contains(to_participant.getKey())) {
                            for (String gene_from : mapProteinsToGenes.get(from_participant.getKey())) {
                                for (String gene_to : mapProteinsToGenes.get(to_participant.getKey())) {
                                    if (gene_from.equals(gene_to)) {
                                        continue;
                                    }
                                    if (addedEdges.containsEntry(gene_from, gene_to)) {
                                        continue;
                                    }
                                    addedEdges.put(gene_from, gene_to);
                                    String line = String.join(
                                            separator,
                                            gene_from,
                                            gene_to,
                                            "Reaction",
                                            reaction,
                                            from_participant.getValue().toString(),
                                            to_participant.getValue().toString());
                                    if (hitProteins.contains(from_participant.getKey()) && hitProteins.contains(to_participant.getKey())) {
                                        outputInternalEdges.write(line);
                                        outputInternalEdges.newLine();
                                    } else {
                                        outputExternalEdges.write(line);
                                        outputExternalEdges.newLine();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            addedEdges.clear();
            // Output complex neighbours
            for (String complex : imapProteinsToComplexes.get(protein)) {

                // Avoid adding edges related to the same complex
                if (checkedComplexes.contains(complex)) {
                    continue;
                }
                checkedComplexes.add(complex);

                // For each pair of components in this complex
                for (String from_component : imapComplexesToProteins.get(complex)) {
                    for (String to_component : imapComplexesToProteins.get(complex)) {
                        if (hitProteins.contains(from_component) || hitProteins.contains(to_component)) {
                            for (String gene_from : mapProteinsToGenes.get(from_component)) {
                                for (String gene_to : mapProteinsToGenes.get(to_component)) {
                                    if (!gene_from.equals(gene_to)) {
                                        continue;
                                    }
                                    if (addedEdges.containsEntry(gene_from, gene_to)) {
                                        continue;
                                    }
                                    addedEdges.put(gene_from, gene_to);
                                    String line = String.join(separator, gene_from, gene_to, "Complex", complex, "component", "component");
                                    if (hitProteins.contains(from_component) && hitProteins.contains(to_component)) {
                                        outputInternalEdges.write(line);
                                        outputInternalEdges.newLine();
                                    } else {
                                        outputExternalEdges.write(line);
                                        outputExternalEdges.newLine();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            addedEdges.clear();
            // Output set neighbours
            for (String set : imapProteinsToSets.get(protein)) {

                // Avoid adding edges related to the same complex
                if (checkedSets.contains(set)) {
                    continue;
                }
                checkedSets.add(set);

                // For each pair of members of this set
                for (String from_member : imapSetsToProteins.get(set)) {
                    for (String to_member : imapSetsToProteins.get(set)) {
                        if (hitProteins.contains(from_member) || hitProteins.contains(to_member)) {
                            for(String gene_from : mapProteinsToGenes.get(from_member)){
                                for(String gene_to : mapProteinsToGenes.get(to_member)){
                                    if(!gene_from.equals(gene_to) || addedEdges.containsEntry(gene_from, gene_to)){
                                        continue;
                                    }
                                    addedEdges.put(gene_from, gene_to);
                                    String line = String.join(separator, gene_from, gene_to, "Set", set, "member/candidate", "member/candidate");
                                    if (hitProteins.contains(from_member) && hitProteins.contains(to_member)) {
                                        outputInternalEdges.write(line);
                                        outputInternalEdges.newLine();
                                    } else {
                                        outputExternalEdges.write(line);
                                        outputExternalEdges.newLine();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        outputInternalEdges.close();
        outputExternalEdges.close();
        System.out.println("Finished writing edges files: \n" + outputPath + "geneInternalEdges.tsv\n" + outputPath + "geneExternalEdges.tsv");
    }

    private static void writeProteoformGraph() throws IOException {

        if(imapProteoformsToReactions == null){
            imapProteoformsToReactions = (ImmutableSetMultimap<Proteoform,String>) getSerializedObject("imapProteoformsToReactions.gz");
        }

        System.out.println("Creating proteoform connection graph...");

        ImmutableSetMultimap<Proteoform, String> imapProteoformsToComplexes = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToComplexes.gz");
        ImmutableSetMultimap<String, Proteoform> imapComplexesToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapComplexesToProteoforms.gz");
        ImmutableSetMultimap<String, Proteoform> imapSetsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("imapSetsToProteoforms.gz");
        ImmutableSetMultimap<Proteoform, String> imapProteoformsToSets = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("imapProteoformsToSets.gz");
        HashSet<String> checkedComplexes = new HashSet<>();
        HashSet<String> checkedReactions = new HashSet<>();
        HashSet<String> checkedSets = new HashSet<>();

        //Create output files
        outputVertices = new BufferedWriter(new FileWriter(outputPath + "proteoformVertices.tsv"));
        outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteoformInternalEdges.tsv"));
        outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteoformExternalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + separator + " name" + eol);
        outputInternalEdges.write("id1" + separator + "id2" + separator + "type" + separator + "container_id" + separator + "role1" + separator + "role2" + eol);
        outputExternalEdges.write("id1" + separator + "id2" + separator + "type" + separator + "container_id" + separator + "role1" + separator + "role2" + eol);

        // Load static mapping
        iProteins = (ImmutableMap<String, String>) getSerializedObject("iProteins.gz");

        // Write the vertices file
        for (Proteoform proteoform : hitProteoforms) {
            String line = String.join(separator, proteoform.toString(ProteoformFormat.SIMPLE), iProteins.get(proteoform.getUniProtAcc()));
            outputVertices.write(line);
            outputVertices.newLine();
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "proteoformVertices.tsv");

        // Write edges among input proteins
        for (Proteoform proteoform : hitProteoforms) {
            // Output reaction neighbours
            for (String reaction : imapProteoformsToReactions.get(proteoform)) {

                // Avoid adding edges related to the same reaction
                if (checkedReactions.contains(reaction)) {
                    continue;
                }
                checkedReactions.add(reaction);

                //For each pair of participant proteoforms
                for (Map.Entry<Proteoform, Role> from_participant : imapReactions.get(reaction).getProteoformParticipants().entries()) {
                    for (Map.Entry<Proteoform, Role> to_participant : imapReactions.get(reaction).getProteoformParticipants().entries()) {

                        if (from_participant.getKey().compareTo(to_participant.getKey()) < 0) {   // Only different and ordered pairs to avoid duplicate edges
                            if (hitProteoforms.contains(from_participant.getKey()) || hitProteoforms.contains(to_participant.getKey())) {
                                String line = String.join(
                                        separator,
                                        from_participant.getKey().toString(ProteoformFormat.SIMPLE),
                                        to_participant.getKey().toString(ProteoformFormat.SIMPLE),
                                        "Reaction",
                                        reaction,
                                        from_participant.getValue().toString(),
                                        to_participant.getValue().toString()
                                );
                                if (hitProteoforms.contains(from_participant.getKey()) && hitProteoforms.contains(to_participant.getKey())) {
                                    outputInternalEdges.write(line);
                                    outputInternalEdges.newLine();
                                } else {
                                    outputExternalEdges.write(line);
                                    outputExternalEdges.newLine();
                                }
                            }
                        }
                    }
                }
            }

            // Output complex neighbours
            for (String complex : imapProteoformsToComplexes.get(proteoform)) {

                // Avoid adding edges related to the same complex
                if (checkedComplexes.contains(complex)) {
                    continue;
                }
                checkedComplexes.add(complex);

                // For each pair of components in this complex
                for (Proteoform from_component : imapComplexesToProteoforms.get(complex)) {
                    for (Proteoform to_component : imapComplexesToProteoforms.get(complex)) {
                        if (from_component.compareTo(to_component) < 0) {
                            if (hitProteoforms.contains(from_component) || hitProteoforms.contains(to_component)) {
                                String line = String.join(
                                        separator,
                                        from_component.toString(ProteoformFormat.SIMPLE),
                                        to_component.toString(ProteoformFormat.SIMPLE),
                                        "Complex",
                                        complex,
                                        "component",
                                        "component"
                                );
                                if (hitProteoforms.contains(from_component) && hitProteoforms.contains(to_component)) {
                                    outputInternalEdges.write(line);
                                    outputInternalEdges.newLine();
                                } else {
                                    outputExternalEdges.write(line);
                                    outputExternalEdges.newLine();
                                }
                            }
                        }
                    }
                }
            }

            // Output set neighbours
            for (String set : imapProteoformsToSets.get(proteoform)) {

                // Avoid adding edges related to the same complex
                if (checkedSets.contains(set)) {
                    continue;
                }
                checkedSets.add(set);

                // For each pair of members of this set
                for (Proteoform from_member : imapSetsToProteoforms.get(set)) {
                    for (Proteoform to_member : imapSetsToProteoforms.get(set)) {
                        if (from_member.compareTo(to_member) < 0) {
                            if (hitProteoforms.contains(from_member) || hitProteoforms.contains(to_member)) {
                                String line = String.join(
                                        separator,
                                        from_member.toString(ProteoformFormat.SIMPLE),
                                        to_member.toString(ProteoformFormat.SIMPLE),
                                        "Set",
                                        set,
                                        "member/candidate",
                                        "member/candidate"
                                );
                                if (hitProteoforms.contains(from_member) && hitProteoforms.contains(to_member)) {
                                    outputInternalEdges.write(line);
                                    outputInternalEdges.newLine();
                                } else {
                                    outputExternalEdges.write(line);
                                    outputExternalEdges.newLine();
                                }
                            }
                        }
                    }
                }
            }
        }
        outputInternalEdges.close();
        outputExternalEdges.close();
        System.out.println("Finished writing edges files: \n" + outputPath + "proteoformInternalEdges.tsv\n" + outputPath + "proteoformExternalEdges.tsv");
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
                outputAnalysis.write(line);
                outputAnalysis.newLine();
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

    private static void outputSearchWithRsidHeader() throws IOException {

        outputSearch.write("RSID" + separator + "UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
                + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.newLine();
    }

    private static void outputSearchWithChrBpHeader() throws IOException {

        outputSearch.write("CHROMOSOME" + separator + "BASE_PAIR" + separator + "UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
                + "PATHWAY_STID" + separator + "PATHWAY_DISPLAY_NAME");

        if (commandLine.hasOption("tlp")) {
            outputSearch.write(separator + "TOP_LEVEL_PATHWAY_STID" + separator + "TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
        outputSearch.newLine();
    }

    private static void outputSearchWithProteoform(List<String[]> searchResult) throws IOException {

        outputSearch.write("PROTEOFORM" + separator + "UNIPROT" + separator + "REACTION_STID" + separator + "REACTION_DISPLAY_NAME" + separator
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
     * @param opt         Short name
     * @param longOpt     Long name
     * @param hasArg      If requires a value argument
     * @param description Short text to explain the functionality of the option
     * @param required    If the user has to specify this option each time the
     *                    program is run
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
     * Get the snp instance from a line with chromosome and base pair.
     * This method expects the line to be validated already
     */
    private static Snp getSnpFromChrBp(String line) {
        String[] fields = line.split("\\s");
        Integer chr = Integer.valueOf(fields[0]);
        Long bp = Long.valueOf(fields[1]);

        return new Snp(chr, bp);
    }
}
