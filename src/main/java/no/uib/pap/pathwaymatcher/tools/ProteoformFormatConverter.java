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
     * @param args The path, source file and result file
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        // Read a file
        String filesPath = "../ProteoformNetworks/resources/reactome/";
        String sourceFile = "all_proteoforms_neo4j.csv";
        String resultFile = "all_proteoforms.csv";

        if(args.length > 0) {
            filesPath = args[0];
            sourceFile = args[1];
            resultFile = args[2];
        }

        Path filePath = Paths.get(filesPath, sourceFile);
        FileWriter outFile = new FileWriter(filesPath + resultFile);
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
