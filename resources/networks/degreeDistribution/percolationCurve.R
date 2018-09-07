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
#' @return Data frame with three columns: Sizes, Completeness and RelativeSizeLCC
#' Sizes: the number of edges for the different subgraphs tested
#' Completeness: The product of fractions of vertices and edges in the subgraphs with respect to the original graph
#' RelativeSizeLCC: The quotient of the number of edges in the subgraphs and the number of edges in the original graph
#' 
getPercolationCurvePoints <- function(graph, measures, replicates) {
  
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

    for (s in sizes) {                                              # Sample for different sizes of subgraphs
      cat("\n***** ", s, " *****\n\n")
      sg <- removeNRandomEdges(sg, gsize(sg) - s)                   # Reduce the subgraph sg to the desired size
      cat("Subgraph reduced to size: ", gsize(sg), "\n")
      
      completeness <- getCompleteness(graph, subgraph = sg)         # Calculate completeness of the subgraph
      cat("Calculated completeness\n")
      lcc <- getLargestConnectedComponent(sg)                        #ss Calculate relative size of the largest connected component
      cat("Calculated the largest connected component\n")
      cat("|E(graph)| = ", gsize(graph), ",|E(lcc)| = ", gsize(lcc), "\n")
      relativeSizeLCC <- getRelativeSize(graph, subgraph = lcc) 
      cat("Calculated relativeSizeLCC\n")
      
      currentSample <- data.frame(s, completeness, relativeSizeLCC)
      names(currentSample) <- names
      samples <- rbind(samples, currentSample)                        # Add current sample to the dataframe of results
      cat("Added to dataframe\n")
    }
    cat("\n")
  }
  
  #samples$Sizes <- as.numeric(samples$Sizes)
  return(samples)
}

#' Make percolation curve plot using point samples
#' 
#' @description Plots completeness (x-axis) vs relative size of the largest connected component (y-axis)
#' Adds an adjustment curve with standard error for the points.
#' 
#' @param samples Data frame with three columns: Sizes(int), Completeness(num) and RelativeSizeLCC(num)
#' 
#' @return ggplot2 object of the plot
#' 
plotPercolationCurve <- function(samples) {
  p <- ggplot2::ggplot(samples, aes(x=Completeness, y=RelativeSizeLCC)) + 
    geom_point() +
    geom_smooth()
  return(p)
}