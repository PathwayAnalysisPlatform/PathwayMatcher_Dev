PathwayMatcher
======

[![GitHub license](http://dmlc.github.io/img/apache2.svg)](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/LICENSE.txt)

## Overview

PathwayMatcher is a software tool writen in Java to search for pathways related to a list of proteins in Reactome. Current version is 1.0.

## Introduction

Biological pathways are an excellent resource to analyze the causes and consequences of certain phenotypes. Most of the components of the pathways are proteins. When searching for relevant pathways to perform analysis of a patient sample proteins, it is very common to lose information due to lack of precision in the search. This leads to result sets with many extra selected pathways that are not really related to the input sample.  We present more fine grained approach to search, not only with the gene names, but also with post translational modifications of the proteins, such as phosphorylation. Ultimately, any omics dataset with its mutations and modifications will be mapped directly to the functional knowledgebases allowing the functional interpretation by researchers and clinicians. The reference database used is Reactome, a free, open source, curated and peer reviewed database of biological reactions, that contains the quality data needed for this type of fine grained search.
 database of biological reactions. It can be readily queried with omics datasets, and we are improving its features by extending the matching the clinical data to the biological pathways. 
Not only will the gene names be used, but also mutations or post translational modifications such as phosphorylation. 

## Installation

1. Install Neo4j
1. Download Reactome.
1. Install java 
1. Download PathwayMatcher executable.
1. Run PathwayMatcher.

## Use cases

PathwayMatcher can search for pathway with various input types and can generate a result in different formats. 

#### Types of input:

1. Max Quant matrix file.
1. Peptide list
    * Simple list
    * List with PTM sites
    * List with PTM types and sites
1. Gene/Protein list
    * Simple list
    * List with PTM sites
    * List with PTM types and sites


#### Types of output:

1. Pathway list
1. Reaction list
1. Table file
A csv file with six columns: Protein, PTM, Pathway Id, Pathway Name, Reaction Id, Reaction Name

#### Execution

* Run Neo4j
* Verify Configuration
* Execute PathwayMatcher

[//]: # (## Examples)



[//]: # (## Configuration)



[//]: # (## Modify and contribute)



## Licence

PathwayMatcher is a free open-source project, following an [Apache License 2.0](https://github.com/LuisFranciscoHS/PathwaySearch/blob/master/LICENSE.txt "Apache Licence"). 

## Acknowledgements

* [KG Jebsen Center for Diabetes Research](http://www.uib.no/en/diabetes "KG Jebsen Center for Diabetes Research Homepage")
* [University of Bergen (UiB)](http://www.uib.no/en "UiB's Homepage")
* [EMBL-EBI](http://www.ebi.ac.uk/ "EBI's Homepage")

[//]: # (## Cites)

## About the reference data

#### Publication references

How many publications do reactions have?
There are 77701 reactions in total and 

How many human reactions have publication?
There are 9297 reactions in total and 8719 (93.78%) have publication.

Get how many publications are associated to a reaction
~~~~
MATCH (r:Reaction)-[lr:literatureReference]-(p:Publication) 
WHERE r.speciesName = "Homo sapiens" AND lr IS NOT NULL
WITH r, size(collect(p)) as NumberOfPublications
RETURN r.displayName, NumberOfPublications  ORDER BY NumberOfPublications DESC
~~~~

Get how many human reactions are annotated
~~~~
MATCH (r:Reaction)
WHERE r.speciesName = "Homo sapiens"
RETURN DISTINCT count(r)
~~~~

Get how many human reactions have a publication
~~~~
OPTIONAL MATCH (r:Reaction)-[lr:literatureReference]-(p:Publication) 
WHERE r.speciesName = "Homo sapiens" AND lr IS NOT NULL
RETURN  count(DISTINCT r)
~~~~