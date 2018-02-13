package no.uib.pap.pathwaymatcher.Preprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TreeSet;

import com.google.common.io.Files;

import no.uib.pap.model.Proteoform;

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
}
