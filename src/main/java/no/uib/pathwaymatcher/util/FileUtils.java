package no.uib.pathwaymatcher.util;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_READ_INPUT_FILE;

public class FileUtils {
    public static List<String> getInput(String fileName){
        List<String> input = null;
        try {
            input = Files.readLines(new File(fileName), Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("Error reading the input file: " + fileName);
            System.exit(COULD_NOT_READ_INPUT_FILE.getCode());
        }
        return input;
    }
}
