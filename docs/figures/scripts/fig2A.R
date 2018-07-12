# 
# This script makes the figure 2A of the manuscript.
#
startTimeAll <- proc.time()


# Libraries

library(ggplot2)
library(ggrepel)
library(igraph)
library(scico)


# Parameters

## input files

accessionsFile <- "resources/networks/cAMP_example/proteins_output/proteinInternalEdges.tsv.gz"
proteoformsFile <- "resources/networks/cAMP_example/proteoforms_output/proteoformInternalEdges.tsv.gz"
allProteoformsFile <- "resources/networks/all/1.8.1/proteoformInternalEdges.tsv.gz"

## Colors

palette <- 'cork'
accessionColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
proteoformColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)

## Categories

matchingCategoryLevels <- c("missing" ,"possible", "common")
matchingCategoryColors <- c("red3", accessionColor, proteoformColor)


# Functions

#' Returns the accession corresponding to a proteoform.
#' 
#' @param proteoform the proteoform identifier
#' @return the protein accession of the proteoform
getAccession <- function(proteoform) {
    
    accession <- substr(proteoform, start = 1, stop = regexpr(proteoform, pattern = ";")-1)
    
    indexDash <- regexpr(accession, pattern = "-")
    
    if (indexDash > 1) {
        
        accession <- substr(accession, start = 1, stop = indexDash - 1)
        
    }
    
    return(accession)
    
}

#' Returns a vector indicating whether edges are common between accessions and proteoforms.
#' 
#' @param accessionEdges the accession edges
#' @param proteoformEdges the proteoform edges
#' @param allProteoformEdges all proteoform edges
#' 
#' @return a vector indicating whether edges are common between accessions and proteoforms
getEdgesAnnotation <- function(accessionEdges, proteoformEdges, allProteoformEdges) {
  
  annotation <- character(nrow(accessionEdges))
  
  for (i in 1:nrow(accessionEdges)) {
    
    fromI <- accessionEdges$id1[i]
    toI <- accessionEdges$id2[i]
    
    possibleProteoform <- sum((allProteoformEdges$accession1 == fromI & allProteoformEdges$accession2 == toI) | (allProteoformEdges$accession2 == fromI & allProteoformEdges$accession1 == toI)) > 0
    
    correctProteoform <- sum((proteoformEdges$accession1 == fromI & proteoformEdges$accession2 == toI) | (proteoformEdges$accession2 == fromI & proteoformEdges$accession1 == toI)) > 0
    
    if (correctProteoform) {
        
        annotation[i] <- "common"
        
    } else if (possibleProteoform) {
      
      annotation[i] <- "possible"
      
    } else {
      
      annotation[i] <- "missing"
      
    }
  }
  
  return(annotation)
  
}

#' Returns the main component of a graph.
#' 
#' @param graph the graph
#' 
#' @return the main component as a graph
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

#' Returns a ggplot object of the plotted graph.
#' 
#' @param graph the graph to plot
#' @param layout the layout to use
#' 
#' @return a ggplot object of the plotted graph
plotGraph <- function(graph, layout) {
  
  verticesDF <- data.frame(x = layout[, 1], y = layout[, 2])
  verticesDF$name <- V(graph)$name
  
  edgesList <- get.edgelist(graph)
  edgesDF <- data.frame(name1 = edgesList[, 1], name2 = edgesList[, 2])
  
  x1 <- c()
  y1 <- c()
  x2 <- c()
  y2 <- c()
  
  for (i in 1:nrow(edgesDF)) {
    
    index <- which(verticesDF$name == edgesDF$name1[i])
    
    if (length(index) == 0) {
      stop(paste0("Vertice ", edgesDF$name1[i], " not found."))
    } else if (length(index) > 1) {
      stop(paste0("Vertice ", edgesDF$name1[i], " maps to multiple vertices."))
    }
    
    x1 <- c(x1, verticesDF$x[index])
    y1 <- c(y1, verticesDF$y[index])
    
    index <- which(verticesDF$name == edgesDF$name2[i])
    
    if (length(index) == 0) {
      stop(paste0("Vertice ", edgesDF$name2[i], " not found."))
    } else if (length(index) > 1) {
      stop(paste0("Vertice ", edgesDF$name2[i], " maps to multiple vertices."))
    }
    
    x2 <- c(x2, verticesDF$x[index])
    y2 <- c(y2, verticesDF$y[index])
    
  }
  
  edgesDF$x1 <- x1
  edgesDF$y1 <- y1
  edgesDF$x2 <- x2
  edgesDF$y2 <- y2
  
  edgesDF$annotation <- E(graph)$annotation
  edgesDF$linetype <- ifelse(E(graph)$annotation == "missing", "Missing", "Annotated")
  edgesDF$linetype <- factor(edgesDF$linetype, levels = c("Missing", "Annotated"))
  
  edgesDF$annotation <- factor(edgesDF$annotation, levels = matchingCategoryLevels)
  
  edgesDF <- edgesDF[order(edgesDF$annotation), ]
  
  plot <- ggplot() + theme_bw()
  plot <- plot + geom_segment(data = edgesDF, aes(x = x1, y = y1, xend = x2, yend = y2, col = annotation, linetype = linetype), size = 0.5)
  plot <- plot + geom_point(data = verticesDF, aes(x = x, y = y), fill = "grey90", col = "black", shape = 21, size = 1)
  plot <- plot + scale_linetype_manual(values = c(3, 1))
  plot <- plot + scale_color_manual(values = matchingCategoryColors)
  plot <- plot + theme(axis.title = element_blank(),
                       axis.text = element_blank(),
                       axis.ticks = element_blank(),
                       panel.grid = element_blank(),
                       legend.position = "none")
  
  return(plot)
  
}


