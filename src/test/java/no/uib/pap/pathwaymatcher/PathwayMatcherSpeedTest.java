package no.uib.pap.pathwaymatcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import no.uib.pap.model.InputType;

/**
 * Class used to run PathwayMatcher repeatedly and get measurements of execution time.
 */
public class PathwayMatcherSpeedTest {

    /**
     * Output file
     */
    private static PrintWriter timesFile;

    private static final int REPETITIONS = 2;   //Number of times a specific test is run
    private static final int SAMPLE_SETS = 2;   //Number of random sample sets to be used
    private static final int WARMUP_OFFSET = 1;  // Number of runs for the warm up

    public static TreeMap<String, Long> totals = new TreeMap<>();
    public static TreeMap<String, Long[]> times = new TreeMap<>();
    public static TreeMap<String, Integer> repetitions = new TreeMap<>();

    static int SIZES[] = {1, 2};

    /**
     * Input files path
     */
    static final String INPUT_PATH = "resources/input/";
    static final String ALL_PEPTIDES = INPUT_PATH + "Peptides/AllPeptides.csv";
    static final String ALL_PROTEINS = INPUT_PATH + "Proteins/UniProt/uniprot-all.list";
    static final String ALL_PROTEOFORMS = INPUT_PATH + "ReactomeAllProteoformsSimple.csv";
    static final String ALL_SNPS = INPUT_PATH + "MoBa.csv";

    /**
     * PathwayMatcher input arguments
     */
    public static String[] args = {"-t", "uniprotList", "-i", INPUT_PATH + "uniprotList.txt", "-tlp", "-o", "output/"};

    /**
     * Result files with calculated times
     */
    public static final String FILE_TIMES = "times.csv";


    public static void main(String args[]) throws IOException {

        timesFile = new PrintWriter(new File(FILE_TIMES));
        timesFile.write("Type,Sample,Size,ms,Repetition\n");

        SIZES = new int[]{1, 2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000};
        runPathwayMatcher(InputType.UNIPROT, Files.readLines(new File(ALL_PROTEINS), Charset.defaultCharset()));
        SIZES = new int[]{1, 2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000};
        runPathwayMatcher(InputType.PROTEOFORMS, Files.readLines(new File(ALL_PROTEOFORMS), Charset.defaultCharset()));
        SIZES = new int[]{1, 20000, 40000, 60000, 80000, 100000, 120000, 140000, 160000, 180000, 200000};
        runPathwayMatcher(InputType.PEPTIDES, Files.readLines(new File(ALL_PEPTIDES), Charset.defaultCharset()));
        SIZES = new int[]{100000, 600000, 120000, 1800000};
        runPathwayMatcher(InputType.RSIDS, Files.readLines(new File(ALL_SNPS), Charset.defaultCharset()));

        timesFile.close();
    }

    /**
     * Run PathwayMatcher using random sample sets created in the moment
     *
     * @param inputType Type of input to send to PathwayMatcher such as: uniprotList, rsidList, peptideList...
     * @throws IOException
     */
    private static void runPathwayMatcher(InputType inputType, List<String> allElements) throws IOException {

        Stopwatch stopwatch = Stopwatch.createUnstarted();

        for (int T = 0; T < SAMPLE_SETS; T++) {

            Collections.shuffle(allElements);           // Shuffle the total master list

            createFiles(inputType, allElements);        //Create input files of all sizes

            for (int S = 0; S < SIZES.length; S++) {
                for (int R = 0; R < REPETITIONS + WARMUP_OFFSET; R++) {

                    switch (inputType) {        //Set up arguments to run PathwayMatcher
                        case RSIDS:
                            args = new String[]{"-t", inputType.toString(), "-i", INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt"};
                            break;
                        case PEPTIDES:
                            args = new String[]{"-t", inputType.toString(), "-i", INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt"};
                            break;
                        case UNIPROT:
                        case PROTEOFORMS:
                            args[1] = inputType.toString();
                            args[3] = INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt";
                            break;
                        default:
                            break;
                    }
                    System.out.println("Running: " + args[3]);

                    stopwatch.start();
                    PathwayMatcher.main(args);
                    stopwatch.stop();

                    Duration duration = stopwatch.elapsed();
                    stopwatch.reset();

                    if (R < WARMUP_OFFSET) {
                        continue;
                    }

                    timesFile.write(inputType + "," + T + "," + SIZES[S] + "," + new DecimalFormat("#0.000").format(duration.toNanos() / 1000000.0) + "," + R + "\n");
                    timesFile.flush();
                }
            }
        }
    }

    public static void createFiles(InputType inputType, List<String> allLines) throws IOException {

        FileWriter[] files = new FileWriter[SIZES.length];

        for (int F = 0; F < SIZES.length; F++) {
            files[F] = new FileWriter(INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[F]) + ".txt");
        }

        int row = 0;
        for (String line : allLines) {
            row++;
            for (int S = SIZES.length - 1; S >= 0; S--) {
                if (SIZES[S] < row) {
                    continue;
                }
                files[S].write(line + "\n");
            }
        }

        for (int F = 0; F < SIZES.length; F++) {
            files[F].close();
        }
    }
}
