# Verify the general characteristics of the protein and proteoform networks
# Notice that they are handled as undirected graphs.

library(igraph)
source("graphs.R")

#' Gets sample points for a percolation curve of a graph
#' 
#' Tries different sizes of random subgraphs to check the completeness 
#' and relative size of the largest connected component
#' 
#' @param graph The graph in igraph format
#' @param measures Number of sizes of subgraphs to sample. The size is given by the number of edges
#' @param replicates Number of replicate measurements for each size
#' 
#' @return A data frame with three columns: Sizes, Completeness and RelativeSizeLCC
#' Sizes: the number of edges for the different subgraphs tested
#' Completeness: The product of fractions of vertices and edges in the subgraphs with respect to the original graph
#' RelativeSizeLCC: The quotient of the number of edges in the subgraphs and the number of edges in the original graph
#' 
getPercolationCurvePoints <- function(graph, measures, replicates, verbose = FALSE) {
  
  sizes <- as.integer(seq(gsize(graph), 0, length.out = measures))              # Create the sequence of subgraph sizes
  names <- c("Sizes", "Completeness", "RelativeSizeLCC")
  
  # Create an empty result data frame
  samples <- data.frame(
    Sizes=integer(),
    Completeness=double(),
    RelativeSizeLCC=double()
  )
  
  # Sample all sizes for each replicate
  for(r in 1:replicates) {
    print(paste("***** Replicate: ", r, " *****"))
    cat("Sizes: ")
    sg <- graph                                                     # Create a copy of the original graph as a subgraph sg
    if(verbose) print(sg)
    for (s in sizes) {                                              # Sample for different sizes of subgraphs
      cat(s, " ")
      sg <- removeNRandomEdges(sg, gsize(sg) - s)                   # Reduce the subgraph sg to the desired size
      if(verbose) print(sg)
      
      completeness <- getCompleteness(graph, subgraph = sg)         # Calculate completeness of the subgraph
      
      lcc <- getLargestConnectedComponent(sg)                        #ss Calculate relative size of the largest connected component
      if(verbose) cat("|E(g)| = ", gsize(graph), ",|E(lcc)| = ", gsize(lcc), "\n")
      relativeSizeLCC <- getRelativeSize(graph, subgraph = lcc) 
      
      if(verbose) print(paste(s, completeness, relativeSizeLCC, sep = "\t"))
      currentSample <- data.frame(s, completeness, relativeSizeLCC)
      names(currentSample) <- names
      samples <- rbind(samples, currentSample)                        # Add current sample to the dataframe of results
    }
    cat("\n")
  }
  
  #samples$Sizes <- as.numeric(samples$Sizes)
  return(samples)
}

# Load undirected graphs

pG <- loadGraph("all/1.8.1/proteinInternalEdges.tsv.gz")
mG <- loadGraph("all/1.8.1/proteoformInternalEdges.tsv.gz")

# Calculate the size of the largest component
plcs <- length(component_distribution(pG)) -1
mlcs <- length(component_distribution(mG)) -1

# Example to get the largest connectec component
g <- simplify(
  graph.compose(
    graph.ring(10), 
    graph.star(5, mode = "undirected")
  )
) + edge("7", "8")

plot(g)

# Repeat 10 times all the sampling


samples <- getPercolationCurvePoints(graph = g, measures = 10, replicates = 10)
