# This script plots the number of hit reactions and pathways for proteins and proteoforms

###############################
# Load libraries

library(ggplot2)
require(cowplot)
library(stats)
source("loadHits.R")

###############################
# First, load data

hits <- load.hits()

# Make a summary of the hits for proteins and proteoforms

summary(hits[which(hits$Type == "Modified Protein"),])
summary(hits[which(hits$Type == "Modified Protein Proteoform"),])

###############################
# Create plots

colorProteins <- "#3182bd"
colorProteoforms <- "#31a354"
yMin <- 0
yMax <- 100

plot.violin.pathways <- ggplot(hits, aes(x = Type, y = Pathways, fill = factor(Type))) + 
  geom_violin() +
  scale_y_continuous(limits = c(yMin, yMax)) +
  scale_fill_manual(values = c(colorProteins, colorProteoforms)) +
  theme_bw() + ylab("# Pathways") +
  guides(fill=FALSE)
plot.violin.pathways

plot.violin.reactions <- ggplot(hits, aes(x = Type, y = Reactions, fill = factor(Type))) + 
  geom_violin() +
  scale_y_continuous(limits = c(yMin, yMax)) +
  scale_fill_manual(values = c(colorProteins, colorProteoforms)) +
  theme_bw() + ylab("# Reactions") +
  guides(fill=FALSE)
plot.violin.reactions

# Put the plots together in a grid

plot_grid(
  plot.violin.reactions, 
  plot.violin.pathways,
  labels = c("A", "B"))

