library(igraph)
library(data.table)

source("graphs.R") 

# Library for percolation theory studies on pathway networks

#' Get mth moment for the degree distribution of the graph
#' 
#' @param moment number of moment
#' @param graph igraph object
#' 
#' @return Double value of the mth moment
getMoment <- function(moment = 1, graph) {
  
  degreeSequence <- degree(graph, v = V(graph), mode = c("all", "out", "in", "total"), loops = FALSE, normalized = FALSE)
  
  result = sum(sapply(as.data.frame.vector(degreeSequence), function (x) x**moment))/gorder(graph)
  
  return(result)
}

#' Get percolation threshold probability (link percolation) (Incomplete)
#' 
#' It is calculated using the derivative of the quotiend of the first moment, and the difference between the second moment and the first. 
#' The moments are calculated over the degree sequence of the graph.
#' Only link removal is considered here, therefore nodes with degree 0 are allowed in the calculations. 
#' 
#' @param graph igraph object
#' @return Double value of the percolation threshold probability
#' 
getPercolationThreshold <- function(graph) {
  
  firstMoment <- getMoment(1, graph)
  secondMoment <- getMoment(2, graph)
  
  return(firstMoment/(secondMoment - firstMoment))
}

#' Get percolation threshold probability (node percolation background)
#'
#' Taking the shortcut on the bottom of the page.  There it says that
#' <k> and <k^2> are first and second moments of the degree
#' distribution. In statistics interpretation first and second moments are called mean and
#' variance.
#'
#' @param graph igraph object
#' @return Double value Percolation threshold
#'
getPT <- function (graph) {
  degdist <- degree(graph, loops = FALSE)
  degdist <- degdist[degdist > 0]
  mean(degdist)/(var(degdist) - mean(degdist))
}

#' Get percolation threshold probability approximation for graphs with arbitrary degree distribution
#' 
#' @param graph igraph object
#' @return Double value of the percolation threshold probability
#' 
getPercolationThresholdSimplified <- function(graph) {
  return(1/gsize(graph))
}

#' Get completeness of a subgraph with respect to a graph
#' 
#' Calculates the completeness as the ratio of the fraction of vertices in the 
#' subgraph times the fraction of edges in the subgraph with respect to the
#' original graph
#' 
#' @param graph the complete graph as an igraph object
#' @param subgraph the subgraph of the complete graph as an igraph
#'
#' @return Double value of the completeness
getCompleteness <- function(graph, subgraph) {
  
  verticesFraction <- gorder(subgraph) / gorder(graph)
  edgesFraction <- gsize(subgraph) / gsize(graph)
  
  return(verticesFraction * edgesFraction)
}

#' Get relative size of a subgraph with respect to a graph
#' 
#' @param graph the complete graph as an igraph object
#' @param subgraph the subgraph of the complete graph as an igraph
#'
#' @return Double value of the relative size
getRelativeSize <- function(graph, subgraph) {
  
  return(gsize(subgraph) / gsize(graph))
}

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

#' Gets sample points for a percolation curve of a graph
#'
#' Tries different sizes of random subgraphs to check the completeness
#' and relative size of the largest connected component
#'
#' @param graph The graph in igraph format
#' @param measures Number of sizes of subgraphs to sample. The size is given by the number of edges
#' @param replicates Number of replicate measurements for each size
#'
#' @return Data table with columns nVertices, nEdges, subPT, subPT2,
#'     lcc, completeness, and relativeLCC. nVertices, nEdges, and lcc
#'     are absolute measurements of the sampled subgraph;
#'     completeness, and relativeLCC are relative measures of sampled
#'     subgraph vs the graph they were sampled from. subPT and subPT2
#'     are ... whatever they are. (subPT is done with LFHS code,
#'     subPT2 with BB code)
#'
getPercolationData <- function(graph, measures, replicates, verbose = TRUE) {
  
  ## Create the sequence of subgraph sizes
  ## subgraph the size of original is not a(n interesting) subgraph
  ## graph of size 0 is not interesting
  sizes <- as.integer(seq(gsize(graph)-1, 1, length.out = measures))
  
  samples <- rbindlist(lapply(sizes, function (s) {
    
    if(verbose)
      cat("Size: ", s)
    
    subVals <- replicate(replicates, getMeasures(graph, s))
    
    data.table(nVertices = unlist(subVals["subOrder", ]),
               nEdges = s,
               subPT = unlist(subVals["subPT", ]),
               subPT2 = unlist(subVals["subPT2", ]),
               lcc = unlist(subVals["lcc", ]))
    
  }))
  totalV <- gorder(graph)
  totalE <- gsize(graph)
  samples[, `:=` (completeness = (nVertices/totalV)*(nEdges/totalE),
                  relativeLCC = lcc/totalV)]
  
  samples
}

getMeasures <- function (graph, size) {
  sg <- removeNRandomEdges(graph, gsize(graph) - size)
  list(subOrder = sum(degree(sg) > 0),
       subPT = getPercolationThreshold(sg),
       subPT2 = getPT(sg),
       lcc = gorder(getLargestConnectedComponent(sg)))
}