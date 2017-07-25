/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uib.pathwaymatcher.db;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public interface ReactomeQueries {

    /**
     * Cypher query to check if Reactome contains a Protein using its UniProt
     * Id. Requires a parameter @id when running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example: "P69906" or
     * "P68871"
     */
    String containsUniProtId = "MATCH (re:ReferenceEntity{identifier:{id}})\nWHERE re.databaseName = \"UniProt\"\n"
            + "RETURN re.identifier as protein";

    /**
     * Cypher query to get a list of Ewas associated to a Protein using its
     * UniProt Id. Requires a parameter @id when running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example: "P69906" or
     * "P68871"
     */
    String getEwasByUniprotId = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n"
            + "RETURN re.identifier as protein, ewas.stId as ewas";

    /**
     * Cypher query to get a list of Ewas, with their possible PTMs, associated
     * to a Protein. Requires a parameter @id when running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example: "P69906" or
     * "P68871"
     */
    String getEwasAndPTMsByUniprotId = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}}),\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)\n"
            + "WHERE mr.coordinate IS NOT null\n"
            + "RETURN ewas.stId as ewas, ewas.displayName as name, collect(mr.coordinate) as sites, collect(t.identifier) as mods";

    /**
     * Cypher query to get a list of Ewas, with their possible PTMs, associated
     * to a Protein isoform using its UniProt Id. The isoform id contains a dash
     * ('-') and specifies the version number. Requires a parameter @id when
     * running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example: "Q15303-2",
     * "Q15303-4"
     */
    String getEwasByUniprotIsoform = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:{id}})\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)\n"
            + "WHERE mr.coordinate IS NOT null\n"
            + "RETURN ewas.stId as ewas, ewas.displayName as name, collect(mr.coordinate) as sites, collect(t.identifier) as mods";

    /**
     * Cypher query to get a list of Ewas associated to a Protein isoform using
     * its UniProt Id. The isoform id contains a dash ('-') and specifies the
     * version number. Requires a parameter @id when running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example:"Q15303-2",
     * "Q15303-4"
     */
    String getEwasAndPTMsByUniprotIsoform = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:{id}})\n"
            + "RETURN re.identifier as protein, ewas.stId as ewas";

    /**
     * Cypher query to get a list of Pathways and Reactions that contain an
     * Ewas. Requires a parameter @stId when running the query.
     *
     * @param stId The stable identifier of the Ewas in Reactome. Example:
     * "R-HSA-2230966"
     */
    String getPathwaysByEwas = "MATCH (p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n"
            + "(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{stId:{stId}})\n"
            + "RETURN p.stId AS Pathway, p.displayName AS PathwayDisplayName, rle.stId AS Reaction, rle.displayName as ReactionDisplayName";

    /**
     * Cypher query to get a list of Pathways and Reactions that contain a
     * protein referenced by a UniProtId. Requires a parameter @id when running
     * the query.
     *
     * @param id The UniProt id of the protein to search. Example: "P69905"
     */
    String getPathwaysByUniProtId = "MATCH (p:Pathway)-[:hasEvent]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n"
            + "WHERE re.databaseName = \"UniProt\"\nRETURN DISTINCT p.stId AS pathway, r.stId AS reaction";

    /**
     * Cypher query to get a list of Pathways and Reactions that contain a
     * protein referenced by a UniProtId. Requires a parameter @id when running
     * the query. It shows also two columns witht the name and id of the
     * TopLevelPathways where the reactions and pathways are.
     *
     * @param id The UniProt id of the protein to search. Example: "P69905"
     */
    String getPathwaysByUniProtIdWithTLP = "MATCH (tlp:TopLevelPathway)-[:hasEvent*]->(p:Pathway)-[:hasEvent]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n"
            + "WHERE re.databaseName = \"UniProt\"\n"
            + "RETURN DISTINCT tlp.stId as TopLevelPathwayStId, tlp.displayName as TopLevelPathwayName, p.stId AS pathway, r.stId AS reaction";

    public enum Queries {
        getProteinsByPsiMod {
            public String toString() {
                return "MATCH (re:ReferenceEntity)<-[:referenceEntity]-(ewas:EntityWithAccessionedSequence)-[:hasModifiedResidue]->(mr)-[:psiMod]->(mod)\n"
                        + "WHERE mod.identifier IN {modList} AND ewas.speciesName = \"Homo sapiens\"\n"
                        + "RETURN DISTINCT re.identifier as protein";
            }
        },
        getCountAllPTMs {
            public String toString() {
                return "MATCH path = (re:ReferenceEntity)<-[:referenceEntity]-(ewas:EntityWithAccessionedSequence)-[:hasModifiedResidue]->(mr)-[:psiMod]->(mod) \n"
                        + "WHERE ewas.speciesName = \"Homo sapiens\" RETURN count(DISTINCT re), mod.identifier, mod.name";
            }
        }
    }
}
