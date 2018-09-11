# Verify the general characteristics of the protein and proteoform networks
# Notice that they are handled as undirected graphs.

# Load libraries

library(igraph)
library(ggplot2)
source("graphs.R")
source("degreeDistribution/Percolation.R")

# Load undirected graphs

pG <- loadGraph("all/1.8.1/proteinInternalEdges.tsv.gz")
mG <- loadGraph("all/1.8.1/proteoformInternalEdges.tsv.gz")



# Get approximated percolation curve

samples <- getPercolationCurvePoints(graph = mG, measures = 3, replicates = 1)
p <- plotPercolationCurve(samples)
p

write.csv(samples, "proteoformNetworkSamples.csv", row.names=FALSE, na="")
