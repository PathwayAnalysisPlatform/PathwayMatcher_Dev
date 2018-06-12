package no.uib.pap.pathwaymatcher;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import no.uib.pap.model.InputType;
import no.uib.pap.pathwaymatcher.PathwayMatcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;

/**
 * Class used to run PathwayMatcher repeatedly and get measurements of execution time.
 * <p>
 * The configuration values to run the tests are declared on the variables by default.
 * In case there is an argument file when running, then it will read the values from the file.
 * The file will contain the following rows:
 * REPETITIONS
 * SAMPLE_SETS
 * WARMUP_OFFSET
 * INPUT_PATH
 * ALL_PEPTIDES_PATH
 * ALL_PROTEINS_PATH
 * ALL_PROTEOFORMS_PATH
 * ALL_SNPS_PATH
 * PEPTIDE_SIZES separated by tabs
 * PROTEIN_SIZES separated by tabs
 * PROTEOFORM_SIZES separated by tabs
 * SNPS_SIZES separated by tabs
 */
public class PathwayMatcherSpeedTest {

    /**
     * Output file
     */
    private static PrintWriter timesFile;

    private static int REPETITIONS = 2;   //Number of times a specific test is run
    private static int SAMPLE_SETS = 2;   //Number of random sample sets to be used
    private static int WARMUP_OFFSET = 1;  // Number of runs for the warm up

    public static TreeMap<String, Long> totals = new TreeMap<>();
    public static TreeMap<String, Long[]> times = new TreeMap<>();
    public static TreeMap<String, Integer> repetitions = new TreeMap<>();

    /**
     * Input files path
     */
    static String INPUT_PATH = "resources/input/";
    static String ALL_PEPTIDES = INPUT_PATH + "Peptides/AllPeptides.csv";
    static String ALL_PROTEINS = INPUT_PATH + "Proteins/UniProt/uniprot-all.list";
    static String ALL_PROTEOFORMS = INPUT_PATH + "ReactomeAllProteoformsSimple.csv";
    static String ALL_SNPS = "extra/SampleDatasets/GeneticVariants/MoBa.csv";

    /**
     * Result files with calculated times
     */
    public static final String FILE_TIMES = "times.csv";


    public static void main(String args[]) throws IOException {

        System.out.println(System.getProperty("user.dir"));

        timesFile = new PrintWriter(new File(FILE_TIMES));
        timesFile.write("Type,Sample,Size,ms,Repetition\n");

        ArrayList<Integer> PEPTIDE_SIZES = new ArrayList<>();
        ArrayList<Integer> PROTEIN_SIZES = new ArrayList<>();
        ArrayList<Integer> PROTEOFORM_SIZES = new ArrayList<>();
        ArrayList<Integer> SNPS_SIZES = new ArrayList<>();

        if (args.length == 0) {
            Collections.addAll(PROTEIN_SIZES, new Integer[]{1, 2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000});
            Collections.addAll(PROTEOFORM_SIZES, new Integer[]{1, 2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000});
            Collections.addAll(PEPTIDE_SIZES, new Integer[]{1, 20000, 40000, 60000, 80000, 100000, 120000, 140000, 160000, 180000, 200000});
            Collections.addAll(SNPS_SIZES, new Integer[]{100000, 600000, 120000, 1800000});
        }
        // Read parameters from file if provided
        else if (args.length == 1) {

            Scanner sc = new Scanner(new File(args[0]));

            while (sc.hasNext()) {
                switch (sc.next()) {
                    case "REPETITIONS":
                        REPETITIONS = sc.nextInt();
                        break;
                    case "SAMPLE_SETS":
                        SAMPLE_SETS = sc.nextInt();
                        break;
                    case "WARMUP_OFFSET":
                        WARMUP_OFFSET = sc.nextInt();
                        break;
                    case "ALL_PEPTIDES":
                        ALL_PEPTIDES = sc.next();
                        break;
                    case "ALL_PROTEINS":
                        ALL_PROTEINS = sc.next();
                        break;
                    case "ALL_PROTEOFORMS":
                        ALL_PROTEOFORMS = sc.next();
                        break;
                    case "ALL_SNPS":
                        ALL_SNPS = sc.next();
                        break;
                    case "PEPTIDE_SIZES":
                        while (sc.hasNextInt()) {
                            PEPTIDE_SIZES.add(sc.nextInt());
                        }
                        break;
                    case "PROTEIN_SIZES":
                        while (sc.hasNextInt()) {
                            PROTEIN_SIZES.add(sc.nextInt());
                        }
                        break;
                    case "PROTEOFORM_SIZES":
                        while (sc.hasNextInt()) {
                            PROTEOFORM_SIZES.add(sc.nextInt());
                        }
                        break;
                    case "SNPS_SIZES":
                        while (sc.hasNextInt()) {
                            SNPS_SIZES.add(sc.nextInt());
                        }
                        break;
                }
            }
        }

//        runPathwayMatcher(InputType.UNIPROT, Files.readLines(new File(ALL_PROTEINS), Charset.defaultCharset()), PROTEIN_SIZES);
//        runPathwayMatcher(InputType.PROTEOFORMS, Files.readLines(new File(ALL_PROTEOFORMS), Charset.defaultCharset()), PROTEOFORM_SIZES);
//        runPathwayMatcher(InputType.PEPTIDES, Files.readLines(new File(ALL_PEPTIDES), Charset.defaultCharset()), PEPTIDE_SIZES);
        runPathwayMatcher(InputType.RSIDS, Files.readLines(new File(ALL_SNPS), Charset.defaultCharset()), SNPS_SIZES);

        timesFile.close();
    }

