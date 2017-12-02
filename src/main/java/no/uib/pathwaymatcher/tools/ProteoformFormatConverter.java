package no.uib.pathwaymatcher.tools;

import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;

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

        Conf.ProteoformFormat from = Conf.ProteoformFormat.NEO4J;
        Conf.ProteoformFormat to = Conf.ProteoformFormat.PRO;

        Parser parserFrom = ParserFactory.createParser(from);
        Parser parserTo = ParserFactory.createParser(to);

        // Convert each proteoform
        for (String line : lines) {
            try {
                Proteoform proteoform = parserFrom.getProteoform(line);
                String str = parserTo.getString(proteoform);
                outFile.write(str + "\n");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        outFile.close();
    }
}
