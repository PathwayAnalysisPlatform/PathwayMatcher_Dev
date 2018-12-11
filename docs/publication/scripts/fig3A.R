# 
# This script extracts the share of proteoforms per pathway.
#
startTimeAll <- proc.time()


# Libraries

library(ggplot2)
library(ggforce)
library(scico)
library(gtable)
library(grid)


# Parameters

## Input files

matchesFile <- "resources/networks/all/1.8.1/search.tsv.gz"

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

matchesDF <- read.table(matchesFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)


## require at least two participants per reaction

toExcelude <- c()

for (reaction in unique(matchesDF$REACTION_STID)) {
 
    proteoforms <- unique(matchesDF$PROTEOFORM[matchesDF$REACTION_STID == reaction])
    
    if (length(proteoforms) == 1) {
        
        toExcelude[length(toExcelude) + 1] <- reaction
        
    }
}

matchesDF <- matchesDF[!matchesDF$REACTION_STID %in% toExcelude, ]


## Pathway uniqueness

pathwayNames <- unique(matchesDF[, c("PATHWAY_STID", "PATHWAY_DISPLAY_NAME")])
pathwayNames$proteins <- 0
pathwayNames$proteoforms <- 0
pathwayNames$diff <- 0
pathwayNames$reactions <- 0
pathwayNames$subPathways <- 0
pathwayNames$sharedPathways <- 0

for (i in 1:nrow(pathwayNames)) {
    
    pathway <- pathwayNames$PATHWAY_STID[i]
    
    proteins <- unique(matchesDF$UNIPROT[matchesDF$PATHWAY_STID == pathway])
    pathwayNames$proteins[i] <- length(proteins)
    
    proteoforms <- unique(matchesDF$PROTEOFORM[matchesDF$PATHWAY_STID == pathway])
    pathwayNames$proteoforms[i] <- length(proteoforms)
    
    proteinsFromProteoforms <- sapply(X = proteoforms, FUN = getAccession, USE.NAMES = F)
    diff <- sum(paste0(proteinsFromProteoforms, ";") != proteoforms)
    pathwayNames$diff[i] <- diff
    
    reactions <- unique(matchesDF$REACTION_STID[matchesDF$PATHWAY_STID == pathway])
    pathwayNames$reactions[i] <- length(reactions)
    
    pathwaysUnion <- unique(matchesDF$PATHWAY_STID[matchesDF$REACTION_STID %in% reactions])
    
    for (pathwayShared in pathwaysUnion) {
        
        if (pathwayShared != pathway) {
            
            reactionsShared <- unique(matchesDF$REACTION_STID[matchesDF$PATHWAY_STID == pathwayShared])
            
            if (sum(reactionsShared %in% reactions) == length(reactionsShared)) {
                
                pathwayNames$subPathways[i] <- pathwayNames$subPathways[i] + 1
                
            } else {
                
                pathwayNames$sharedPathways[i] <- pathwayNames$sharedPathways[i] + 1
                
            }
        }
    }
}

pathwayNames$share <- pathwayNames$diff / pathwayNames$proteoforms

write.table(pathwayNames, "resources/networks/all/1.8.1/pathwayProteoformShare.txt", row.names = F, col.names = T, sep = '\t', quote = F)


pathwaysCumulative = numeric(1001)

for (i in 0:1000) {
    
    pathwaysCumulative[i+1] <- 100 * sum(pathwayNames$share >= i/1000) / nrow(pathwayNames)
    
}

cumulativePlotDF <- data.frame(x = pathwaysCumulative, y = (0:1000)/10)
cumulativePlotDF <- cumulativePlotDF[order(cumulativePlotDF$y, decreasing = T), ]
cumulativePlotDF <- cumulativePlotDF[!duplicated(cumulativePlotDF$x), ]


pointPlotDF <- pathwayNames
pointPlotDF$sharePercent <- 100 * pointPlotDF$share

for (i in 1:nrow(pointPlotDF)) {
    
    pointPlotDF$maxX[i] <- max(cumulativePlotDF$x[cumulativePlotDF$y >= pointPlotDF$sharePercent[i]])
    
}

pointPlotDF$x <- runif(n = nrow(pointPlotDF)) * pointPlotDF$maxX


pathwayShare <- ggplot()

pathwayShare <- pathwayShare + geom_line(data = cumulativePlotDF, mapping = aes(x = x, y = y), col = proteoformColor, alpha = 0.5, size = 0.5)
pathwayShare <- pathwayShare + geom_point(data = pointPlotDF, mapping = aes(x = x, y = sharePercent), col = proteoformColor, alpha = 0.5, size = 0.5)

pathwayShare <- pathwayShare + scale_y_continuous(name = "Proteoform-specific Participants [%]", breaks = (0:5)*20)
pathwayShare <- pathwayShare + scale_x_continuous(name = "Cumulative Share of Pathways [%]", breaks = (0:5)*20)

pathwayShare <- pathwayShare + theme(legend.position = "none",
                                     panel.grid.minor = element_blank(),
                                     panel.border = element_rect(color = "white"),
                                     axis.line = element_line(color = "black", size = 0.25))


png("docs/publication/plots/fig_3A.png", height = 12, width = 12, units = "cm", res = 600)
plot(pathwayShare)
dummy <- dev.off()