#' Returns the accession corresponding to a proteoform.
#' 
#' @param proteoform the proteoform identifier
#' @return the protein accession of the proteoform
getAccession <- function(proteoform) {
  
  accession <- substr(proteoform, start = 1, stop = regexpr(proteoform, pattern = ";")-1)
  
  indexDash <- regexpr(accession, pattern = "-")
  
  if (indexDash > 1) {
    
    accession <- substr(accession, start = 1, stop = indexDash - 1)
    
  }
  
  return(accession)
  
}


# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

internalEdgesAccessions <- read.table(accessionsFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)
internalEdgesProteoforms <- read.table(proteoformsFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)

allProteoformEdges <- read.table(allProteoformsFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)


## Extract accessions from proteoforms

internalEdgesProteoforms <- internalEdgesProteoforms[, c("id1", "id2")]

names(internalEdgesProteoforms) <- c("proteoform1", "proteoform2")

lengths <- sapply(internalEdgesProteoforms$proteoform1, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(internalEdgesProteoforms$proteoform1, FUN = regexpr, pattern = ';', USE.NAMES = F)
internalEdgesProteoforms$isProteoform1 <- lengths > separatorI
lengths <- sapply(internalEdgesProteoforms$proteoform2, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(internalEdgesProteoforms$proteoform2, FUN = regexpr, pattern = ';', USE.NAMES = F)
internalEdgesProteoforms$isProteoform2 <- lengths > separatorI

internalEdgesProteoforms$accession1 <- sapply(X = internalEdgesProteoforms$proteoform1, FUN = getAccession, USE.NAMES = F)
internalEdgesProteoforms$accession2 <- sapply(X = internalEdgesProteoforms$proteoform2, FUN = getAccession, USE.NAMES = F)

allProteoformEdges <- allProteoformEdges[, c("id1", "id2")]

names(allProteoformEdges) <- c("proteoform1", "proteoform2")

lengths <- sapply(allProteoformEdges$proteoform1, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(allProteoformEdges$proteoform1, FUN = regexpr, pattern = ';', USE.NAMES = F)
allProteoformEdges$isProteoform1 <- lengths > separatorI
lengths <- sapply(allProteoformEdges$proteoform2, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(allProteoformEdges$proteoform2, FUN = regexpr, pattern = ';', USE.NAMES = F)
allProteoformEdges$isProteoform2 <- lengths > separatorI

allProteoformEdges$accession1 <- sapply(X = allProteoformEdges$proteoform1, FUN = getAccession, USE.NAMES = F)
allProteoformEdges$accession2 <- sapply(X = allProteoformEdges$proteoform2, FUN = getAccession, USE.NAMES = F)

allProteoformEdges <- allProteoformEdges[allProteoformEdges$isProteoform1 | allProteoformEdges$isProteoform2, ]


## Compare proteoforms

internalEdgesAccessions$annotation <- getEdgesAnnotation(internalEdgesAccessions, internalEdgesProteoforms, allProteoformEdges)

internalEdgesAccessions <- internalEdgesAccessions[, c("id1", "id2", "annotation")]


## Make graphs

internalGraphAccessions <- graph_from_data_frame(d = internalEdgesAccessions, directed = F)
internalGraphAccessions <- simplify(internalGraphAccessions, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")

# Extract main component

internalGraphAccessionsMc <- getMainComponent(internalGraphAccessions)


## Export internal graph

print(paste(Sys.time(), " Exporting internal graph", sep = ""))

edgeColors <- factor(E(internalGraphAccessionsMc)$annotation, levels = matchingCategoryLevels)
levels(edgeColors) <- matchingCategoryColors
edgeColors <- as.character(edgeColors)

l <- layout_with_kk(internalGraphAccessionsMc)
plot <- plotGraph(internalGraphAccessionsMc, l)

png("docs/figures/plots/Fig_2A.png", height = 9, width = 8, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/figures/plots/Fig_2A.pdf", height = unit(4.5, "cm"), width = unit(4, "cm"))
plot(plot)
dummy <- dev.off()

