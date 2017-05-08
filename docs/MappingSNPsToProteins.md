# Mapping SNPs to Genes to Proteins

Possibilities:
* Use biomart R package
* Use ensembl Rest API
* Download complete dbSNP for human
* Download all associated SNPs from UniProt

## Use Ensemble Rest API

The documentation is in [http://rest.ensembl.org/#Variation](http://rest.ensembl.org/#Variation).

The REST method used is [POST vep/:species/id](http://rest.ensembl.org/documentation/info/vep_id_post) to fetch variant consequences for multiple ids.

## Use biomart R package

#### Download and install of the R package
~~~~
source("https://bioconductor.org/biocLite.R")
biocLite("biomaRt")
~~~~
For more details go to the Bioconductor page for [biomaRt](http://bioconductor.org/packages/biomaRt/).

#### Use the R package

* Load library to R:
~~~~
library("biomaRt")
listMarts()
listEnsembl()
~~~~
* Use de variation biomart
~~~~
variation = useEnsembl(biomart="snp", dataset="hsapiens_snp")
~~~~

* Get the Ensemble Gene Id for a dbSNP id
~~~~
rs1333049 <- getBM(attributes=c('refsnp_id','chr_name','ensembl_gene_stable_id','ensembl_transcript_stable_id'), filters = 'snp_filter', values ="rs1333049", mart = variation)
~~~~
~~~~
refsnp_id chr_name ensembl_gene_stable_id ensembl_transcript_stable_id
1  rs1333049        9        ENSG00000240498              ENST00000585267
2  rs1333049        9        ENSG00000240498              ENST00000580576
3  rs1333049        9        ENSG00000240498              ENST00000428597
4  rs1333049        9        ENSG00000240498              ENST00000584816
5  rs1333049        9        ENSG00000240498              ENST00000584020
6  rs1333049        9        ENSG00000240498              ENST00000577551
7  rs1333049        9        ENSG00000240498              ENST00000584637
8  rs1333049        9        ENSG00000240498              ENST00000581051
9  rs1333049        9        ENSG00000240498              ENST00000582072
10 rs1333049        9        ENSG00000240498              ENST00000422420
~~~~

* Create an R function to retrieve the Ensemble Gene Id of a dbSNP id
~~~~
getEnsembleGene <- function(rs = "rs3043732") {
  results <- getBM(attributes=c('refsnp_id','chr_name','ensembl_gene_stable_id','ensembl_transcript_stable_id'), filters = 'snp_filter', values = rs, mart = variation)
  return(results)
}
~~~~
For more details go to the [documentation for biomaRt](https://bioconductor.org/packages/release/bioc/vignettes/biomaRt/inst/doc/biomaRt.html#introduction).

