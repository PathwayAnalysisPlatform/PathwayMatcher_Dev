package no.uib.pap.pathwaymatcher;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import no.uib.pap.methods.analysis.ora.Analysis;
import no.uib.pap.methods.analysis.ora.AnalysisResult;
import no.uib.pap.methods.search.Search;
import no.uib.pap.methods.search.SearchResult;
import no.uib.pap.model.*;
import no.uib.pap.model.Error;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;

public class PathwayMatcher {

    private static final String separator = "\t";    // Column separator

    // Search and analysis parameters
    private static InputType inputType;
    private static Boolean showTopLevelPathways = false;
    private static MatchType matchType = MatchType.SUBSET;
    private static Long range = 0L;
    private static int populationSize = 1;

    // File parameters
    private static String input_path = "";
    private static String output_path = "";
    private static String fasta_path = "";

    // Graph parameters
    private static boolean doDefaultGraph = false;
    private static boolean doGeneGraph = false;
    private static boolean doUniprotGraph = false;
    private static boolean doProteoformGraph = false;

    public static void main(String args[]) {

        BufferedWriter output_search;
        BufferedWriter output_analysis;
        SearchResult searchResult;
        AnalysisResult analysisResult;

        Stopwatch stopwatch = Stopwatch.createStarted();

        System.setProperty("version", "1.8.2");

        parseArguments(args);

        try {
            List<String> input = readInput(input_path);

            output_search = createOutputFiles(output_path, "search.tsv");
            Mapping mapping = new Mapping(inputType, showTopLevelPathways); // Load static structures needed for all the cases

            searchResult = Search.search(input, inputType, showTopLevelPathways, mapping,
                    matchType, range, fasta_path);
            searchResult.writeToFile(output_search, separator);
            output_search.close();

            output_analysis = createOutputFiles(output_path, "analysis.tsv");
            setPopulationSize(mapping.getProteinsToReactions().keySet().size(), mapping.getProteoformsToReactions().keySet().size());
            analysisResult = Analysis.analysis(searchResult, populationSize);
            analysisResult.writeToFile(output_analysis, inputType, separator);
            output_analysis.close();

            NetworkGenerator.writeGraphs(doGeneGraph, doUniprotGraph, doProteoformGraph,
                    inputType, searchResult, mapping, output_path);

            stopwatch.stop();
            Duration duration = stopwatch.elapsed();
            System.out.println("PathwayMatcher finished (" + duration.toMillis() / 1000 + "s)");

        } catch (IOException e) {
            if (e.getMessage().contains("network") || e.getMessage().contains("directory")) {
                System.out.println(e.getMessage());
            } else {
                System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage() + ": " +
                        output_path + "search.txt  " +
                        System.lineSeparator() +
                        output_path + "analysis.txt");
            }
            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
        }
    }

    private static Option createOption(String opt, String longOpt, boolean hasArg, String description) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(false);
        return option;
    }

    private static Options createUsageOptions() {
        Options options = new Options();
        options.addOption(createOption("t", "inputType", true, "Input inputType: gene|ensembl|uniprot|peptide|rsid|proteoform"));
        options.addOption(createOption("r", "range", true, "Ptm sites range of error"));
        options.addOption(createOption("tlp", "toplevelpathways", false, "Show Top Level Pathway columns"));
        options.addOption(createOption("m", "matchType", true, "Proteoform match criteria: strict|one|superset|subset|one_no_types|superset_no_types|subset_no_types"));
        options.addOption(createOption("i", "input", true, "Input file"));
        options.addOption(createOption("o", "output", true, "Output path"));
        options.addOption(createOption("g", "graph", false, "Create connection graph"));
        options.addOption(createOption("gu", "graphUniprot", false, "Create protein connection graph"));
        options.addOption(createOption("gp", "graphProteoform", false, "Create proteoform connection graph"));
        options.addOption(createOption("gg", "graphGene", false, "Create gene connection graph"));
        options.addOption(createOption("f", "fasta", true, "Proteins where to find the peptides"));
        options.addOption(createOption("h", "help", false, "Print usage and available arguments"));
        options.addOption(createOption("v", "version", false, "Print version of PathwayMatcher"));
        return options;
    }

    private static void parseArguments(String args[]) {
        if (args.length == 0) {
            System.exit(Error.NO_ARGUMENTS.getCode());
        }

        CommandLineParser parser = new DefaultParser();
        Options options = createUsageOptions();
        CommandLine commandLine;
        HelpFormatter formatter = new HelpFormatter();

        try {
            commandLine = parser.parse(options, args);

            // Check for help arguments
            if (commandLine.hasOption("h")) {
                formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
                System.exit(0);
            }
            if (commandLine.hasOption("v")) {
                System.out.println("PathwayMatcher version " + System.getProperty("version"));
                System.exit(0);
            }

            // Set search parameters
            setInputType(commandLine.getOptionValue("t"));
            showTopLevelPathways = commandLine.hasOption("tlp");
            setMatchType(commandLine.getOptionValue("m"));
            setRange(commandLine.getOptionValue("r"));

            // Set file parameters
            setInputPath(commandLine.getOptionValue("i"));
            setOutputPath(commandLine.getOptionValue("o"));
            setFasta(commandLine.getOptionValue("f"));

            // Set graph parameters
            doDefaultGraph = commandLine.hasOption("g");
            setDoGeneGraph(commandLine.hasOption("gg"));
            setDoUniprotGraph(commandLine.hasOption("gu"));
            setDoProteoformGraph(commandLine.hasOption("gp"));

        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
            if (e.getMessage().startsWith("Missing")) {
                System.exit(Error.MISSING_ARGUMENT.getCode());
            }
            System.exit(Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        }
    }

    private static void setInputType(String value) throws ParseException {

        if (value == null) {
            throw new ParseException("Missing required option: t");
        }

        value = value.toUpperCase();
        if (!InputType.isValueOf(value)) {
            throw new ParseException(Error.INVALID_INPUT_TYPE.getMessage());
        }

        inputType = InputType.valueOf(value);
    }

    private static void setMatchType(String value) throws ParseException {
        switch (inputType) {
            case PROTEOFORM:
            case PROTEOFORMS:
            case MODIFIEDPEPTIDE:
            case MODIFIEDPEPTIDES:
                if (value == null) {
                    matchType = MatchType.SUBSET;
                } else {
                    value = value.toUpperCase();
                    if (MatchType.isValueOf(value)) {
                        matchType = MatchType.valueOf(value);
                    } else {
                        System.out.println(Error.INVALID_MATCHING_TYPE.getMessage());
                        System.exit(Error.INVALID_MATCHING_TYPE.getCode());
                    }
                }
                break;
        }
    }

    private static void setRange(String value) {
        switch (inputType) {
            case PROTEOFORM:
            case PROTEOFORMS:
            case MODIFIEDPEPTIDE:
            case MODIFIEDPEPTIDES:
                if (value != null) {
                    range = NumberUtils.toLong(value, 0L);  // Try to set value, if it doesn't work, set to 0
                }
                break;
        }
    }

    private static void setPopulationSize(int totalProteins, int totalProteoforms) {
        switch (inputType) {
            case GENE:
            case GENES:
            case ENSEMBL:
            case ENSEMBLS:
            case UNIPROT:
            case UNIPROTS:
            case RSID:
            case RSIDS:
            case CHRBP:
            case CHRBPS:
            case VCF:
            case PEPTIDE:
            case PEPTIDES:
                populationSize = totalProteins;
                break;
            case PROTEOFORM:
            case PROTEOFORMS:
            case MODIFIEDPEPTIDE:
            case MODIFIEDPEPTIDES:
                populationSize = totalProteoforms;
                break;
            default:
                populationSize = 0;
                break;
        }
    }

    private static void setInputPath(String value) throws ParseException {
        if (value == null) {
            throw new ParseException("Missing required option: i");
        }

        input_path = value;
    }

    private static void setOutputPath(String value) {
        if (value == null) {
            output_path = "";
        } else {
            output_path = value.endsWith("/") ? value : value + "/";
        }
    }

    private static void setFasta(String value) throws ParseException {
        switch (inputType) {
            case PEPTIDES:
            case PEPTIDE:
            case MODIFIEDPEPTIDE:
            case MODIFIEDPEPTIDES:
                if (value == null) {
                    throw new ParseException("Missing required option: f");
                } else {
                    File f = new File(value);
                    if (!f.exists() || f.isDirectory()) {
                        System.out.println(Error.COULD_NOT_READ_FASTA_FILE.getMessage());
                        System.exit(Error.COULD_NOT_READ_FASTA_FILE.getCode());
                    }
                    fasta_path = value;
                }
        }
    }

    private static void setDoGeneGraph(boolean value) {
        if (value) {
            doGeneGraph = true;
            return;
        }
        if (doDefaultGraph) {
            switch (inputType) {
                case GENE:
                case GENES:
                    doGeneGraph = true;
                    return;
            }
        }
        doGeneGraph = false;
    }

    private static void setDoUniprotGraph(boolean value) {
        if (value) {
            doUniprotGraph = true;
            return;
        }
        if (doDefaultGraph) {
            switch (inputType) {
                case UNIPROT:
                case UNIPROTS:
                case ENSEMBL:
                case ENSEMBLS:
                case PEPTIDE:
                case PEPTIDES:
                case VCF:
                case RSID:
                case RSIDS:
                case CHRBP:
                case CHRBPS:
                    doUniprotGraph = true;
                    return;
            }
        }
        doUniprotGraph = false;
    }

    private static void setDoProteoformGraph(boolean value) {
        if (value) {
            doProteoformGraph = true;
            return;
        }
        if (doDefaultGraph) {
            switch (inputType) {
                case PROTEOFORMS:
                case PROTEOFORM:
                case MODIFIEDPEPTIDE:
                case MODIFIEDPEPTIDES:
                    doProteoformGraph = true;
                    return;
            }
        }
        doProteoformGraph = false;
    }

    private static BufferedWriter createOutputFiles(String path, String file) {
        File outputDir = new File(path);
        BufferedWriter br = null;

        try {
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    throw new IOException();
                }
            }
            br = new BufferedWriter(new FileWriter(path + file));
        } catch (IOException e) {
            System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage());
            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
        }
        return br;
    }

    private static List<String> readInput(String path) {
        File file = new File(path);
        try {
            return Files.readLines(file, Charset.forName("ISO-8859-1"));
        } catch (IOException e) {
            System.out.println("The input file: " + path + " was not found.");
            System.exit(Error.COULD_NOT_READ_INPUT_FILE.getCode());
        }
        return new ArrayList<>();
    }
}



