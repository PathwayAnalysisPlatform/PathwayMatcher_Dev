package no.uib.pap.pathwaymatcher.Matching;

import static no.uib.pap.model.Error.ERROR_READING_VEP_TABLES;
import static no.uib.pap.model.Error.sendError;
import static no.uib.pap.model.Warning.EMPTY_ROW;
import static no.uib.pap.model.Warning.INVALID_ROW;
import static no.uib.pap.model.Warning.sendWarning;
import static no.uib.pap.model.InputPatterns.matches_ChrBp;
import static no.uib.pap.model.InputPatterns.matches_Rsid;
import static no.uib.pap.model.InputPatterns.matches_Vcf_Record;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.hitProteins;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.imapChrBpToProteins;
import static no.uib.pap.pathwaymatcher.PathwayMatcher.imapRsIdsToProteins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Snp;
import no.uib.pap.pathwaymatcher.PathwayMatcher;

public class VariantMatcher {

	public static void mapRsIds() throws IOException {

		System.out.println("Loading rsids map...");
		imapRsIdsToProteins = (ImmutableSetMultimap<String, String>) PathwayMatcher
				.getSerializedObject("imapRsIdsToProteins.gz");
		System.out.println("Mapping input...");

		int row = 0;
		for (String line : PathwayMatcher.input) {
			line = line.trim();
			row++;
			if (line.isEmpty()) {
				sendWarning(EMPTY_ROW, row);
				continue;
			}

			if (matches_Rsid(line)) {
				for (String protein : imapRsIdsToProteins.get(line)) {
					hitProteins.add(protein);
				}
			} else {
				sendWarning(INVALID_ROW, row);
			}
		}

		PathwayMatcher.mapProteins();
	}

	public static void mapChrBp() throws IOException {

		System.out.println("Loading chr and bp map...");
		imapChrBpToProteins = (ImmutableSetMultimap<String, String>) PathwayMatcher
				.getSerializedObject("imapChrBpToProteins.gz");
		System.out.println("Mapping input...");

		Snp snp = null;
		int row = 1;
		for (String line : PathwayMatcher.input) {
			line = line.trim();
			row++;
			if (line.isEmpty()) {
				sendWarning(EMPTY_ROW, row);
				continue;
			}

			if (matches_ChrBp(line)) {
				snp = getSnpFromChrBp(line);
				for (String protein : imapChrBpToProteins.get(snp.getChr() + "_" + snp.getBp())) {
					hitProteins.add(protein);
				}
			} else {
				sendWarning(INVALID_ROW, row);
			}
		}
		PathwayMatcher.mapProteins();
	}

	public static void mapVCF() throws IOException {

		System.out.println("Loading chr and bp map...");
		imapChrBpToProteins = (ImmutableSetMultimap<String, String>) PathwayMatcher
				.getSerializedObject("imapChrBpToProteins.gz");
		System.out.println("Mapping input...");

		Snp snp = null;
		int row = 0;
		for (String line : PathwayMatcher.input) {
			line = line.trim();
			row++;
			if (line.isEmpty()) {
				sendWarning(EMPTY_ROW, row);
				continue;
			}

			if (line.startsWith("#")) {
				continue;
			}
			if (matches_Vcf_Record(line)) {
				snp = getSnpFromVcf(line);

				for (String protein : imapChrBpToProteins.get(snp.getChr() + "_" + snp.getBp())) {
					hitProteins.add(protein);
				}
			} else {
				sendWarning(INVALID_ROW, row);
				System.out.println(line);
			}
		}
		System.out.println("Hit proteins: ");
		for(String protein : hitProteins) {
			System.out.println(protein);
		}
		PathwayMatcher.mapProteins();
	}

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

	public static void old_mapVariants() throws IOException {
		Set<Snp> snpSet = new HashSet<>();
		TreeMultimap<Snp, String> allSnpToSwissprotMap = TreeMultimap.create();

		// Create set of snps
		int row = 0;
		for (String line : PathwayMatcher.input) {
			line = line.trim();
			row++;
			if (line.isEmpty()) {
				sendWarning(EMPTY_ROW, row);
				continue;
			}

			switch (PathwayMatcher.inputType) {
			case RSIDS:
				if (matches_Rsid(line)) {
					snpSet.add(Snp.getSnp(line));
				} else if (matches_ChrBp(line)) {
					Snp snp = getSnpFromChrBp(line);
					snpSet.add(snp);
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
								PathwayMatcher.hitProteins.add(snpToSwissprotPair.getValue());
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
		PathwayMatcher.mapProteins();
	}

	/*
	 * This method expects the line to be validated already
	 */
	private static Snp getSnpFromChrBp(String line) {
		String[] fields = line.split("\\s");
		Integer chr = Integer.valueOf(fields[0]);
		Long bp = Long.valueOf(fields[1]);

		return new Snp(chr, bp);
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

		String[] rsids = fields[PathwayMatcher.rsidColumnIndex].split(",");
		String[] uniprots = fields[PathwayMatcher.swissprotColumnIndex].split(",");

		for (String rsid : rsids) {
			for (String uniprot : uniprots) {
				Snp snp = new Snp(chr, bp, rsid);
				mapping.put(snp, uniprot);
			}
		}
		return mapping;
	}
}
