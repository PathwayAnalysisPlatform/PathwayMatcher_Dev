# PathwayMatcher v1.5

Free, open-source command line program to search for reactions and pathways related to input data.

Project structure:
* PathwayMatcher: Main program to load the static mapping and call other methods. Contains no implementation of the search or analysis methods.
* Model: Classes to represent the objects related to pathway analysis.
* Methods: Separate jar with the search and analysis methods. They are called with the static mapping sent as arguments.
* Extractor: Predecesor project executed localy. Creates static mapping files. Not shipped with PathwayMatcher.

Performs:
* Pathway search in Reactome
* Over representation analysis

Shipped as:
* Self contained executable Jar file
* Docker image

What is included in the package:
* The PathwayMatcher program
* Mapping files for all supported data types

Requirements to run:
* Java 1.8+
* Neo4j with Reactome only for extractor, not for pathway matcher.

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

* Search: Tab separated file with the list of reactions and pathways related to each input entity
* Analysis: Tab separated file with the list of most statistically significant pathways

## Proteoforms
* Each proteoform is formed by a uniprot accession number + the isoform + the set of post translational modifications.
* Each post translational modification is composed by a PSIMOD type and a positive integer coordinate.

# Command line arguments accepted are:
* t, type
* r, range
* tlp, toplevelpathways
* m, matching : Flexible, One and Strict
* i, input
* o, output