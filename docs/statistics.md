# Cypher queries to obtain statistics about the Pathway Network

# Physical entities

The total number of PhysicalEntity objects for human that are associated to a UniProt accession: 24198 
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
RETURN DISTINCT pe.stId, pe.displayName, re.identifier
~~~~

# Proteins

* Count UniProt accessions related to human PhysicalEntity objects: 10763
~~~~
MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})
RETURN count(DISTINCT re.identifier) as protein
~~~~

* Count number of human PhysicalEntity objects related to a UniProt accession distinguishing
 between isoform sequences: 10935
 ~~~~
 MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})
 RETURN DISTINCT re.identifier, re.variantIdentifier
 ~~~~
 
 * Number of proteins with a specific isoform: 260 
 ~~~~
 MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})
  WHERE re.variantIdentifier IS NOT NULL
  RETURN DISTINCT re.identifier, re.variantIdentifier
 ~~~~
 
* Number of proteins with more than one isoform: 64 
~~~~
MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})
 WHERE re.variantIdentifier IS NOT NULL
 WITH DISTINCT re.identifier as accession, collect(DISTINCT re.variantIdentifier) as isoforms
 WHERE size(isoforms) > 1
 RETURN DISTINCT accession, isoforms
~~~~

* Number of reactions per protein:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT r.stId) as reactionSet
RETURN DISTINCT protein, size(reactionSet) as reactionCount
~~~~

* Number of pathways for each protein:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent*]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WITH re.identifier as protein, collect(DISTINCT p.stId) as pathwaySet
RETURN protein, size(pathwaySet) as pathwayCount
~~~~

* Number of reactions and pathways for each protein:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent*]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WITH re.identifier as protein, collect(DISTINCT p.stId) as pathwaySet, collect(DISTINCT r.stId) as reactionSet
RETURN protein, size(pathwaySet) as pathwayCount, size(reactionSet) as reactionCount
~~~~

* Statistics of reactions for all proteins:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT r.stId) as reactionSet
WITH DISTINCT protein, size(reactionSet) as reactionCount
RETURN min(reactionCount) as minReactionCount, avg(reactionCount) as avgReactionCount, max(reactionCount) as maxReactionCount
~~~~

* Statistics of pathways for all proteins:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT p.stId) as pathwaySet
WITH protein, size(pathwaySet) as pathwayCount
RETURN min(pathwayCount) as minPathwayCount, avg(pathwayCount) as avgPathwayCount, max(pathwayCount) as maxPathwayCount
~~~~

### Results

* Reactions per protein

|Min|Average|Max|
|---|---|---|
|0|7.083|315|

* Reactions per protein with at least one annotated reaction

|Min |Average | Max|
| --- | --- | --- |
| 1 | 7.424 | 315 |

* Pathways per protein

|Min |Average | Max|
| --- | --- | --- |
|0|8.330|292|

* For proteins with at least one pathway annotated

|Min |Average | Max|
| --- | --- | --- |
|1|8.733|292|


---------------------------------------------------------------------------------------------------------------------------
# Proteoforms

* PTM sets per PhysicalEntity, including proteins without modifications (include empty sets)
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
WITH DISTINCT re.identifier as protein, pe, collect(DISTINCT p.stId) as pathwaySet
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(mr)
RETURN DISTINCT protein, pe.stId, pathwaySet, collect(mr.coordinate) as ptmSet
ORDER BY protein
~~~~

* Count PTMSets for each protein:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(mr)
WITH DISTINCT re.identifier as protein, pe, collect(mr.coordinate) as ptmSet
WITH DISTINCT protein, size(collect(ptmSet)) as ptmSetCount
RETURN min(ptmSetCount), avg(ptmSetCount), max(ptmSetCount)
~~~~

* Get all proteoforms: 

~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT pe, re, COLLECT(type + ":" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END) AS ptms
WITH DISTINCT pe, re, ptms
RETURN DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, collect(DISTINCT pe.stId) as equivalentPe, ptms
~~~~

* Get all proteoforms with at least one post translational modification: 3105 (22.37% of the proteoforms)
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe.stId AS physicalEntity,
                re.identifier AS protein,
                re.variantIdentifier AS isoform,
                tm.coordinate as coordinate, 
                mod.identifier as type ORDER BY type, coordinate
WITH DISTINCT physicalEntity,
				protein,
                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,
                COLLECT(type + ":" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END) AS ptms
WHERE size(ptms) > 0
                RETURN DISTINCT isoform, ptms
                ORDER BY isoform, ptms
~~~~
If we do not distinguish between isoforms then it is 3090 proteoforms (22.26%).

* Number of proteins with at least a post translational modification: 1486 (13.8% of the proteins in Reactome).
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe.stId AS physicalEntity,
                re.identifier AS protein,
                re.variantIdentifier AS isoform,
                tm.coordinate as coordinate, 
                mod.identifier as type ORDER BY type, coordinate
