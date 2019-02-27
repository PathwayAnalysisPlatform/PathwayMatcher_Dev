package no.uib.pap.pathwaymatcher;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import no.uib.pap.methods.search.SearchResult;
import no.uib.pap.model.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static no.uib.pap.model.InputType.*;

class NetworkGenerator {

    private static String getFirst(String s1, String s2) {
        return s1;
    }

    private static Set<Role> getFirst(Set<Role> s1, Set<Role> s2) {
        return s1;
    }

    private static String getValidLine(String entity1, String entity2,
                                       String container, String container_id, String role) {
        if (entity1.compareTo(entity2) > 0) {   // Swap the values if not in order
            entity2 = getFirst(entity1, entity1 = entity2);
        }

        return String.join("\t", entity1, entity2, container, container_id, role, role);
    }

    private static List<String> getValidLines(String entity1, String entity2,
                                              String container, String container_id,
                                              Set<Role> roles1, Set<Role> roles2) {
        List<String> validLines = new ArrayList<>();

        if (entity1.compareTo(entity2) > 0) {   // Swap the values if not in order
            entity2 = getFirst(entity1, entity1 = entity2);
            roles2 = getFirst(roles1, roles2 = roles1);
        }

        for (Role first_role : roles1) {
            for (Role second_role : roles2) {
                validLines.add(String.join("\t", entity1, entity2, container, container_id, first_role.toString(), second_role.toString()));
            }
        }

        return validLines;
    }

    private static void writeLine(String line, BufferedWriter output) throws IOException {
        output.write(line);
        output.newLine();
    }

    private static void writeLines(Collection<String> lines, BufferedWriter output) throws IOException {
        for (String line : lines) {
            output.write(line);
            output.newLine();
        }
    }

    /**
     * Decide which network is done and call for respective writer functions.
     *
     * @param doGeneGraph       command line argument requesting the gene network
     * @param doProteinGraph    command line argument requesting the protein network
     * @param doProteoformGraph command line argument requesting the proteoform network
     * @param inputType         type of data: uniprot | proteoform | peptide...
     * @param searchResult      structured filled after search execution
     * @param mapping           static mapping data
     * @param outputPath        directory for output files
     */
    static void writeGraphs(boolean doGeneGraph,
                            boolean doProteinGraph,
                            boolean doProteoformGraph,
                            InputType inputType,
                            SearchResult searchResult,
                            Mapping mapping,
                            String outputPath) throws IOException {
        if (doGeneGraph) {
            try {
                writeGeneGraph(searchResult, mapping, outputPath, inputType);
            } catch (IOException e) {
                throw new IOException("Can't create gene network file.");
            }
        }
        if (doProteinGraph) {
            try {
                writeProteinGraph(searchResult, mapping, outputPath);
            } catch (IOException e) {
                throw new IOException("Can't create protein network file.");
            }
        }
        if (doProteoformGraph) {
            try {
                writeProteoformGraph(searchResult, mapping, outputPath, inputType);
            } catch (IOException e) {
                throw new IOException("Can't create proteoform network file.");
            }
        }
    }

