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
 *         This class takes all the zip files corresponding to the
 *         "TUM_first_pool" of ProteomeTools and gathers the content of the Each
 *         zip file contains a peptide list in a file called
 *         "TrypticPeptodes.txt". The program then gathers all the peptides from
 *         each peptide file. The result is an output file with a single column
 *         called PTPs_ProteomeTools.csv, with one peptide each line.
 */

public class ProteomeTools_PTPListExtractor {
	public static void main(String args[]) {

		System.out.println(System.getProperty("user.dir"));

		FileWriter output = null;
		Set<String> peptideSet = new TreeSet<>();
		try {
			// output = new FileWriter("./resources/ProteomeTools/Proteotypic.csv");
			// output = new FileWriter("./resources/ProteomeTools/SRMAtlas.csv");
			output = new FileWriter("./resources/ProteomeTools/MissingGene.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}

		File directory = new File("C:/Users/Francisco/Documents/phd/Projects/White-tailed/resources/ProteomeTools");
		if (args.length > 0) {
			directory = new File(args[0]);
		}

		System.out.println("Reading repository files at: " + directory.getPath());

		// System.out.println("The contents are:");

		for (final File file : directory.listFiles()) { // Get each file in the directory
			System.out.println(" --- " + file.getName() + " --- ");
			if (!file.getName().endsWith(".zip")) {
				continue;
			}
			// if(!file.getName().contains("first_pool")){
			// if(!file.getName().contains("Thermo_SRM_")){
			if (!file.getName().contains("TUM_second_")) {
				continue;
			}
			try {
				ZipFile zipFile = new ZipFile(file.getPath());
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) { // Read the contents of the zip file
					ZipEntry entry = entries.nextElement();
					// System.out.println("-- " + entry.getName());
					if (entry.getName().equals("peptides.txt")) {
						InputStream peptideFileStream = zipFile.getInputStream(entry);
						BufferedReader reader = new BufferedReader(new InputStreamReader(peptideFileStream));

						reader.readLine(); // Read header line in the first row
						StringBuilder peptide = new StringBuilder();
						int num;
						boolean peptideUpNext = true;
						while ((num = reader.read()) != -1) { // Read the contents of the paptide file.
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
		for (String peptide : peptideSet) { // Send all non-reduntant peptides to the output
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
