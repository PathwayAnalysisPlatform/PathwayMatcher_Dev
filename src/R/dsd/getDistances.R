# 
# This script calculates DSD in different networks.
#

# Libraries

library(ggplot2, lib = "~/R")
library(igraph,lib =  "~/R")


# Parameters

dsdFile <- "resources/distances/dsd"
simpleFile <- "resources/distances/simple"

computeReactome <- T
computeBiogrid <- T
computeMerged <- T


# Functions

#' Merges the reactome and biogrid graphs.
#' 
#' @param reactomeGraph a vertice of the biogrid edge
#' @param verticeBiogrid2 the other vertice of the biogrid edge
#' @param edgesReactomeCommon the Reactome edge list
#' 
#' @return a boolean indicating whether the given biogrid edge is in Reactome
mergeGraphs <- function(reactomeGraph, biogridGraph) {
    
    result <- union(reactomeGraph, biogridGraph)
    
    if ("source_1" %in% list.edge.attributes(result)) {
        
        source1 <- get.edge.attribute(result, "source_1")
        source2 <- get.edge.attribute(result, "source_2")
        sourceMerge <- ifelse(!is.na(source1), source1, source2)
        
        result <- set.edge.attribute(graph = reactomeGraph, name = "source", value = sourceMerge)
        
        result <- remove.edge.attribute(result, "source_1")
        result <- remove.edge.attribute(result, "source_2")
        
    }
    
    return(result)
}


#' Returns the main component of a graph.
#' 
#' @param graph the entire graph
#' 
#' @return the main component of the graph
getMainComponent <- function(graph) {
    
    components <- decompose(graph)
    mainComponent <- NULL
    maxSize <- 0
    
    for (subGraph in components) {
        
        size <- length(V(subGraph))
        
        if (size > maxSize) {
            
            mainComponent <- subGraph
            maxSize <- size
            
        }
    }
    
    return(mainComponent)
    
}


# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

edgesBiogrid <- read.table("resources/networks/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

edgesReactome <- read.table("resources/networks/reactome/proteinInternalEdges.tsv.gz", header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)
edgesReactome <- edgesReactome[, c("id1", "id2")]


## Make graphs

print(paste(Sys.time(), " Making graphs", sep = ""))

graphReactome <- graph_from_data_frame(edgesReactome)
graphBiogrid <- graph_from_data_frame(edgesBiogrid)


## Simplify graphs

print(paste(Sys.time(), " Simplifying graphs", sep = ""))

graphReactome <- simplify(graphReactome, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")
graphBiogrid <- simplify(graphBiogrid, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")


## Merge Reactome and Biogrid

print(paste(Sys.time(), " Merging Reactome and Biogrid", sep = ""))

graphMerged <- mergeGraphs(graphReactome, graphBiogrid)


## Extract main component

print(paste(Sys.time(), " Extracting main component", sep = ""))

graphReactome <- getMainComponent(graphReactome)
graphBiogrid <- getMainComponent(graphBiogrid)
graphMerged <- getMainComponent(graphMerged)


## Get degrees

print(paste(Sys.time(), " Getting degrees", sep = ""))

degreeReactome <- degree(graphReactome)
degreeBiogrid <- degree(graphBiogrid)
degreeMerged <- degree(graphMerged)

locusDF$degreeReactome <- degreeReactome[locusDF$protein]
locusDF$degreeBiogrid <- degreeBiogrid[locusDF$protein]
locusDF$degreeMerged <- degreeMerged[locusDF$protein]

allLoci <- unique(locusDF$locus)
foundLoci <- locusDF$locus[!is.na(locusDF$degreeReactome) | !is.na(locusDF$degreeBiogrid) | !is.na(locusDF$degreeMerged)]
missing <- allLoci[! allLoci %in% foundLoci]

if (length(missing) > 0) {
    
    print(paste0("The following loci had no connections in the graphs: ", paste(missing, collapse = ", "), "."))
    
}

write.table(locusDF, "results/locusDF", row.names = F, col.names = T, sep = "\t", quote = F)


## Set weights

print(paste(Sys.time(), " Setting weights", sep = ""))

reactomeDF <- as_data_frame(graphReactome)
reactomeEdgesDegree <- degreeReactome[reactomeDF$from] + degreeReactome[reactomeDF$to] - 1
reactomeP <- 1 / reactomeEdgesDegree
reactomePLog <- -log10(reactomeP)
E(graphReactome)$weight <- reactomePLog

biogridDF <- as_data_frame(graphBiogrid)
biogridEdgesDegree <- degreeBiogrid[biogridDF$from] + degreeBiogrid[biogridDF$to] - 1
biogridP <- 1 / biogridEdgesDegree
biogridPLog <- -log10(biogridP)
E(graphBiogrid)$weight <- biogridPLog

mergedDF <- as_data_frame(graphMerged)
mergedEdgesDegree <- degreeMerged[mergedDF$from] + degreeMerged[mergedDF$to] - 1
mergedP <- 1 / mergedEdgesDegree
mergedPLog <- -log10(mergedP)
E(graphMerged)$weight <- mergedPLog


## Get distances

if (computeReactome) {
    
    distancesMatrix <- distances(graph = graphReactome, weights = NA)
    matrixFile <- paste0(simpleFile, "_Reactome.gz")
    write.table(distancesMatrix, gzfile(matrixFile), quote = F, col.names = T, row.names = T)
    
    distancesMatrix <- distances(graph = graphReactome, weights = NULL)
    matrixFile <- paste0(dsdFile, "_Reactome.gz")
    write.table(distancesMatrix, gzfile(matrixFile), quote = F, col.names = T, row.names = T)
    
}

if (computeBiogrid) {
    
    distancesMatrix <- distances(graph = graphBiogrid, weights = NA)
    matrixFile <- paste0(simpleFile, "_Biogrid.gz")
    write.table(distancesMatrix, gzfile(matrixFile), quote = F, col.names = T, row.names = T)
    
    distancesMatrix <- distances(graph = graphBiogrid, weights = NULL)
    matrixFile <- paste0(dsdFile, "_Biogrid.gz")
    write.table(distancesMatrix, gzfile(matrixFile), quote = F, col.names = T, row.names = T)
    
}

if (computeMerged) {
    
    distancesMatrix <- distances(graph = graphMerged, weights = NA)
    matrixFile <- paste0(simpleFile, "_Merged.gz")
    write.table(distancesMatrix, gzfile(matrixFile), quote = F, col.names = T, row.names = T)
    
    distancesMatrix <- distances(graph = graphMerged, weights = NULL)
    matrixFile <- paste0(dsdFile, "_Merged.gz")
    write.table(distancesMatrix, gzfile(matrixFile), quote = F, col.names = T, row.names = T)
    
}

