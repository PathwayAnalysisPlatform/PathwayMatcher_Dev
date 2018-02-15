package no.uib.pap.pathwaymatcher.Matching;

import static no.uib.pap.model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Peptite;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Peptite_And_Mod_Sites;
import static no.uib.pap.pathwaymatcher.PathwayMatcher14.hitProteins;
import static no.uib.pap.pathwaymatcher.PathwayMatcher14.input;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import com.google.common.base.CharMatcher;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.PathwayMatcher14;

public class PeptideMatcher {

	public static void mapPeptides() throws java.text.ParseException, IOException {

		// Note: In this function the duplicate protein identifiers are removed by
		// adding the whole input list to a set.
		if (!initializePeptideMapper()) {
			System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
			System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
		}

		int row = 1;
		for (String line : input) {
			line = line.trim();
			row++;
			if (matches_Peptite(line)) {
				// Process line
				for (String protein : getPeptideMapping(line)) {
					hitProteins.add(protein);
				}
			} else {
				if (line.isEmpty())
					sendWarning(EMPTY_ROW, row);
				else
					sendWarning(INVALID_ROW, row);
			}
		}

		PathwayMatcher14.mapProteins();
	}

	public static void mapModifiedPeptides() throws ParseException {

		if (!initializePeptideMapper()) {
			System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
			System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
		}

		int row = 1;

		for (String line : PathwayMatcher14.input) {
			line = line.trim();
			row++;
			if (matches_Peptite_And_Mod_Sites(line)) {
				PathwayMatcher14.hitProteoforms.addAll(getProteoforms(line));
			} else {
				if (line.isEmpty())
					sendWarning(EMPTY_ROW, row);
				else
					sendWarning(INVALID_ROW, row);
			}
		}
	}

	/**
	 * 
	 * @param line
	 *            The modified peptide string
	 * @return
	 * @throws ParseException
	 */
	public static Set<Proteoform> getProteoforms(String line) throws ParseException {
		HashSet<Proteoform> proteoformSet = new HashSet<>();
		StringBuilder peptide = new StringBuilder();
		StringBuilder coordinate = null;
		StringBuilder mod = null;
		TreeMultimap<String, Long> ptms = TreeMultimap.create();

		// Get the peptide
		// Read until end of line or semicolon
		int pos = 0;
		char c = line.charAt(pos);
		while (c != ';') {
			peptide.append(c);
			pos++;
			if (pos == line.length())
				break;
			c = line.charAt(pos);
		}
		pos++;
		if (peptide.length() == 0) {
			no.uib.pap.model.Warning.sendWarning(no.uib.pap.model.Warning.INVALID_ROW, 0);
			throw new ParseException(no.uib.pap.model.Warning.INVALID_ROW.getMessage(),
					no.uib.pap.model.Warning.INVALID_ROW.getCode());
		}

		// Get ptms one by one
		// While there are characters

		while (pos < line.length()) {
			c = line.charAt(pos);
			while (!Character.isDigit(c)) {
				if (pos + 1 < line.length()) {
					c = line.charAt(++pos);
				} else {
					break;
				}
			}
			coordinate = new StringBuilder();
			mod = new StringBuilder();
			// Read a ptm
			while (c != ':') {
				mod.append(c);
				pos++;
				c = line.charAt(pos);
			}
			pos++;
			c = line.charAt(pos);
			while (Character.isDigit(c) || CharMatcher.anyOf("nulNUL").matches(c)) {
				coordinate.append(c);
				pos++;
				if (pos == line.length())
					break;
				c = line.charAt(pos);
			}
			ptms.put(mod.toString(), Proteoform.interpretCoordinateFromStringToLong(coordinate.toString()));
			if (c != ',') {
				break;
			}
			pos++;
		}

		// Get the uniprot accessions
		for (String protein : getPeptideMapping(peptide.toString())) {
			Proteoform proteoform = new Proteoform(protein, ptms);
			proteoformSet.add(proteoform);
		}

		return proteoformSet;
	}

	/**
	 * The sequence factory contains the indexed fasta file and can retrieve
	 * information on the proteins it contains.
	 */
	private static SequenceFactory sequenceFactory = SequenceFactory.getInstance();

	/**
	 * The peptide mapper is an index that can retrieve the proteins in the sequence
	 * factory that contain a given sequence.
	 */
	private static FMIndex peptideMapper;

	/**
	 * A waiting handler displays progress to the user and allows cancelling
	 * processes. By default a CLI implementation, should be replaced for GUI
	 * applications.
	 */
	private static WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();

	/**
	 * The sequence matching preferences contain the different parameters used for
	 * the matching of amino acid sequences.
	 */
	private static SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences
			.getDefaultSequenceMatching();

	/**
	 * Tolerance used to map ambiguous amino acids.
	 */
	private static Double mzTolerance;

	/**
	 * Loads a protein sequence database file in the fasta format into the sequence
	 * factory and peptide mapping index.
	 *
	 * @param fastaFile
	 *            a file containing the protein sequences in the fasta format
	 */
	private static void loadFastaFile(File fastaFile) throws IOException, ClassNotFoundException {
		sequenceFactory.loadFastaFile(fastaFile, waitingHandler);
		peptideMapper = new FMIndex(waitingHandler, true, null, PeptideVariantsPreferences.getNoVariantPreferences());
	}

	public static Boolean initializePeptideMapper() {

		mzTolerance = 0.5;

		try {
			loadFastaFile(new File(PathwayMatcher14.fasta));
		} catch (ClassNotFoundException ex) {
			System.out.println("Fasta file for peptide mapping was not found."); // TODO Send proper error
			System.exit(1);
		} catch (IOException ex) {
			System.out.println("Error while reading fasta file for peptide mapping."); // TODO Send proper error
			System.exit(1);
		}

		// peptideMapper = new FMIndex(waitingHandler, true, new PtmSettings(), new
		// PeptideVariantsPreferences(), mzTolerance);
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

	public static ArrayList<Pair<String, Integer>> getPeptideMappingWithIndex(String peptideSequence) {
		ArrayList<Pair<String, Integer>> uniprotList = new ArrayList<Pair<String, Integer>>();
		ArrayList<PeptideProteinMapping> peptideProteinMappings = new ArrayList<PeptideProteinMapping>();

		peptideProteinMappings = peptideMapper.getProteinMapping(peptideSequence, sequenceMatchingPreferences);
		for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {
			uniprotList.add(
					new MutablePair<>(peptideProteinMapping.getProteinAccession(), peptideProteinMapping.getIndex()));
		}
		return uniprotList;
	}
}
