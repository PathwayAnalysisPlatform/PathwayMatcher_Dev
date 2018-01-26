package no.uib.pathwaymatcher.Preprocessing;

import com.google.common.collect.Multimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Snp;

import java.io.*;
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
     * @param input The list of identifiers
     * @return The set of equivalent proteoforms
     * @throws ParseException
     */
    public TreeSet<Proteoform> process(List<String> input) throws ParseException {
        
        //TODO Remove this
        FileWriter tmpFw = null;        // Temporary addition to write the chromosome and base pair of the snps to a file
        FileWriter fWFoundRsIds = null;
        try {
            tmpFw = new FileWriter("chr_Bp.txt");
            fWFoundRsIds = new FileWriter("foundRsIds.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.log(Level.INFO, "\nPreprocessing input file...");
        TreeSet<Proteoform> entities = new TreeSet<>();
        Set<Snp> snpSet = new HashSet<>();
        Set<String> foundRsid = new HashSet<>();

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
                BufferedReader br = getBufferedReaderFromResource(strMap.get(Conf.StrVars.vepTablesPath) + strMap.get(Conf.StrVars.vepTableName).replace("XX", chr + ""));
                br.readLine(); //Read header line

                for (String line; (line = br.readLine()) != null; ) {

                    Multimap<Snp, String> snpMap = getSNPAndSwissProtFromVep(line);

                    for (Map.Entry<Snp, String> pair : snpMap.entries()) {
                        if (snpSet.contains(pair.getKey())) {

                            foundRsid.add(pair.getKey().getRsid());
                            entities.add(new Proteoform(pair.getValue()));
                            
                            // TODO Remove this
                            tmpFw.write(pair.getKey().getChr() + " " +  pair.getKey().getBp() + " " + pair.getKey().getRsid() +"\n");
                        }
                    }
                }
            } catch (IOException ex) {
                sendError(ERROR_READING_VEP_TABLES, chr);
            }
        }

        for(String rsid : foundRsid){
            try {
                fWFoundRsIds.write(rsid + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            tmpFw.close();
            fWFoundRsIds.close();
        } catch (IOException e) {
            e.printStackTrace();
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
