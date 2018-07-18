# Cypher queries for statistics

* Prevalence of the different PTM annotations in Reactome. PTM labels are extracted from the Reactome database and the number of proteins annotated with the PTM is displayed for each label. If a protein is carrying multiple instances of the PTM, the PTM is counted only once (__Supplementary Figure 1__):
~~~~
MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
RETURN DISTINCT count(DISTINCT re) as frequency, mod.displayName as name
ORDER BY frequency DESC
LIMIT 20
~~~~

* Number of reactions and pathways for proteins with at least one ptm in any of its proteoforms, and that participate in at least one reaction and pathway (__Supplementary Table 1__).
~~~~
MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT re
MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'
RETURN DISTINCT re.identifier as protein, size(collect(DISTINCT r.stId)) as reactionCount, size(collect(DISTINCT p.stId)) as pathwayCount
ORDER BY pathwayCount DESC
~~~~

* Number of reactions and pathways for proteoforms with at least one ptm, and that participate in at least one reaction and pathway (__Supplementary Table 2__). Note that proteoforms also differentiate between isoforms.
~~~~
MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})
WITH DISTINCT pe, re
MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
WITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type 
ORDER BY type, coordinate
WITH DISTINCT pe, re, COLLECT(type + ":" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END) AS ptms
WITH DISTINCT pe, re, ptms
MATCH (p:Pathway)-[:hasEvent*]->(r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens' AND pe.speciesName = 'Homo sapiens'
RETURN DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as protein, re.displayName as name, re.description as description, ptms, collect(DISTINCT pe.stId) as peSet, size(collect(DISTINCT r.stId)) as reactionCount, size(collect(DISTINCT p.stId)) as pathwayCount
ORDER BY pathwayCount DESC, reactionCount DESC, protein, name
LIMIT 10
~~~~


