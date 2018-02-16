package no.uib.pap.pathwaymatcher.Matching;

import static no.uib.pap.model.Error.COULD_NOT_CREATE_SNP_TO_SWISSPROT_FILE;
import static no.uib.pap.model.Error.ERROR_READING_VEP_TABLES;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Rsid;
import static no.uib.pap.pathwaymatcher.Matching.InputPatterns.matches_Vcf_Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.Snp;
import no.uib.pap.pathwaymatcher.PathwayMatcher14;

public class VariantMatcher {

	/**
	 * Reads a list of gene variants maps them to genes, and then to proteins. The
	 * order of the input does not matter, since the program traverses all the
	 * Chromosome tables and for each one it asks the input set if it is contained.
	 *
	 * @param input
	 *            The list of identifiers
	 * @return The set of equivalent proteoforms
	 * @throws IOException
	 * @throws ParseException
	 */

	public static void mapVariants() throws IOException {
		Set<Snp> snpSet = new HashSet<>();
		TreeMultimap<Snp, String> allSnpToSwissprotMap = TreeMultimap.create();

		// Create set of snps
		int row = 1;
		for (String line : PathwayMatcher14.input) {
			line = line.trim();
			row++;
			if (line.isEmpty()) {
				sendWarning(EMPTY_ROW, row);
			}

			switch (PathwayMatcher14.inputType) {
			case RSIDS:
				if (matches_Rsid(line)) {
					snpSet.add(Snp.getSnp(line));
				} else {
					sendWarning(INVALID_ROW, row);
				}
				break;
			case VCF:
				if (!line.startsWith("#")) {
					continue;
				}
				if (matches_Vcf_Record(line)) {
					Snp snp = getSnpFromVcf(line);
					snpSet.add(snp);
				} else {
					sendWarning(INVALID_ROW, row);
				}
				break;
			default:
				break;
			}
		}

		// Traverse all the vepTables
		for (int chr = 1; chr <= 22; chr++) {
			System.out.println("Scanning vepTable for chromosome " + chr);
			try {
				BufferedReader br = getBufferedReaderFromResource(chr + ".gz");
				br.readLine(); // Read header line

				for (String line; (line = br.readLine()) != null;) {

					Multimap<Snp, String> snpToSwissprotMap = getSNPAndSwissProtFromVep(line);

					for (Map.Entry<Snp, String> snpToSwissprotPair : snpToSwissprotMap.entries()) {
						if (snpSet.contains(snpToSwissprotPair.getKey())) {
							if (!snpToSwissprotPair.getValue().equals("NA")) {
								allSnpToSwissprotMap.put(snpToSwissprotPair.getKey(), snpToSwissprotPair.getValue());
								PathwayMatcher14.hitProteins.add(snpToSwissprotPair.getValue());
							}
						} else {
							break;
						}
					}
				}
			} catch (IOException ex) {
				sendError(ERROR_READING_VEP_TABLES, chr);
			}
		}
		PathwayMatcher14.mapProteins();
	}

	public static void mapVCF() throws ParseException, IOException {
		HashSet<Snp> snpSet = new HashSet<>();
		int row = 0;

		// Create set of snps
		for (String line : PathwayMatcher14.input) {
			row++;
			line = line.trim();

		}

		mapProteins();
	}

	/*
	 * This method expects the line to be validated already
	 */
	private static Snp getSnpFromVcf(String line) {
		String[] fields = line.split(" ");
		Integer chr = Integer.valueOf(fields[0]);
		Long bp = Long.valueOf(fields[1]);
		String rsid = fields[2];

		if (rsid.equals(".")) {
			return new Snp(chr, bp);
		} else {
			return new Snp(chr, bp, rsid);
		}
	}

	public BufferedReader getBufferedReaderFromGz(String path) throws FileNotFoundException, IOException {
		BufferedReader br = null;
		if (path.endsWith(".gz")) {
			File file = new File(path);
			InputStream fileStream = new FileInputStream(file);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream);
			br = new BufferedReader(decoder);
		} else {
			return null;
		}
		return br;
	}

	static BufferedReader getBufferedReaderFromResource(String fileName) throws FileNotFoundException, IOException {

		BufferedReader br = null;
		InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream);
		br = new BufferedReader(decoder);

		return br;
	}

	public static Multimap<Snp, String> getSNPAndSwissProtFromVep(String line) {
		TreeMultimap<Snp, String> mapping = TreeMultimap.create();
		String[] fields = line.split(" ");
		Integer chr = Integer.valueOf(fields[0]);
		Long bp = Long.valueOf(fields[1]);

		String[] rsids = fields[PathwayMatcher14.rsidColumnIndex].split(",");
		String[] uniprots = fields[PathwayMatcher14.swissprotColumnIndex].split(",");

		for (String rsid : rsids) {
			for (String uniprot : uniprots) {
				Snp snp = new Snp(chr, bp, rsid);
				mapping.put(snp, uniprot);
			}
		}
		return mapping;
	}
}
