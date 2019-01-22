# 
# This script extracts the share of proteoforms per pathway.
#
startTimeAll <- proc.time()


# Libraries

library(ggplot2)
library(scico)
library(igraph)
library(ggrepel)


# Parameters

## Input files

matchesFile <- "resources/networks/all/1.8.1/search.tsv.gz"

## Plot theme

theme_set(theme_bw(base_size = 11))


## Colors

pathway1Color <- scico(n = 1, begin = 0.2, end = 0.2, palette = "lajolla")
pathway2Color <- scico(n = 1, begin = 0.4, end = 0.4, palette = "lajolla")
nodesColor <- scico(n = 1, begin = 0.8, end = 0.8, palette = "lajolla")


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

#' Returns the name to display for a proteoform.
#' 
#' @param name the proteoform identifier
#' @param error throw an error if not found
#' 
#' @return the name to display
getDisplayName <- function(name, error = F) {
    
    if (name == "Q8WW43;") {
        
        return("APH1B")
        
    } else if (name == "Q9NZ42;") {
        
        return("PSENEN")
        
    } else if (name == "P49810;") {
        
        return("PSEN2")
        
    } else if (name == "P49768;") {
        
        return("PSEN1")
        
    } else if (name == "Q92542;") {
        
        return("NCSTN")
        
    } else if (name == "Q96BI3;") {
        
        return("APH1A")
        
    } else if (name == "P78504;") {
        
        return("JAG1")
        
    } else if (name == "P0CG47;") {
        
        return("UBB")
        
    } else if (name == "P0CG48;") {
        
        return("UBC")
        
    } else if (name == "P62979;") {
        
        return("RPS27A")
        
    } else if (name == "P62987;") {
        
        return("UBA52")
        
    } else if (name == "O14672;") {
        
        return("ADAM10")
        
    } else if (name == "Q04721;") {
        
        return("NOTCH2")
        
    } else if (name == "Q04721;00804:150,00804:381,00804:462,00804:500,00804:538,00804:613,00804:651,00804:688,00804:726,00804:763,00804:801,00804:879,00804:955,00804:1031,00804:1155,00804:1270,00812:925,00813:78,00813:120,00813:158,00813:197,00813:235,00813:314,00813:352,00813:470,00813:696,00813:771,00813:809,00813:963,00813:1001,00813:1039,00813:1077,00813:1163,00813:1201,00813:1318") {
        
        return("NOTCH2_Gly1")
        
    } else if (name == "Q04721;00804:150,00804:381,00804:462,00804:500,00804:538,00804:613,00804:651,00804:688,00804:726,00804:763,00804:801,00804:879,00804:955,00804:1031,00804:1155,00804:1270,00812:925,00813:78,00813:120,00813:158,00813:197,00813:235,00813:314,00813:352,00813:470,00813:696,00813:771,00813:809,00813:963,00813:1001,00813:1039,00813:1077,00813:1163,00813:1201,00813:1318,00813:null") {
        
        return("NOTCH2_Gly2")
        
    } else if (name == "O00548;") {
        
        return("DLL1")
        
    } else if (name == "Q9Y219;") {
        
        return("JAG2")
        
    } else if (name == "Q86YT6;") {
        
        return("MIB1")
        
    } else if (name == "O76050;") {
        
        return("NEURL1")
        
    } else if (name == "A8MQ27;") {
        
        return("NEURL1B")
        
    } else if (name == "Q96AX9;") {
        
        return("MIB2")
        
    } else if (name == "Q13495;") {
        
        return("MAMLD1")
        
    } else if (name == "Q8IZL2;") {
        
        return("MAML2")
        
    } else if (name == "Q92585;") {
        
        return("MAML1")
        
    } else if (name == "Q06330;") {
        
        return("RBPJ")
        
    } else if (name == "Q09472;") {
        
        return("EP300")
        
    } else if (name == "P16220;00046:133") {
        
        return("CREB1_P")
        
    } else if (name == "Q12860;") {
        
        return("CNTN1")
        
    } else if (name == "Q9NR61;") {
        
        return("DLL4")
        
    } else if (name == "Q96JK9;") {
        
        return("MAML3")
        
    } else if (name == "P21741;") {
        
        return("MDK")
        
    }
    
    if (error) {
        stop(paste0("Proteoform not recognized: ", name, "."))
    }
    
    return("")
    
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


## Pathway graph

pathway <- "R-HSA-1980145"

pathwayReactions <- unique(matchesDF$REACTION_STID[matchesDF$PATHWAY_STID == pathway])

pathwaysUnion <- unique(matchesDF$PATHWAY_STID[matchesDF$REACTION_STID %in% pathwayReactions])

sharedPathways <- c()
subPathways <- c()

for (pathwayShared in pathwaysUnion) {
    
    if (pathwayShared != pathway) {
        
        reactionsShared <- unique(matchesDF$REACTION_STID[matchesDF$PATHWAY_STID == pathwayShared])
        
        if (sum(reactionsShared %in% pathwayReactions) == length(reactionsShared)) {
            
            subPathways[length(subPathways) + 1] <- pathwayShared
            
        } else {
            
            sharedPathways[length(sharedPathways) + 1] <- pathwayShared
            
        }
    }
}

pathwayId <- matchesDF$PATHWAY_DISPLAY_NAME
names(pathwayId) <- matchesDF$PATHWAY_STID

from <- c()
to <- c()
reactions <- c()
pathways <- c()
categories <- c()

for (reaction in pathwayReactions) {
    
    proteoforms <- unique(matchesDF$PROTEOFORM[matchesDF$REACTION_STID == reaction])
    pathwayName <- unique(matchesDF$PATHWAY_STID[matchesDF$REACTION_STID == reaction])
    pathwayName <- pathwayName[pathwayName != pathway]
    pathwayName <- paste(sort(pathwayName), collapse = "_")
    
    if (pathwayName == "R-HSA-157118_R-HSA-162582_R-HSA-2197563") {
        
        category <- "2197563"
        
    } else if (pathwayName == "R-HSA-157118_R-HSA-162582_R-HSA-2979096") {
        
        category <- "2979096"
        
    } else {
        
        stop("Combination not recongized.")
        
    }
    
    if (length(proteoforms) > 1) {
        
        for (i in 1:(length(proteoforms)-1)) {
            for (j in (i+1):length(proteoforms)) {
                
                from[length(from) + 1] <- proteoforms[i]
                to[length(to) + 1] <- proteoforms[j]
                reactions[length(reactions) + 1] <- reaction
                pathways[length(pathways) + 1] <- pathwayName
                categories[length(categories) + 1] <- category
                
            }
        }
    }
}

pathwayDF <- data.frame(from = from, to = to, reaction = reactions, pathway = pathways, category = categories, stringsAsFactors = F)

fromTo <- pathwayDF$to
names(fromTo) <- pathwayDF$from

pathwayNodes <- unique(c(from, to))

from <- c()
to <- c()
reactions <- c()
pathways <- c()
categories <- c()

for (node in pathwayNodes) {
    
    allReactions <- unique(matchesDF$REACTION_STID[matchesDF$PROTEOFORM == node])
    
    for (reaction in allReactions) {
        
        proteoforms <- unique(matchesDF$PROTEOFORM[matchesDF$REACTION_STID == reaction])
        pathwayName <- unique(matchesDF$PATHWAY_STID[matchesDF$REACTION_STID == reaction])
        pathwayName <- pathwayName[pathwayName != pathway]
        pathwayName <- paste(sort(pathwayName), collapse = "_")
        
        if (length(proteoforms) > 1) {
            
            for (i in 1:(length(proteoforms)-1)) {
                for (j in (i+1):length(proteoforms)) {
                    
                    proteoformI <- proteoforms[i]
                    proteoformJ <- proteoforms[j]
                    
                    if(proteoformI %in% pathwayNodes || proteoformJ %in% pathwayNodes) {
                        
                        if (proteoformI %in% names(fromTo) && fromTo[proteoformI] == proteoformJ
                            || proteoformJ %in% names(fromTo) && fromTo[proteoformJ] == proteoformI) {
                            
                        } else {
                            
                            from[length(from) + 1] <- proteoformI
                            to[length(to) + 1] <- proteoformJ
                            reactions[length(reactions) + 1] <- reaction
                            pathways[length(pathways) + 1] <- pathwayName
                            categories[length(categories) + 1] <- "External"
                            
                        }
                    }
                }
            }
        }
    }
}

externalDF <- data.frame(from = from, to = to, reaction = reactions, pathway = pathways, category = categories, stringsAsFactors = F)

graphDF <- rbind(pathwayDF, externalDF)

pathwayGraphInternal <- graph_from_data_frame(pathwayDF)
pathwayGraph <- graph_from_data_frame(graphDF)

pathwayGraphInternal <- simplify(pathwayGraphInternal, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")
pathwayGraph <- simplify(pathwayGraph, remove.multiple = T, remove.loops = T, edge.attr.comb = "first")

V(pathwayGraphInternal)$displayName <- sapply(X = V(pathwayGraphInternal)$name, FUN = getDisplayName)
V(pathwayGraph)$displayName <- sapply(X = V(pathwayGraph)$name, FUN = getDisplayName)


# Plot pathway alone

# graphPlot <- ggraph(pathwayGraphInternal, layout = 'kk')
# graphPlot <- graphPlot + geom_edge_link(aes(col = category), alpha = 0.2)
# graphPlot <- graphPlot + geom_node_label(aes(label = displayName))
# graphPlot <- graphPlot + theme(axis.title = element_blank(),
#        axis.text = element_blank(),
#        axis.ticks = element_blank(),
#        panel.grid = element_blank())

# plot(graphPlot)


# Plot pathway and background

edgesList <- get.edgelist(pathwayGraph)
degrees <- degree(pathwayGraph)

#E(pathwayGraph)$weight <- ifelse(E(pathwayGraph)$category == "External", 0.1, 1)
#E(pathwayGraph)$weight[edgesList[, 1] %in% pathwayNodes & !edgesList[, 2] %in% pathwayNodes & degrees[edgesList[, 2]] == 1] <- 2
#E(pathwayGraph)$weight[edgesList[, 2] %in% pathwayNodes & !edgesList[, 1] %in% pathwayNodes & degrees[edgesList[, 1]] == 1] <- 2

layout <- layout_with_fr(pathwayGraph)

verticesDF <- data.frame(name = V(pathwayGraph)$name, displayName = V(pathwayGraph)$displayName, x = layout[, 1], y = layout[, 2], stringsAsFactors = F)
x <- verticesDF$x
names(x) <- verticesDF$name
y <- verticesDF$y
names(y) <- verticesDF$name

edgesList <- get.edgelist(pathwayGraph)
edgesDF <- data.frame(name1 = edgesList[, 1], name2 = edgesList[, 2], category = E(pathwayGraph)$category, stringsAsFactors = F)
edgesDF$x1 <- x[edgesDF$name1]
edgesDF$y1 <- y[edgesDF$name1]
edgesDF$x2 <- x[edgesDF$name2]
edgesDF$y2 <- y[edgesDF$name2]

edgesBackground <- edgesDF[edgesDF$category == "External", ]
edgesPathway <- edgesDF[edgesDF$category != "External", ]
verticesBackground <- verticesDF[verticesDF$displayName == "", ]
verticesPathway <- verticesDF[verticesDF$displayName != "", ]

graphPlot <- ggplot()

graphPlot <- graphPlot + geom_segment(data = edgesBackground, aes(x = x1, y = y1, xend = x2, yend = y2), col = "black", alpha = 0.01, size = 0.5)
graphPlot <- graphPlot + geom_point(data = verticesBackground, aes(x = x, y = y), col = "black", size = 0.1, alpha = 0.1)

graphPlot <- graphPlot + geom_segment(data = edgesPathway, aes(x = x1, y = y1, xend = x2, yend = y2, col = category), size = 0.5)
graphPlot <- graphPlot + geom_point(data = verticesPathway, aes(x = x, y = y), col = nodesColor, size = 2)
graphPlot <- graphPlot + geom_label_repel(data = verticesPathway, aes(x = x, y = y, label = displayName), col = "black", size = 2, segment.size = 0.2)

graphPlot <- graphPlot + scale_color_manual(values = c(pathway2Color, pathway1Color))

graphPlot <- graphPlot + theme(legend.position = "none",
                               axis.ticks = element_blank(),
                               axis.text = element_blank(),
                               axis.title = element_blank(),
                               panel.grid = element_blank(),
                               panel.border = element_rect(color = "white"))


png(paste0("docs/publication/plots/fig_1A.png"), height = 12, width = 12, units = "cm", res = 600)
plot(graphPlot)
dummy <- dev.off()

