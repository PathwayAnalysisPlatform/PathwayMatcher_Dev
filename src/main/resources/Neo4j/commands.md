# Find pathways and reactions that contain a Protein

## Check if a Uniprot Id is in Reactome
~~~~
MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'})
RETURN pe.stId
~~~~

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

## Find all pathways and reactions in a verbose mode
~~~~
MATCH (p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent), 
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{stId:'R-DME-141433-2'})-[]->(c:Compartment),
(pe)-[:referenceEntity]->(re:ReferenceEntity)
RETURN re.identifier, c.displayName, pe.stId, pe.displayName, p.stId AS Pathway, rle.stId AS Reaction
~~~~

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

## Get all candidate ewas for a modified protein
~~~~
MATCH (pe:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity{identifier:'P31749'}),
(pe)-[:hasModifiedResidue]->(mr)-[:psiMod]->(t)
WHERE mr.coordinate in [17, 308, 473]
RETURN pe.stId as stId, pe.displayName as name, collect(mr.coordinate) as sites, collect(t.identifier) as mods
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

## List all pathways containing an ewas
~~~~
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:EntityWithAccessionedSequence{stId:'R-HSA-141433'})
RETURN pe.stId, pe.displayName, p.stId as PathwayStId, p.displayName as PathwayName, count(DISTINCT rle.stId) AS Reactions
ORDER BY Reactions DESC
~~~~
~~~~
MATCH (p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:EntityWithAccessionedSequence{stId:'R-HSA-141433'})
RETURN DISTINCT p.stId as stId
~~~~

 ## List all the tree location for the pathways containing a protein
 ~~~~
MATCH path =(p:TopLevelPathway{speciesName:'Homo sapiens'})-[he:hasEvent*]->(rle:ReactionLikeEvent)
-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]
->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n
RETURN DISTINCT extract(n IN nodes(path) | n.displayName) as TopToLeaf
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

# Find protein neighbors

## For one protein get proteins in the same complexes
~~~~
MATCH (re:ReferenceEntity{identifier:'P31749'})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(c:Complex)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)
RETURN DISTINCT re.identifier, p.stId, p.displayName, c.displayName, nE.stId, nE.displayName, nP.identifier
~~~~

## For one protein get proteins in the same reactions
~~~~
MATCH (re:ReferenceEntity{identifier:'P31749'})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]-(rle:ReactionLikeEvent)-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)
RETURN DISTINCT re.identifier, p.stId, p.displayName, rle.stId, rle.displayName, nE.stId, nE.displayName, nP.identifier
~~~~

## For all proteins get all proteins in the same complexes (Uniprot id level)
~~~~
MATCH (p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(c:Complex)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(v:EntityWithAccessionedSequence)
RETURN DISTINCT p.stId, p.displayName, v.stId, v.displayName
LIMIT 100
~~~~
## For all proteins get all proteins in the same reactions (Uniprot id level)
~~~~
MATCH (p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]-(rle:ReactionLikeEvent)-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]->(v:EntityWithAccessionedSequence)
RETURN DISTINCT p.stId, p.displayName, rle.stId, rle.displayName, v.stId, v.displayName
LIMIT 100
~~~~

## Find all paths of complexes connecting a list of nodes.
~~~~
WITH ["R-HSA-141400", "R-HSA-141433"] as lista
MATCH path = (p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(c:Complex)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(v:EntityWithAccessionedSequence)
WHERE p.stId in lista AND v.stId in lista
RETURN DISTINCT nodes(path)
~~~~

## Find all paths connecting two nodes
~~~~
WITH ["R-HSA-141400", "R-HSA-141433"] as lista
MATCH path = (p:EntityWithAccessionedSequence)-[*]-(v:EntityWithAccessionedSequence)
WHERE p.stId in lista AND v.stId in lista
RETURN DISTINCT nodes(path)
LIMIT 1
~~~~
## Find shortest path connecting two nodes

# Glossary

* **Modified Protein** is a Protein with a specific PTM Configuration. In Reactome that is called **PhysicalEntity** or **EntityWithAccessionedSequence**.