library(igraph)
library(data.table)

source("graphs.R") 

# Library for percolation theory studies on pathway networks

GetMoment <- function(moment = 1, graph) {
  
  #' Get mth moment for the degree distribution of the graph.
  #' 
  #' Args:
  #'   moment: number of moment. Default is 1
  #'   graph: igraph object
  #' 
  #' Returns:
  #'   Double value of the mth moment
  
  degreeSequence <- degree(graph, v = V(graph), mode = c("all", "out", "in", "total"), loops = FALSE, normalized = FALSE)
  
  result = sum(sapply(as.data.frame.vector(degreeSequence), function (x) x**moment))/gorder(graph)
  
  return(result)
}

# TODO(LuisFranciscoHS): Get the right formula and update the code
GetPercolationThreshold <- function(graph) {
  
  #' Get percolation threshold probability (link percolation) 
  #' 
  #' It is calculated using the derivative of the quotiend of the first moment, and the difference between the second moment and the first. 
  #' The moments are calculated over the degree sequence of the graph.
  #' Only link removal is considered here, therefore nodes with degree 0 are allowed in the calculations. 
  #' 
  #' Args:
  #'   graph: igraph object
  #'  
  #' Returns: 
  #'   Double value of the percolation threshold probability
  #' 
  
  firstMoment <- GetMoment(1, graph)
  secondMoment <- GetMoment(2, graph)
  
  return(firstMoment/(secondMoment - firstMoment))
}

GetPT <- function (graph) {
  
  #' Get percolation threshold probability (node percolation background)
  #'
  #' Taking the shortcut on the bottom of the page.  There it says that
  #' <k> and <k^2> are first and second moments of the degree
  #' distribution. In statistics interpretation first and second moments are called mean and
  #' variance.
  #' 
  #' Args:
  #'   graph: igraph object
  #' 
  #' Returns:
  #'   Double value Percolation threshold
  #'
  
  degdist <- degree(graph, loops = FALSE)
  degdist <- degdist[degdist > 0]
  mean(degdist)/(var(degdist) - mean(degdist))
}

GetPercolationThresholdSimplified <- function(graph) {
  
  #' Get percolation threshold probability approximation for graphs with arbitrary degree distribution
  #' 
  #' @param graph igraph object
  #' @return Double value of the percolation threshold probability
  #'
  
  return(1/gsize(graph))
}

GetCompleteness <- function(graph, subgraph) {
 
  #' Get completeness of a subgraph with respect to a graph
  #' 
  #' Calculates the completeness as the ratio of the fraction of vertices in the 
  #' subgraph times the fraction of edges in the subgraph with respect to the
  #' original graph
  #' 
  #' Args:
  #'   graph: the complete graph as an igraph object
  #'   subgraph: the subgraph of the complete graph as an igraph
  #'
  #' Returns:
  #'   Double value of the completeness
   
  verticesFraction <- gorder(subgraph) / gorder(graph)
  edgesFraction <- gsize(subgraph) / gsize(graph)
  
  return(verticesFraction * edgesFraction)
}

GetRelativeSize <- function(graph, subgraph) {

  #' Get relative size of a subgraph with respect to a graph
  #' 
  #' Args:
  #'   graph: the complete graph as an igraph object
  #'   subgraph: the subgraph of the complete graph as an igraph
  #'
  #' Returns:
  #'   Double value of the relative size
    
  return(gsize(subgraph) / gsize(graph))
}

