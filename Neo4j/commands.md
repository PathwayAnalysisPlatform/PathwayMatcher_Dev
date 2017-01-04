# Find pathways and reactions that contain a Protein

## List all pathways and reactions
~~~~
MATCH (p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),
(pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'})
RETURN p.stId AS Pathway, rle.stId AS Reaction, rle.displayName AS ReactionName
~~~~

## List all reactions 

## Count how many reactions of every low level pathway contain every protein with the same accession number.
~~~~
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),
(pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'})
RETURN re.identifier, p.stId AS Pathway, count(rle.stId) AS Reactions
ORDER BY Reactions DESC
~~~~

## Same as previous V2
~~~~
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity),
(pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'})
RETURN re.identifier, p.stId AS Pathway, count(rle.stId) AS Reactions
ORDER BY Pathway ASC
~~~~
Note: To make previous more generic to all types of pathways, add an '*' at "-[:hasEvent]->".

# Find PTMs of a Protein

## List all PTMs of a Protein
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
RETURN re.identifier, pe.displayName, mr.displayName, t.identifier
~~~~

## Group all PTMs by Modified Protein.
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
RETURN re.identifier, pe.displayName, collect(DISTINCT mr.coordinate), collect(t.displayName)
~~~~

## Find a modified Protein with and accesion number and the PTM sites.
The number of PTMs of the protein must be the required one and the values have to be in the list.
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE mr.coordinate in [17, 308, 473]
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as mods, count(DISTINCT mr.coordinate) as numSites
MATCH (pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE numSites = 3
RETURN re.identifier, pe.displayName, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName), count(DISTINCT mr.coordinate) as numSites
ORDER BY numSites DESC
~~~~

## Count how many reactions of every low level pathway contain every modified protein with the same accession number.
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as types
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
RETURN re.identifier, pe.displayName, sites, types, p.stId, p.displayName, count(DISTINCT rle.stId) AS Reactions
ORDER BY Reactions DESC
~~~~
Note: To make previous more generic to all types of pathways, add an '*' at "-[:hasEvent]->".
## Same as previous V2
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as types
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
RETURN re.identifier, pe.displayName, sites, types, p.stId as Pathway, count(DISTINCT rle.stId) AS Reactions
ORDER BY Pathway ASC
~~~~
Note: To make previous more generic to all types of pathways, add an '*' at "-[:hasEvent]->". 

## List all pathways and reactions that contain a modified Protein
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE mr.coordinate in [17, 308, 473]
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as mods, count(DISTINCT mr.coordinate) as numSites
MATCH (pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE numSites = 3
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as types, count(DISTINCT mr.coordinate) as numSites
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
RETURN re.identifier, pe.displayName, sites, types, p.stId as PathwayStId, p.displayName as PathwayName, count(DISTINCT rle.stId) AS Reactions
ORDER BY Reactions DESC
~~~~

## Count how many reactions of every low level pathway contain every modified protein
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE mr.coordinate in [17, 308, 473]
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as mods, count(DISTINCT mr.coordinate) as numSites
MATCH (pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE numSites = 3
WITH re, pe, collect(DISTINCT mr.coordinate) as sites, collect(t.displayName) as types, count(DISTINCT mr.coordinate) as numSites
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)
RETURN re.identifier, pe.displayName, sites, types, p.stId as Pathway, count(DISTINCT rle.stId) AS Reactions
ORDER BY Pathway ASC
~~~~

# Get post translational modifications

# Glossary

* **Modified Protein** is a Protein with a specific PTM Configuration. In Reactome that is called **PhysicalEntity** or **EntityWithAccessionedSequence**.