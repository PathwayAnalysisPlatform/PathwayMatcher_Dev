package no.uib.pap.pathwaymatcher.tools;

import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.model.Proteoform;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

public class ProteoformFormatConverter {

    /**
     * Converts a list of proteoforms in one format to the other. One proteoform per line. No head lines. Only one format in the file.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        // Read a file

        Path filePath = Paths.get("/home/francisco/Documents/phd/Projects/PathwayMatcher/src/test/resources/Generic", "ReactomeAllProteoformsNeo4j_copy.csv");
        FileWriter outFile = new FileWriter("/home/francisco/Documents/phd/Projects/PathwayMatcher/src/test/resources/Generic/ReactomeAllProteoformsPRO_copy.csv");
        List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());

        ProteoformFormat from = ProteoformFormat.NEO4J;
        ProteoformFormat to = ProteoformFormat.PRO;

        // Convert each proteoform
        for (String line : lines) {
            try {
                no.uib.pap.model.Proteoform proteoform = from.getProteoform(line);
                String str = to.getString(proteoform);
                outFile.write(str + "\n");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        outFile.close();
    }
}
