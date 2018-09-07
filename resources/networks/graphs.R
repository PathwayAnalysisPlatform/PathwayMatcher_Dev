# Common functions to handle graphs

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

#' Raise base number to the power exponent
#' 
#' @param base number to be raised
#' @param exponent the power to raise the base number
#' 
#' @return Integer number of the base raised to the power exponent
pow <- function(base, exponent) {
  result = 1
  for (i in 1:exponent) {
    result = result * base
  }
  return(result)
}

#' Get mth moment for the degree distribution of a graph
#' 
#' @param moment number of moment
#' @param graph igraph object
#' 
#' @return Double value of the mth moment
getMoment <- function(moment = 1, graph){
  
  degreeSequence <- degree(graph, v = V(graph), mode = c("all", "out", "in", "total"), loops = FALSE, normalized = FALSE)
  
  result = (sum(sapply(as.data.frame.vector(degreeSequence), pow, exponent=moment)))/gorder(proteinGraph)
  
  return(result)
}

#' Get percolation threshold probability for complete random graphs
#' 
#' @param graph igraph object
#' @return Double value of the percolation threshold probability
#' 
getPercolationThreshold <- function(graph) {
  firstMoment <- getMoment(1, graph)
  secondMoment <- getMoment(2, graph)
  return(firstMoment/(secondMoment - firstMoment))
}

#' Get percolation threshold probability for graphs with arbitrary degree distribution
#' 
#' @param graph igraph object
#' @return Double value of the percolation threshold probability
#' 
getPercolationThresholdSimplified <- function(graph) {
  return(1/gsize(graph))
}

#' Get largest connected component of a graph
#' 
#' @param graph the entire igraph object
#' 
#' @return igraph object of the largest connected component
getLargestConnectedComponent <- function(graph) {
  
  components <- components(graph)
  
  lcc <- induced.subgraph(g, V(g)[which(components$membership == which.max(components$csize))])
  
  return(lcc)
  
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

#' Removes n random edges from the graph in igraph format
#' 
#' @param graph the complete graph as an igraph object
#' @param n number of edges to remove
#'
#' @return igraph object of the subgraph resulting from removing the n random edges
removeNRandomEdges <- function(graph, n = 1) {
  
  subgraph <- graph
  
  if(n >= 1){
    for(I in 1:n) {
      if(gsize(subgraph) == 0)
        break
      index <- sample(1:gsize(subgraph),1)
      subgraph <- delete.edges(subgraph, E(subgraph)[index])
    }    
  }
  
  return(subgraph)
}