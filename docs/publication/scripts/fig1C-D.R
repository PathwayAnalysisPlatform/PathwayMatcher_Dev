# 
# This script extracts the share of proteoforms per pathway.
#
startTimeAll <- proc.time()


# Libraries

library(ggplot2)
library(igraph)
library(ggrepel)


# Parameters

## Input files

proteoformEdgesFile <- "resources/networks/all/1.8.1/proteoformInternalEdges.tsv.gz"
accessionsEdgesFile <- "resources/networks/all/1.8.1/proteinInternalEdges.tsv.gz"

## Plot theme

theme_set(theme_bw(base_size = 11))


## Colors

palette <- 'cork'
accessionColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
proteoformColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)




# Functions

#' Returns the accession corresponding to a proteoform.
#' 
#' @param proteoform the proteoform identifier
#' 
#' @return the protein accession of the proteoform
getAccession <- function(proteoform) {
    
    accession <- substr(proteoform, start = 1, stop = regexpr(proteoform, pattern = ";")-1)
    
    indexDash <- regexpr(accession, pattern = "-")
    
    if (indexDash > 1) {
        
        accession <- substr(accession, start = 1, stop = indexDash - 1)
        
    }
    
    return(accession)
    
}

#' Returns the modifications found in a proteoform.
#' 
#' @param proteoform the proteoform identifier
#' 
#' @return the modifications found in a proteoform
getModifications <- function(proteoform) {
    
    modifications <- substr(proteoform, start = regexpr(proteoform, pattern = ";")+1, stop = 100000)
    split1 <- strsplit(x = modifications, ",")
    modifications <- substr(split1[[1]], start = 1, stop = regexpr(split1[[1]], pattern = ":")-1)
    
    return(unique(modifications))
    
}


# Main script
 
## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

edgesProteoforms <- read.table(proteoformEdgesFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)
edgesAccessions <- read.table(accessionsEdgesFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)


## Format

edgesProteoforms <- edgesProteoforms[, c("id1", "id2")]
edgesAccessions <- edgesAccessions[, c("id1", "id2")]

accession1 <- sapply(X = edgesProteoforms$id1, FUN = getAccession)
accession2 <- sapply(X = edgesProteoforms$id2, FUN = getAccession)


## Proteoform and protein occurrence

allProteoforms <- unique(c(edgesProteoforms$id1, edgesProteoforms$id2))
allProteoformsAccessions <- sapply(X = allProteoforms, FUN = getAccession)

proteoformsPerAccession <- as.data.frame(table(allProteoformsAccessions))


## Select edges interacting with protein of interest

accession <- "P04637"

proteoformsExample <- unique(c(edgesProteoforms$id1[accession1 == accession], edgesProteoforms$id2[accession2 == accession]))
proteoformsExampleModifications <- c()

for (proteoform in proteoformsExample) {
    
    proteoformsExampleModifications <- c(proteoformsExampleModifications, getModifications(proteoform))
    
}

proteoformsExampleModifications <- unique(proteoformsExampleModifications)

edgesProteoformsExample <- edgesProteoforms[accession1 == accession | accession2 == accession, ]
edgesAccessionsExample <- edgesAccessions[edgesAccessions$id1 == accession | edgesAccessions$id2 == accession, ]


## Make graph

graphProteoforms <- graph_from_data_frame(edgesProteoformsExample)
graphAccessions <- graph_from_data_frame(edgesAccessionsExample)

graphProteoforms <- simplify(graphProteoforms, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")
graphAccessions <- simplify(graphAccessions, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")


## Plot graphs

layout <- layout_with_kk(graphAccessions)

verticesDF <- data.frame(name = V(graphAccessions)$name, x = layout[, 1], y = layout[, 2], stringsAsFactors = F)
verticesDF$target <- ifelse(verticesDF$name == accession, accession, "Other")
x <- verticesDF$x
names(x) <- verticesDF$name
y <- verticesDF$y
names(y) <- verticesDF$name

edgesList <- get.edgelist(graphAccessions)
edgesDF <- data.frame(name1 = edgesList[, 1], name2 = edgesList[, 2], stringsAsFactors = F)
edgesDF$x1 <- x[edgesDF$name1]
edgesDF$y1 <- y[edgesDF$name1]
edgesDF$x2 <- x[edgesDF$name2]
edgesDF$y2 <- y[edgesDF$name2]

graphPlot <- ggplot()

graphPlot <- graphPlot + geom_segment(data = edgesDF, aes(x = x1, y = y1, xend = x2, yend = y2), col = accessionColor, alpha = 0.2, size = 0.5)
graphPlot <- graphPlot + geom_point(data = verticesDF, aes(x = x, y = y, col = target, size = target, alpha = target))

graphPlot <- graphPlot + scale_color_manual(values = c(accessionColor, "red"))
graphPlot <- graphPlot + scale_size_manual(values = c(1, 2))
graphPlot <- graphPlot + scale_alpha_manual(values = c(0.5, 1))

graphPlot <- graphPlot + theme(legend.position = "none",
                               axis.ticks = element_blank(),
                               axis.text = element_blank(),
                               axis.title = element_blank(),
                               panel.grid = element_blank(),
                               panel.border = element_rect(color = "white"))


png(paste0("docs/publication/plots/fig_1C.png"), height = 12, width = 12, units = "cm", res = 600)
plot(graphPlot)
dummy <- dev.off()

layout <- layout_with_kk(graphProteoforms)

verticesDF <- data.frame(name = V(graphProteoforms)$name, x = layout[, 1], y = layout[, 2], stringsAsFactors = F)
verticesDF$accession <- sapply(X = verticesDF$name, FUN = getAccession)
verticesDF$target <- ifelse(verticesDF$accession == accession, accession, "Other")
x <- verticesDF$x
names(x) <- verticesDF$name
y <- verticesDF$y
names(y) <- verticesDF$name

edgesList <- get.edgelist(graphProteoforms)
edgesDF <- data.frame(name1 = edgesList[, 1], name2 = edgesList[, 2], stringsAsFactors = F)
edgesDF$x1 <- x[edgesDF$name1]
edgesDF$y1 <- y[edgesDF$name1]
edgesDF$x2 <- x[edgesDF$name2]
edgesDF$y2 <- y[edgesDF$name2]
edgesDF$accession1 <- sapply(X = edgesDF$name1, FUN = getAccession)
edgesDF$accession2 <- sapply(X = edgesDF$name2, FUN = getAccession)

edgesDF$target <- ifelse(edgesDF$accession1 == accession & edgesDF$accession2 == accession, accession, "Other")
edgesDF <- edgesDF[order(edgesDF$target), ]

graphPlot <- ggplot()

graphPlot <- graphPlot + geom_segment(data = edgesDF, aes(x = x1, y = y1, xend = x2, yend = y2, col = target, size = target), alpha = 0.2)
graphPlot <- graphPlot + geom_point(data = verticesDF, aes(x = x, y = y, col = target, size = target, alpha = target))

graphPlot <- graphPlot + scale_color_manual(values = c(proteoformColor, "red"))
graphPlot <- graphPlot + scale_size_manual(values = c(1, 2))
graphPlot <- graphPlot + scale_alpha_manual(values = c(0.5, 1))

graphPlot <- graphPlot + theme(legend.position = "none",
                               axis.ticks = element_blank(),
                               axis.text = element_blank(),
                               axis.title = element_blank(),
                               panel.grid = element_blank(),
                               panel.border = element_rect(color = "white"))


png(paste0("docs/publication/plots/fig_1D.png"), height = 12, width = 12, units = "cm", res = 600)
plot(graphPlot)
dummy <- dev.off()

