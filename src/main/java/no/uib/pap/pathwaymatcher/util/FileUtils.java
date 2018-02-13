package no.uib.pap.pathwaymatcher.util;

import static no.uib.pap.model.Error.COULD_NOT_READ_INPUT_FILE;
import static no.uib.pap.model.Error.sendError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.common.io.Files;

public class FileUtils {
    public static List<String> getInput(String fileName){
        List<String> input = null;
        try {
            input = Files.readLines(new File(fileName), Charset.defaultCharset());
        } catch (IOException e) {
            sendError(COULD_NOT_READ_INPUT_FILE);
        }
        return input;
    }
    
    
}
