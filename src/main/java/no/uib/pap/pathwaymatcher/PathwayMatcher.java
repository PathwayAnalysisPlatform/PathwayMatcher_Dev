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

import java.io.*;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;

import static no.uib.pap.methods.analysis.ora.Analysis.decidePopulationSize;

public class PathwayMatcher {

    private static final String separator = "\t";    // Column separator

    private static BufferedWriter output_search;
    private static BufferedWriter output_analysis;

    private static MatchType matchType = MatchType.SUBSET;

    public static void main(String args[]) {

        InputType inputType;
        SearchResult searchResult;
        AnalysisResult analysisResult;
        String output_path = "";
        Long margin = 0L;

        Stopwatch stopwatch = Stopwatch.createStarted();

        System.setProperty("version", "1.8.2");

        // ******** ******** Read and process command line arguments ******** ********
        CommandLineParser parser = new DefaultParser();
        Options helpOptions = createHelpOptions();
        Options options = createUsageOptions();
        CommandLine commandLine;
        HelpFormatter formatter = new HelpFormatter();

        try {
            if (args.length == 0) {
                throw new ParseException("No arguments");
            }

            // Check consistency of help options
            if (hasHelpArguments(helpOptions, args)) {
                commandLine = parser.parse(helpOptions, args);
            } else {
                // Check consistency of all other options
                commandLine = parser.parse(options, args);
            }

            if (commandLine.hasOption("h")) {
                formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
                System.exit(0);
            }

            if (commandLine.hasOption("v")) {
                System.out.println("PathwayMatcher version " + System.getProperty("version"));
                System.exit(0);
            }

            if (commandLine.hasOption("r")) {
                margin = Long.valueOf(commandLine.getOptionValue("r"));
            }

            try {
                inputType = createInputType(commandLine.getOptionValue("inputType"));
                setMatchType(inputType, commandLine);
                checkConsistency_Fasta(inputType, commandLine);

                output_path = createOutputPath(commandLine);

                List<String> input = readInput(commandLine.getOptionValue("i"));

                output_search = createOutputFiles(output_path, "search.tsv");
                Mapping mapping = new Mapping(inputType, commandLine.hasOption("tlp")); // Load static structures needed for all the cases
                searchResult = Search.search(input, inputType, commandLine.hasOption("tlp"), mapping,
                        matchType, margin, commandLine.getOptionValue("f"));
                searchResult.writeToFile(output_search, separator);
                output_search.close();

                int populationSize = decidePopulationSize(inputType, mapping.getProteinsToReactions().keySet().size(), mapping.getProteoformsToReactions().keySet().size());
                output_analysis = createOutputFiles(output_path, "analysis.tsv");
                analysisResult = Analysis.analysis(searchResult, populationSize);
                analysisResult.writeToFile(output_analysis, inputType, separator);
                output_analysis.close();


                // ******** ******** Write networks ******** ********
                NetworkGenerator.writeNetworks(commandLine.hasOption("g"),
                        commandLine.hasOption("gg"),
                        commandLine.hasOption("gu"),
                        commandLine.hasOption("gp"),
                        inputType,
                        searchResult,
                        mapping,
                        output_path);

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

        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar PathwayMatcher.jar <options>", options);
            if (args.length == 0) {
                System.exit(Error.NO_ARGUMENTS.getCode());
            }
            if (e.getMessage().startsWith("Missing required option: i")) {
                System.exit(Error.NO_INPUT.getCode());
            }
            if (e.getMessage().startsWith("Missing required option: f")) {
                System.exit(Error.COULD_NOT_READ_FASTA_FILE.getCode());
            }
            if (e.getMessage().startsWith(Error.INVALID_MATCHING_TYPE.getMessage())) {
                System.exit(Error.INVALID_MATCHING_TYPE.getCode());
            }
            if (e.getMessage().startsWith("Missing required option:")) {
                System.exit(Error.MISSING_ARGUMENT.getCode());
            }
            System.exit(Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        }
    }

    private static InputType createInputType(String inputType) throws ParseException {
        inputType = inputType.toUpperCase();
        if (!InputType.isValueOf(inputType.toUpperCase())) {
            throw new ParseException(Error.INVALID_INPUT_TYPE.getMessage());
        }
        return InputType.valueOf(inputType);
    }

    private static void setMatchType(InputType inputType, CommandLine commandLine) throws ParseException {
        switch (inputType) {
            case PROTEOFORM:
            case PROTEOFORMS:
            case MODIFIEDPEPTIDE:
            case MODIFIEDPEPTIDES:
                if (commandLine.hasOption("m")) {
                    String matchTypeValue = commandLine.getOptionValue("m").toUpperCase();
                    if (MatchType.isValueOf(matchTypeValue)) {
                        matchType = MatchType.valueOf(matchTypeValue);
                    } else {
                        throw new ParseException(Error.INVALID_MATCHING_TYPE.getMessage());
                    }
                }
                break;
        }
    }

    private static void checkConsistency_Fasta(InputType inputType, CommandLine commandLine) throws IOException, ParseException {
        switch (inputType) {
            case PEPTIDES:
            case PEPTIDE:
            case MODIFIEDPEPTIDE:
            case MODIFIEDPEPTIDES:
                if (commandLine.hasOption("f")) {
                    File f = new File(commandLine.getOptionValue("f"));
                    if (!f.exists() || f.isDirectory()) {
                        throw new IOException(Error.COULD_NOT_READ_FASTA_FILE.getMessage());
                    }
                } else {
                    throw new ParseException("Missing required option: f");
                }
        }
    }

    private static BufferedWriter createOutputFiles(String path, String file) {
        File outputDir = new File(path);
        BufferedWriter br = null;

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            br = new BufferedWriter(new FileWriter(path + file));
        } catch (IOException e) {
            System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage());
            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
        }
        return br;
    }

