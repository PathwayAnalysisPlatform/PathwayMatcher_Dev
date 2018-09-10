
library(igraph)
library(ggplot2)
library(data.table)
library(gridExtra)


source("graphs.R")
source("degreeDistribution/Percolation.R")


# Load undirected graphs

pG <- loadGraph("all/1.8.1/proteinInternalEdges.tsv.gz")
mG <- loadGraph("all/1.8.1/proteoformInternalEdges.tsv.gz")

## Get approximated percolation curve
{
    set.seed(1)
    samplesPG <- getPercolationData(graph = pG,
                                           measures = 20,
                                           replicates = 10)
    set.seed(1)
    samplesMG <- getPercolationData(graph = mG,
                                           measures = 20,
                                           replicates = 10)
}

samplesPG$db <- "Protein"
samplesMG$db <- "Proteoform"
samples <- rbind(samplesPG, samplesMG)

grid.arrange(
    ggplot(samplesPG, aes(x=completeness, y=relativeLCC)) +
    geom_point() + geom_smooth(),
    ggplot(samplesPG, aes(x=completeness, y=lcc)) +
    geom_point() + geom_smooth(),
    ggplot(samplesPG, aes(x=completeness, y=subPT)) +
    geom_point() + geom_smooth(),
    ggplot(samplesPG, aes(x=completeness, y=subPT2)) +
    geom_point() + geom_smooth(), nrow=2)

grid.arrange(
    ggplot(samplesMG, aes(x=completeness, y=relativeLCC)) +
    geom_point() + geom_smooth(),
    ggplot(samplesMG, aes(x=completeness, y=lcc)) +
    geom_point() + geom_smooth(),
    ggplot(samplesMG, aes(x=completeness, y=subPT)) +
    geom_point() + geom_smooth(),
    ggplot(samplesMG, aes(x=completeness, y=subPT2)) +
    geom_point() + geom_smooth(), nrow=2)

ggsave("PCv2.png", width=14, height=7,
       grid.arrange(
           ggplot(samples, aes(x=completeness, y=relativeLCC, col=db)) +
           geom_point() + geom_smooth() +
           scale_y_continuous("Relative Size Largest Component"),
           ggplot(samples, aes(x=completeness, y=lcc, col=db)) +
           geom_point() + geom_smooth() +
           scale_y_continuous("# Nodes in Largest Component"),
           ggplot(samples, aes(x=completeness, y=subPT, col=db)) +
           geom_point() + geom_smooth() +
           scale_y_continuous("Percolation Threshold (LFHS)"),
           ggplot(samples, aes(x=completeness, y=subPT2, col=db)) +
           geom_point() + geom_smooth() +
           scale_y_continuous("Percolation Threshold (BB)"),
           nrow=2)
       )
