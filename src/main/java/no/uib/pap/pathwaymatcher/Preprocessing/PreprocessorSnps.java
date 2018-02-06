package no.uib.pap.pathwaymatcher.Preprocessing;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Snp;
import no.uib.pap.pathwaymatcher.Conf;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

import static no.uib.pap.model.Error.*;
import static no.uib.pap.model.Warning.*;
import static no.uib.pap.pathwaymatcher.Conf.strMap;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pap.pathwaymatcher.util.InputPatterns.matches_Rsid;

public class PreprocessorSnps extends PreprocessorVariants {

    /**
     * Reads a list of gene variants maps them to genes, and then to proteins.
     * The order of the input does not matter, since the program traverses all the Chromosome
     * tables and for each one it asks the input set if it is contained.
     *
     * @param input The list of identifiers
     * @return The set of equivalent proteoforms
     * @throws ParseException
     */
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();
        Set<Snp> snpSet = new HashSet<>();
        TreeMultimap<Snp, String> allSnpToSwissprotMap = TreeMultimap.create();
        FileWriter snpToSwissprotFile = null;
        try {
            snpToSwissprotFile = new FileWriter("snpTpSwissprot.txt");
        } catch (IOException e) {
            sendError(COULD_NOT_CREATE_SNP_TO_SWISSPROT_FILE);
        }

        logger.log(Level.INFO, "\nFiltering snps...");

        // Create set of snps
        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;

            if (matches_Rsid(line)) {
                snpSet.add(Snp.getSnp(line));
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }
        }

        // Traverse all the vepTables
        for (int chr = 1; chr <= 22; chr++) {
            logger.log(Level.INFO, "Scanning vepTable for chromosome " + chr);
            try {
                BufferedReader br = getBufferedReaderFromResource(strMap.get(Conf.StrVars.vepTableName).replace("XX", chr + ""));
                br.readLine(); //Read header line

                for (String line; (line = br.readLine()) != null; ) {

                    Multimap<Snp, String> snpToSwissprotMap = getSNPAndSwissProtFromVep(line);

                    for (Map.Entry<Snp, String> snpToSwissprotPair : snpToSwissprotMap.entries()) {
                        if (snpSet.contains(snpToSwissprotPair.getKey())) {
                            if(!snpToSwissprotPair.getValue().equals("NA")){
                                allSnpToSwissprotMap.put(snpToSwissprotPair.getKey(), snpToSwissprotPair.getValue());
                                entities.add(new Proteoform(snpToSwissprotPair.getValue()));
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                sendError(ERROR_READING_VEP_TABLES, chr);
            }
        }

        try {
            snpToSwissprotFile.write("chr\tbp\trs_id\tUniProtAcc\n");
            for (Map.Entry<Snp, String> snpToSwissprotPair : allSnpToSwissprotMap.entries()) {
                snpToSwissprotFile.write(snpToSwissprotPair.getKey().getChr() + "\t"
                        + snpToSwissprotPair.getKey().getBp() + "\t"
                        + snpToSwissprotPair.getKey().getRsid() + "\t"
                        + snpToSwissprotPair.getValue() + "\n");
            }

            snpToSwissprotFile.close();
        } catch (IOException e) {
            sendError(COULD_NOT_CREATE_SNP_TO_SWISSPROT_FILE);
        }
        return entities;
    }

    /**
     * Takes a single gene variant and maps it to genes, then to proteins.
     *
     * @param input
     * @return
     * @throws ParseException
     */
    public TreeSet<Proteoform> process(String input) throws ParseException {
        return process(Arrays.asList(input));
    }
}
