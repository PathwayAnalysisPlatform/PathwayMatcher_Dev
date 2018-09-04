# Calculate percolation threshold for a graph

# Libraries

library(igraph)
source("../graphs.R")

proteinGraph <- loadGraph("../all/1.8.1/proteinInternalEdges.tsv.gz")

pc <- getPercolationThreshold(proteinGraph)
pc

proteoformGraph <- loadGraph("../all/1.8.1/proteoformInternalEdges.tsv.gz")

pc <- getPercolationThreshold(proteoformGraph)
pc

pc <- getPercolationThresholdSimplified(proteinGraph)
pc

pc <- getPercolationThresholdSimplified(proteoformGraph)
pc
