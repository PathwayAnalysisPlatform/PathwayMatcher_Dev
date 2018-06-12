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

hits <- load.hits.Merged(fileProteins = "HitsPerProtein.csv", fileProteoforms = "HitsPerProteoform.csv")

###############################
# Second, create plots

xMin <- 0
xMax <- 25

library(plyr)
cdat <- ddply(hitsReactions, "Type", summarise, Count.mean=mean(Count))
cdat

plot.density.reactions <- ggplot(hitsReactions, aes(x=Count, colour = Type, fill = Type)) + 
  scale_fill_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  scale_color_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  geom_density(size=1, alpha = 0.4, show.legend = F) +
  scale_x_continuous(name = "# mapped reactions", limits = c(xMin, xMax)) +
  theme_bw() +
  geom_vline(data=cdat, aes(xintercept=Count.mean, color=Type),linetype="dashed", show.legend = F) 

plot.density.reactions

cdat <- ddply(hitsPathways, "Type", summarise, Count.mean=mean(Count))
cdat

plot.density.pathways <- ggplot(hitsPathways, aes(x=Count, colour = Type, fill = Type)) + 
  scale_fill_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  scale_color_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  geom_density(size=1, alpha = 0.4) +
  scale_x_continuous(name = "# mapped pathways", limits = c(xMin, xMax)) +
  theme_bw() +
  geom_vline(data=cdat, aes(xintercept=Count.mean, color=Type),linetype="dashed")

plot.density.pathways

###############################
# Third, put the plots together in a grid

plot_grid(
  plot.density.reactions, 
  plot.density.pathways,
  labels = c("A", "B"))

###############################
# Fourth, create facets with densities

plot.density <- ggplot(hits, aes(x=Count, colour = Type, fill = Type)) + 
  facet_grid(. ~ Hit) +
  scale_fill_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  scale_color_manual(values=c("#999999", "#E69F00", "#56B4E9")) +
  geom_density(size=1, alpha = 0.4, show.legend = T) +
  scale_x_continuous(limits = c(xMin, xMax)) +
  theme_bw() +
  geom_vline(data=cdat, aes(xintercept=Count.mean, color=Type),linetype="dashed", show.legend = T) 

plot.density