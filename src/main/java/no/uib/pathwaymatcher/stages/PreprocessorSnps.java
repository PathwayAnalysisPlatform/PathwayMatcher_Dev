package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Pair;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Snp;
import no.uib.pathwaymatcher.model.Warning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.ERROR_READING_VEP_TABLES;
import static no.uib.pathwaymatcher.model.Error.VEP_DIRECTORY_NOT_FOUND;
import static no.uib.pathwaymatcher.model.Error.sendError;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Rsid;

public class PreprocessorSnps extends PreprocessorVariants {

    /**
     * Reads a list of gene variants maps them to genes, and then to proteins.
     * The order of the input does not matter, since the program traverses all the Chromosome
     * tables and for each one it asks the input set if it is contained.
     *
     * @param input
     * @return
     * @throws ParseException
     */
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();
        HashSet<Snp> snpSet = new HashSet<>();

        try {
            Preprocessor.validateVepTables(strMap.get(Conf.StrVars.vepTablesPath));
        } catch (FileNotFoundException e) {
            sendError(ERROR_READING_VEP_TABLES);
        } catch (NoSuchFileException e) {
            sendError(VEP_DIRECTORY_NOT_FOUND);
        }

        // Create set of snps
        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;

            if (matches_Rsid(line)) {
                snpSet.add(new Snp(line));
            } else {
                logger.log(Level.WARNING, "Row " + row + " with wrong format", Warning.INVALID_ROW.getCode());
            }

        }

        // Traverse all the vepTables
        for (int chr = 1; chr <= 22; chr++) {
            logger.log(Level.FINE, "Scanning vepTable for chromosome " + chr);
            try {
                BufferedReader br = getBufferedReader(strMap.get(Conf.StrVars.vepTablesPath) + strMap.get(Conf.StrVars.vepTableName).replace("XX", chr + ""));
                br.readLine(); //Read header line

                for (String line; (line = br.readLine()) != null; ) {

                    Pair<String, String> snp = getRsIdAndSwissProtFromVep(line);

                    if (!snp.getRight().equals("NA")) {

                        if (snpSet.contains(snp.getLeft())) {
                            String[] ids = snp.getRight().split(",");
                            for (String id : ids) {
                                entities.add(new Proteoform(id));
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                sendError(ERROR_READING_VEP_TABLES, chr);
            }
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
