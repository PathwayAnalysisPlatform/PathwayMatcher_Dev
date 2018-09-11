# Common functions to handle igraphs

#' Loads an igraph file into an object
#' 
#' @param file path and name to the file
#' 
#' @return igraph object of the graph writen in the file
loadGraph <- function(file) {
  
  print(paste(Sys.time(), " Loading data from: ", file, sep = ""))
  table <- read.table(file, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)
  G <- graph_from_data_frame(table)
  
  return(G)
}

#' Get size of the largest connected component of a graph
#' 
#' @param graph igraph object of the graph
#' 
#' @return integer size of the largest connected component
getLargestConnecectComponentSize <- function(graph) {
  return(length(component_distribution(graph)) -1)
}

#' Get largest connected component of a graph
#' 
#' @param graph the entire graph igraph object
#' 
#' @return igraph object of the largest connected component
getLargestConnectedComponent <- function(graph) {
  
  components <- components(graph)
  
  lcc <- induced.subgraph(graph, V(graph)[which(components$membership == which.max(components$csize))])
  
  return(lcc)
}

#' Removes n random edges from the graph in igraph format
#' 
#' @param graph the complete graph as an igraph object
#' @param n number of edges to remove
#'
#' @return igraph object of the subgraph resulting from removing the n random edges
removeNRandomEdges <- function(graph, n = 1) {
  
  if(gsize(graph) < n)
      n <- gsize(graph)
  
  if(n >= 1){
      indexes <- sample(1:gsize(graph), n, replace = F)
      graph <- delete.edges(graph, E(graph)[indexes])
  }
  
  return(graph)
}

#' Returns a data frame containing log2 binned degree probabilities for the given degrees.
#' 
#' @param vector of integers with degree sequence of a graph
#'
#' @return data frame containing log2 binned degree probabilities for the given degrees
getDegreDF <- function(degrees) {
  
  bin <- 0
  degreesBinned <- c()
  ps <- c()
  
  maxDegree <- max(degrees)
  
  while(T) {
    
    bi <- 2 ^ bin
    biPlusOne <- 2 ^ (bin+1)
    degree <- mean(bi:(biPlusOne-1))
    
    pi <- sum(degrees >= bi & degrees < biPlusOne) / (biPlusOne - bi)
    
    if (pi > 0) {
      
      ps <- c(ps, pi)
      degreesBinned <- c(degreesBinned, degree)
      
    }
    
    if (biPlusOne > maxDegree) {
      
      break()
      
    }
    
    bin <- bin + 1
    
  }
  
  ps <- ps / length(degrees)
  
  degreeDF <- data.frame(degree = degreesBinned, p = ps)
  
  return(degreeDF)
  
}