    /**
     * Run PathwayMatcher using random sample sets created in the moment
     *
     * @param inputType Type of input to send to PathwayMatcher such as: uniprotList, rsidList, peptideList...
     * @throws IOException
     */
    private static void runPathwayMatcher(InputType inputType, List<String> allElements, List<Integer> SIZES) throws IOException {

        Stopwatch stopwatch = Stopwatch.createUnstarted();
        String[] args = {"-t", "uniprotList", "-i", INPUT_PATH + "uniprotList.txt", "-tlp", "-o", "output/"};

        for (int T = 0; T < SAMPLE_SETS; T++) {

            Collections.shuffle(allElements);           // Shuffle the total master list

            createFiles(inputType, allElements, SIZES);        //Create input files of all sizes

            for (Integer size : SIZES) {
                for (int R = 0; R < REPETITIONS + WARMUP_OFFSET; R++) {

                    switch (inputType) {        //Set up arguments to run PathwayMatcher
                        case RSIDS:
                            args = new String[]{"-t", inputType.toString(), "-i", INPUT_PATH + inputType + "_" + String.format("%08d", size) + ".txt"};
                            break;
                        case PEPTIDES:
                            args = new String[]{"-t", inputType.toString(), "-i", INPUT_PATH + inputType + "_" + String.format("%08d", size) + ".txt"};
                            break;
                        case UNIPROT:
                        case PROTEOFORMS:
                            args[1] = inputType.toString();
                            args[3] = INPUT_PATH + inputType + "_" + String.format("%08d", size) + ".txt";
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

                    timesFile.write(inputType + "," + T + "," + size + "," + new DecimalFormat("#0.000").format(duration.toNanos() / 1000000.0) + "," + R + "\n");
                    timesFile.flush();
                }
            }

            deleteFiles(inputType, SIZES);
        }
    }

    public static void createFiles(InputType inputType, List<String> allLines, List<Integer> SIZES) throws IOException {

        FileWriter[] files = new FileWriter[SIZES.size()];

        int index = 0;
        for (Integer size : SIZES) {
            files[index] = new FileWriter(INPUT_PATH + inputType + "_" + String.format("%08d", size) + ".txt");
            index++;
        }

        int row = 0;
        for (String line : allLines) {
            row++;
            ListIterator li = SIZES.listIterator(SIZES.size());
            int S = SIZES.size() - 1;
            while (li.hasPrevious()) {
                if (S < 0) {
                    break;
                }
                if ((Integer) li.previous() < row) {
                    continue;
                }
                files[S].write(line + "\n");
                S--;
            }
        }

        for (int F = 0; F < SIZES.size(); F++) {
            files[F].close();
        }
    }

    private static void deleteFiles(InputType inputType, List<Integer> SIZES) {
        for (Integer size : SIZES) {
            File file = new File(INPUT_PATH + inputType + "_" + String.format("%08d", size) + ".txt");
            if (file.exists()) {
                file.delete();
            }
        }
    }
}