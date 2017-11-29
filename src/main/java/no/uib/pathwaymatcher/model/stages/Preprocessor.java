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
 * Classes of this type receive the user input and convert it to a standarized protein or proteoform.
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
}
