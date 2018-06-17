package no.uib.pap.pathwaymatcher.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

import no.uib.pap.model.ProteoformFormat;

public class ProteoformFormatConverter {

    /**
     * Converts a list of proteoforms in one format to the other. One proteoform per line. No head lines. Only one format in the file.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        // Read a file

        Path filePath = Paths.get("", "export.csv");
        FileWriter outFile = new FileWriter("output/allSimpleProteoforms.tsv");
        List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());

        ProteoformFormat from = ProteoformFormat.NEO4J;
        ProteoformFormat to = ProteoformFormat.SIMPLE;
        boolean skipHeaderLine = true;

        // Convert each proteoform
        for (String line : lines) {
            if(skipHeaderLine){
                skipHeaderLine = false;
                continue;
            }
            try {
                no.uib.pap.model.Proteoform proteoform = from.getProteoform(line);
                String str = to.getString(proteoform);
                outFile.write(str + "\n");
                System.out.println("Converted: " + str);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        outFile.close();
    }
}
