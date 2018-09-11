# This script plots the number of hit reactions and pathways for proteins and proteoforms

###############################
# Load libraries

library(ggplot2)
require(cowplot)
library(stats)
library(plyr)
source("loadHits.R")

###############################
# First, load data

hitsReactions <- load.hits(fileProteins = "ReactionsPerProtein.csv", fileProteoforms = "ReactionsPerProteoform.csv")
hitsPathways <- load.hits(fileProteins = "PathwaysPerProtein.csv", fileProteoforms = "PathwaysPerProteoform.csv")

hits <- load.hits.Merged(fileProteins = "HitsPerProtein.csv", fileProteoforms = "HitsPerProteoform.csv")

###############################
# Second, create plots


cdat <- ddply(hitsPathways, "Type", summarise, Count.mean=mean(Count))
cdat

plot.density.ByAccession <- ggplot(hits[which(hits$Type == "Protein" & hits$Hit == "Pathway"),], aes(x=Count)) + 
  scale_fill_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  scale_color_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  geom_density(size=1, alpha = 0.4, show.legend = F) +
  scale_x_log10(name = "# mapped pathways") +
  theme_bw() +
  geom_vline(xintercept=mean(hits$Count[which(hits$Type == "Protein" & hits$Hit == "Pathway")]), linetype="dashed", color = "black")
plot.density.ByAccession

plot.density.ByProteoform <- ggplot(hits[which(hits$Type == "Proteoform" & hits$Hit == "Pathway"),], aes(x=Count)) + 
  scale_fill_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  scale_color_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  geom_density(size=1, alpha = 0.4, show.legend = F) +
  scale_x_log10(name = "# mapped pathways") +
  theme_bw() +
  geom_vline(xintercept=mean(hits$Count[which(hits$Type == "Proteoform" & hits$Hit == "Pathway")]), linetype="dashed", color = "black")
plot.density.ByProteoform

###############################
# Third, put the plots together in a grid

plot_grid(
  plot.density.ByAccession, 
  plot.density.ByProteoform,
  plot.scatter,
  labels = c("A", "B","C"))

###############################
# Fourth, create facets with densities

plot.density <- ggplot(hits, aes(x=Count, colour = Type, fill = Type)) + 
  facet_grid(. ~ Hit) +
  scale_fill_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  scale_color_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  geom_density(size=1, alpha = 0.4, show.legend = T) +
  scale_x_log10() +
  theme_bw() +
  geom_vline(data=cdat, aes(xintercept=Count.mean, color=Type),linetype="dashed", show.legend = T) 

plot.density
