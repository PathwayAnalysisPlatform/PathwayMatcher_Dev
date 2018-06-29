# Publications supporting Reactome

Proteins are connected in three possible ways:
•	Participate in the same Reaction
•	Are members of the same Complex
•	Are members of the same Set

In a similar fashion as the proteins, proteoforms are connected in the same three ways.

Therefore, the interactions among proteins are supported by the curated literature references in Reactome. For v63, the number of
publications supporting the reactions are 14866.

## Cypher queries

* Get literature references of human Reactions:
~~~~
MATCH (r:Reaction{speciesName:'Homo sapiens'})-[:literatureReference]->(lr:LiteratureReference)
RETURN DISTINCT r.stId as Reaction, r.displayName as Name, lr.pubMedIdentifier as PubMed, lr.year as Year
ORDER BY Year, Reaction
~~~~

* Get publications that support both a pathway and an inner reaction
~~~~
MATCH (r:Reaction{speciesName:'Homo sapiens'})-[:literatureReference]->(lr:LiteratureReference)<-[:literatureReference]-(p:Pathway{speciesName:"Homo sapiens"}), (p)-[:hasEvent*]->(r)
RETURN DISTINCT p, r, lr
~~~~