GetNodePercolationCurvePoints <- function(graph, 
                                          measures, 
                                          replicates, 
                                          entity = "Unknown",
                                          DefineSizes = function(graph, measures) as.integer(seq(gorder(graph), 0, length.out = measures))) {
  
  #' Gets sample points for a percolation curve of a graph
  #' 
  #' For different sizes of random subgraphs to check the completeness 
  #' and relative size of the largest connected component
  #' 
  #' Args:
  #'   graph: The graph in igraph format
  #'   measures: Number of sizes of subgraphs to sample. The size is given by the number of edges
  #'   replicates: Number of replicate measurements for each size
  #' 
  #' Returns:
  #'   Data frame with three columns: Sizes, Completeness and RelativeSizeLCC
  #'     Sizes: the number of edges for the different subgraphs tested
  #'     Completeness: The product of fractions of vertices and edges in the subgraphs with respect to the original graph
  #'     RelativeSizeLCC: The quotient of the number of edges in the subgraphs and the number of edges in the original graph
  #' 
  
  sizes <- DefineSizes(graph, measures)              # Create the sequence of subgraph sizes
  
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
      sg <- RemoveNVertices(sg, gorder(sg) - s)                   # Reduce the subgraph sg to the desired size
      cat("Graph reduced to order: ", s, "\n")
      
      completeness <- (gorder(sg) / gorder(graph)) * (gsize(sg) / gsize(graph))
      relativeSizeLCC <- gsize(GetLCC(sg)) / gsize(graph)
      #cat("|E(graph)| = ", gsize(graph), ",|E(lcc)| = ", gsize(lcc), "\n")
      
      samples <- rbind(samples, c(gorder(sg), completeness, relativeSizeLCC))                  
    }
    cat("\n")
  }
  
  names(samples) <- c("Order", "Completeness", "RelativeSizeLCC")
  samples$Entity <- entity
  
  return(samples)
}

GetLog2Bins <- function(graph, measures = 1) {
  
  #' Get bin sizes dividing by two at each step.
  #' 
  #' Args:
  #'   graph: igraph object with the graph
  #'   measures: this argument is ignored, but needed in the plotting function
  #'   
  #' Returns:
  #'   vector with subsizes of the graph dividing by two until it is not possible
  
  bins.number <- ceiling(log2(gorder(graph)))
  
  bins.sizes <- integer(bins.number)
  max.size <- gorder(graph)
  
  for (i in 1:bins.number) {
    bins.sizes[i] <- max.size
    max.size <- ceiling(max.size / 2)
  }
  
  return(bins.sizes)
}

GetLinkPercolationCurvePoints <- function(graph, 
                                          measures, 
                                          replicates, 
                                          entity = "Unknown",
                                          DefineSizes = function(graph, measures = 1) as.integer(seq(gsize(graph), 0, length.out = measures))) {
  
  #' Gets sample points for a node percolation curve of a graph
  #' 
  #' Tries different sizes of random subgraphs to check the completeness 
  #' and relative size of the largest connected component
  #' 
  #' Args:
  #'  graph: The graph in igraph format
  #'  measures: Number of sizes of subgraphs to sample. The size is given by the number of edges
  #'  replicates: Number of replicate measurements for each size
  #' 
  #' Returns:
  #'   Data frame with three columns: Sizes, Completeness and RelativeSizeLCC
  #'     Sizes: the number of edges for the different subgraphs tested
  #'     Completeness: The product of fractions of vertices and edges in the subgraphs with respect to the original graph
  #'     RelativeSizeLCC: The quotient of the number of edges in the subgraphs and the number of edges in the original graph
  #' 
  
  sizes <- DefineSizes(graph, measures)              # Create the sequence of subgraph sizes
  
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
      sg <- RemoveNEdges(sg, gsize(sg) - s)                   # Reduce the subgraph sg to the desired size
      cat("Graph reduced to size: ", gsize(sg), "\n")
      
      completeness <- (gorder(sg) / gorder(graph)) * (gsize(sg) / gsize(graph))
      relativeSizeLCC <- gsize(GetLCC(sg)) / gsize(graph)
      #cat("|E(graph)| = ", gsize(graph), ",|E(lcc)| = ", gsize(lcc), "\n")

      samples <- rbind(samples, c(s, completeness, relativeSizeLCC))                  
    }
    cat("\n")
  }
  
  names(samples) <- c("Sizes", "Completeness", "RelativeSizeLCC")
  samples$Entity <- entity
  
  return(samples)
}

