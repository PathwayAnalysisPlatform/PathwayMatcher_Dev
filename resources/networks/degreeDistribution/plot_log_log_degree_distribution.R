#
# Plotthe degree distribution of the vertices in a network
#

# Libraries

library(ggplot2)
library(igraph)
library(plyr)
library(ggplot2)

source("../graphs.R")

# Load protein data

proteins.graph <- loadGraph("../all/1.8.1/proteinInternalEdges.tsv.gz")
proteoforms.graph <- loadGraph("../all/1.8.1/proteoformInternalEdges.tsv.gz")

# Make log-log degree distribution plots

## Calculate the distributions

### Proteins
proteins.degrees <- as.data.frame(degree(proteins.graph)) 
names(proteins.degrees) <- c("degrees")              
proteins.degrees$entity <- "protein"

proteins.degrees.frequencies <- as.data.frame(table(proteins.degrees))
proteins.degrees.frequencies[, 1] <- as.numeric(proteins.degrees.frequencies[, 1])
proteins.degrees.frequencies$Fraction <- proteins.degrees.frequencies$Freq / gorder(proteins.graph)

### Proteoforms
proteoforms.degrees <- as.data.frame(degree(proteoforms.graph)) 
names(proteoforms.degrees) <- c("degrees")              
proteoforms.degrees$entity <- "proteoform"

proteoforms.degrees.frequencies <- as.data.frame(table(proteoforms.degrees))
proteoforms.degrees.frequencies[, 1] <- as.numeric(proteoforms.degrees.frequencies[, 1])
proteoforms.degrees.frequencies$Fraction <- proteoforms.degrees.frequencies$Freq / gorder(proteoforms.graph)

## Make the plots

degrees.frequencies <- rbind(proteins.degrees.frequencies, proteoforms.degrees.frequencies)

plot <- ggplot(degrees.frequencies, aes(x = degrees, y = Fraction, color = entity)) +
  geom_point() +
  scale_x_continuous("Degree",
                     trans = "log10") +
  scale_y_continuous("Fraction",
                     trans = "log10") +
  geom_smooth() +
  ggtitle("Degree Distribution (log-log)") +
  theme_bw()
plot