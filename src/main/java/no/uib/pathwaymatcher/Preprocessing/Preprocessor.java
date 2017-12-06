package no.uib.pathwaymatcher.Preprocessing;

import com.google.common.io.Files;
import no.uib.pathwaymatcher.Conf.StrVars;
import no.uib.pathwaymatcher.model.Proteoform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.TreeSet;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.model.Error.VEP_DIRECTORY_NOT_FOUND;

/**
 * Classes of this type receive the user input and convert it to a standarized protein or proteoform.
 *
 * @author Luis Francisco Hernández Sánchez
 */
public abstract class Preprocessor {

    public abstract TreeSet<Proteoform> process(List<String> input) throws java.text.ParseException;

    public static List<String> readInput(String fileName) throws IOException {
        File file = new File(fileName);
        return Files.readLines(file, Charset.defaultCharset());
    }

    public static Boolean validateVepTables(String path) throws FileNotFoundException, NoSuchFileException {

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        File vepDirectory = new File(path);
        if (!vepDirectory.exists()) {
            throw new NoSuchFileException(VEP_DIRECTORY_NOT_FOUND.getMessage());
        } else {
            for (int chr = 1; chr <= 22; chr++) {
                if (!(new File(path + strMap.get(StrVars.vepTableName).replace("XX", chr + "")).exists())) {
                    throw new FileNotFoundException("The vep table for chromosome " + chr + " was not found. Expected: " + path + strMap.get(StrVars.vepTableName).replace("XX", chr + ""));
                }
            }
        }
        return true;
    }
}
