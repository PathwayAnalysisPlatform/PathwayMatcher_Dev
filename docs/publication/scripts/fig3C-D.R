# 
# This script extracts and plots the degree distribution difference between proteins and proteoforms.
#
startTimeAll <- proc.time()


# Libraries

library(ggplot2)
library(ggrepel)
library(igraph)
library(scico)
library(gtable)
library(grid)


# Parameters

## Network files

proteoformEdgesFile <- "resources/networks/all/1.8.1/proteoformInternalEdges.tsv.gz"
accessionsEdgesFile <- "resources/networks/all/1.8.1/proteinInternalEdges.tsv.gz"

## Colors

palette <- 'cork'
accessionColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
proteoformColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)


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


#' Returns the accession corresponding to a proteoform.
#' 
#' @param proteoform the proteoform identifier
#' @return the protein accession of the proteoform
getGenes <- function(accession) {
  
  i <- which(accessionsMapping$accession == accession)
  
  return(c(accessionsMapping$id[i]))
  
}


#' Returns the degree of the protein with the given accession.
#' 
#' @param accession the protein accession
#' @param accessionNames the accession names corresponding to the degrees
#' @param accessionDegrees the degrees of the protein accessions
#' @return the degree of the protein accession
getDegreeFromAccession <- function(accession, accessionNames, accessionDegrees) {
  
  i <- which(accessionNames == accession)
  
  if (length(i) == 0) {
    
    return(0);
    
  }
  
  return(accessionDegrees[i])
  
}


#' Returns the degree of the protein with the given accession when mapping back to the genes
#' 
#' @param accession the protein accession
#' @param geneNames the gene names corresponding to the degrees
#' @param geneDegrees the degrees of the gene names
#' @return the degree of the protein accession
getDegreeFromGene <- function(accession, geneNames, geneDegrees) {
  
  degree <- 0
  
  geneMapping <- getGenes(accession)
  
  for (geneName in geneMapping) {
    
    i <- which(geneNames == geneName)
    
    if (length(i) > 0) {
      
      degree <- degree + geneDegrees[i]
      
    }
  }
  
  
  return(degree)
  
}


#' Returns a data frame containing log2 binned degree probabilities for the given degrees.
#' 
#' @param degrees the degrees
#' @return a data frame containing log2 binned degree probabilities for the given degrees
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


# Main script
 
## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

edgesProteoforms <- read.table(proteoformEdgesFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)
edgesAccessions <- read.table(accessionsEdgesFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)


## Format

edgesProteoforms <- edgesProteoforms[, c("id1", "id2")]
edgesAccessions <- edgesAccessions[, c("id1", "id2")]


## Make graph

graphProteoforms <- graph_from_data_frame(edgesProteoforms)
graphAccessions <- graph_from_data_frame(edgesAccessions)

