package no.uib.pathwaymatcher.vep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
     * Constructor.
     */
    public VepFileConverter() {

    }

    /**
     * Converts the VEP export file into a format supported by the PathwayMatcher.
     * 
     * @param vepFile the VEP file to process
     * @param outputFile the file where to write the output
     * 
     * @throws IOException exception thrown whenever an IO error occurs
     */
    public void processFile(File vepFile, File outputFile) throws IOException {

        // Set up file reader
        BufferedReader br = new BufferedReader(new FileReader(vepFile));

        try {

            // Set up file writer
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write("chr bp id allele gene feature_type Feature consequence cDNA_position cds_position protein_position amino_acids codons existing_variation impact swissprot trembl uniparc pheno");
            bw.newLine();

            try {

                // Iterate the VEP file
                String line;
                while ((line = br.readLine()) != null) {

                    // Skip the comments
                    char firstLetter = line.charAt(0);
                    if (firstLetter != '#') {

                        // Extract features
                        String[] lineSplit = line.split(separatorVep);
                        String rsId = importValue(lineSplit[0]);
                        String location = lineSplit[1];
                        String[] chrBp = location.split(":");
                        String chr = importValue(chrBp[0]);
                        String bp = importValue(chrBp[1]);
                        String allele = importValue(lineSplit[3]);
                        String gene = importValue(lineSplit[4]);
                        String featureType = importValue(lineSplit[6]);
                        String feature = importValue(lineSplit[5]);
                        String consequence = importValue(lineSplit[7]);
                        String cDnaPosition = importValue(lineSplit[8]);
                        String cdsPosition = importValue(lineSplit[9]);
                        String proteinPosition = importValue(lineSplit[10]);
                        String aminoAcids = importValue(lineSplit[11]);
                        String codons = importValue(lineSplit[12]);
                        String existingVariation = importValue(lineSplit[13]);
                        String impact = importValue(lineSplit[14]);
                        String swissprot = importValue(lineSplit[18]);
                        String trembl = importValue(lineSplit[19]);
                        String uniparc = importValue(lineSplit[20]);
                        String pheno = importValue(lineSplit[23]);

                        // Export
                        int lineLength = chr.length() + bp.length() + rsId.length() + allele.length() + gene.length() + featureType.length() + feature.length() + consequence.length()
                                + cDnaPosition.length() + cdsPosition.length() + proteinPosition.length() + aminoAcids.length() + codons.length() + existingVariation.length()
                                + impact.length() + swissprot.length() + trembl.length() + uniparc.length() + pheno.length() + 18;
                        StringBuilder exportLine = new StringBuilder(lineLength);
                        exportLine.append(chr).append(separatorOutput);
                        exportLine.append(bp).append(separatorOutput);
                        exportLine.append(rsId).append(separatorOutput);
                        exportLine.append(allele).append(separatorOutput);
                        exportLine.append(gene).append(separatorOutput);
                        exportLine.append(featureType).append(separatorOutput);
                        exportLine.append(feature).append(separatorOutput);
                        exportLine.append(consequence).append(separatorOutput);
                        exportLine.append(cDnaPosition).append(separatorOutput);
                        exportLine.append(cdsPosition).append(separatorOutput);
                        exportLine.append(proteinPosition).append(separatorOutput);
                        exportLine.append(aminoAcids).append(separatorOutput);
                        exportLine.append(codons).append(separatorOutput);
                        exportLine.append(existingVariation).append(separatorOutput);
                        exportLine.append(impact).append(separatorOutput);
                        exportLine.append(swissprot).append(separatorOutput);
                        exportLine.append(trembl).append(separatorOutput);
                        exportLine.append(uniparc).append(separatorOutput);
                        exportLine.append(pheno).append(System.lineSeparator());
                        bw.write(exportLine.toString());
                    }

                }

            } finally {
                bw.close();
            }

        } finally {

            br.close();

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
        
        return value;
    }

}
