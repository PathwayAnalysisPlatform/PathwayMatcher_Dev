package no.uib.pathwaymatcher.tools;

import org.neo4j.ogm.model.Result;
import org.reactome.server.analysis.core.util.MapList;
import org.reactome.server.analysis.core.util.Pair;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ExtractorProteoforms {

    private static final String FILE_REACTOME_ALL_PROTEOFORMS = "./resources/reactomeAllProteoforms.txt";
    private static final String FILE_REACTOME_ALL_PROTEIN_IDENTIFIERS = "./resources/HumanReactomeProteins.txt";
    private static final String FILE_UNIPROT_ALL_HUMAN_CURATED = "./resources/Human20218.txt";
    private static Set<String> proteins;
    private static final int NUMBER_REACTOME_PROTEOFORMS = 14824;
    private static final int NUMBER_REACTOME_PROTEINS = 10682;
    private static final int NUMBER_REACTOME_ISOFORMS = 10845;

    private static final String QUERY_GET_PROTEIN_PROTEOFORMS = "MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n" +
            "WITH DISTINCT pe, re\n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm)-[:psiMod]->(mod)\n" +
            "WITH DISTINCT pe.displayName AS physicalEntity,\n" +
            "                re.identifier AS referenceEntity,\n" +
            "                re.variantIdentifier AS variantIdentifier,\n" +
            "                tm.coordinate as coordinate, \n" +
            "                mod.identifier as type ORDER BY type, coordinate\n" +
            "WITH DISTINCT physicalEntity, \n" +
            "\t\t\t\treferenceEntity,\n" +
            "                variantIdentifier,\n" +
            "                COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END + \":\" + type) AS ptms\n" +
            "RETURN DISTINCT referenceEntity, variantIdentifier, ptms";

    /**
     * For each protein (UniProt accession) in Reactome, get the top n proteins with the biggest number of pathway/reaction
     * hits difference between different forms of the protein.
     * The form of the protein is specified by a set of annotated translational modifications.
     */

    public static void Main(String args[]) {
        Map<String, Integer> hitDifferences = new HashMap<String, Integer>();
        //TODO
    }

    /**
     * Gets the sets of reactions that are hit by each form of a protein and gets the maximum nu
     *
     * @param id The UniProt accession for the protein to be checked.
     */
    private static void getReactionHitDifference(String id) {
        //TODO
    }

    /**
     * Get all possible groups of ptms for a specific protein from Reactome. Differentiates proteoforms for each
     * isoform.
     */
    private static MapList<String, MapList<String, Long>> getProteoforms(String uniprotAccession) {
//        uniprotAccession = "P52948";
        System.out.println("Getting proteoforms for: " + uniprotAccession);
        MapList<String, MapList<String, Long>> proteoforms = new MapList<>("isoform", "proteoform");       // List of pairs: Isoform, Proteoform list

        GeneralService genericService = ReactomeGraphCore.getService(GeneralService.class);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("id", uniprotAccession);
        Result result = genericService.query(QUERY_GET_PROTEIN_PROTEOFORMS, parameters);
        for (Map<String, Object> entry : result) {
            MapList<String, Long> ptms = new MapList<>("type", "coordinate");

            String[] ptmList = new String[0];
            if (entry.get("ptms") instanceof String[]) {
                ptmList = (String[]) entry.get("ptms");
                for (String ptm : ptmList) {
                    String[] ptmParts = ptm.split(":");
                    ptms.add(ptmParts[1], (ptmParts[0].equals("null") ? null : Long.valueOf(ptmParts[0])));
                }
            }
            proteoforms.add((entry.get("variantIdentifier") != null) ? entry.get("variantIdentifier").toString() : uniprotAccession, ptms);
        }
        return proteoforms;
    }

    /**
     * Get the reaction hits for each protein form.
     *
     * @return
     */
    private static Map<Set<Pair<Integer, String>>, Set<Set<String>>> getReactionHits() {
        return null;
    }

    /**
     * Get list of ewas that are referenced by the same UniProt accession and set of PTMs.
     */
    private static Set<String> getEwasByPTMs() {
        return null;
    }

    /**
     * Gets a data structure containing all the proteoforms in Reactome. It tries to read from a file at {@link FILE_REACTOME_ALL_PROTEOFORMS}
     * but if it does not exist or is incomplete, then it queries Reactome to create the file and make a full list.
     *
     * @return MapList<String,MapList<String,Long>> It uses MapList to avoid repeating the UniProt Accession and then
     * a nested MapList to avoid repeating the mod types.
     */
    public static MapList<String, MapList<String, Long>> getAllProteoforms() throws IOException {
        MapList<String, MapList<String, Long>> proteoforms = new MapList<>();

        // Read file with all proteoforms in Reactome
        File file = new File(FILE_REACTOME_ALL_PROTEOFORMS);
        if (file.exists() && !file.isDirectory()) {
            // Read file with all the Proteoforms in Reactome
            proteoforms = readAllProteoforms();
        }
        // If it hasn't the right total number of rows
        if (proteoforms.size() != NUMBER_REACTOME_ISOFORMS) {   //It counts the number of proteins, because the proteoform entries are collapsed in a single MapList

            try {
                file.createNewFile();

                //Read all the human proteins
                File fileUniprot = new File(FILE_REACTOME_ALL_PROTEIN_IDENTIFIERS);
                FileInputStream fileInputStreamUniprot = new FileInputStream(fileUniprot);
                BufferedInputStream bfUniprot = new BufferedInputStream(fileInputStreamUniprot);

                FileWriter fwProteoforms = new FileWriter(FILE_REACTOME_ALL_PROTEOFORMS);

                proteins = new TreeSet<>();
                String line;
                while ((line = readNextLine(bfUniprot)).length() > 0) {
                    proteins.add(line);
                }

                // Query Reactome for all the proteoforms for each protein
                ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "neo4j2", ReactomeNeo4jConfig.class);
                for (String protein : proteins) {
                    MapList<String, MapList<String, Long>> oneProteinProteoforms = getProteoforms(protein);
                    for (String isoform : oneProteinProteoforms.keySet()) {
                        for (MapList<String, Long> proteoform : oneProteinProteoforms.getElements(isoform)) {
                            proteoforms.add(isoform, proteoform); // Store the proteoform in memory

                            boolean firstPtm = true;
                            // Write the proteoform to the file
                            fwProteoforms.write(isoform + ";");
                            for (String modType : proteoform.keySet()) {
                                for (Long coordinate : proteoform.getElements(modType)) {
                                    if (!firstPtm) {
                                        fwProteoforms.write(",");
                                    }
                                    fwProteoforms.write(modType.toString() + ":" + (coordinate == null ? null : coordinate.toString()));
                                    firstPtm = false;
                                }
                            }
                            fwProteoforms.write("\n");
                        }
                    }
                }
                fwProteoforms.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            proteoforms = readAllProteoforms();
        }

        return proteoforms;
    }

    /**
     * Reads all the proteoforms in the file {@link FILE_REACTOME_ALL_PROTEOFORMS} and fill the appropiate data structure.
     * It does not check if the list in the file is complete, nor queries Reactome.
     *
     * @return It just returns a data structure filled with the contents of the file.
     */
    private static MapList<String, MapList<String, Long>> readAllProteoforms() throws IOException {
        File file = new File(FILE_REACTOME_ALL_PROTEOFORMS);
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bf = new BufferedInputStream(fileInputStream);
        MapList<String, MapList<String, Long>> proteoforms = new MapList<>("protein", "proteoform");

        String line = "";
        while ((line = readNextLine(bf)).length() > 0) {
            Pair<String, MapList<String, Long>> proteoform = getProteoformCustom(line);
            proteoforms.add(proteoform.getFst(), proteoform.getSnd());      // Inside it calls getOrCreate. If protein exists adds one more element to the values, if not creates value list.
        }
        return proteoforms;
    }

    /**
     * Parse a line with a proteoform in the CUSTOM FORMAT. Ex: P12345-2;246:00916,467:00916,632:00916
     *
     * @param line The line to parse containing the proteoform in PROTEOFORM_CUSTOM.
     * @return
     */
    private static Pair<String, MapList<String, Long>> getProteoformCustom(String line) {

        StringBuilder protein = new StringBuilder();
        StringBuilder coordinate = null;
        StringBuilder mod = null;
        MapList<String, Long> ptms = new MapList<>("type", "coordinate");

        // Get the identifier
        // Read until end of line or semicolon
        int pos = 0;
        char c = line.charAt(pos);
        while (c != ';') {
            protein.append(c);
            pos++;
            if (pos == line.length())
                break;
            c = line.charAt(pos);
        }
        pos++;

        // Get ptms one by one
        //While there are characters

        while (pos < line.length()) {
            mod = new StringBuilder();
            coordinate = new StringBuilder();
            //Read a ptm
            c = line.charAt(pos);
            while (c != ':') {
                mod.append(c);
                pos++;
                c = line.charAt(pos);
            }
            pos++;
            c = line.charAt(pos);
            while (c != ',') {
                coordinate.append(c);
                pos++;
                if (pos == line.length())
                    break;
                c = line.charAt(pos);
            }
            Long numero = null;
            if (!coordinate.toString().equals("null")) {
                numero = Long.valueOf(coordinate.toString());
            }
            ptms.add(mod.toString(), numero);
            pos++;
        }

        return new Pair<>(protein.toString(), ptms);
    }

    private static String readNextLine(BufferedInputStream bf) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = bf.read()) != -1) {
            if (c == '\n') {
                break;
            }
            line.append((char) c);
        }
        return line.toString();
    }
}
