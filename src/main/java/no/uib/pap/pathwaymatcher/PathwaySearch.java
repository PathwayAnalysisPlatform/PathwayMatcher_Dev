package no.uib.pap.pathwaymatcher;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.TreeMultimap;

import no.uib.pap.model.Pathway;
import no.uib.pap.model.Snp;

/**
 * Retrieves the pathways and reactions that contain the input entitites as
 * participants.
 * 
 * @author Francisco
 *
 */
public class PathwaySearch {

	public static void main(String[] args) {

		HashSet<String> proteinSet = (HashSet<String>)getSerializedObject("../PathwayAnalysisPlatform/Extractor/proteins.gz");
		HashMap<String, String> reactions = (HashMap<String, String>)getSerializedObject("../PathwayAnalysisPlatform/Extractor/reactions.gz");
		HashSet<Pathway> pathways = (HashSet<Pathway>)getSerializedObject("../PathwayAnalysisPlatform/Extractor/pathways.gz");
		TreeMultimap<String, String> mapProteinsToReactions = (TreeMultimap<String, String>)getSerializedObject("../Extractor/PathwayAnalysisPlatform/mapProteinsToReactions.gz");

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
		
		for(Pathway pathway : pathways){
			System.out.println(pathway.toString());
		}
		
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
