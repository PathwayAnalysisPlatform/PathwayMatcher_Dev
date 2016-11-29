package no.uib.pathwayminer;

/*
 * INPUT: A list of proteins with a specific phosphosite set
 * OUTPUT: A list with the status of every protein,  a list of the pahways containing them

*ewas = EntityWithAccessionedSequence = Reactome Protein
*event = pathways/reactions/TopLevelPathways
*re = ReferenceEntity

Strict mode:
Only hits with the Reactome Proteins that have the isoform variant.
Only hits with the Reactome Proteins that contain at least one of the phosphosites.

Flexible mode:

Protein status:
Case 1: Protein found and no requested phosphosites --> pathway hit 1
Case 2: Protein found and all requested phosphosites are found --> pathwat hit 2                              
Case 3: Protein found and at least one of the requested sites was not found --> pathway hit 1
Case 4: Protein not found --> No search //TODO Pathway hit 5


Pathways hit by:
Case 1: ewas related to a uniprot id.
Case 2: ewas related to a uniprot id and having at least one site of a list.
Case 3: ewas related to a uniprot isoform and having at least one site of a list. //TODO
Case 4: ewas related to a uniprot isoform. //TODO
Case 5: interactors of requested not found ewas //TODO

 */

import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.protein.Header;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import no.uib.pathwayminer.db.Connection;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 * @author Optimus Franck
 * @author Marc Vaudel
 */
public class Analyzer {

    /**
     * The sequence factory contains the indexed fasta file and can retrieve
     * information on the proteins it contains.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The peptide mapper is an index that can retrieve the proteins in the
     * sequence factory that contain a given sequence.
     */
    private FMIndex peptideMapper;
    /**
     * A waiting handler displays progress to the user and allows cancelling
     * processes. By default a CLI implementation, should be replaced for GUI
     * applications.
     */
    private WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();
    /**
     * The sequence matching preferences contain the different parameters used
     * for the matching of amino acid sequences.
     */
    private SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();

    private static Driver driver;

