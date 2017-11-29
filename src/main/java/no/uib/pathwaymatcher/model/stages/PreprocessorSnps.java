package no.uib.pathwaymatcher.model.stages;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.Conf.boolMap;
import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.PathwayMatcher.print;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.model.Error.ERROR_READING_VEP_TABLES;
import static no.uib.pathwaymatcher.model.Error.INVALID_ROW;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Rsid;

public class PreprocessorSnps extends PreprocessorVariants{

    /**
     *  Reads a list of gene variants maps them to genes, and then to proteins.
     *
     * @param input
     * @return
     * @throws ParseException
     */
    public Set<String> process(List<String> input) throws ParseException {

        Set<String> entities = new HashSet<>();
        HashSet<String> snpSet;

        Preprocessor.validateVepTables();

        // Remove duplicate entries
        snpSet = new HashSet<>(input);

        int row = 1;
        for (String line : input) {
            line = line.trim();
            row++;
            if (matches_Rsid(line)) {
                if (matches_Rsid(line)) {
                    entities.add(line);
                } else if (boolMap.get(Conf.BoolVars.ignoreMisformatedRows)) {
                    System.out.println("Ignoring invalid row " + row + ": " + line);
                } else {
                    throw new ParseException("Row " + row + " with wrong format", INVALID_ROW.getCode());
                }
            }
        }

/*************************************************************************/
        TreeMap<String, TreeSet<String>> proteinList = new TreeMap<>();    //This map will filter the found proteins to be unique
        TreeSet<String> outputList = new TreeSet<String>();     // This set will filter the mapped pathways to be unique rows


        // Traverse all the vepTables
        for (int chr = 1; chr <= 22; chr++) {
            PathwayMatcher.println("Scanning vepTable for chromosome " + chr);
            try {
                BufferedReader br = getBufferedReader(strMap.get(Conf.StrVars.vepTablesPath) + strMap.get(Conf.StrVars.vepTableName).replace("XX", chr + ""));
                String[] fields = br.readLine().split(" ");
                for (String line; (line = br.readLine()) != null;) {
                    Pair<String, String> snp = getRsIdAndSwissProt(line);
                    if (!snp.getRight().equals("NA")) {
                        if (snpSet.contains(snp.getLeft())) {
                            String[] ids = snp.getRight().split(",");
                            for (String id : ids) {
                                if (!proteinList.containsKey(id)) {
                                    proteinList.put(id, new TreeSet<>());
                                }
                                proteinList.get(id).add(snp.getLeft());
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                PathwayMatcher.println(ERROR_READING_VEP_TABLES.getMessage() + " Chromosome: " + chr);
                System.exit(ERROR_READING_VEP_TABLES.getCode());
            }
        }

        // Search for the pathways of all the unique proteins
        println("Number of proteins mapped: " + proteinList.size());
        try {
            FileWriter proteinsEncodedFile = new FileWriter("./proteinsEncoded.csv");
            for (Map.Entry<String, TreeSet<String>> proteinEntry : proteinList.entrySet()) {
                proteinsEncodedFile.write(proteinEntry.getKey() + "\n");
            }
            proteinsEncodedFile.close();
        } catch (IOException ex) {
            Logger.getLogger(Gatherer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        print("Getting pathways and reactions...\n0% ");
        int cont = 0;
        int percent = 0;
        int total = proteinList.size();
        while (proteinList.size() > 0) {

            Map.Entry<String, TreeSet<String>> proteinEntry = proteinList.pollFirstEntry();
            String uniProtId = proteinEntry.getKey();
            TreeSet<String> rsIdsMapped = proteinEntry.getValue();
            if (Filter.containsUniProt(uniProtId)) {
                List<String> rows = Filter.getFilteredPathways(uniProtId);
                for (String rsIdMapped : rsIdsMapped) {
                    for (String row : rows) {
                        outputList.add(row + "," + rsIdMapped);  //Adds all the mapping to pathways and reactions using the current SwissProt and rsId
                    }
                }
            }

            int newPercent = cont * 100 / total;
            if (percent < newPercent) {
                print(newPercent + "% ");
                if (newPercent % 10 == 0) {
                    println("");
                }
                percent = newPercent;
            }
            cont++;
        }
        println("100% ");

        // Write result to output file: I wait until all the rows are added to the list so that duplicates are eliminated and all are sorted.
        print("Writing result to file...\n0% ");
        percent = 0;
        cont = 0;
        total = outputList.size();
        FileWriter output;
        try {
            output = new FileWriter(strMap.get(Conf.StrVars.output));
            if (boolMap.get(Conf.BoolVars.showTopLevelPathways)) {
                output.write("TopLevelPathwayId,TopLevelPathwayName,");
            }
            output.write("pathway,reaction,protein,rsid\n");
            while (outputList.size() > 0) {
                output.write(outputList.pollFirst() + "\n");
                int newPercent = cont * 100 / total;
                if (percent < newPercent) {
                    print(newPercent + "% ");
                    if (newPercent % 10 == 0) {
                        println("");
                    }
                    percent = newPercent;
                }
                cont++;
            }
            output.close();
        } catch (IOException ex) {
            System.out.println("There was a problem writing to the output file " + strMap.get(Conf.StrVars.output));
            System.exit(1);
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
    public Set<String> process(String input) throws ParseException {
        return process(Arrays.asList(input));
    }
}
