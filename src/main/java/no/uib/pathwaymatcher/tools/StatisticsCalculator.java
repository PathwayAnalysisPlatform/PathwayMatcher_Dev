package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.model.Pair;
import no.uib.pathwaymatcher.model.stages.Gatherer;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.model.stages.PreprocessorVariants.getBufferedReader;

public class StatisticsCalculator {

    private static final String REACTOME_PROTEINS = "C:\\Users\\Francisco\\Documents\\phd\\Projects\\Golden\\resources\\HumanReactomeProteins.txt";
    private static Set<String> reactomeProteins = new TreeSet<>();

    public static void main(String args[]) throws IOException {

        // Read all proteins in Reactome
        BufferedReader br = new BufferedReader(new FileReader(REACTOME_PROTEINS));
        String line = "";
        while ((line = br.readLine()) != null) {
            reactomeProteins.add(line);
        }

        getFrequenciesPerSNP_v1();
        getFrequenciesPerSNP_v2();
    }

    public static void getFrequenciesPerSNP_v1() throws IOException {

        int c;
        PrintWriter freqFile = new PrintWriter("resources/output/snpFrequencies.csv");
        PrintWriter rpFreqFile = new PrintWriter("resources/output/snpToReactomeFrequencies.csv");

        // Read all VEP tables
        Set<String> proteinsForSNP = new TreeSet<>();
        Set<String> reactomeProteinsForSNP = new TreeSet<>();
        Set<String> snpMapped = new TreeSet<>();
        Pair<String, String> snp = new Pair<>("", "");
        String prevSnp = "";

        for (int chr = 1; chr <= 22; chr++) {
            try {
                BufferedReader br = getBufferedReader("./resources/vep/XX.gz".replace("XX", chr + ""));
                String vepRow = "";

                System.out.println("Scanning vepTable for chromosome " + chr);

                vepRow = br.readLine();
                while ((vepRow = br.readLine()) != null) {

                    snp = getRsIdAndSwissProt(vepRow);

                    if (snp.getLeft().equals("NA")) {
                        continue;
                    }

                    if (!snp.getLeft().equals(prevSnp) && prevSnp.length() > 0) {     // If found a new snp
                        if (proteinsForSNP.size() > 0) {
                            freqFile.write("SNP;" + snp.getLeft() + ";" + proteinsForSNP.size() + "\n");
                            proteinsForSNP.clear();
                        }
                        if (reactomeProteinsForSNP.size() > 0) {
                            rpFreqFile.write("SNP;" + snp.getLeft() + ";" + reactomeProteinsForSNP.size() + "\n");
                            reactomeProteinsForSNP.clear();
                        }
                    }

                    if (!snp.getRight().equals("NA")) {         // If current row has proteins mapped
                        String[] ids = snp.getRight().split(",");
                        for (String id : ids) {
                            proteinsForSNP.add(id);
                            if (reactomeProteins.contains(id)) {
                                reactomeProteinsForSNP.add(id);
                            }
                        }
                    }
                    prevSnp = snp.getLeft();
                }
                // Add the last SNP to the hit list
                if (proteinsForSNP.size() > 0) {
                    freqFile.write("SNP;" + snp.getLeft() + ";" + proteinsForSNP.size() + "\n");
                }
                if (reactomeProteinsForSNP.size() > 0) {
                    rpFreqFile.write("SNP;" + snp.getLeft() + ";" + reactomeProteinsForSNP.size() + "\n");
                    reactomeProteinsForSNP.clear();
                }

                br.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }

        freqFile.close();
        rpFreqFile.close();



    }

    public static void getFrequenciesPerSNP_v2() throws IOException {
        int c;
        PrintWriter freqFile = new PrintWriter("resources/output/snpFrequencies.csv");
        PrintWriter rpFreqFile = new PrintWriter("resources/output/snpToReactomeFrequencies.csv");

        // Read all VEP tables
        Set<String> proteinsForSNP = new TreeSet<>();
        Set<String> reactomeProteinsForSNP = new TreeSet<>();
        Pair<String, String> snp = new Pair<>("", "");
        String prevSnp = "";

        for (int chr = 1; chr <= 22; chr++) {
            try {
                BufferedReader br = getBufferedReader("./resources/vep/XX.gz".replace("XX", chr + ""));
                String vepRow = "";

                System.out.println("Scanning vepTable for chromosome " + chr);

                vepRow = br.readLine();
                while ((vepRow = br.readLine()) != null) {

                    snp = getRsIdAndSwissProt(vepRow);

                    if (snp.getLeft().equals("NA")) {
                        continue;
                    }

                    if (!snp.getLeft().equals(prevSnp) && prevSnp.length() > 0) {     // If found a new snp
                        if (proteinsForSNP.size() > 0) {
                            freqFile.write("SNP;" + snp.getLeft() + ";" + proteinsForSNP.size() + "\n");
                            proteinsForSNP.clear();
                        }
                        if (reactomeProteinsForSNP.size() > 0) {
                            rpFreqFile.write("SNP;" + snp.getLeft() + ";" + reactomeProteinsForSNP.size() + "\n");
                            reactomeProteinsForSNP.clear();
                        }
                    }

                    if (!snp.getRight().equals("NA")) {         // If current row has proteins mapped
                        String[] ids = snp.getRight().split(",");
                        for (String id : ids) {
                            proteinsForSNP.add(id);
                            if (reactomeProteins.contains(id)) {
                                reactomeProteinsForSNP.add(id);
                            }
                        }
                    }
                    prevSnp = snp.getLeft();
                }
                // Add the last SNP to the hit list
                if (proteinsForSNP.size() > 0) {
                    freqFile.write("SNP;" + snp.getLeft() + ";" + proteinsForSNP.size() + "\n");
                }
                if (reactomeProteinsForSNP.size() > 0) {
                    rpFreqFile.write("SNP;" + snp.getLeft() + ";" + reactomeProteinsForSNP.size() + "\n");
                    reactomeProteinsForSNP.clear();
                }

                br.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }
        freqFile.close();
        rpFreqFile.close();
    }

    public static Pair<String, String> getRsIdAndSwissProt(String line) {
        String[] fields = line.split(" ");
        return new Pair<>(fields[2], fields[5]);
    }
}
