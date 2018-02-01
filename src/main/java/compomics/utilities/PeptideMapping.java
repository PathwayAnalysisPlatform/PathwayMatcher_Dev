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
import no.uib.pathwaymatcher.model.Pair;
import no.uib.pathwaymatcher.Conf.StrVars;
import static no.uib.pathwaymatcher.Conf.strMap;

public class PeptideMapping {
	
	//Again this is a new modification to the file

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
     * Tolerance used to map ambiguous amino acids.
     */
    private static Double mzTolerance = 0.5;

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

    public static Boolean initializePeptideMapper() {

        if(strMap.get(StrVars.fastaFile).equals("*")){
            System.out.println("Fasta file was not provided. Use the command line argument: -f <path_and_file_name>");
            System.exit(1);
        }
        
        //println(System.getProperty("user.dir"));
        try {
            loadFastaFile(new File(strMap.get(StrVars.fastaFile)));
        } catch (ClassNotFoundException ex) {
            System.out.println("Fasta file for peptide mapping was not found.");
            //Logger.getLogger(PeptideMapping.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Error while reading fasta file for peptide mapping.");
            //Logger.getLogger(PeptideMapping.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        //peptideMapper = new FMIndex(waitingHandler, true, new PtmSettings(), new PeptideVariantsPreferences(), mzTolerance);
        peptideMapper = new FMIndex(waitingHandler, true, new PtmSettings(), new PeptideVariantsPreferences());
        return true;
    }

    public static ArrayList<String> getPeptideMapping(String peptideSequence) {
        ArrayList<String> uniprotList = new ArrayList<String>(8);
        ArrayList<PeptideProteinMapping> peptideProteinMappings = new ArrayList<PeptideProteinMapping>();

        peptideProteinMappings = peptideMapper.getProteinMapping(peptideSequence, sequenceMatchingPreferences);
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            uniprotList.add(peptideProteinMapping.getProteinAccession());
        }
        return uniprotList;
    }
    
    public static ArrayList<Pair<String,Integer>> getPeptideMappingWithIndex(String peptideSequence) {
        ArrayList<Pair<String,Integer>> uniprotList = new ArrayList<Pair<String,Integer>>();
        ArrayList<PeptideProteinMapping> peptideProteinMappings = new ArrayList<PeptideProteinMapping>();

        peptideProteinMappings = peptideMapper.getProteinMapping(peptideSequence, sequenceMatchingPreferences);
        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
            uniprotList.add(new Pair<String,Integer>(peptideProteinMapping.getProteinAccession(), peptideProteinMapping.getIndex()));
        }
        return uniprotList;
    }
}
