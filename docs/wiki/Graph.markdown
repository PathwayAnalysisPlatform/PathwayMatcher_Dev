# Proteins Connection Graph

__PathwayMatcher__ allows the user to generate a protein connection graph as an additional output when executing the pathway search and analysis.
To obtain this graph just add the command line argument __-g__ or __--graph__.

## Graph Definition

<p>
The connection graph is defined by a set of vertices and edges, where vertices represent proteins
and edges represent connections/relations between proteins according to the data model in the Reactome database. <br>

Proteins are referenced only by their UniProt<sup>[\[1\]](#references)</sup> accession. <br></p>

There is an connection between two proteins when:

* (Protein1)--(Complex)--(Protein2): Both are components of the same complex.
* (Protein1)--(Reaction)--(Protein2): Both participate in the same reaction.
* (Protein1)--(Set)--(Protein2): Both are members of the same entity set.

This connections are undirected, they have no direction; the two proteins are just related to each other. <br>
<p>
Proteins can participate with multiple roles in a chemical reaction: 

* input (reactant)
* output (product)
* catalyst
* regulator

Proteins participate independently or as components of a complex or entity set:

* (Reaction)--(Protein)
* (Reaction)--(Complex)--(Protein)
* (Reaction)--(Complex)--(Complex)--(Protein)
* (Reaction)--(Set)--(Protein)
* (Reaction)--(Set)--(Set)--(Protein)
* (Reaction)--(Complex)--(Set)--(Protein)
* (Reaction)--(Complex)--(Set)--(Set)--(Complex)--(Protein)
* ...
</p>

<p>
Finally, there are two types of edges: internal and external.

* Internal edges are connections between proteins of the input list. 
* External edges are connections between a protein in the input list and a protein not in the input list.
</p>

## Graph representation

The graph is defined in three files _vertices.tsv_, _internalEdges.tsv_ and _externalEdges.tsv_.
By default, they are saved in the same directory where __PathwayMatcher__ is located. To save them in a different directory use the command line argument __-o__.

### Vertices file

A tab separated file (.tsv) with two columns, one vertex (protein) each row:

* __id__: Uniprot accession of the protein
* __name__: Colloquial name of the protein

Example:
~~~~
id	 name
P35070	 Probetacellulin
P21359	 Neurofibromin
Q8IV61	 Ras guanyl-releasing protein 3
~~~~

### Edges files

Tab separated files (.tsv) with 6 columns, one edge (connection) each row:

* __id1__: UniProt accession of one protein in the connection
* __id2__: UniProt accession of the second protein in the connection
* __type__: Where the two proteins meet (Complex or Reaction)
* __container_id__: Id of the complex or reaction
* __role1__: Role of the first protein in the connection
* __role2__: Role of the second protein in the connection

Example:
~~~~
id1	 id2	 type	  container_id   role1	 role2
P27361	 P28482  Reaction R-HSA-5675373  input	 output
P27361	 P28562	 Reaction R-HSA-5675373  input	 catalyst
P27361	 P28562  Reaction R-HSA-5675373  output	 catalyst
O43524	 P84022	 Complex  R-HSA-1535906	component component
~~~~

# References
\[1\] [UniProt: the universal protein knowledgebase. Nucleic Acids Res. 45: D158-D169 (2017)](http://dx.doi.org/doi:10.1093/nar/gkw1099) <br>
