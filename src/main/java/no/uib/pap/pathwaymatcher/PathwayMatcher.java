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

import static no.uib.pap.methods.analysis.ora.Analysis.getPopulationSize;

public class PathwayMatcher {

    private static final String separator = "\t";    // Column separator

    public static void main(String args[]) {

        String outputPath = "";
        InputType inputType;
        MatchType matchType = MatchType.SUBSET;
        AnalysisResult analysisResult;

        Long margin = 0L;

        Stopwatch stopwatch = Stopwatch.createStarted();

        System.setProperty("version", "1.8.2");

        // ******** ******** Read and process command line arguments ******** ********
        CommandLineParser parser = new DefaultParser();
        Options helpOptions = createHelpOptions();
        Options options = createOptions();
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

            String inputTypeValue = commandLine.getOptionValue("inputType").toUpperCase();
            if (!InputType.isValueOf(inputTypeValue)) {
                System.out.println("Invalid input type: " + inputTypeValue);
                System.exit(Error.INVALID_INPUT_TYPE.getCode());
            }

            inputType = InputType.valueOf(inputTypeValue);

            // Check that the matching criteria for proteoforms is specified
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
                            System.out.println(Error.INVALID_MATCHING_TYPE.getMessage());
                            System.exit(Error.INVALID_MATCHING_TYPE.getCode());
                        }
                    }
                    break;
            }

            // Check that the argument with the fasta file is comming for peptide inputs
            switch (inputType) {
                case PEPTIDES:
                case PEPTIDE:
                case MODIFIEDPEPTIDE:
                case MODIFIEDPEPTIDES:
                    if (commandLine.hasOption("f")) {
                        File f = new File(commandLine.getOptionValue("f"));
                        if (!f.exists() || f.isDirectory()) {
                            System.out.println(Error.COULD_NOT_READ_FASTA_FILE.getMessage());
                            System.exit(Error.COULD_NOT_READ_FASTA_FILE.getCode());
                        }
                    } else {
                        throw new MissingArgumentException("Missing required option: f");
                    }
            }

            // ******** ******** Finished checking arguments. From here they are consistent ******** ********

            // ******** ******** Read input ******** ********
            List<String> input = new ArrayList<>();
            File file = new File(commandLine.getOptionValue("i"));
            try {
                input = Files.readLines(file, Charset.defaultCharset());
            } catch (IOException e) {
                System.out.println("The input file: " + commandLine.getOptionValue("i") + " was not found.");
                System.exit(Error.COULD_NOT_READ_INPUT_FILE.getCode());
            }

            // ******** ******** Create output files ******** ********
            if (commandLine.hasOption("o")) {
                outputPath = commandLine.getOptionValue("o");
                outputPath = outputPath.endsWith("/") ? outputPath : outputPath + "/";
            }

            try {
                System.out.println("Creating output files.");

                File outputDir = new File(outputPath);
                if(!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                BufferedWriter outputSearch = new BufferedWriter(new FileWriter(outputPath + "search.tsv"));
                BufferedWriter outputAnalysis = new BufferedWriter(new FileWriter(outputPath + "analysis.tsv"));

                // ******** ******** Perform search and analysis ******** ********
                Mapping mapping = new Mapping(inputType, commandLine.hasOption("tlp")); // Load static structures needed for all the cases
                SearchResult searchResult = Search.search(input, inputType, commandLine.hasOption("tlp"), mapping,
                        matchType, margin, commandLine.getOptionValue("f"));

                searchResult.writeToFile(outputSearch, separator);
                System.out.println("Matching results writen to: " + outputPath + "search.csv");
                System.out.println("Starting ORA analysis...");

                int populationSize = getPopulationSize(inputType, mapping.getProteinsToReactions().keySet().size(), mapping.getProteoformsToReactions().keySet().size());
                analysisResult = Analysis.analysis(searchResult, populationSize);
                analysisResult.writeToFile(outputAnalysis, inputType, separator);
                System.out.println("Analysis results writen to: " + outputPath + "analysis.csv");

                outputSearch.close();
                outputAnalysis.close();

                // ******** ******** Write networks ******** ********
                NetworkGenerator.writeNetworks(commandLine.hasOption("g"),
                        commandLine.hasOption("gg"),
                        commandLine.hasOption("gu"),
                        commandLine.hasOption("gp"),
                        inputType,
                        searchResult,
                        mapping,
                        outputPath);

                stopwatch.stop();
                Duration duration = stopwatch.elapsed();
                System.out.println("PathwayMatcher finished (" + duration.toMillis() / 1000 + "s)");

            } catch (IOException e) {
                if (e.getMessage().contains("network") || e.getMessage().contains("directory")) {
                    System.out.println(e.getMessage());
                } else {
                    System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage() + ": " + outputPath + "search.txt  "
                            + System.lineSeparator() + outputPath + "analysis.txt");
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
            if (e.getMessage().startsWith("Missing required option:")) {
                System.exit(Error.MISSING_ARGUMENT.getCode());
            }
            System.exit(Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        }
    }

    private static Options createHelpOptions() {
        Options options = new Options();
        options.addOption(getOption("h", "help", false, "Print usage and available arguments", false));
        options.addOption(getOption("v", "version", false, "Print version of PathwayMatcher", false));
        return options;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(getOption("t", "inputType", true, "Input inputType: gene|ensembl|uniprot|peptide|rsid|proteoform", true));
        options.addOption(getOption("r", "range", true, "Ptm sites margin of error", false));
        options.addOption(getOption("tlp", "toplevelpathways", false, "Show Top Level Pathway columns", false));
        options.addOption(getOption("m", "matchType", true, "Proteoform match criteria: strict|one|superset|subset|one_no_types|superset_no_types|subset_no_types", false));
        options.addOption(getOption("i", "input", true, "Input file", true));
        options.addOption(getOption("o", "output", true, "Output path", false));
        options.addOption(getOption("g", "graph", false, "Create connection graph", false));
        options.addOption(getOption("gu", "graphUniprot", false, "Create protein connection graph", false));
        options.addOption(getOption("gp", "graphProteoform", false, "Create proteoform connection graph", false));
        options.addOption(getOption("gg", "graphGene", false, "Create gene connection graph", false));
        options.addOption(getOption("f", "fasta", true, "Proteins where to find the peptides", false));
        options.addOption(getOption("h", "help", false, "Print usage and available arguments", false));
        options.addOption(getOption("v", "version", false, "Print version of PathwayMatcher", false));
        return options;
    }

    private static Option getOption(String opt, String longOpt, boolean hasArg, String description, boolean required) {
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
}