graphProteoforms <- simplify(graphProteoforms, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")
graphAccessions <- simplify(graphAccessions, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")


## Extract proteoforms

allProteoforms <- V(graphProteoforms)$name
lengths <- sapply(allProteoforms, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(allProteoforms, FUN = regexpr, pattern = ';', USE.NAMES = F)
proteoforms <- allProteoforms[lengths > separatorI]

proteoform0 <- edgesProteoforms[! edgesProteoforms$id1 %in% proteoforms & ! edgesProteoforms$id2 %in% proteoforms, ]
proteoform1 <- edgesProteoforms[edgesProteoforms$id1 %in% proteoforms & ! edgesProteoforms$id2 %in% proteoforms
                                | ! edgesProteoforms$id1 %in% proteoforms & edgesProteoforms$id2 %in% proteoforms, ]
proteoform2 <- edgesProteoforms[edgesProteoforms$id1 %in% proteoforms & edgesProteoforms$id2 %in% proteoforms, ]

proteoform0Graph <- graph_from_data_frame(proteoform0)
proteoform1Graph <- graph_from_data_frame(proteoform1)
proteoform2Graph <- graph_from_data_frame(proteoform2)

proteoform0Graph <- simplify(proteoform0Graph, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")
proteoform1Graph <- simplify(proteoform1Graph, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")
proteoform2Graph <- simplify(proteoform2Graph, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")


## Get proteoform degree using other matchings

matchingDF <- data.frame(proteoform = V(graphProteoforms)$name, degreeProteoform = degree(graphProteoforms), stringsAsFactors = F)
lengths <- sapply(matchingDF$proteoform, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(matchingDF$proteoform, FUN = regexpr, pattern = ';', USE.NAMES = F)
proteoforms <- matchingDF$proteoform[lengths > separatorI]
matchingDF <- matchingDF[proteoforms, ]
matchingDF <- matchingDF[matchingDF$degreeProteoform > 0, ]

matchingDF$accession <- sapply(X = matchingDF$proteoform, FUN = getAccession, USE.NAMES = F)
matchingDF$degreeAccession <- sapply(X = matchingDF$accession, FUN = getDegreeFromAccession, V(graphAccessions)$name, degree(graphAccessions), USE.NAMES = F)

matchingDF$ratioAccession <- ifelse(matchingDF$degreeAccession != 0, log10(matchingDF$degreeProteoform / matchingDF$degreeAccession), NA)


## Plot degree distributions and ratios

degrees <- c(matchingDF$degreeAccession, matchingDF$degreeProteoform)
matching <- c(rep("Gene", nrow(matchingDF)), rep("Proteoform", nrow(matchingDF)))

plotDF <- data.frame(degree = degrees, matching = matching, stringsAsFactors = F)
plotDF$matching <- factor(plotDF$matching, levels = c("Gene", "Proteoform"))

degreePlot <- ggplot() + theme_bw(base_size = 11)
degreePlot <- degreePlot + geom_violin(data = plotDF, aes(x = matching, y = degree, col = matching, fill = matching), alpha = 0.5)

degreePlot <- degreePlot + scale_color_manual(values = c(accessionColor, proteoformColor))
degreePlot <- degreePlot + scale_fill_manual(values = c(accessionColor, proteoformColor))

degreePlot <- degreePlot + xlab("") + ylab("Degree")
degreePlot <- degreePlot + theme(legend.position = "none",
                                 panel.border = element_rect(color = "white"),
                                 axis.line = element_line(color = "black", size = 0.25))


ratios <- c(matchingDF$ratioAccession)
matching <- c(rep("Ratio", nrow(matchingDF)))

plotDF <- data.frame(ratio = ratios, matching = matching, stringsAsFactors = F)
plotDF$matching <- factor(plotDF$matching, levels = c("Ratio"))

ratioPlot <- ggplot() + theme_bw(base_size = 11)

ratioPlot <- ratioPlot + geom_violin(data = plotDF, aes(x = matching, y = ratio), col = "black", fill = "grey60", alpha = 0.5, na.rm = T)
ratioPlot <- ratioPlot + geom_boxplot(data = plotDF, aes(x = matching, y = ratio), col = "black", fill = NA, alpha = 0.5, na.rm = T)

ratioPlot <- ratioPlot + xlab("") + ylab("Ratio")

ratioPlot <- ratioPlot + theme(legend.position = "none",
                               panel.border = element_rect(color = "white"),
                               axis.line = element_line(color = "black", size = 0.25))

degreeGrob <- ggplotGrob(degreePlot)
ratioGrob <- ggplotGrob(ratioPlot)

plotGrob <- cbind(degreeGrob, ratioGrob, size = "first")
plotGrob$widths[14] <- unit(0.5, "null")


png("docs/publication/plots/fig_3C.png", height = 12, width = 12, units = "cm", res = 600)
grid.draw(plotGrob)
dummy <- dev.off()


## Plot degree p

degreeAccessions <- getDegreDF(matchingDF$degreeAccession)
degreeProteoforms <- getDegreDF(matchingDF$degreeProteoform)

degreeAll <- c(degreeAccessions$degree, degreeProteoforms$degree)
pAll <- c(degreeAccessions$p, degreeProteoforms$p)
matching <- c(rep("Gene", nrow(degreeAccessions)), rep("Proteoform", nrow(degreeProteoforms)))

plotDF <- data.frame(degree = degreeAll, p = pAll, matching, stringsAsFactors = F)
plotDF$degree <- log10(plotDF$degree)
plotDF$p <- log10(plotDF$p)
plotDF$matching <- factor(plotDF$matching, levels = c("Gene", "Proteoform"))

plot <- ggplot() + theme_bw(base_size = 11)
plot <- plot + geom_point(data = plotDF, aes(x = degree, y = p, shape = matching, col = matching), alpha = 0.8, size = 2)

plot <- plot + scale_color_manual(name = "Matching", values = c(accessionColor, proteoformColor))

plot <- plot + xlab("Degree [log10]")
plot <- plot + ylab("p [log10]")

plot <- plot + theme(legend.position = "none")


## Plot degree comparison

plotDF <- matchingDF[matchingDF$degreeProteoform > 0 & matchingDF$degreeAccession > 0, ]
plotDF$degreeAccessionLog <- log10(plotDF$degreeAccession)
plotDF$degreeProteoformLog <- log10(plotDF$degreeProteoform)

plotDF <- plotDF[order(abs(plotDF$ratioAccession)), ]

minRatio <- min(plotDF$ratioAccession)
maxRatio <- max(plotDF$ratioAccession)
maxAmplitude <- max(abs(minRatio), abs(maxRatio))
beginGradient <- (maxAmplitude - abs(minRatio)) / (2 * maxAmplitude)
endGradient <- 1 - ((maxAmplitude - maxRatio) / (2 * maxAmplitude))

maxDegree <- max(plotDF$degreeAccessionLog, plotDF$degreeProteoformLog)

plot <- ggplot() + theme_bw(base_size = 11)
plot <- plot + geom_abline(slope = 1, intercept = 0, color = "black", linetype = "dashed", size = 0.5)
plot <- plot + geom_point(data = plotDF, aes(x = degreeAccessionLog, y = degreeProteoformLog, col = ratioAccession), alpha = 0.8)

plot <- plot + scale_color_scico(palette = palette, begin = beginGradient, end = endGradient) 

plot <- plot + scale_x_continuous(name = "Degree Gene [log10]", limits = c(0, maxDegree))
plot <- plot + scale_y_continuous(name = "Degree Proteoform [log10]", limits = c(0, maxDegree))

plot <- plot + theme(legend.position = "none",
                     panel.border = element_rect(color = "white"),
                     axis.line = element_line(color = "black", size = 0.25))

png("docs/publication/plots/fig_3D.png", height = 12, width = 12, units = "cm", res = 600)
plot(plot)
dummy <- dev.off()


## Extract tables

matchingDF$degreeAccessionLog <- log10(matchingDF$degreeAccession)
matchingDF$degreeProteoformLog <- log10(matchingDF$degreeProteoform)

lowDegreeAccession <- matchingDF[matchingDF$degreeAccessionLog < 1.5, ]
write.table(lowDegreeAccession, gzfile("docs/publication/tables/lowDegreeAccession.gz"), sep = "\t", col.names = T, row.names = F, quote = F)

lowDegreeProteoform <- matchingDF[matchingDF$degreeProteoformLog < 1.5, ]
write.table(lowDegreeProteoform, gzfile("docs/publication/tables/lowDegreeProteoform.gz"), sep = "\t", col.names = T, row.names = F, quote = F)

hubGain <- matchingDF[matchingDF$degreeProteoformLog > 2.5 & matchingDF$degreeAccessionLog < 2.2, ]
write.table(hubGain, gzfile("docs/publication/tables/highDegreeGain.gz"), sep = "\t", col.names = T, row.names = F, quote = F)

hubLoss <- matchingDF[matchingDF$degreeAccessionLog > 2.8, ]
write.table(hubLoss, gzfile("docs/publication/tables/highDegreeLoss.gz"), sep = "\t", col.names = T, row.names = F, quote = F)


