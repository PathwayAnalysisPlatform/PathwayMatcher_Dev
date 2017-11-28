package no.uib.pathwaymatcher;

import com.google.common.base.Stopwatch;
import no.uib.pathwaymatcher.util.ConstantHolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static no.uib.pathwaymatcher.util.ConstantHolder.*;
import static no.uib.pathwaymatcher.util.CreateSpeedTestFiles.createFiles;
import static no.uib.pathwaymatcher.util.CreateSpeedTestFiles.getFileAsList;
import static no.uib.pathwaymatcher.util.Statistics.getMean;
import static no.uib.pathwaymatcher.util.Statistics.getStandardDeviation;

/**
 * Class used to run PathwayMatcher repeatedly and get measurements of execution time.
 *
 */
public class PathwayMatcherSpeedTest {

    private static PrintWriter stdDevFile;
    private static PrintWriter timesFile;


    public static void main(String args[]) throws IOException {

        stdDevFile = new PrintWriter(new File(FILE_STDDEV));
        timesFile = new PrintWriter(new File(FILE_TIMES));
        timesFile.write("Type,Sample,Size,ms,Repetition\n");

//        SIZES = new int[]{1, 2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000};
//        runPathwayMatcher(Conf.InputTypeEnum.uniprotList, getFileAsList(ALL_PROTEINS));
//        SIZES = new int[]{1, 2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000};
//        runPathwayMatcher(Conf.InputTypeEnum.uniprotListAndModSites, getFileAsList(ALL_PROTEOFORMS));
//        SIZES = new int[]{1, 20000, 40000, 60000, 80000, 100000, 120000, 140000, 160000, 180000, 200000};
        runPathwayMatcher(Conf.InputTypeEnum.peptideList, getFileAsList(ALL_PEPTIDES));
//        SIZES = new int[]{100000, 600000, 120000, 1800000};
//        runPathwayMatcher(Conf.InputTypeEnum.rsidList, getFileAsList(ALL_SNPS));

//        runPathwayMatcher(Conf.InputType.uniprotList);
//        runPathwayMatcher(Conf.InputType.uniprotListAndSites);
//        runPathwayMatcher(Conf.InputType.uniprotListAndModSites);
//        runPathwayMatcher(Conf.InputType.rsidList);
//        runPathwayMatcher(Conf.InputType.peptideList);
//
//        timesFile.write("Type,Sample,Size,ms,Repetition\n");
//        for (String entry : times.keySet()) {
//            System.out.printf("%s:\t%10.5f\n", entry, calculateAverage(times.get(entry)));
//            timesFile.printf("%s,%10.5f\n", entry, getMean(times.get(entry)));
//        }
//
        timesFile.close();
        stdDevFile.close();
    }

    /**
     * Run PathwayMatcher using the input files available
     *
     * @param inputType
     * @throws IOException
     */

    private static void runPathwayMatcher(String inputType) throws IOException {

        Stopwatch stopwatch = Stopwatch.createUnstarted();

        for (int S = 0; S < SIZES.length; S++) {
            for (int R = 0; R < REPETITIONS + WARMUP_OFFSET; R++) {
                stopwatch.start();
                if (inputType == Conf.InputType.rsidList) {
                    args = new String[]{"-t", inputType, "-i", PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt", "-u", "neo4j", "-p", "neo4j2", "-vep", "resources/vep/"};
                } else if (inputType == Conf.InputType.peptideList) {
                    args = new String[]{"-t", inputType, "-i", PATH + inputType + "_" + String.format("%06d", SIZES[S]) + ".txt", "-u", "neo4j", "-p", "neo4j2", "-f", "resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta"};
                } else {
                    ConstantHolder.args[1] = inputType;
                    ConstantHolder.args[3] = PATH + inputType + "_" + String.format("%05d", SIZES[S]) + ".txt";
                }
                System.out.println("Running: " + ConstantHolder.args[3]);
                PathwayMatcher.main(ConstantHolder.args);
                stopwatch.stop();
                Duration duration = stopwatch.elapsed();
                stopwatch.reset();
                if (R < WARMUP_OFFSET) {
                    continue;
                }

                times.putIfAbsent(ConstantHolder.args[3], new Long[REPETITIONS]);
                times.get(ConstantHolder.args[3])[R - WARMUP_OFFSET] = duration.toNanos();
            }
        }
    }

    /**
     * Run PathwayMatcher using random sample sets created in the moment
     *
     * @param inputType Type of input to send to PathwayMatcher such as: uniprotList, rsidList, peptideList...
     * @throws IOException
     */
    private static void runPathwayMatcher(Conf.InputTypeEnum inputType, List<String> allElements) throws IOException {

        Stopwatch stopwatch = Stopwatch.createUnstarted();

        for (int T = 0; T < SAMPLE_SETS; T++) {

            Collections.shuffle(allElements);           // Shuffle the total master list

            createFiles(inputType, allElements);        //Create input files of all sizes

            for (int S = 0; S < SIZES.length; S++) {
                for (int R = 0; R < REPETITIONS + WARMUP_OFFSET; R++) {

                    switch (inputType) {        //Set up arguments to run PathwayMatcher
                        case rsidList:
                            args = new String[]{"-t", inputType.toString(), "-i", INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt", "-u", "neo4j", "-p", "neo4j2", "-vep", "resources/vep/"};
                            break;
                        case peptideList:
                            args = new String[]{"-t", inputType.toString(), "-i", INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt", "-u", "neo4j", "-p", "neo4j2", "-f", "resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta"};
                            break;
                        case uniprotList:
                        case uniprotListAndModSites:
                            ConstantHolder.args[1] = inputType.toString();
                            ConstantHolder.args[3] = INPUT_PATH + inputType + "_" + String.format("%08d", SIZES[S]) + ".txt";
                            break;
                    }
                    System.out.println("Running: " + ConstantHolder.args[3]);

                    stopwatch.start();
                    PathwayMatcher.main(ConstantHolder.args);
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

    /**
     * Calculate average excluding values away from mean more than 1 stdDev.
     *
     * @param longs
     * @return
     */
    public static double calculateAverage(Long[] longs) {
        double mean = getMean(longs);
        double stdDev = getStandardDeviation(longs, mean);
        double sum = 0.0;
        int cont = 0;
        for (Long value : longs) {
            if (Math.abs(value - mean) <= stdDev) {
                cont++;
                sum += value;
            }
        }
        return sum / cont;
    }
}
