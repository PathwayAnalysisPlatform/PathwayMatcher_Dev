package no.uib.pathwaymatcher.vep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class can be used to convert a VEP output file into a table for mapping
 * to pathways. VEP files must be tab separated.
 *
 * @author Marc Vaudel
 */
public class VepFileConverter {

    /**
     * The separator of the VEP file.
     */
    public final static String separatorVep = "\t";
    /**
     * The separator to use in the output.
     */
    public final static char separatorOutput = ' ';
    /**
     * Encoding.
     */
    public static final String encoding = "UTF-8";

    /**
     * Constructor.
     */
    public VepFileConverter() {

    }

    /**
     * The main method takes a vep file as input and extracts variant mapping.
     * No sanity check is conducted.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        VepFileConverter vepFileConverter = new VepFileConverter();

//        File vepFile = new File(args[0]);
//        File outputFile = new File(args[1]);
        File vepFile = new File("C:\\Projects\\ERC\\vep\\22.gz");
        File outputFile = new File("C:\\Projects\\ERC\\vep\\22_test.gz");

        try {

            vepFileConverter.processFile(vepFile, outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts the VEP export file into a format supported by the
     * PathwayMatcher.
     *
     * @param vepFile the VEP file to process
     * @param outputFile the file where to write the output
     *
     * @throws IOException exception thrown whenever an IO error occurs
     */
    public void processFile(File vepFile, File outputFile) throws IOException {

        // Set up file reader
        InputStream vepFileStream = new FileInputStream(vepFile);
        InputStream vepGzipStream = new GZIPInputStream(vepFileStream);
        Reader decoder = new InputStreamReader(vepGzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            // Set up file writer
            FileOutputStream outputFileStream = new FileOutputStream(outputFile);
            GZIPOutputStream outputGzipStream = new GZIPOutputStream(outputFileStream);
            OutputStreamWriter outputEncoder = new OutputStreamWriter(outputGzipStream, encoding);

            try (BufferedWriter bw = new BufferedWriter(outputEncoder)) {

                bw.write("chr bp id allele gene swissprot trembl nearest");
                bw.newLine();

                // Iterate the VEP file
                String line;
                while ((line = br.readLine()) != null) {

                    // Skip the comments
                    char firstLetter = line.charAt(0);
                    if (firstLetter != '#') {

                        // Extract features
                        String[] lineSplit = line.split(separatorVep);
                        String location = lineSplit[1];
                        String[] chrBp = location.split(":");
                        String chr = importValue(chrBp[0]);
                        String bp = importValue(chrBp[1]);
                        String rsId = importValue(lineSplit[12]);
                        String allele = importValue(lineSplit[2]);
                        String gene = importValue(lineSplit[3]);
                        String swissprot = importValue(lineSplit[23]);
                        String trembl = importValue(lineSplit[24]);
                        String nearest = importValue(lineSplit[29]);

                        // Export
                        int lineLength = chr.length() + bp.length() + rsId.length()
                                + allele.length() + gene.length()
                                + swissprot.length() + trembl.length()
                                + nearest.length() + 7;
                        StringBuilder exportLine = new StringBuilder(lineLength);
                        exportLine.append(chr).append(separatorOutput);
                        exportLine.append(bp).append(separatorOutput);
                        exportLine.append(rsId).append(separatorOutput);
                        exportLine.append(allele).append(separatorOutput);
                        exportLine.append(gene).append(separatorOutput);
                        exportLine.append(swissprot).append(separatorOutput);
                        exportLine.append(trembl).append(separatorOutput);
                        exportLine.append(nearest).append(System.lineSeparator());
                        bw.write(exportLine.toString());
                    }
                }
            }
        }
    }

    /**
     * Imports the value as present in the VEP file.
     *
     * @param value the original value
     *
     * @return the processed value
     */
    private String importValue(String value) {

        if (value.length() == 1 && value.charAt(0) == '-') {
            return "NA";
        }
        char[] valueAsCharArray = value.toCharArray();
        for (int i = 0; i < valueAsCharArray.length; i++) {
            if (valueAsCharArray[i] == ' ') {
                valueAsCharArray[i] = '_';
            }
        }

        return value;
    }

}
