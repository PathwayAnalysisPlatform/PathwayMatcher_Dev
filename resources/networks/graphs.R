# Common functions to handle graphs

#' Loads an igraph file into an object
#' 
#' @param file path and name to the file
#' @return igraph object
#' 
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
#' @return base raised to the power exponent
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
getMoment <- function(moment = 1, graph){
  
  degreeSequence <- degree(graph, v = V(graph), mode = c("all", "out", "in", "total"), loops = FALSE, normalized = FALSE)
  
  result = (sum(sapply(as.data.frame.vector(degreeSequence), pow, exponent=moment)))/gorder(proteinGraph)
  
  return(result)
}

#' Get percolation threshold probability for complete random graphs
#' 
#' @param graph igraph object
#' @return percolation threshold probability
#' 
getPercolationThreshold <- function(graph) {
  firstMoment <- getMoment(1, graph)
  secondMoment <- getMoment(2, graph)
  return(firstMoment/(secondMoment - firstMoment))
}

#' Get percolation threshold probability for graphs with arbitrary degree distribution
#' 
#' @param graph igraph object
#' @return percolation threshold probability
#' 
getPercolationThresholdSimplified <- function(graph) {
  return(1/gsize(graph))
}