/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compomics.utilities;

import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.protein.Header;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeptideMapping {

    /**
     * The sequence factory contains the indexed fasta file and can retrieve
     * information on the proteins it contains.
     */
    private static SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The peptide mapper is an index that can retrieve the proteins in the
     * sequence factory that contain a given sequence.
     */
    private static FMIndex peptideMapper;
    /**
     * A waiting handler displays progress to the user and allows cancelling
     * processes. By default a CLI implementation, should be replaced for GUI
     * applications.
     */
    private static WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();
    /**
     * The sequence matching preferences contain the different parameters used
     * for the matching of amino acid sequences.
     */
    private static SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();

    /**
     * Loads a protein sequence database file in the fasta format into the
     * sequence factory and peptide mapping index.
     *
     * @param fastaFile a file containing the protein sequences in the fasta
     * format
     */
    private static void loadFastaFile(File fastaFile) throws IOException, ClassNotFoundException {
        sequenceFactory.loadFastaFile(fastaFile, waitingHandler);
        peptideMapper = new FMIndex(waitingHandler, true, null, PeptideVariantsPreferences.getNoVariantPreferences());
    }

    /**
     * Example of code for the mapping of peptides to proteins.
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the fasta file
     * @throws InterruptedException exception thrown if a threading issue
     * occurred while reading the fasta file
     */
    public static void example() throws IOException, InterruptedException {

        System.out.println(System.getProperty("user.dir"));
        try {
            loadFastaFile(new File("./src/main/resources/other/Uniprot_HomoSapiens_20151105_CanonicalANDIsoform_20196Entries.fasta"));
        } catch (ClassNotFoundException ex) {
            System.out.println("Fasta file for peptide mapping was not found.");
            Logger.getLogger(PeptideMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        peptideMapper = new FMIndex(waitingHandler, true, new PtmSettings(), new PeptideVariantsPreferences());
        // Take an example sequence
        String peptideSequence = "AGEGEN";

        // Map a peptide sequence to the protein sequences
        ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(peptideSequence, sequenceMatchingPreferences);

        // Iterate all peptide protein mappings
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {

            // The peptide sequence
            peptideSequence = peptideProteinMapping.getPeptideSequence();

            // The accession of the protein it was mapped to
            String accession = peptideProteinMapping.getProteinAccession();

            // The (zero-based) index of the peptide on the protein sequence
            int index = peptideProteinMapping.getIndex();

            // You can get more information on the protein using the sequence factory
            com.compomics.util.experiment.biology.Protein protein = sequenceFactory.getProtein(accession);

            // For example, you can get the full protein sequence
            String proteinSequence = protein.getSequence();

            // More information can be found in the header of every protein in the fasta file. But be careful, the content of the header is database dependent. So the information will not always be here.
            Header proteinHeader = sequenceFactory.getHeader(accession);

            // Uniprot databases usually contain the gene name in the header. Can be very helpful.
            String geneName = proteinHeader.getGeneName();

            // The species also. But here be careful again, the taxonomy used might not be the same as the one in Reactome.
            String species = proteinHeader.getTaxonomy();
        }
    }
}