WITH DISTINCT physicalEntity,
				protein,
                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,
                COLLECT(type + ":" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END) AS ptms
WHERE size(ptms) > 0
                RETURN DISTINCT protein, collect(ptms)
~~~~

* Number of proteins with at least a post translational modification distinguishing isoforms: 1520 (14.12%)
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe.stId AS physicalEntity,
                re.identifier AS protein,
                re.variantIdentifier AS isoform,
                tm.coordinate as coordinate, 
                mod.identifier as type ORDER BY type, coordinate
WITH DISTINCT physicalEntity,
				protein,
                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,
                COLLECT(type + ":" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END) AS ptms
WHERE size(ptms) > 0
                RETURN DISTINCT isoform, collect(ptms)
~~~~

* Number of reactions of each proteoform:
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe,
              re,
              tm.coordinate as coordinate, 
              mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT 
	        pe,
		    re,
            COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END + ":" + type) AS ptms
WITH DISTINCT pe, re, ptms
OPTIONAL MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens' AND pe.speciesName = 'Homo sapiens'
RETURN DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, ptms, size(collect(DISTINCT r.stId)) as reactionCount
ORDER BY protein
~~~~

* Statistics of reactions of each proteoform:
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe,
              re,
              tm.coordinate as coordinate, 
              mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT 
	        pe,
		    re,
            COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END + ":" + type) AS ptms
WITH DISTINCT pe, re, ptms
OPTIONAL MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens' AND pe.speciesName = 'Homo sapiens'
WITH DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, collect(DISTINCT pe.stId) as equivalentPe, ptms, size(collect(DISTINCT r.stId)) as reactionCount
RETURN min(reactionCount) as minReactionCount, avg(reactionCount) as avgReactionCount, max(reactionCount) as maxReactionCount
~~~~

* Number of pathways for each proteoform:
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT pe, re, COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END + ":" + type) AS ptms
WITH DISTINCT pe, re, ptms
MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens' AND pe.speciesName = 'Homo sapiens'
RETURN DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, ptms, size(collect(DISTINCT p.stId)) as pathwayCount
~~~~

* Statistics of pathways for each proteoform: 
~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe,
              re,
              tm.coordinate as coordinate, 
              mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT 
	        pe,
		    re,
            COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END + ":" + type) AS ptms
WITH DISTINCT pe, re, ptms
OPTIONAL MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens' AND pe.speciesName = 'Homo sapiens'
WITH DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, collect(DISTINCT pe.stId) as equivalentPe, ptms, size(collect(DISTINCT p.stId)) as pathwayCount
RETURN min(pathwayCount) as minPathwayCount, avg(pathwayCount) as avgPathwayCount, max(pathwayCount) as maxPathwayCount
~~~

### Results

* PTM sets per protein

|min|average|max|
|---|---|---|
|1|2.232|316|

* Reactions per proteoform

|min|average|max|
|---|---|---|
|0|6.128|313|

*  Reactions per proteoform with at least one reaction annotated

|min|average|max|
|---|---|---|
|1|6.494|313|

* Pathways per proteoform

|min|average|max|
|---|---|---|
|0|7.66|291|

* Pathways per proteoform with at least one pathway annotated

|min|average|max|
|---|---|---|
|1|8.122|291|

* Note: Only counting the number of pathways and reactions for the proteins that actually have at least one reaction and pathway annotated. Because in the query, I am not using optional match for the pathway and reaction connection. Use the "OPTIONAL MATCH" for all proteins.
* Note: Finds all the pathways using the reactions, no matter the level in the pathway hierarchy.


* Top proteins participating in most reactions:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, re.displayName as name, re.description as description, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT r.stId) as reactionSet
RETURN DISTINCT protein, name, description, size(reactionSet) as reactionCount 
ORDER BY reactionCount DESC
LIMIT 10
~~~~

* Top proteins participating in most pathways ant its containing reactions:
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, re.displayName as name, re.description as description, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT r.stId) as reactionSet, collect(DISTINCT p.stId) as pathwaySet
RETURN DISTINCT protein, name, description, size(reactionSet) as reactionCount, size(pathwaySet) as pathwayCount
ORDER BY pathwayCount DESC, reactionCount DESC, protein, name
LIMIT 10
~~~~

* Top proteoforms participating in most pathways and its containing reactions:
~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT pe, re, COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END + ":" + type) AS ptms
WITH DISTINCT pe, re, ptms
MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens' AND pe.speciesName = 'Homo sapiens'
RETURN DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, re.displayName as name, re.description as description, ptms, collect(DISTINCT pe.stId) as peSet, size(collect(DISTINCT r.stId)) as reactionCount, size(collect(DISTINCT p.stId)) as pathwayCount
ORDER BY pathwayCount DESC, reactionCount DESC, protein, name
LIMIT 10
~~~

