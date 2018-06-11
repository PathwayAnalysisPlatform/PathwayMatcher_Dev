# Proteins

* Get all proteins: 10761
~~~~
MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})
RETURN re.identifier as protein
~~~~

* Number of reactions per protein
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT r.stId) as reactionSet
RETURN DISTINCT protein, size(reactionSet) as reactionCount
~~~~

* Number of pathways per protein
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent*]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WITH re.identifier as protein, collect(DISTINCT p.stId) as pathwaySet
RETURN protein, size(pathwaySet) as pathwayCount
~~~~

* Number of reactions and pathways for each protein
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (p:Pathway{speciesName:'Homo sapiens'})-[:hasEvent*]->(r:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WITH re.identifier as protein, collect(DISTINCT p.stId) as pathwaySet, collect(DISTINCT r.stId) as reactionSet
RETURN protein, size(pathwaySet) as pathwayCount, size(reactionSet) as reactionCount
~~~~

* Stats of reactions for all proteins
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT r.stId) as reactionSet
WITH DISTINCT protein, size(reactionSet) as reactionCount
RETURN min(reactionCount) as minReactionCount, avg(reactionCount) as avgReactionCount, max(reactionCount) as maxReactionCount
~~~~

* Stats of pathway for all proteins
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
WITH re.identifier as protein, collect(DISTINCT pe.stId) as peSet, collect(DISTINCT p.stId) as pathwaySet
WITH protein, size(pathwaySet) as pathwayCount
RETURN min(pathwayCount) as minPathwayCount, avg(pathwayCount) as avgPathwayCount, max(pathwayCount) as maxPathwayCount
~~~~

## Results

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

* Count PTMSets for each protein
~~~~
MATCH (re:ReferenceEntity{databaseName:"UniProt"})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:"Homo sapiens"})
WITH re, pe
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(mr)
WITH DISTINCT re.identifier as protein, pe, collect(mr.coordinate) as ptmSet
WITH DISTINCT protein, size(collect(ptmSet)) as ptmSetCount
RETURN min(ptmSetCount), avg(ptmSetCount), max(ptmSetCount)
~~~~

* Get all proteoforms: 13812
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT pe, re, COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END + ":" + type) AS ptms
WITH DISTINCT pe, re, ptms
RETURN DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, collect(DISTINCT pe.stId) as equivalentPe, ptms
~~~~

* Number of reactions per proteoform
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

* Stats for reactions per proteoform
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

* Number of pathways proteoform 
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

* Stats for pathways for each group of PhysicalEntity with the same PTMSet 
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

## Results

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
