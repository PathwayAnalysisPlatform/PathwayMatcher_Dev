package no.uib.pap.pathwaymatcher.tools;

import java.io.*;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Luis Francisco Hernández Sánchez <luis.sanchez@uib.no>
 *
 *  This class takes all the zip files corresponding to the "TUM_first_pool" of ProteomeTools and gathers the content of the
 *  each zip file contains a peptide list in a file called "peptides.txt". The program then gathers all the peptides from each peptide file.
 *  The result is an output file with a single column called PTPs_ProteomeTools.csv, with one peptide each line.
 *
 *  ProteomeTools: http://www.proteometools.org/index.php?id=49
 *  Dataset at ProteomeXchange Consortium via the PRIDE repository: http://proteomecentral.proteomexchange.org/cgi/GetDataset?ID=PXD004732
 *
 *  Note: Before running this class you have to adjust the paths indicating the location of the downloaded repository
 *  files and where to store the resulting file.
 */

public class ExtractorPeptides {

    private static final String PATH_OUTPUT = "./resources/ProteomeTools/PTPs_ProteomeTools.csv";
    private static final String PATH_DOWNLOADED_FILES = "./resources/ProteomeTools";

    public static void main(String args[]) {

        FileWriter output = null;
        Set<String> peptideSet = new TreeSet<>();
        try {
            output = new FileWriter(PATH_OUTPUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File directory = new File(PATH_DOWNLOADED_FILES);
        if (args.length > 0) {
            directory = new File(args[0]);
        }

        System.out.println("Reading repository files at: " + directory.getPath());

        //System.out.println("The contents are:");

        for (final File file : directory.listFiles()) {                         // Get each file in the directory
            System.out.println(" --- " + file.getName() + " --- ");
            if (!file.getName().endsWith(".zip")) {
                continue;
            }
            try {
                ZipFile zipFile = new ZipFile(file.getPath());
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {                                 // Read the contents of the zip file
                    ZipEntry entry = entries.nextElement();
                    //System.out.println("-- " + entry.getName());
                    if (entry.getName().equals("peptides.txt")) {
                        InputStream peptideFileStream = zipFile.getInputStream(entry);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(peptideFileStream));

                        reader.readLine(); // Read header line in the first row
                        StringBuilder peptide = new StringBuilder();
                        int num;
                        boolean peptideUpNext = true;
                        while ((num = reader.read()) != -1) {            // Read the contents of the peptide file.
                            char c = (char) num;
                            if (peptideUpNext) {
                                if (c == '\t') {
                                    peptideUpNext = false;
                                } else {
                                    peptide.append(c);
                                }
                            } else {
                                if (c == '\n') {
                                    peptideUpNext = true;
                                    peptideSet.add(peptide.toString());
                                    peptide = new StringBuilder();
                                }
                            }
                        }
                    }
                }
            	zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (String peptide : peptideSet) {
            try {
                output.write(peptide + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
