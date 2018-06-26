PathwayMatcher
======

[![GitHub license](http://dmlc.github.io/img/apache2.svg)](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/LICENSE.txt)


## Overview

PathwayMatcher is a software tool writen in Java to search for pathways related to a list of proteins in Reactome. Current version is 1.0. 

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki) for more information.*

## Introduction

Biological pathways are an excellent resource to analyze the causes and consequences of certain phenotypes. Most of the components of the pathways are proteins. When searching for relevant pathways to perform analysis of a patient sample proteins, it is very common to lose information due to lack of precision in the search. This leads to result sets with many extra selected pathways that are not really related to the input sample.  We present more fine grained approach to search, not only with the gene names, but also with post translational modifications of the proteins, such as phosphorylation. Ultimately, any omics dataset with its mutations and modifications will be mapped directly to the functional knowledgebases allowing the functional interpretation by researchers and clinicians. The reference database used is Reactome, a free, open source, curated and peer reviewed database of biological reactions, that contains the quality data needed for this type of fine grained search.
 database of biological reactions. It can be readily queried with omics datasets, and we are improving its features by extending the matching the clinical data to the biological pathways. 
Not only will the gene names be used, but also mutations or post translational modifications such as phosphorylation. 

## Bioconda

[![install with bioconda](https://anaconda.org/bioconda/pathwaymatcher/badges/installer/conda.svg)](http://bioconda.github.io/recipes/pathwaymatcher/README.html)
[![bioconda downloads](https://anaconda.org/bioconda/pathwaymatcher/badges/downloads.svg)](http://bioconda.github.io/recipes/pathwaymatcher/README.html)
[![latest release](https://anaconda.org/bioconda/pathwaymatcher/badges/latest_release_date.svg)](http://bioconda.github.io/recipes/pathwaymatcher/README.html)

PathwayMatcher is available in [Bioconda](bioconda.github.io/recipes/pathwaymatcher/README.html). Install with:

```bash
conda install pathwaymatcher
```

and update with:

```bash
conda update pathwaymatcher
```


## Installation

1. Install java 
1. Download PathwayMatcher executable.
1. Run PathwayMatcher.

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Installation) for more information.*

## Usage

PathwayMatcher can search for reactions and pathways with various input types, and generates mapping files to the database.

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Usage) for more information.*

#### Input:

The input can be:
* [Genetic variants](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Input#genetic-variants)
* [Genes](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Input#genes)
* [Peptides](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Input#peptides)
* [Protein](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Input#proteins)
* [Proteoforms](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Input#proteoforms)

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Input) for more information.*

#### Output:

The output of PathwayMatcher is composed of two files, the [Reaction and Pathway mapping](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Output#search) and the [statistical analysis](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Output#analysis) of the relevant pathways.

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Output) for more information.*

## Licence

PathwayMatcher is a free open-source project, following an [Apache License 2.0](https://github.com/LuisFranciscoHS/PathwaySearch/blob/master/LICENSE.txt "Apache Licence"). 

## Acknowledgements

* [KG Jebsen Center for Diabetes Research](http://www.uib.no/en/diabetes "KG Jebsen Center for Diabetes Research Homepage")
* [University of Bergen (UiB)](http://www.uib.no/en "UiB's Homepage")
* [EMBL-EBI](http://www.ebi.ac.uk/ "EBI's Homepage")
