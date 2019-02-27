# PathwayMatcher v1.4

Free, open-source command line program to search for reactions and pathways related to input data.

Performs:
* Pathway search
* Over representation analysis

Shipped as:
* Jar file
* Docker image

What is included in the package:
* The PathwayMatcher program
* Mapping files for all supported data types

Requirements to run:
* Java 1.8+
* (Upgrade)No need of Neo4j with Reactome

Pathway data sources:
* Reactome graph database v63.
* Variant effect predictors (VEP) mapping

## Input

The input data types supported are:
* Gene names
* Ensembl identifiers
* UniProt accessions
* Rsid genetic variants
* Genetic variants specified by chromosome and base pair
* Proteoforms

## Output

* Tab separated file with the list of reactions and pathways related to each input entity
* Tab separated file with the list of most statistically significant pathways

## Proteoforms
* Each proteoform is formed by a uniprot accession number + the isoform + the set of post translational modifications.
* Each post translational modification is composed by a PSIMOD type and a positive integer coordinate.

# Command line arguments accepted are:
* t, type
* r, range
* tlp, toplevelpathways
* m, matching
* i, input
* o, output