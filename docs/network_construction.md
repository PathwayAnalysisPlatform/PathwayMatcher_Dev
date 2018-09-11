# Network datasets

For our studies we use multiple protein and proteoform interaction networks based on the pathway network in the
Reactome graph database. 

## Pathway network 
The reference pathway multiple types of nodes. Among those types we have Reactions and PhysicalEntities, which include proteins.
If we only consider the annotations of reactions and the proteins participating in them
the network resembles a bipartite graph with two types of vertices (proteins and reactions) and
the edges connect only pairs of vertices of different type.

<-- insert figure -->

In addition to that, there are other types of nodes in the graph database, namely the Complexes and Entity Sets. 
We will call these Group nodes, because they group together multiple proteins for a purpose. It can be
that proteins bind together to form a single functional complex, or that they form a set of similar 
proteins that can be used interchangeably to perform a roles. Group nodes are connected to the protein 
nodes, to denote that they belong to the group. Group nodes can also be connected to each other to
denote that bigger complexes are composed of other smaller complexes. 

<-- insert figure -->

## Protein interaction network
We transformed the pathway network into a protein interaction network by replacing the reaction
nodes for direct connections among the proteins. Every time two proteins participate in the same 
reaction, we represent that with a link between the two proteins. 

<-- insert figure -->

In a similar way, we also replace the group nodes,
by connecting with a link the proteins which are members of the same group (Complex or Entity Set).

## Proteoform interaction network

In real life, the proteins need to be modified sometimes in certain positions of the sequence by 
adding other molecular groups to the side branches of the amino acids. In certain cases, proteins
can only perform their assigned task when they have the specific set of modifications on them.
They could even perform different sets of activities by having different sets of modifications.
 
This is annotated in the original Pathway network using _TranslationalModification_ nodes.
 They denote a modification in a particular protein by connecting a protein node with a modification
 node. Proteins can have none, one or multiple modifications. We call proteoform a specific protein sequence
 with a specific set of modifications. 
 
 <-- insert example -->
 
 There are cases where proteins participate in one Reaction when they have a certain set
  of modifications, and in another Reaction when they have a different set of modifications.

<-- insert example -->  

When 
 

## Disease modules


