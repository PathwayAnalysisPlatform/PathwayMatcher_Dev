# 
# This script plots the number of references over time.
#
startTimeAll <- proc.time()


# Libraries

library(ggplot2)
library(ggrepel)
library(igraph)
library(scico)


# Parameters

## Network files

proteoformEdgesFile <- "resources/networks/all/1.8.1/proteoformInternalEdges.tsv.gz"
referenesFile <- "docs/publication/tables/publications.csv.gz"

## Plot theme

theme_set(theme_bw(base_size = 11))


## Colors

palette <- 'cork'
accessionColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
proteoformAccessionColor <- scico(n = 1, begin = 0.7, end = 0.7, palette = palette)
proteoformColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)


# Functions

#' Indicates whether the publication refers to a proteoform-proteoform reaction.
#' 
#' @param publicationId the publication identifier
#' @param referencesTable the publication table
#' @param edgesProteoforms the proteoform edges table
#' 
#' @return whether the publication refers to a proteoform-proteoform reactions
isProteoformProteoform <- function(publicationId, referencesTable, edgesProteoforms) {
    
    is <- which(edgesProteoforms$container_id %in% referencesTable$Reaction[referencesTable$PubMed == publicationId])
    return(sum(edgesProteoforms$isProteoform1[is] & edgesProteoforms$isProteoform2[is]) > 0)
    
}

#' Indicates whether the publication refers to a proteoform-accession reaction.
#' 
#' @param publicationId the publication identifier
#' @param referencesTable the publication table
#' @param edgesProteoforms the proteoform edges table
#' 
#' @return whether the publication refers to a proteoform-accession reactions
isProteoformAccession <- function(publicationId, referencesTable, edgesProteoforms) {
    
    is <- which(edgesProteoforms$container_id %in% referencesTable$Reaction[referencesTable$PubMed == publicationId])
    return(sum(edgesProteoforms$isProteoform1[is] | edgesProteoforms$isProteoform2[is]) > 0)
    
}

#' Indicates whether the publication refers to an accession-accession reaction.
#' 
#' @param publicationId the publication identifier
#' @param referencesTable the publication table
#' @param edgesProteoforms the proteoform edges table
#' 
#' @return whether the publication refers to a accession-accession reactions
isAccessionAccession <- function(publicationId, referencesTable, edgesProteoforms) {
    
    is <- which(edgesProteoforms$container_id %in% referencesTable$Reaction[referencesTable$PubMed == publicationId])
    return(sum(edgesProteoforms$isProteoform1[is] | edgesProteoforms$isProteoform2[is]) == 0)
    
}


# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

edgesProteoforms <- read.table(proteoformEdgesFile, header = T, sep = "\t", quote = "", comment.char = "", stringsAsFactors = F)

referencesTable <- read.table(referenesFile, header = T, sep = ",", stringsAsFactors = F, quote = "\"")


## Format

lengths <- sapply(edgesProteoforms$id1, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(edgesProteoforms$id1, FUN = regexpr, pattern = ';', USE.NAMES = F)
edgesProteoforms$isProteoform1 <- lengths > separatorI
lengths <- sapply(edgesProteoforms$id2, FUN = nchar, USE.NAMES = F)
separatorI <- sapply(edgesProteoforms$id2, FUN = regexpr, pattern = ';', USE.NAMES = F)
edgesProteoforms$isProteoform2 <- lengths > separatorI


## Annotate references

publicationsList <- unique(referencesTable$PubMed)

proteoformProteoform <- sapply(X = publicationsList, FUN = isProteoformProteoform, referencesTable = referencesTable, edgesProteoforms = edgesProteoforms, USE.NAMES = F)
proteoformAccession <- sapply(X = publicationsList, FUN = isProteoformAccession, referencesTable = referencesTable, edgesProteoforms = edgesProteoforms, USE.NAMES = F)
accessionAccession <- sapply(X = publicationsList, FUN = isAccessionAccession, referencesTable = referencesTable, edgesProteoforms = edgesProteoforms, USE.NAMES = F)

publicationtoYear <- referencesTable$Year
names(publicationtoYear) <- referencesTable$PubMed

publicationsYear <- publicationtoYear[publicationsList]

publicationsDF <- data.frame(publication = publicationsList, year = publicationsYear, pp = proteoformProteoform, pa = proteoformAccession, aa = accessionAccession, stringsAsFactors = F)

## Get cumulative number of publications over time

years <- c()
nPublications <- c()
types <- c()

for (year in 1960:2017) {
    
    nAA <- sum(publicationsDF$aa[publicationsDF$year <= year])
    nPA <- sum(publicationsDF$pa[publicationsDF$year <= year])
    nPP <- sum(publicationsDF$pp[publicationsDF$year <= year])
    
    i <- length(years) + 1
    years[i] <- year
    nPublications[i] <- nAA
    types[i] <- "AA"
    
    i <- length(years) + 1
    years[i] <- year
    nPublications[i] <- nPA
    types[i] <- "PA"
    
    i <- length(years) + 1
    years[i] <- year
    nPublications[i] <- nPP
    types[i] <- "PP"
    
}

referencesDF <- data.frame(year = years, n = nPublications, type = types, stringsAsFactors = F)
referencesDF$type <- factor(referencesDF$type, levels = c("AA", "PA", "PP"))

nAA <- sum(publicationsDF$aa)
nPA <- sum(publicationsDF$pa)
nPP <- sum(publicationsDF$pp)


## Plot the growth per year

plot <- ggplot()

plot <- plot + geom_line(data = referencesDF, aes(x = year, y = n, col = type, linetype = type))

plot <- plot + scale_color_manual(values = c(accessionColor, proteoformAccessionColor, proteoformColor))
plot <- plot + scale_linetype_manual(values = c("dotted", "longdash", "solid"))

plot <- plot + scale_y_continuous(expand = c(0, 0), limits = c(0, 1.05 * (nAA + 100)))
plot <- plot + scale_x_continuous(expand = c(0, 0))

plot <- plot + geom_text(aes(x = 2016, y = nAA + 100, label = nAA), col = accessionColor, hjust = 1, vjust = 0)
plot <- plot + geom_text(aes(x = 2016, y = nPA + 100, label = nPA), col = proteoformAccessionColor, hjust = 1, vjust = 0)
plot <- plot + geom_text(aes(x = 2016, y = nPP + 100, label = nPP), col = proteoformColor, hjust = 1, vjust = 0)

plot <- plot + xlab("Year") + ylab("# Publications")

plot <- plot + theme(legend.position = "none",
                     panel.grid.minor = element_blank(),
                     panel.border = element_rect(color = "white"),
                     axis.line = element_line(color = "black", size = 0.25))


png("docs/publication/plots/Fig_1B.png", height = 12, width = 12, units = "cm", res = 600)
plot(plot)
dummy <- dev.off()


