# This script plots the number of hit reactions and pathways for proteins and proteoforms

###############################
# Load libraries

library(ggplot2)
require(cowplot)
library(stats)
source("loadHits.R")

###############################
# First, load data

hitsReactions <- load.hits(fileProteins = "ReactionsPerProtein.csv", fileProteoforms = "ReactionsPerProteoform.csv")
hitsPathways <- load.hits(fileProteins = "PathwaysPerProtein.csv", fileProteoforms = "PathwaysPerProteoform.csv")

# Make a summary of the hits for proteins and proteoforms

summary(hitsPathways[which(hitsPathways$Type == "Protein"),])
summary(hitsPathways[which(hitsPathways$Type == "Proteoform"),])
summary(hitsReactions[which(hitsReactions$Type == "Protein"),])
summary(hitsReactions[which(hitsReactions$Type == "Proteoform"),])

###############################
# Create plots

colorProteins <- "#3182bd"
colorProteoforms <- "#31a354"
yMin <- 0
yMax <- 25

plot.violin.pathways <- ggplot(hitsPathways, aes(x = Type, y = Count, fill = factor(Type))) + 
  geom_violin() +
  scale_y_continuous(limits = c(yMin, yMax)) +
  scale_fill_manual(values = c(colorProteins, colorProteoforms)) +
  theme_bw() + ylab("# Pathways") +
  guides(fill=FALSE)
plot.violin.pathways

plot.violin.reactions <- ggplot(hitsReactions, aes(x = Type, y = Count, fill = factor(Type))) + 
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

