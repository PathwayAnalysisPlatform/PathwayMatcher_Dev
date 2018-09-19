# Load the different networks we have to the environment

source("graphs.R")

# Load protein data

all.proteins.graph <- LoadGraph("datasets/all_proteins.tsv.gz")
all.proteoforms.graph <- LoadGraph("datasets/all_proteoforms.tsv.gz")
pp.graph <- LoadGraph("datasets/pp.tsv.gz")
mm.graph <- LoadGraph("datasets/mm.tsv.gz")
pm.graph <- LoadGraph("datasets/pm.tsv.gz") 
