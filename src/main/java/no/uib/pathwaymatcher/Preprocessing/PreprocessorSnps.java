package no.uib.pathwaymatcher.Preprocessing;

import com.google.common.collect.Multimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Snp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.logger;
import static no.uib.pathwaymatcher.model.Error.*;
import static no.uib.pathwaymatcher.model.Warning.*;
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
        Set<String> rsidSet = new HashSet<>();

        try {
            validateVepTables(strMap.get(Conf.StrVars.vepTablesPath));
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
                rsidSet.add(line);
            } else {
                if (line.isEmpty()) sendWarning(EMPTY_ROW, row);
                else sendWarning(INVALID_ROW, row);
            }

        }

        // Traverse all the vepTables
        for (int chr = 1; chr <= 22; chr++) {
            logger.log(Level.INFO, "Scanning vepTable for chromosome " + chr);
            try {
                BufferedReader br = getBufferedReaderFromResource(strMap.get(Conf.StrVars.vepTablesPath) + strMap.get(Conf.StrVars.vepTableName).replace("XX", chr + ""));
                br.readLine(); //Read header line

                for (String line; (line = br.readLine()) != null; ) {

                    Multimap<Snp, String> snpMap = getRsIdAndSwissProtFromVep(line);

                    for (Map.Entry<Snp, String> pair : snpMap.entries()) {
                        if (rsidSet.contains(pair.getKey().getRsid())) {
                            entities.add(new Proteoform(pair.getValue()));
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