    public static void main(String args[]) throws IOException {
        //Configuration variables:
        Boolean createProteinStatusFile = true;
        Boolean createProteinsNotFoundFile = true;
        Boolean createProteinsWithMissingSitesFile = false;
        Boolean createHitPathwayFile = true;
        int maxNumberOfProteins = 20000;
        TreeSet<String> pathways = new TreeSet<String>();

        //File streams for result files
        FileWriter ProteinsNotFoundStream = new FileWriter("./src/main/resources/csv/ProteinsNotFound.csv");                     // Saves the list of uniprot ids when proteins where not found
        FileWriter ProteinsWithMissingSitesStream = new FileWriter("./src/main/resources/csv/ProteinsWithMissingSites.csv");     // Saves the list of uniprot ids when proteins do not have registered sites
        FileWriter ProteinStatusStream = new FileWriter("./src/main/resources/csv/ProteinStatus.csv");                           // Saves the list of uniptot ids, case, sites expected, reactome Ids, sites found, displayName. When something is missing it is left blank.
        FileWriter hitPathwayStream = new FileWriter("./src/main/resources/csv/HitPathway.csv");                                 // <Uniprot Id, Reactome Id, pathwat with dotted route>, sorted according to the three columns

        driver = GraphDatabase.driver(Connection.host, AuthTokens.basic(Connection.username, Connection.password));

        /**
         * *********************************************************************
         */
        //Read list in my standard format: unprorId,list of sites separated with ','
        //Uniprot id,sites
        int cont = 0;
        try {
            
            BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/csv/listFile.csv"));
            br.readLine();

            for (String line; (line = br.readLine()) != null && cont < maxNumberOfProteins; cont++) {
                String[] parts = line.split(",");
                String[] sites = (parts.length > 1) ? parts[1].split(";") : new String[0];
                HashSet<Integer> sitesList = new HashSet<Integer>();
                List<String> resultPathways = new ArrayList<String>();
                Protein p;

                System.out.println(cont + "\tReading: " + parts[0]);

                if (parts.length <= 0) {
                    continue;
                }
                if (parts[0].contains("_")) {
                    continue;
                }

                for (int I = 0; I < sites.length; I++) {
                    sitesList.add(Integer.valueOf(sites[I]));
                }

                //Get status of the protein
                p = queryForProtein(parts[0], sitesList);

                switch (p.status) {
                    case 1:                                                 //Protein found and no requested phosphosites
                        //Proceed to Pathwat hit 1
                        resultPathways = queryForPathways(p.id);
                        break;
                    case 2:                                                 //Protein found and all requested phosphosites are found
                        //Proceed to Pathway hit 2
                        resultPathways = queryForPathways(p.id, sitesList);
                        break;
                    case 3:                                                 //Protein found and at least one of the requested sites was not found
                        if (createProteinsWithMissingSitesFile) {
                            ProteinsWithMissingSitesStream.write(p.id + "\n");
                        }
                        //Proceed to Pathway hit 1
                        resultPathways = queryForPathways(p.id);
                        break;
                    case 4:                                                 //Protein not found
                        if (createProteinsNotFoundFile) {
                            ProteinsNotFoundStream.write(p.id + "\n");
                        }
                        //No pathway search
                        break;
                }

                if (createProteinStatusFile) {
                    if (p.reactomeProteinList.size() == 0) {
                        ProteinStatusStream.write(no.uib.pathwayminer.StringsFunctions.Join.join(";", p.id, String.valueOf(p.status), "", "", p.requestedSites.toString(), "", "", "") + "\n");
                    } else {
                        for (ReactomeProtein rp : p.reactomeProteinList) {
                            ProteinStatusStream.write(no.uib.pathwayminer.StringsFunctions.Join.join(";", rp.id, String.valueOf(p.status), rp.stId, rp.displayName, p.requestedSites.toString(), rp.sites.toString(), rp.siteNames.toString()) + "\n");
                        }
                    }
                }
                if (createHitPathwayFile) {
                    //Merge the pathways with the rest
                    for (int I = 0; I < resultPathways.size(); I++) {
                        pathways.add(resultPathways.get(I));
                    }
                }

                ProteinStatusStream.flush();

            }   // -- Ends for all proteins/rows

            if (createHitPathwayFile) {
                for (String pathway : pathways) {
                    hitPathwayStream.write(pathway + "\n");
                }
            }

            ProteinsWithMissingSitesStream.close();
            ProteinStatusStream.close();
            ProteinsNotFoundStream.close();
            hitPathwayStream.close();
            driver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pathway hit 1: Return all pathways containing ewas related to a uniprot id.
    private static List<String> queryForPathways(String id) {
        Session session = driver.session();
        String query = "";
        StatementResult queryResult;
        List<String> hitPathways = new ArrayList<String>();

//        if (!id.contains("-")) {
//            query = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{identifier:'{id}'})\n";
//        } else {
//            query = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:'{id}'})\n";
//        }
        query += "//Find all paths from TopLevelPathways to EWAS related to the Uniprot Id\n"
                + "MATCH path =(p:TopLevelPathway{speciesName:'Homo sapiens'})-[he:hasEvent*]->(rle:ReactionLikeEvent)"
                + "-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]"
                + "->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n"
                + "RETURN DISTINCT extract(n IN nodes(path) | n.displayName) as TopToLeaf";

        queryResult = session.run(query, Values.parameters("id", id));

        while (queryResult.hasNext()) {
            hitPathways.add(queryResult.next().get("TopToLeaf").asList().toString());
            //System.out.println(queryResult.next().get("TopToLeaf").asList());
        }

        session.close();
        return hitPathways;
    }

    private static List<String> queryForPathways(String id, HashSet<Integer> sitesList) {
        Session session = driver.session();
        String query = "";
        StatementResult queryResult;
        List<String> hitPathways = new ArrayList<String>();

        query += "//Find all paths from TopLevelPathways to EWAS related to the Uniprot Id and PTMs\n"
                + "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                + "WHERE re.identifier = {id}\n"
                + "WITH ewas, re\n"
                + "MATCH (ewas)-[:hasModifiedResidue]->(mr)\n"
                + "WHERE mr.displayName CONTAINS \"phospho\" AND mr.coordinate IN [419,202]\n"
                + "WITH ewas, mr, re\n"
                + "MATCH path = (e:Event{speciesName:'Homo sapiens'})-[:hasEvent|input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(ewas)\n"
                + "RETURN DISTINCT extract(n IN nodes(path) | n.displayName) as TopToLeaf";

        queryResult = session.run(query, Values.parameters("id", id, "sitesList", sitesList));

        while (queryResult.hasNext()) {
            hitPathways.add(queryResult.next().get("TopToLeaf").asList().toString());
        }

        session.close();
        return hitPathways;
    }

    private static Protein queryForProtein(String id, HashSet<Integer> sites) {
        Protein result = new Protein();
        Session session = driver.session();
        String query = "";
        StatementResult queryResult;

        result.status = 4;
        result.id = id;
        result.requestedSites = sites;

        if (!id.contains("-")) {
            query = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n";
        } else {
            query = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:{id}})\n";
        }

        query += "OPTIONAL MATCH (ewas)-[:hasModifiedResidue]->(mr)\n"
                + "WHERE mr.displayName CONTAINS \"phospho\"\n"
                + "RETURN re.identifier as id, ewas.stId as stId, ewas.displayName as displayName, collect(mr.coordinate) as sites, collect(mr.displayName) as siteNames\n"
                + "ORDER BY re.identifier";

        queryResult = session.run(query, Values.parameters("id", id));
        if (!queryResult.hasNext()) {                                             // Case 4: No protein found
            result.status = 4;
        } else {
            while (queryResult.hasNext()) {
                Record record = queryResult.next();

                ReactomeProtein rp = new ReactomeProtein();
                rp.id = id;
                rp.stId = record.get("stId").asString();
                rp.displayName = record.get("displayName").asString();
                for (Object s : record.get("sites").asList()) {
                    rp.sites.add(Integer.valueOf(s.toString()));
                    result.knownSites.add(Integer.valueOf(s.toString()));
                }
                for (Object n : record.get("siteNames").asList()) {
                    rp.siteNames.add(n.toString());
                }

                result.reactomeProteinList.add(rp);
            }

            if (!result.knownSites.containsAll(sites)) // Case 3: Protein found and at least one of the requested sites was not found
            {
                result.status = 3;
            } else if (result.requestedSites.size() == 0) //Case 1: Protein found and no requested phosphosites 
            {
                result.status = 1;
            } else //Case 2: Protein found and all requested phosphosites are found
            {
                result.status = 2;
            }
        }

        session.close();
        return result;
    }

    /**
     * Loads a protein sequence database file in the fasta format into the
     * sequence factory and peptide mapping index.
     *
     * @param fastaFile a file containing the protein sequences in the fasta
     * format
     */
    private void loadFastaFile(File fastaFile) throws IOException, ClassNotFoundException {
        sequenceFactory.loadFastaFile(fastaFile, waitingHandler);
        peptideMapper = new FMIndex(waitingHandler, true, null, PeptideVariantsPreferences.getNoVariantPreferences());
    }

    /**
     * Example of code for the mapping of peptides to proteins.
     * 
     * @throws IOException exception thrown if an error occurred while reading the fasta file
     * @throws InterruptedException exception thrown if a threading issue occurred while reading the fasta file
     */
    private void example() throws IOException, InterruptedException {

        // Take an example sequence
        String peptideSequence = "TEST";

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