    private static String createOutputPath(CommandLine commandLine) {
        if (commandLine.hasOption("o")) {
            return commandLine.getOptionValue("o").endsWith("/") ? commandLine.getOptionValue("o") : commandLine.getOptionValue("o") + "/";
        } else {
            return "";
        }
    }

    private static Options createHelpOptions() {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Print usage and available arguments", false));
        options.addOption(createOption("v", "version", false, "Print version of PathwayMatcher", false));
        return options;
    }

    private static Options createUsageOptions() {
        Options options = new Options();
        options.addOption(createOption("t", "inputType", true, "Input inputType: gene|ensembl|uniprot|peptide|rsid|proteoform", true));
        options.addOption(createOption("r", "range", true, "Ptm sites margin of error", false));
        options.addOption(createOption("tlp", "toplevelpathways", false, "Show Top Level Pathway columns", false));
        options.addOption(createOption("m", "matchType", true, "Proteoform match criteria: strict|one|superset|subset|one_no_types|superset_no_types|subset_no_types", false));
        options.addOption(createOption("i", "input", true, "Input file", true));
        options.addOption(createOption("o", "output", true, "Output path", false));
        options.addOption(createOption("g", "graph", false, "Create connection graph", false));
        options.addOption(createOption("gu", "graphUniprot", false, "Create protein connection graph", false));
        options.addOption(createOption("gp", "graphProteoform", false, "Create proteoform connection graph", false));
        options.addOption(createOption("gg", "graphGene", false, "Create gene connection graph", false));
        options.addOption(createOption("f", "fasta", true, "Proteins where to find the peptides", false));
        options.addOption(createOption("h", "help", false, "Print usage and available arguments", false));
        options.addOption(createOption("v", "version", false, "Print version of PathwayMatcher", false));
        return options;
    }

    private static Option createOption(String opt, String longOpt, boolean hasArg, String description, boolean required) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(required);
        return option;
    }

    private static boolean hasHelpArguments(Options options, String args[]) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("h")) {
                return true;
            }
            if (commandLine.hasOption("v")) {
                return true;
            }
        } catch (ParseException e) {
            //System.out.println("There were no arguments compatible with the help options.");
        }
        return false;
    }

    private static List<String> readInput(String path) {
        File file = new File(path);
        try {
            return Files.readLines(file, Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("The input file: " + path + " was not found.");
            System.exit(Error.COULD_NOT_READ_INPUT_FILE.getCode());
        }
        return null;
    }

}



