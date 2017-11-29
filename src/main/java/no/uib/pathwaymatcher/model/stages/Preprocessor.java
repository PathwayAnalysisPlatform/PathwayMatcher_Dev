package no.uib.pathwaymatcher.model.stages;

import com.google.common.io.Files;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Conf.*;
import no.uib.pathwaymatcher.PathwayMatcher;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.uib.pathwaymatcher.Conf.*;
import static no.uib.pathwaymatcher.PathwayMatcher.println;
import static no.uib.pathwaymatcher.util.InputPatterns.matches_Vcf_Record;

/**
 * Classes of this type receive the user input and convert it to a standarized file with proteoforms.
 *
 * @author Luis Francisco Hernández Sánchez
 */
public abstract class Preprocessor {

    public abstract Set<?> process(List<String> input) throws java.text.ParseException;

    protected FileWriter output;

    public static List<String> readInput(String fileName) {
        File file = new File(fileName);
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readLines(file, Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("Could not find the file: " + fileName);
            System.exit(1);
        }
        return lines;
    }

    /* This Class should transform any type of file to the standard format of representing the Modified Proteins. */
    public static Boolean standarizeFile() {

        Boolean parseResult = false;


        try {
            switch (Conf.InputTypeEnum.valueOf(strMap.get(StrVars.inputType))) {
                case uniprotList:
                    parseResult = parseFormat_uniprotList();
                    break;
                case uniprotListAndModSites:
                    parseResult = parseFormat_uniprotListAndModSites();
                    break;
                case peptideList:
                    parseResult = parseFormat_peptideList();
                    break;
                case peptideListAndModSites:
                    parseResult = parseFormat_peptideListAndModSites();
                    break;
                case rsidList:
                    parseResult = parseFormat_snpList();
                    break;
                case rsid:
                    break;
                case ensemblList:
                    parseResult = parseFormat_ensemblList();
                    break;
                case geneList:
                    parseResult = parseFormat_geneList();
                    break;
                case vcf:
                    parseResult = parseFormat_vcf();
                    break;
            }
        } catch (java.text.ParseException e) {

        } catch (IOException e) {
            System.out.println("There was a problem reading the input file.");
            System.exit(1);
        }
        try {
            output.close();
        } catch (IOException ex) {
            if (Conf.boolMap.get(Conf.BoolVars.verbose)) {
                System.out.println("\nThe output file standarized has a problem.");
            }
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (parseResult) {
            println("\nFile parsed correctly!");
        } else {
            System.out.println("\nThe format of the file is incorrect.");
        }
        return parseResult;
    }

    static Boolean validateVepTables() {
        File vepDirectory = new File(strMap.get(StrVars.vepTablesPath));
        if (!vepDirectory.exists()) {
            PathwayMatcher.println("The vepTablesPath provided does not exist.");
            System.exit(1);
        } else {
            for (int chr = 1; chr <= 22; chr++) {
                if (!(new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + "")).exists())) {
                    PathwayMatcher.println("The vep table for chromosome " + chr + " was not found. Expected: " + strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                    System.exit(1);
                }
            }
        }
        return true;
    }


    private static Boolean parseFormat_vcf() {
        Boolean parsedCorrectly = true;

        try {
            LineIterator it = FileUtils.lineIterator(new File(Conf.strMap.get(Conf.StrVars.input)), "UTF-8");
            String line = "";
            while (it.hasNext()) {
                line = it.nextLine();
                if (!line.startsWith("#")) {
                    break;
                }
            }

            // Read all data record lines from the file
            do {
                try {
                    if (!matches_Vcf_Record(line)) {
                        if (boolMap.get(BoolVars.ignoreMisformatedRows)) {
                            System.out.println("Ignoring invalid row: " + line);
                        } else {
                            throw new ParseException("Row " + line + " with wrong format", 0);
                        }
                    }
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    parsedCorrectly = false;
                    System.exit(0);
                }
                if (it.hasNext()) {
                    line = it.nextLine();
                } else {
                    break;
                }
            } while (true);

            LineIterator.closeQuietly(it);

        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the input file specified.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot read the input file specified.");
            System.exit(1);
        }
        return parsedCorrectly;
    }
}