PlotLinkPercolationCurve <- function(samples, colors = c("blue3", "green3", "red3")) {
  
  #' Make percolation curve plot using point samples
  #' 
  #' Plots completeness (x-axis) vs relative size of the largest connected component (y-axis)
  #' Adds an adjustment curve with standard error for the points.
  #' 
  #' Args:
  #'  samples Data frame with three columns: Sizes(int), Completeness(num) and RelativeSizeLCC(num)
  #' 
  #' Returns:
  #'   ggplot2 object of the plot
  #' 
  
  theme_set(theme_bw())
  
  means <- aggregate(.~Sizes+Entity, samples, FUN = mean)
  
  p <- ggplot2::ggplot() + 
    geom_point(data = samples, aes(x=Completeness, y=RelativeSizeLCC, color = Entity)) +
    geom_line(data = means, aes(x=Completeness, y=RelativeSizeLCC, color = Entity)) +
    scale_color_manual(values = colors) +
#    scale_x_continuous("Size", trans = "log2", breaks = unique(samples$Sizes)) +
#    scale_y_continuous("Relative Size of LCC", trans = "log2") +
    ggtitle("Percolation curve approximation") +
    theme(axis.text.x = element_text(angle = 90, hjust = 1))
  return(p)
}

PlotNodePercolationCurve <- function(samples, colors = c("blue3", "green3", "red3")) {
  
  #' Make percolation curve plot using point samples
  #' 
  #' Plots completeness (x-axis) vs relative size of the largest connected component (y-axis)
  #' Adds an adjustment curve with standard error for the points.
  #' 
  #' Args:
  #'  samples Data frame with three columns: Sizes(int), Completeness(num) and RelativeSizeLCC(num)
  #' 
  #' Returns:
  #'   ggplot2 object of the plot
  #' 
  
  theme_set(theme_bw())
  
  means <- aggregate(.~Order+Entity, samples, FUN = mean)
  
  p <- ggplot2::ggplot() + 
    geom_point(data = samples, aes(x=Completeness, y=RelativeSizeLCC, color = Entity)) +
    geom_line(data = means, aes(x=Completeness, y=RelativeSizeLCC, color = Entity)) +
    scale_color_manual(values = colors) +
    #    scale_x_continuous("Size", trans = "log2", breaks = unique(samples$Sizes)) +
    #    scale_y_continuous("Relative Size of LCC", trans = "log2") +
    ggtitle("Percolation curve approximation") +
    theme(axis.text.x = element_text(angle = 90, hjust = 1))
  return(p)
}

GetPercolationExtraData <- function(graph, measures, replicates, verbose = TRUE) {
  
  #' Gets sample points for a percolation curve of a graph
  #'
  #' Tries different sizes of random subgraphs to check the completeness
  #' and relative size of the largest connected component
  #'
  #' Args:
  #'  graph: The graph in igraph format
  #'  measures: Number of sizes of subgraphs to sample. The size is given by the number of edges
  #'  replicates: Number of replicate measurements for each size
  #'
  #' Returns:
  #'   Data table with columns nVertices, nEdges, subPT, subPT2,
  #'     lcc, completeness, and relativeLCC. nVertices, nEdges, and lcc
  #'     are absolute measurements of the sampled subgraph;
  #'     completeness, and relativeLCC are relative measures of sampled
  #'     subgraph vs the graph they were sampled from. subPT and subPT2
  #'     are ... whatever they are. (subPT is done with LFHS code,
  #'     subPT2 with BB code)
  #'
  
  ## Create the sequence of subgraph sizes
  ## subgraph the size of original is not a(n interesting) subgraph
  ## graph of size 0 is not interesting
  sizes <- as.integer(seq(gsize(graph)-1, 1, length.out = measures))
  
  samples <- rbindlist(lapply(sizes, function (s) {
    
    if(verbose)
      cat("Size: ", s)
    
    subVals <- replicate(replicates, GetMeasuresExtended(graph, s))
    
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

GetMeasures <- function (graph, size) {
  list(subOrder = sum(degree(graph) > 0),
       lcc = gorder(GetLCC(graph)))
}

GetMeasuresExtended <- function (graph, size) {
  sg <- RemoveNEdges(graph, gsize(graph) - size)
  list(subOrder = sum(degree(sg) > 0),
       subPT = GetPercolationThreshold(sg),
       subPT2 = GetPT(sg),
       lcc = gorder(GetLCC(sg)))
}