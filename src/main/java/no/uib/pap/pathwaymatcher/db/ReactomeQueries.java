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
package no.uib.pap.pathwaymatcher.db;

/**
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
    public static final String CONTAINSUNIPROTID = "MATCH (re:ReferenceEntity{identifier:{id}})\nWHERE re.databaseName = \"UniProt\"\n"
            + "RETURN re.identifier as protein";

    public static final String getAllProteins = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN DISTINCT re.identifier as Identifiers";

    String getCountAllProteins = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN count(DISTINCT re.identifier) as count";

    String getAllProteinsWithIsoforms = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN DISTINCT (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as Identifiers";

    String getCountAllProteinsWithIsoforms = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN count(DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as count";

    String getCountAllProteoforms = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"}) \nWITH DISTINCT pe, re \nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \nWITH DISTINCT pe, (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type \nWITH DISTINCT pe.stId as ewas, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \nRETURN count(DISTINCT {accession: proteinAccession, ptms: ptms}) as count";

    String getCountAllProteoformsWithSubsequenceRanges = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"}) \n" +
            "WITH DISTINCT pe, re \n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \n" +
            "WITH DISTINCT pe, (CASE WHEN size(re.variantIdentifier) > 0 THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type ORDER BY type, coordinate \n" +
            "WITH DISTINCT pe, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \n" +
            "RETURN count( DISTINCT {proteinAccession: proteinAccession, start:(CASE WHEN pe.startCoordinate IS NOT NULL AND pe.startCoordinate <> -1 THEN pe.startCoordinate ELSE \"null\" END), end: (CASE WHEN pe.endCoordinate IS NOT NULL AND pe.endCoordinate <> -1 THEN pe.endCoordinate ELSE \"null\" END), ptms: ptms}) as count";

    String getAllProteoformsWithSubsequenceRange = "//Get count all proteoforms with subsequence ranges\n" +
            "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"}) \nWITH DISTINCT pe, re \nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \nWITH DISTINCT pe, (CASE WHEN size(re.variantIdentifier) > 0 THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type ORDER BY type, coordinate \nWITH DISTINCT pe, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \nRETURN DISTINCT proteinAccession,\n(CASE WHEN pe.startCoordinate IS NOT NULL AND pe.startCoordinate <> -1 THEN pe.startCoordinate ELSE \"null\" END) as startCoordinate, (CASE WHEN pe.endCoordinate IS NOT NULL AND pe.endCoordinate <> -1 THEN pe.endCoordinate ELSE \"null\" END) as endCoordinate, ptms ORDER BY proteinAccession, startCoordinate, endCoordinate";

    /**
     * Get the UniProt accession of a Protein by using its Ensembl id Id.
     * Requires a parameter @id when running the query.
     *
     * @param id The Ensembl Id of the protein of interest. Example:
     * "ENSG00000186439"
     */
    String getUniprotAccessionByEnsembl = "MATCH (re:ReferenceEntity)\n"
            + "WHERE {id} IN re.otherIdentifier\n"
            + "RETURN re.identifier as uniprotAccession";

    /**
     * Get the UniProt accession with their Ensembl Id for all swissprot human
     * proteins.
     */
    String getAllUniprotAccessionToEnsembl = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
            + "WITH re.identifier as uniprotAccession, FILTER(x IN re.otherIdentifier WHERE x STARTS WITH 'ENS') as genes\n"
            + "WHERE size(genes) > 0  \n"
            + "UNWIND genes as ensemblId\n"
            + "RETURN DISTINCT uniprotAccession, ensemblId";

    /**
     * Get the UniProt accession with their Gene Names for all swissprot human
     * proteins.
     */
    String getAllUniprotAccessionToGeneName = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
            + "WITH re.identifier as uniprotAccession, re.geneName as genes\n"
            + "WHERE size(genes) > 0  \n"
            + "UNWIND genes as gene\n"
            + "RETURN DISTINCT uniprotAccession, gene";

    /**
     * Cypher query to get a list of Ewas associated to a Protein using its UniProt Id.
     *
     * @param id The UniProt Id of the protein of interest. Example: "P69906-1"
     */
    String getEwasByUniprotId = "MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity)\n" +
            "     WHERE pe.speciesName = \"Homo sapiens\" AND re.databaseName = \"UniProt\" AND (re.identifier = {id} OR re.variantIdentifier = {id})\n" +
            "     RETURN (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as protein, count(pe.stId) as count, collect(DISTINCT pe.stId) as ewas\n" +
            "     ORDER BY protein, ewas";

    /**
     * Cypher query to get a list of Ewas, with their possible PTMs, associated
     * to a Protein. Requires a parameter @id when running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example: "P69906" or
     * "P68871"
     */
    String getEwasAndPTMsByUniprotId = "MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity) \nWHERE pe.speciesName = \"Homo sapiens\" AND re.databaseName = \"UniProt\" \nWITH DISTINCT pe, re OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \nWITH DISTINCT pe, (CASE WHEN size(re.variantIdentifier) > 0 THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type ORDER BY type, coordinate \nWHERE proteinAccession = {id}\nWITH DISTINCT pe, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \nRETURN DISTINCT proteinAccession, \n\t\t\t\tpe.stId as ewas,\n\t\t\t\t(CASE WHEN pe.startCoordinate IS NOT NULL AND pe.startCoordinate <> -1 THEN pe.startCoordinate ELSE \"null\" END) as startCoordinate, \n                (CASE WHEN pe.endCoordinate IS NOT NULL AND pe.endCoordinate <> -1 THEN pe.endCoordinate ELSE \"null\" END) as endCoordinate, \n                ptms ORDER BY proteinAccession, startCoordinate, endCoordinate";

    /**
     * Cypher query to get a list of Ewas, with their possible PTMs, associated
     * to a Protein isoform using its UniProt Id. The isoform id contains a dash
     * ('-') and specifies the version number. Requires a parameter @id when
     * running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example: "Q15303-2",
     * "Q15303-4"
     */
    String getEwasByUniprotIsoform = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:{id}})\n"
            + "RETURN re.identifier as protein, ewas.stId as ewas";

    /**
     * Cypher query to get a list of Ewas associated to a Protein isoform using
     * its UniProt Id. The isoform id contains a dash ('-') and specifies the
     * version number. Requires a parameter @id when running the query.
     *
     * @param id The UniProt Id of the protein of interest. Example:"Q15303-2",
     * "Q15303-4"
     */
    String getEwasAndPTMsByUniprotIsoform = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceIsoform{variantIdentifier:{id}})\n"
            + "WITH ewas, re\n"
            + "OPTIONAL MATCH (ewas)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)\n"
            + "RETURN ewas.stId as ewas, ewas.displayName as name, collect(t.identifier) as ptms, collect({mod: t.identifier, site: mr.coordinate}) as ptmList";

    /**
     * Cypher query to get a list of Pathways and Reactions that contain an
     * Ewas. Requires a parameter @stId when running the query.
     *
     * @param stId The stable identifier of the Ewas in Reactome. Example:
     * "R-HSA-2230966"
     */
    String getPathwaysByEwas = "MATCH (tlp:TopLevelPathway)-[:hasEvent*]->(p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n" +
            "(rle)-[:input|output|catalystActivity|disease|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)\n" +
            "WHERE tlp.speciesName = \"Homo sapiens\" AND p.speciesName = \"Homo sapiens\" AND rle.speciesName = \"Homo sapiens\"\n" +
            "AND (pe.stId = {stId})\n" +
            "RETURN DISTINCT p.stId AS Pathway, p.displayName AS PathwayDisplayName, rle.stId AS Reaction, rle.displayName as ReactionDisplayName";

    /**
     * Cypher query to get a list of TopLevelPathways, Pathways and Reactions
     * that contain an Ewas. Requires a parameter @stId when running the query.
     *
     * @param stId The stable identifier of the Ewas in Reactome. Example:
     * "R-HSA-2230966"
     */
    String getPathwaysByEwasWithTLP = "MATCH (tlp:TopLevelPathway)-[:hasEvent*]->(p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n(rle)-[:input|output|catalystActivity|disease|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)\nWHERE tlp.speciesName = \"Homo sapiens\" AND p.speciesName = \"Homo sapiens\" AND rle.speciesName = \"Homo sapiens\"\nAND pe.stId = {stId}\nRETURN DISTINCT tlp.stId as TopLevelPathwayStId, tlp.displayName as TopLevelPathwayDisplayName, p.stId AS Pathway, p.displayName AS PathwayDisplayName, rle.stId AS Reaction, rle.displayName as ReactionDisplayName";

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

    String getAllPathwaysAndReactionsByPTMSet = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity),\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)\n"
            + "WHERE ewas.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt' \n"
            + "WITH DISTINCT re, ewas, mr ORDER BY mr.coordinate\n"
            + "WITH  DISTINCT re, ewas, collect(mr.coordinate) as ptmSet\n"
            + "MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction), \n"
            + "(r)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(ewas)\n"
            + "WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'\n"
            + "WITH DISTINCT re, collect(ewas.stId) as equivalentEwas, ptmSet, collect(DISTINCT p.stId) as pathwaySet, collect(DISTINCT r.stId) as reactionSet\n"
            + "RETURN DISTINCT re.identifier as protein, ptmSet, equivalentEwas, pathwaySet, reactionSet \n"
            + "ORDER BY protein, ptmSet";

    String getPathwayAndReactionCountsByPTMSet = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity),\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)\n"
            + "WHERE ewas.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt' \n"
            + "WITH DISTINCT re, ewas, mr ORDER BY mr.coordinate\n"
            + "WITH  DISTINCT re, ewas, collect(mr.coordinate) as ptmSet\n"
            + "MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction), \n"
            + "(r)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(ewas)\n"
            + "WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'\n"
            + "WITH DISTINCT re, collect(ewas.stId) as equivalentEwas, ptmSet, collect(DISTINCT p.stId) as pathwaySet, collect(DISTINCT r.stId) as reactionSet\n"
            + "RETURN DISTINCT re.identifier as protein, ptmSet, equivalentEwas, size(pathwaySet) as pathwayCount, size(reactionSet) as reactionCount\n"
            + "ORDER BY protein, ptmSet";

    String getPathwayAndReactionStatsByPTMSet = "MATCH (ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity),\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)\n"
            + "WHERE ewas.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt' \n"
            + "WITH DISTINCT re, ewas, mr ORDER BY mr.coordinate\n"
            + "WITH  DISTINCT re, ewas, collect(mr.coordinate) as ptmSet\n"
            + "MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction), \n"
            + "(r)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(ewas)\n"
            + "WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'\n"
            + "WITH DISTINCT re, collect(ewas.stId) as equivalentEwas, ptmSet, collect(DISTINCT p.stId) as pathwaySet, collect(DISTINCT r.stId) as reactionSet\n"
            + "WITH DISTINCT re.identifier as protein, ptmSet, equivalentEwas, size(pathwaySet) as pathwayCount, size(reactionSet) as reactionCount\n"
            + "RETURN min(pathwayCount) as minPathwayCount, avg(pathwayCount) as avgPathwayCount, max(pathwayCount) as maxPathwayCount, min(reactionCount) as minReactionCount, avg(reactionCount) as avgReactionCount, max(reactionCount) as maxReactionCount";

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
        },
        getCountAllPTMsSorted {
            public String toString() {
                return "MATCH path = (re:ReferenceEntity)<-[:referenceEntity]-(ewas:EntityWithAccessionedSequence)-[:hasModifiedResidue]->(mr)-[:psiMod]->(mod)\n"
                        + "WHERE ewas.speciesName = \"Homo sapiens\" \n"
                        + "RETURN mod.identifier as id, mod.name as name, count(DISTINCT re) as frequency\n"
                        + "ORDER BY frequency DESC";
            }
        },
        getNumberOfPTMsByProtein {
            public String toString() {
                return "MATCH path = (re:ReferenceEntity)<-[:referenceEntity]-(ewas:EntityWithAccessionedSequence{speciesName:\"Homo sapiens\"})-[:hasModifiedResidue]->(mr)-[:psiMod]->(mod)\n"
                        + "RETURN re.identifier as UniprotAccession, ewas.displayName as proteinAtLocation, size(collect(mod)) as total, size(collect(DISTINCT mod)) as unique\n"
                        + "ORDER BY UniprotAccession, proteinAtLocation  DESC";
            }
        },
        getLastPublicationOfReaction {
            public String toString() {
                return "MATCH (r:Reaction)-[:literatureReference]-(p:Publication)\n"
                        + "            WITH DISTINCT r.stId AS Reaction, p as publications ORDER BY publications.year\n"
                        + "            RETURN Reaction, last(collect(publications)) as Publication LIMIT 10";
            }
        }
    }

    /**
     * Get list subcellular locations for a protein by Uniprot Accession
     *
     * @param id The UniProt id of the protein to search
     */
    String getSubcellularLocationsByUniProtId = "MATCH (re:ReferenceEntity{identifier:{id}})-[:referenceEntity]-(e:EntityWithAccessionedSequence)-[:compartment]-(c)\n"
            + "RETURN DISTINCT re.identifier,e.stId, c.displayName";

    /**
     * For each protein, get how many subcellular location are associated.
     */
    String getAllSubcellularLocationsCount = "MATCH (re:ReferenceEntity)-[:referenceEntity]-(e:EntityWithAccessionedSequence)-[:compartment]-(c)\n"
            + "RETURN DISTINCT re.identifier, size(collect(DISTINCT c.displayName)) as locationCount\n"
            + "ORDER BY locationCount";

    /**
     * Get minimum, average and maximum for the number of subcellular locations
     * associated to each protein.
     */
    String getSubcellularLocationsStats = "MATCH (re:ReferenceEntity)-[:referenceEntity]-(e:EntityWithAccessionedSequence)-[:compartment]-(c)\n"
            + "WITH DISTINCT re, size(collect(DISTINCT c.displayName)) as locationCount\n"
            + "RETURN min(locationCount) as min, avg(locationCount) as avg, max(locationCount) as max";

    /**
     * Cound the number of human proteins with UniProt accession in Reactome.
     */
    String getNumberOfProteins = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})-[:referenceEntity]-(ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})\n"
            + "RETURN count(DISTINCT re)";

    /**
     * For each protein, count the number of pathways where the protein is a
     * participant.
     */
    String getNumberOfPathwayByProtein = "MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
            + "RETURN DISTINCT count(p), re.identifier";

    /**
     * Get statistics on: For each protein, count the number of pathways where
     * the protein is a participant. min1 1, avg 3.00, 129
     */
    String getStatisticsOfPathwayByProtein = "MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
            + "WITH DISTINCT count(DISTINCT p) as pathwayCount, re\n"
            + "RETURN min(pathwayCount) as min, avg(pathwayCount) as avg, max(pathwayCount) as max";

    /**
     * For each protein, count the number of reactions where the protein is a
     * participant.
     */
    String getNumberOfReactionsByProtein = "MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
            + "RETURN DISTINCT count(r), re.identifier";

    /**
     * Get statistics on: For each protein, count the number of reactions where
     * the protein is a participant. Min 1, avg 7.26, max 288
     */
    String getStatisticsOfReactionsByProtein = "MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
            + "WITH DISTINCT count(DISTINCT r) as reactionCount, re\n"
            + "RETURN min(reactionCount) as min, avg(reactionCount) as avg, max(reactionCount) as max";

    String getPTMSetsByProtein = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'}),\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)\n"
            + "WHERE mr.coordinate IS NOT null\n"
            + "WITH DISTINCT re, ewas, collect(DISTINCT mr) as ptms\n"
            + "RETURN DISTINCT re.identifier, collect(ewas.stId), collect(DISTINCT ptms) as ptmSets";

    String getEwasSetByPTMSet = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'}),\n"
            + "(ewas)-[:hasModifiedResidue]->(mr)\n"
            + "WITH DISTINCT re, ewas, mr ORDER BY mr.coordinate\n"
            + "WITH  DISTINCT re, ewas, collect(mr.coordinate) as ptmSet\n"
            + "RETURN DISTINCT re.identifier, collect(ewas.stId) as equivalentEwas, ptmSet\n"
            + "ORDER BY re.identifier";

    String getProteinProteoforms = "MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:\"P52948\"})\n" +
            "WITH DISTINCT pe, re\n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm)-[:psiMod]->(mod)\n" +
            "WITH DISTINCT pe.displayName AS physicalEntity,\n" +
            "                re.identifier AS referenceEntity,\n" +
            "                re.variantIdentifier AS variantIdentifier,\n" +
            "                tm.coordinate as coordinate, \n" +
            "                mod.identifier as type ORDER BY type, coordinate\n" +
            "RETURN DISTINCT physicalEntity, \n" +
            "\t\t\t\treferenceEntity,\n" +
            "                variantIdentifier,\n" +
            "                COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END + \":\" + type) AS ptms";

    /**
     * Returns a list of entries with three columns.
     * referenceEntity.identifier (UniProt accession), referenceEntity.variantIdentifier, ptms
     * <p>
     * The first two columns are a string, the third is a list of strings with the shape "coordinate:mod".
     * If the variant is empty, then it refers to the canonical sequence of the protein.
     */
    String getProteinProteoformsSimple = "MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n" +
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

    String getReactionsAndPathwaysAndProteoformsByProtein = "// Get Reactions & Pathways with TLP & Proteforms by UniProtAcc\n" +
            "MATCH (tlp:TopLevelPathway)-[:hasEvent*]->(p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),\n" +
            "(rle)-[:input|output|catalystActivity|disease|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:\"P01308\"})\n" +
            "WHERE \n" +
            "    tlp.speciesName = \"Homo sapiens\" AND \n" +
            "    p.speciesName = \"Homo sapiens\" AND \n" +
            "    rle.speciesName = \"Homo sapiens\" AND \n" +
            "    pe.speciesName = \"Homo sapiens\" AND \n" +
            "    re.databaseName = \"UniProt\"\n" +
            "WITH DISTINCT tlp, p, rle, pe, re \n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \n" +
            "WITH DISTINCT tlp, p, rle, pe, (CASE WHEN size(re.variantIdentifier) > 0 THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type \n" +
            "ORDER BY type, coordinate \n" +
            "WITH DISTINCT tlp, p, rle, pe, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \n" +
            "RETURN DISTINCT \n" +
            "    proteinAccession,\n" +
            "    (CASE WHEN pe.startCoordinate IS NOT NULL AND pe.startCoordinate <> -1 THEN pe.startCoordinate ELSE \"null\" END) as startCoordinate,\n" +
            "    (CASE WHEN pe.endCoordinate IS NOT NULL AND pe.endCoordinate <> -1 THEN pe.endCoordinate ELSE \"null\" END) as endCoordinate,\n" +
            "    ptms,\n" +
            "    rle.stId AS Reaction, \n" +
            "    rle.displayName as ReactionDisplayName,\n" +
            "    p.stId as Pathway,\n" +
            "    p.displayName as PathwayDisplayName,\n" +
            "    tlp.stId as TopLevelPathwayStId,\n" +
            "    tlp.displayName as TopLevelPathwayDisplayName\n" +
            "ORDER BY proteinAccession, startCoordinate, endCoordinate, ptms, ReactionDisplayName, PathwayDisplayName, TopLevelPathwayDisplayName";

    String getEntitiesInPathway = "MATCH (p:Pathway{speciesName:\"Homo sapiens\", stId:\"R-HSA-2219528\"})-[:hasEvent*]->(rle:ReactionLikeEvent{speciesName: \"Homo sapiens\"}),\n" +
            "      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),\n" +
            "      (pe{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN DISTINCT re.identifier as Identifiers";

    String getCountEntitiesInPathway = "MATCH (p:Pathway{speciesName:\"Homo sapiens\", stId:{stId}})-[:hasEvent*]->(rle:ReactionLikeEvent{speciesName: \"Homo sapiens\"}),\n      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),\n      (pe{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\nRETURN count(DISTINCT re.identifier) as count";

    String getCountEntitesInPathwayDistinguishingProteoforms = "MATCH (p:Pathway{speciesName:\"Homo sapiens\", stId:\"R-HSA-983169\"})-[:hasEvent*]->(rle:ReactionLikeEvent{speciesName: \"Homo sapiens\"}),\n" +
            "      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),\n" +
            "      (pe{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "WITH DISTINCT pe, re \n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \n" +
            "WITH DISTINCT pe, (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type \n" +
            "WITH DISTINCT pe.stId as ewas, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \n" +
            "RETURN count(DISTINCT {accession: proteinAccession, ptms: ptms}) as count";

    String getEntitiesInPathwayDistinguishingProteoforms = "MATCH (p:Pathway{speciesName:\"Homo sapiens\", stId:\"R-HSA-983169\"})-[:hasEvent*]->(rle:ReactionLikeEvent{speciesName: \"Homo sapiens\"}),\n" +
            "      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),\n" +
            "      (pe{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "WITH DISTINCT pe, re \n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod) \n" +
            "WITH DISTINCT pe, (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type \n" +
            "WITH DISTINCT pe.stId as ewas, proteinAccession, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms \n" +
            "RETURN DISTINCT proteinAccession, ptms";

    String getCountReactionsInPathway = "MATCH (p:Pathway{speciesName:\"Homo sapiens\", stId:{stId}})-[:hasEvent*]->(rle:ReactionLikeEvent{speciesName: \"Homo sapiens\"}),\n      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),\n      (pe{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\nRETURN count(DISTINCT rle.stId) as count";

}
