PathwayMatcher
======

## Overview

PathwayMatcher is a standalone command line tool to match human biomedical data to pathways. Its advanced mapping functions allow matching multiple types of omics data to the [Reactome](http://www.reactome.org/) database: lists of genetic variants, gene or protein identifiers, lists of peptides including post-translational modifications, and proteoforms. For example, if a protein is provided with a phosphorylation at a given site, it is possible to match only those pathways involving the protein in the given phosphorylation state. PathayMatcher then exports the reactions and pathways matched, standard overrepresentation analysis, and biological networks.  

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki) for more information.*

## Bioconda

[![install with bioconda](https://img.shields.io/badge/install%20with-bioconda-brightgreen.svg?style=flat-square)](http://bioconda.github.io/recipes/pathwaymatcher/README.html)
[![latest release](https://anaconda.org/bioconda/pathwaymatcher/badges/latest_release_date.svg)](http://bioconda.github.io/recipes/pathwaymatcher/README.html)
[![bioconda downloads](https://anaconda.org/bioconda/pathwaymatcher/badges/downloads.svg)](http://bioconda.github.io/recipes/pathwaymatcher/README.html)

PathwayMatcher is available in [Bioconda](https://anaconda.org/bioconda/pathwaymatcher). 

Install with:

```bash
conda install -c bioconda pathwaymatcher
```

and update with:

```bash
conda update -c bioconda pathwaymatcher
```

## Galaxy
[![Clone PathwayMatcher](https://img.shields.io/badge/clone%20in-galaxy-brightgreen.svg?style=flat-square)](https://toolshed.g2.bx.psu.edu/view/galaxyp/reactome_pathwaymatcher/f66af2b04a98)

PathwayMatcher is available in the [Galaxy Tool Shed](https://toolshed.g2.bx.psu.edu) under [reactome_pathwaymatcher](https://toolshed.g2.bx.psu.edu/view/galaxyp/reactome_pathwaymatcher/f66af2b04a98).

### Try it now

PathwayMatcher is ready to use without any previous requirement at the [official European Galaxy Instance](https://usegalaxy.eu/?tool_id=toolshed.g2.bx.psu.edu%2Frepos%2Fgalaxyp%2Freactome_pathwaymatcher%2Freactome_pathwaymatcher%2F1.8.0&version=1.8.0)



## Local Installation
[![Download PathwayMatcher](https://img.shields.io/badge/download-all%20platforms-brightgreen.svg?style=flat-square)](https://github.com/LuisFranciscoHS/PathwayMatcher/releases)

1. Install java [here](https://www.java.com/en/download) unless already available on your machine. _Note: Java 1.8 is required._
2. Download the latest version of the PathwayMatcher executable [here](https://github.com/LuisFranciscoHS/PathwayMatcher/releases).
3. Run PathwayMatcher as detailed [here](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Usage).

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

The output of PathwayMatcher is composed of three files, the [Reaction and Pathway mapping](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Output#search), the [statistical analysis](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Output#analysis) of the relevant pathways, and biological networks constructed based on the input.

*See our [Wiki](https://github.com/LuisFranciscoHS/PathwayMatcher/wiki/Output) for more information.*

## Global Networks

Networks files constructed using all the entities in PathwayMatcher are available [here](https://github.com/LuisFranciscoHS/PathwayMatcher/tree/master/resources/networks/all). `protein*` and `proteoform*` files correspond to the networks obtained using protein and proteoform matching, repesctively.

## Licence
[![GitHub license](http://dmlc.github.io/img/apache2.svg)](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/LICENSE.txt)

PathwayMatcher is a free open-source project, distributed under the permisive [Apache License 2.0](https://github.com/LuisFranciscoHS/PathwaySearch/blob/master/LICENSE.txt "Apache Licence"). 

## Acknowledgements

* [KG Jebsen Center for Diabetes Research](http://www.uib.no/en/diabetes "KG Jebsen Center for Diabetes Research Homepage")
* [University of Bergen (UiB)](http://www.uib.no/en "UiB's Homepage")
* [EMBL-EBI](http://www.ebi.ac.uk/ "EBI's Homepage")