    private static void writeGeneGraph(SearchResult searchResult, Mapping mapping, String outputPath, InputType inputType) throws IOException {
        System.out.println("Creating gene connection graph...");

        TreeMultimap<String, String> addedEdges = TreeMultimap.create();

        //Create output files
        BufferedWriter outputVertices = new BufferedWriter(new FileWriter(outputPath + "geneVertices.tsv"));
        BufferedWriter outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "geneInternalEdges.tsv"));
        BufferedWriter outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "geneExternalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + "\t" + " name" + System.lineSeparator());
        outputInternalEdges.write("id1" + "\t" + "id2" + "\t" + "type" + "\t" + "container_id" + "\t" + "role1" + "\t" + "role2" + System.lineSeparator());
        outputExternalEdges.write("id1" + "\t" + "id2" + "\t" + "type" + "\t" + "container_id" + "\t" + "role1" + "\t" + "role2" + System.lineSeparator());

        // Make sure all static maps are loaded
        // Load static mapping
        mapping.loadMapsForGeneNetwork();

        // Make sure all results are calculated
        // Get the list of hit genes for input cases different to -t gene. The list is generated during the search for the gene input type.
        if (!inputType.equals(GENE) && !inputType.equals(GENES)) {
            searchResult.calculateHitGenes(mapping);
        }

        // Write the vertices file
        for (String gene : searchResult.getHitGenes()) {
            for (String protein : mapping.getGenesToProteins().get(gene)) {
                String line = String.join("\t", gene, mapping.getProteins().get(protein));
                outputVertices.write(line);
                outputVertices.newLine();
            }
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "geneVertices.tsv");

        // Write edges
        for (String protein : searchResult.getHitProteins()) {

            //***** Output Reaction edges ******/
            // For all reactions where the current protein participates
            for (String reaction : mapping.getProteinsToReactions().get(protein)) {

                // For all other proteins in the same reaction
                for (String other_protein : mapping.getReactions().get(reaction).getProteinParticipantsWithRole().keySet()) {
                    if (!other_protein.equals(protein)) {
                        // For all the genes corresponding to each protein
                        for (String gene : mapping.getProteinsToGenes().get(protein)) {
                            for (String other_gene : mapping.getProteinsToGenes().get(other_protein)) {
                                if (!gene.equals(other_gene)) {
                                    List<String> validLines = getValidLines(gene, other_gene,
                                            "Reaction", reaction,
                                            mapping.getReactions().get(reaction).getProteinParticipantsWithRole().get(protein),
                                            mapping.getReactions().get(reaction).getProteinParticipantsWithRole().get(other_protein));
                                    if (searchResult.getInputGenes().contains(other_gene)) {
                                        writeLines(validLines, outputInternalEdges);
                                    } else {
                                        writeLines(validLines, outputExternalEdges);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            addedEdges.clear();

            //***** Output complex edges ******/
            for (String complex : mapping.getProteinsToComplexes().get(protein)) {

                // For each pair of components in this complex
                for (String other_protein : mapping.getComplexesToProteins().get(complex)) {
                    if (!other_protein.equals(protein)) {
                        for (String gene : mapping.getProteinsToGenes().get(protein)) {
                            for (String other_gene : mapping.getProteinsToGenes().get(other_protein)) {
                                if (!gene.equals(other_gene)) {
                                    String validLine = getValidLine(gene, other_gene, "Complex", complex, "component");
                                    if (searchResult.getInputGenes().contains(other_gene)) {
                                        writeLine(validLine, outputInternalEdges);
                                    } else {
                                        writeLine(validLine, outputExternalEdges);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            addedEdges.clear();

            //***** Output set edges ******/
            for (String set : mapping.getProteinsToSets().get(protein)) {

                // For each pair of members of this set
                for (String other_protein : mapping.getSetsToProteins().get(set)) {
                    if (!other_protein.equals(protein)) {

                        for (String gene : mapping.getProteinsToGenes().get(protein)) {
                            for (String other_gene : mapping.getProteinsToGenes().get(other_protein)) {
                                if (!gene.equals(other_gene)) {
                                    String validLine = getValidLine(gene, other_gene, "Set", set, "member/candidate");
                                    if (searchResult.getInputGenes().contains(other_gene)) {
                                        writeLine(validLine, outputInternalEdges);
                                    } else {
                                        writeLine(validLine, outputExternalEdges);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        outputInternalEdges.close();
        outputExternalEdges.close();
        System.out.println("Finished writing edges files: \n" + outputPath + "geneInternalEdges.tsv\n" + outputPath + "geneExternalEdges.tsv");
    }

    private static void writeProteinGraph(SearchResult searchResult, Mapping mapping, String outputPath) throws IOException {

        System.out.println("Creating protein connection graph...");

        TreeMultimap<String, String> addedEdges = TreeMultimap.create();

        //Create output files
        BufferedWriter outputVertices = new BufferedWriter(new FileWriter(outputPath + "proteinVertices.tsv"));
        BufferedWriter outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteinInternalEdges.tsv"));
        BufferedWriter outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteinExternalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + "\t" + " name" + System.lineSeparator());
        outputInternalEdges.write("id1" + "\t" + "id2" + "\t" + "type" + "\t" + "container_id" + "\t" + "role1" + "\t" + "role2" + System.lineSeparator());
        outputExternalEdges.write("id1" + "\t" + "id2" + "\t" + "type" + "\t" + "container_id" + "\t" + "role1" + "\t" + "role2" + System.lineSeparator());

        // Load static mapping
        mapping.loadMapsForProteinNetwork();

        // Write the vertices file
        for (String protein : searchResult.getInputProteins()) {
            String line = String.join("\t", protein, mapping.getProteins().get(protein));
            outputVertices.write(line);
            outputVertices.newLine();
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "proteinVertices.tsv");

        // For each of the proteins in the result
        for (String protein : searchResult.getHitProteins()) {

            //***** Output Reaction edges ******/

            // For each reaction that where the protein participates
            for (String reaction : mapping.getProteinsToReactions().get(protein)) {

                // For all other participant proteins in the reaction
                for (String other_protein : mapping.getReactions().get(reaction).getProteinParticipantsWithRole().keySet()) {

                    if (!protein.equals(other_protein)) {
                        List<String> validLines = getValidLines(protein, other_protein,
                                "Reaction", reaction,
                                mapping.getReactions().get(reaction).getProteinParticipantsWithRole().get(protein),
                                mapping.getReactions().get(reaction).getProteinParticipantsWithRole().get(other_protein));
                        if (searchResult.getInputProteins().contains(other_protein)) {
                            writeLines(validLines, outputInternalEdges);
                        } else {
                            writeLines(validLines, outputExternalEdges);
                        }
                    }
                }
            }
            addedEdges.clear();


            //***** Output complex edges *****/

            // For each complex where the protein is component
            for (String complex : mapping.getProteinsToComplexes().get(protein)) {

                // For each other component of the comples
                for (String other_protein : mapping.getComplexesToProteins().get(complex)) {

                    if (!protein.equals(other_protein)) {
                        String validLine = getValidLine(protein, other_protein, "Complex", complex, "component");
                        if (searchResult.getInputProteins().contains(other_protein)) {
                            writeLine(validLine, outputInternalEdges);
                        } else {
                            writeLine(validLine, outputExternalEdges);
                        }
                    }
                }
            }

            addedEdges.clear();

            //****** Output set edges *****/

            // For each set where this protein is member
            for (String set : mapping.getProteinsToSets().get(protein)) {

                // For each other member of this set
                for (String other_protein : mapping.getSetsToProteins().get(set)) {

                    if (!protein.equals(other_protein)) {
                        String validLine = getValidLine(protein, other_protein, "Set", set, "member/candidate");
                        if (searchResult.getInputProteins().contains(other_protein)) {
                            writeLine(validLine, outputInternalEdges);
                        } else {
                            writeLine(validLine, outputExternalEdges);
                        }
                    }
                }
            }
        }
        outputInternalEdges.close();
        outputExternalEdges.close();
        System.out.println("Finished writing edges files: \n" + outputPath + "proteinInternalEdges.tsv\n" + outputPath + "proteinExternalEdges.tsv");
    }

    private static void writeProteoformGraph(SearchResult searchResult, Mapping mapping, String outputPath, InputType inputType) throws IOException {

        System.out.println("Creating proteoform connection graph...");

        //Create output files
        BufferedWriter outputVertices = new BufferedWriter(new FileWriter(outputPath + "proteoformVertices.tsv"));
        BufferedWriter outputInternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteoformInternalEdges.tsv"));
        BufferedWriter outputExternalEdges = new BufferedWriter(new FileWriter(outputPath + "proteoformExternalEdges.tsv"));

        // Write headers
        outputVertices.write("id" + "\t" + " name" + System.lineSeparator());
        outputInternalEdges.write("id1" + "\t" + "id2" + "\t" + "type" + "\t" + "container_id" + "\t" + "role1" + "\t" + "role2" + System.lineSeparator());
        outputExternalEdges.write("id1" + "\t" + "id2" + "\t" + "type" + "\t" + "container_id" + "\t" + "role1" + "\t" + "role2" + System.lineSeparator());

        mapping.loadMapsForProteoformNetwork();

        // Make sure all results are calculated
        if (!inputType.equals(PROTEOFORM) && !inputType.equals(MODIFIEDPEPTIDE)) {
            searchResult.calculateHitProteoforms(mapping);
        }

        // Write the vertices file
        for (Proteoform proteoform : searchResult.getHitProteoforms()) {
            String line = String.join("\t", proteoform.toString(ProteoformFormat.SIMPLE), mapping.getProteins().get(proteoform.getUniProtAcc()));
            outputVertices.write(line);
            outputVertices.newLine();
        }
        outputVertices.close();
        System.out.println("Finished writing " + outputPath + "proteoformVertices.tsv");

        // Write edges among input proteins
        for (Proteoform proteoform : searchResult.getHitProteoforms()) {

            //***** Output Reaction edges ******/
            // For all reactions where the protein is participant
            for (String reaction : mapping.getProteoformsToReactions().get(proteoform)) {

                //For each other participant proteoforms
                for (Proteoform other_proteoform : mapping.getReactions().get(reaction).getProteoformParticipants().keySet()) {
                    String proteoform_str = proteoform.toString(ProteoformFormat.SIMPLE);
                    String other_proteoform_str = other_proteoform.toString(ProteoformFormat.SIMPLE);

                    if (!proteoform_str.equals(other_proteoform_str)) {
                        List<String> validLines = getValidLines(proteoform_str, other_proteoform_str,
                                "Reaction", reaction,
                                mapping.getReactions().get(reaction).getProteinParticipantsWithRole().get(proteoform_str),
                                mapping.getReactions().get(reaction).getProteinParticipantsWithRole().get(other_proteoform_str));
                        if (searchResult.getInputProteoforms().contains(other_proteoform)) {
                            writeLines(validLines, outputInternalEdges);
                        } else {
                            writeLines(validLines, outputExternalEdges);
                        }
                    }
                }
            }

            //***** Output complex edges ******/
            for (String complex : mapping.getProteoformsToComplexes().get(proteoform)) {

                // For each pair of components in this complex
                for (Proteoform other_proteoform : mapping.getComplexesToProteoforms().get(complex)) {

                    String proteoform_str = proteoform.toString(ProteoformFormat.SIMPLE);
                    String other_proteoform_str = other_proteoform.toString(ProteoformFormat.SIMPLE);

                    if (!proteoform_str.equals(other_proteoform_str)) {
                        String validLine = getValidLine(proteoform_str, other_proteoform_str,
                                "Complex", complex, "component");
                        if (searchResult.getInputProteoforms().contains(other_proteoform)) {
                            writeLine(validLine, outputInternalEdges);
                        } else {
                            writeLine(validLine, outputExternalEdges);
                        }
                    }
                }
            }

            //***** Output set edges ******/
            for (String set : mapping.getProteoformsToSets().get(proteoform)) {

                // For each pair of members of this set
                for (Proteoform other_proteoform : mapping.getSetsToProteoforms().get(set)) {

                    String proteoform_str = proteoform.toString(ProteoformFormat.SIMPLE);
                    String other_proteoform_str = other_proteoform.toString(ProteoformFormat.SIMPLE);

                    if (!proteoform_str.equals(other_proteoform_str)) {
                        String validLine = getValidLine(proteoform_str, other_proteoform_str,
                                "Set", set, "member/candidate");
                        if (searchResult.getInputProteoforms().contains(other_proteoform)) {
                            writeLine(validLine, outputInternalEdges);
                        } else {
                            writeLine(validLine, outputExternalEdges);
                        }
                    }
                }
            }
        }
        outputInternalEdges.close();
        outputExternalEdges.close();
        System.out.println("Finished writing edges files: \n" + outputPath + "proteoformInternalEdges.tsv\n" + outputPath + "proteoformExternalEdges.tsv");
    }
}
