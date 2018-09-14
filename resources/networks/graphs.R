# Common functions to handle igraphs

#' Loads an igraph file into an object
#' 
#' @param file path and name to the file
#' 
#' @return igraph object of the graph writen in the file
LoadGraph <- function(file) {
  
  print(paste(Sys.time(), " Loading data from: ", file, sep = ""))
  table <- read.table(file, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)
  G <- graph_from_data_frame(table)
  
  return(G)
}

#' Get largest connected component of a graph
#' 
#' @param graph the entire graph igraph object
#' 
#' @return igraph object of the largest connected component
GetLCC_deprecated <- function(graph) {
  
  components <- components(graph)
  
  lcc <- induced.subgraph(graph, V(graph)[which(components$membership == which.max(components$csize))])
  
  return(lcc)
}


GetLcc <- function(graph) {
  
  # Get largest connected component of a graph (alternative implementation) ----
  # 
  # Args:
  #   graph: the entire graph igraph object
  # 
  # Returns: 
  #   igraph object of the largest connected component
  # ----
  
  components <- decompose(graph)
  largest <- which.max(sapply(components, gsize))
  
  return(components[[largest]])
}

#' Removes n random edges from the graph in igraph format
#' 
#' @param graph the complete graph as an igraph object
#' @param n number of edges to remove
#'
#' @return igraph object of the subgraph resulting from removing the n random edges
RemoveNEdges <- function(graph, n = 1) {
  
  if(gsize(graph) < n)
      n <- gsize(graph)
  
  if(n >= 1){
      indexes <- sample(1:gsize(graph), n, replace = F)
      graph <- delete.edges(graph, E(graph)[indexes])
  }
  
  return(graph)
}

#' Removes n random vertices from the graph in igraph format
#' 
#' @param graph the complete graph as an igraph object
#' @param n number of edges to remove
#'
#' @return igraph object of the subgraph resulting from removing the n random edges
RemoveNVertices <- function(graph, n = 1) {
  
  if(gorder(graph) < n)
    n <- gorder(graph)
  
  if(n >= 1){
    indexes <- sample(1:gorder(graph), n, replace = F)
    graph <- delete.vertices(graph, V(graph)[indexes])
  }
  
  return(graph)
}

GetBinnedDegreeProbabilities <- function(degrees) {
  # Get data frame with log2 binned degree probabilities for a degree sequence.
  # 
  # Args:
  #   degrees: vector of integers with degree sequence of a graph
  #   
  # Returns:
  #   Data frame containing log2 binned numeric degree probabilities for the 
  #   given degrees
    
  bin <- 0
  degreesBinned <- c()
  ps <- c()
  
  maxDegree <- max(degrees)
  
  while(T) {
    
    bi <- 2 ^ bin     # 1, 2, 4, 8 ...
    biPlusOne <- bi * 2 # 2, 4, 8, 16...
    degree <- mean(bi:(biPlusOne-1))
    
    if (biPlusOne > maxDegree) {
      break()
    }
    
    # Average all measures in interval [bi, biPlusOne]
    pi <- sum(degrees >= bi & degrees < biPlusOne) / (biPlusOne - bi) 
    
    if (pi > 0) {
      ps <- c(ps, pi)
      degreesBinned <- c(degreesBinned, degree)
    }
    
    bin <- bin + 1
  }
  
  ps <- ps / length(degrees)
  
  degreeDF <- data.frame(degree = degreesBinned, p = ps)
  
  return(degreeDF)
  
}