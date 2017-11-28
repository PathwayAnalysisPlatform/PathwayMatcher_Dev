package no.uib.pathwaymatcher.util;

import java.util.TreeMap;

public class ConstantHolder {
    public static final int REPETITIONS = 10;   //Number of times a specific test is run
    public static final int SAMPLE_SETS = 15;   //Number of random sample sets to be used
    public static final int WARMUP_OFFSET = 1;  // Number of runs for the warm up

    public static TreeMap<String, Long> totals = new TreeMap<>();
    public static TreeMap<String, Long[]> times = new TreeMap<>();
    public static TreeMap<String, Integer> repetitions = new TreeMap<>();

//    public static int SIZES[] = {1, 2};
//    public static int SIZES[] = {1, 2, 5, 10, 20, 50, 100, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000};
        public static int SIZES[] = {1, 20000, 40000, 60000, 80000, 100000, 120000, 140000, 160000, 180000, 200000};
//    public static int SIZES[] = {6000, 12000, 18000};
//    public static int SIZES[] = {600000, 120000, 1800000};
//    public static int snpSizes[] = {150, 200};
    public static int snpSizes[] = {1, 10, 50, 100, 150, 200, 250, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000, 5000000, 10000000};

    /**
     * Input files path
     */
    public static final String PATH = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\Golden\\resources";
    public static final String INPUT_PATH = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\PathwayMatcher\\resources\\input\\";

    public static final String ALL_PEPTIDES = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\PathwayMatcher\\resources\\ProteomeTools\\AllPeptides.csv";
    public static final String ALL_PROTEINS = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\Golden\\resources\\HumanReactomeProteins.txt";
    public static final String ALL_PROTEOFORMS = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\Golden\\resources\\reactomeAllProteoforms.txt";
    public static final String ALL_SNPS = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\PathwayMatcher\\resources\\input\\snpList005.csv";

    /**
     * PathwayMatcher input arguments
     */
    public static String[] args = {"-t", "uniprotList", "-i", INPUT_PATH + "uniprotList.txt", "-u", "neo4j", "-p", "neo4j2"};

    /**
     * Result files with calculated times
     */
    public static final String FILE_TIMES = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\PathwayMatcher\\target\\test-classes\\times.csv";
    public static final String FILE_STDDEV = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\PathwayMatcher\\target\\test-classes\\stdDev.csv";

}
