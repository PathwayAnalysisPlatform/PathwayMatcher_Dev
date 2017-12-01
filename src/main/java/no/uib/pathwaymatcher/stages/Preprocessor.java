package no.uib.pathwaymatcher.stages;

import com.google.common.io.Files;
import no.uib.pathwaymatcher.Conf.*;
import no.uib.pathwaymatcher.PathwayMatcher;
import no.uib.pathwaymatcher.model.Proteoform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

import static no.uib.pathwaymatcher.Conf.*;

/**
 * Classes of this type receive the user input and convert it to a standarized protein or proteoform.
 *
 * @author Luis Francisco Hernández Sánchez
 */
public abstract class Preprocessor {

    public abstract TreeSet<Proteoform> process(List<String> input) throws java.text.ParseException;

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
            PathwayMatcher.logger.log(Level.SEVERE, "The vepTablesPath provided does not exist.");
            System.exit(1);
        } else {
            for (int chr = 1; chr <= 22; chr++) {
                if (!(new File(strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + "")).exists())) {
                    PathwayMatcher.logger.log(Level.SEVERE, "The vep table for chromosome " + chr + " was not found. Expected: " + strMap.get(StrVars.vepTablesPath) + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                    System.exit(1);
                }
            }
        }
        return true;
    }
}
