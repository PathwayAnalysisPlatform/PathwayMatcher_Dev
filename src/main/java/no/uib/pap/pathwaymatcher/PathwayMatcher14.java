package no.uib.pap.pathwaymatcher;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Pathway;
import no.uib.pap.model.Proteoform;
import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.model.Snp;

/**
 * Retrieves the pathways and reactions that contain the input entitites as
 * participants.
 * 
 * @author Francisco
 *
 */
public class PathwayMatcher14 {

	private static final String path = "../PathwayAnalysisPlatform/Extractor/";
	
	public static void main(String[] args) throws ParseException {

		TreeMultimap<String, String> mapGenesToProteins = (TreeMultimap<String, String>)getSerializedObject(path + "mapGenesToProteins.gz");
		TreeMultimap<String, String> mapEnsemblToProteins = (TreeMultimap<String, String>)getSerializedObject(path + "mapEnsemblToProteins.gz");
		TreeMultimap<Proteoform, String> mapProteoformsToReactions = (TreeMultimap<Proteoform, String>)getSerializedObject(path + "mapProteoformsToReactions.gz");
		TreeMultimap<String, String> mapProteinsToReactions = (TreeMultimap<String, String>)getSerializedObject(path + "mapProteinsToReactions.gz");
		TreeMultimap<String, String> mapReactionsToPathways = (TreeMultimap<String, String>)getSerializedObject(path + "mapReactionsToPathways.gz");
		TreeMultimap<String, String> mapPathwaysToTopLevelPathways = (TreeMultimap<String, String>)getSerializedObject(path + "mapPathwaysToTopLevelPathways.gz");
		
		
		HashSet<Pathway> pathways = (HashSet<Pathway>)getSerializedObject(path + "pathways.gz");
		HashMap<String, String> reactions = (HashMap<String, String>)getSerializedObject(path + "reactions.gz");

		// Test for mapGenesToProteins
//		for(String geneName : mapGenesToProteins.keySet()) {
//			System.out.println(geneName);
//		}
//		
//		String geneName = "INSR";
//		System.out.print(geneName + ": ");
//		for(String protein : mapGenesToProteins.get(geneName)) {
//			System.out.print(protein + ", ");
//		}
//		System.out.println("");
		
		// Test for mapEnsemblToProteins
//		for(String ensembl : mapEnsemblToProteins.keySet()) {
//			System.out.print(ensembl + ": ");
//			for(String protein : mapEnsemblToProteins.get(ensembl)) {
//				System.out.print(protein + ", ");
//			}
//			System.out.println("");
//		}
//		
//		String ensembl = "ENSP00000370731";
////		String ensembl = "ENST00000250971";
////		String ensembl = "ENST00000397262";
//		System.out.print(ensembl + ": ");
//		for(String protein : mapEnsemblToProteins.get(ensembl)) {
//			System.out.print(protein + ", ");
//		}
//		System.out.println("");
		
		
		// Tests for mapProteoformsToReactions
		
//		boolean hasFound = false;
//		for(Proteoform proteoform : mapProteoformsToReactions.keySet()) {
//			System.out.print(proteoform.toString(ProteoformFormat.NEO4J) + ": ");
//			for(String reaction : mapProteoformsToReactions.get(proteoform)) {
//				System.out.print(reaction + ", ");
//			}
//			System.out.println("");
//			if(proteoform.getUniProtAcc().equals("P01308")) {
//				hasFound = true;
//			}
//			if(hasFound && !proteoform.getUniProtAcc().startsWith("P01308")) {
//				break;
//			}
//		}
//		
//		Proteoform proteoform = ProteoformFormat.NEO4J.getProteoform("\"\"\"P01308\"\"\",\"[\"\"00087:53\"\",\"\"00798:31\"\",\"\"00798:43\"\"]\"");
//		
//		System.out.println("*****\n");
//		for(String reaction : mapProteoformsToReactions.get(proteoform)) {
//			System.out.println("The expected answerr is: R-HSA-977136 passing by the Physical entity R-HSA-429343. \n Result: " + reaction);
//		}
		
//		for (String protein : proteinSet) {
//			System.out.println(protein);
//		}
//		
//		System.out.println("O43918: " + (proteinSet.contains("O43918")?"Yes":"No"));
//		System.out.println("P01308: " + (proteinSet.contains("P01308")?"Yes":"No"));
//
//		for (Entry<String, String> entry : reactions.entrySet()) {
//			System.out.println(entry.getKey() + " + " + entry.getValue());
//		}
//		
//		System.out.println("R-HSA-74707: " + (reactions.containsKey("R-HSA-74707")?"Yes":"No"));
//		System.out.println("R-HSA-422017: " + (reactions.containsKey("R-HSA-422017")?"Yes":"No"));
		
//		for(Pathway pathway : pathways){
//			System.out.println(pathway.toString());
//		}
		
//		for(String reaction : mapProteinsToReactions.get("P01308")) {
//			System.out.println("Reaction: " + reaction);
//		}
			
	}
	
	private static Object getSerializedObject(String fileName) {
		Object obj = null;
		try {

			FileInputStream fin = new FileInputStream(fileName);
			GZIPInputStream gis = new GZIPInputStream(fin);
			ObjectInputStream ois = new ObjectInputStream(gis);
			obj = ois.readObject();
			ois.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return obj;
	}

}
