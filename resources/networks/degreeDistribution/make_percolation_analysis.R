# Make percolation curve sampling and plotting 

# Load libraries ----

library(igraph)
library(ggplot2)
library(plyr)
source("graphs.R")
source("degreeDistribution/percolation.R")

# Load undirected graphs
# (Notice that they are handled as undirected graphs)

source("load_networks.R")


## Percolation Section networks (uniform sizes) -----

w <- 14
h <- 7
replicates <- 20
factor <- 0.9
path <- "degreeDistribution/data/sections_"

type <- "link"
mm.samples.link <- GetPercolationCurvePoints(mm.graph, factor = factor, replicates = replicates, entity = "mm", type = type)
pp.samples.link <- GetPercolationCurvePoints(pp.graph, factor = factor, replicates = replicates, entity = "pp", type = type)
pm.samples.link <- GetPercolationCurvePoints(pm.graph, factor = factor, replicates = replicates, entity = "pm", type = type)
sections.samples.link <- rbind(mm.samples.link, pp.samples.link, pm.samples.link)
write.csv(sections.samples.link, paste(path, "_", type, "_percolation_curve_approximation.csv", sep = ""), row.names=FALSE, na="")

type <- "node"
mm.samples.node <- GetPercolationCurvePoints(mm.graph, factor = factor, replicates = replicates, entity = "mm", type = type)
pp.samples.node <- GetPercolationCurvePoints(pp.graph, factor = factor, replicates = replicates, entity = "pp", type = type)
pm.samples.node <- GetPercolationCurvePoints(pm.graph, factor = factor, replicates = replicates, entity = "pm", type = type)
sections.samples.node <- rbind(mm.samples.node, pp.samples.node, pm.samples.node)
write.csv(sections.samples.link, paste(path, "_", type, "_percolation_curve_approximation.csv", sep = ""), row.names=FALSE, na="")

scale <- "log10"
showScaled <- TRUE
path <- "degreeDistribution/plots/sections_"
sections.plot.node <- PlotPercolationCurve(sections.samples.node, showRelSize = F, showScaled = showScaled)
sections.plot.node
ggsave(paste(path, "node_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w, heigth = h)

sections.plot.node <- PlotPercolationCurve(sections.samples.node, showRelSize = T, showScaled = showScaled)
sections.plot.node
ggsave(paste(path, "node_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w, heigth = h)

sections.plot.link <- PlotPercolationCurve(sections.samples.link, showRelSize = F, showScaled = showScaled)
sections.plot.link
ggsave(paste(path, "link_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w, heigth = h)

sections.plot.link <- PlotPercolationCurve(sections.samples.link, showRelSize = T, showScaled = showScaled)
sections.plot.link
ggsave(paste(path, "link_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w, heigth = h)

# Full networks -------------- 

replicates <- 30
factor <- 0.9
path <- "degreeDistribution/data/all_"

type <- "link"
all.proteins.samples.link <- GetPercolationCurvePoints(all.proteins.graph, factor = factor, replicates = replicates, entity = "proteins", type = type)
all.proteoforms.samples.link <- GetPercolationCurvePoints(all.proteoforms.graph, factor = factor, replicates = replicates, entity = "proteoforms", type = type)
all.samples.link <- rbind(all.proteins.samples.link, all.proteoforms.samples.link)
write.csv(all.samples.link, paste(path, "_", type, "_percolation_curve_approximation.csv", sep = ""), row.names=FALSE, na="")

type <- "node"
all.proteins.samples.node <- GetPercolationCurvePoints(all.proteins.graph, factor = factor, replicates = replicates, entity = "proteins", type = type)
all.proteoforms.samples.node <- GetPercolationCurvePoints(all.proteoforms.graph, factor = factor, replicates = replicates, entity = "proteoforms", type = type)
all.samples.node <- rbind(all.proteins.samples.node, all.proteoforms.samples.node)
write.csv(all.samples.link, paste(path, "_", type, "_percolation_curve_approximation.csv", sep = ""), row.names=FALSE, na="")

scale <- "linear"
showScaled <- FALSE
path <- "degreeDistribution/plots/all_"
all.plot.node <- PlotPercolationCurve(all.samples.node, showRelSize = F, showScaled = showScaled)
all.plot.node
ggsave(paste(path, "node_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)

all.plot.node <- PlotPercolationCurve(all.samples.node, showRelSize = T, showScaled = showScaled)
all.plot.node
ggsave(paste(path, "node_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)

all.plot.link <- PlotPercolationCurve(all.samples.link, showRelSize = F, showScaled = showScaled)
all.plot.link
ggsave(paste(path, "link_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)

all.plot.link <- PlotPercolationCurve(all.samples.link, showRelSize = T, showScaled = showScaled)
all.plot.link
ggsave(paste(path, "link_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)

# Identify substructure ----

# The node percolation plot for the mm graph shows a substructure, then we try
# identify the reason of that separation in two connected components

replicates <- 5
factor <- 0.9
path <- "degreeDistribution/data/mm_"

type <- "link"

# Get 2 groups of nodes: group 1 with relative Size of Lcc > 0.25, and group 2 with < 0.25
mm.samples <- GetSubcomponents(mm.graph, factor = factor, replicates = replicates, entity = "mm", type = type)
write.csv(mm.samples, paste(path, type, "_groups.csv", sep = ""), row.names=FALSE, na="")

freq1 <- plyr::count(mm.samples[which(mm.samples$Group == "1"),], 'value')
freq2 <- plyr::count(mm.samples[which(mm.samples$Group == "2"),], 'value')

# Get list of proteins that appear 10 times or more in the group 2 (relative Size of Lcc < 0.25)
path <- "degreeDistribution/data/mm_"
write.csv(freq1, paste(path, type, "_group1_freq.csv", sep = ""), row.names=FALSE, na="")
write.csv(freq2, paste(path, type, "_group2_freq.csv", sep = ""), row.names=FALSE, na="")

path <- "degreeDistribution/plots/mm_"

plot <- ggplot() + geom_bar(data = freq1, aes(x = value, y = freq), stat = "identity") +
  ggtitle("Link Percolation group 1 member frequency") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))
ggsave(paste(path, type, "_group1", ".png", sep = ""), width = w)
plot

plot <- ggplot() + geom_bar(data = freq2, aes(x = value, y = freq), stat = "identity") +
  ggtitle("Link Percolation group 2 member frequency") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))
ggsave(paste(path, type, "_group2", ".png", sep = ""), width = w)  
plot

# From the plots we can appreciate that the members of group 2 
# appeared in that group more than 300 times. Therefore we
# select those members as group 2.

GetAccession <- function(x) {
  substr(x, 1, gregexpr(";", x)[[1]][1]-1)
}

g2 <- mm.samples[which(mm.samples$Group == "2" & mm.samples$value > 300),]
path <- "degreeDistribution/data/mm_"
write.table(sapply(g2$key, GetAccession), 
          paste(path, type, "_group2.csv", sep = ""), 
          row.names=FALSE, na="", quote = F)

# Run PathwayMatcher with to get mapping pathways and reactions

g2.map <- read.csv("degreeDistribution/data/mm_link_group2/search.tsv", sep = "\t")

# Plot the bars for the most frequent top level pathways
tlpFreq <- plyr::count(g2.map, vars = 'TOP_LEVEL_PATHWAY_DISPLAY_NAME')
tlpFreq <- tlpFreq[order(-tlpFreq$freq),]
write.csv(tlpFreq, paste(path, type, "_group2_tlp_freq.csv", sep = ""), row.names=FALSE, na="")

path <- "degreeDistribution/plots/mm_"
tlpFreq$TOP_LEVEL_PATHWAY_DISPLAY_NAME <- factor(tlpFreq$TOP_LEVEL_PATHWAY_DISPLAY_NAME, levels = unique(as.character(tlpFreq$TOP_LEVEL_PATHWAY_DISPLAY_NAME)))
plot <- ggplot() + geom_bar(data = tlpFreq, aes(x = TOP_LEVEL_PATHWAY_DISPLAY_NAME, y = freq), stat = "identity") +
  ggtitle("Link Percolation group 2 top level pathways frequency") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))
ggsave(paste(path, type, "_group2_tlp_freq", ".png", sep = ""), width = w)  
plot

# Plot the most common pathways

path <- "degreeDistribution/data/mm_"
pFreq <- plyr::count(g2.map, vars='PATHWAY_DISPLAY_NAME')
pFreq <- pFreq[order(-pFreq$freq),]
write.csv(pFreq, paste(path, type, "_group2_p_freq.csv", sep = ""), row.names=FALSE, na="")

path <- "degreeDistribution/plots/mm_"
pFreq$PATHWAY_DISPLAY_NAME <- factor(pFreq$PATHWAY_DISPLAY_NAME, levels = unique(as.character(pFreq$PATHWAY_DISPLAY_NAME)))
plot <- ggplot() + geom_bar(data = pFreq, aes(x = PATHWAY_DISPLAY_NAME, y = freq), stat = "identity") +
  ggtitle("Link Percolation group 2 pathways frequency") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))
ggsave(paste(path, type, "_group2_p_freq", ".png", sep = ""), width = w)  
plot

# Identify the most common post translational modifications of group 2

GetSingleMod <- function(i, x) {
  substr(x, i+1, i+5)
}

GetModifications <- function(x) {
  indexes <- gregexpr("(;|,)[[:digit:]]{5}", x)[[1]]
  sapply(indexes, GetSingleMod, x = x) # The first argument goes implicit in the sapply
}

path <- "degreeDistribution/data/mm_"
mods <- unlist(sapply(g2$key, GetModifications))
modsFreq <- plyr::count(mods)
names(modsFreq) <- c("mod", "freq")
modsFreq <- modsFreq[order(-modsFreq$freq),]
write.table(modsFreq, 
            paste(path, type, "_group2_mod.csv", sep = ""), 
            row.names=FALSE, na="", quote = F)
modsFreq$name <- c("4-hydroxy-L-proline", 
                   "3-hydroxy-L-proline", 
                   "O5-glucosylgalactosyl-L-hydroxylysine", 
                   "5-hydroxy-L-lysine", 
                   "O5-galactosyl-L-hydroxylysine")

path <- "degreeDistribution/plots/mm_"
modsFreq$mod <- factor(modsFreq$mod, levels = unique(as.character(modsFreq$mod)))
plot <- ggplot() + geom_bar(data = modsFreq, aes(x = mod, y = freq), stat = "identity") +
  ggtitle("Link Percolation group 2 modifications frequency") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))
ggsave(paste(path, type, "_group2_mod_freq", ".png", sep = ""), width = w)  
plot

# Identify the most common post translational modifications for all proteoforms
all.mods <- unlist(sapply(mm.samples$key, GetModifications))
all.mods.freq <- plyr::count(all.mods)
names(all.mods.freq) <- c("mod", "freq")
all.mods.freq <- all.mods.freq[order(-all.mods.freq$freq),]
path <- "degreeDistribution/data/mm_"
write.table(all.mods.freq, 
            paste(path, type, "_mod.csv", sep = ""), 
            row.names=FALSE, na="", quote = F)

path <- "degreeDistribution/plots/mm_"
all.mods.freq$mod <- factor(all.mods.freq$mod, levels = unique(as.character(all.mods.freq$mod)))
plot <- ggplot() + geom_bar(data = all.mods.freq, aes(x = mod, y = freq), stat = "identity") +
  ggtitle("All proteoforms modifications frequency") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))
ggsave(paste(path, type, "_mod_freq", ".png", sep = ""), width = w)  
plot

# only 00039 is in the general top 5
# all the corresponding modifications of the selected types fall belong to proteins in the group 2 (proteins forming the Largest connected component below 0.25 completeness)
# no other proteins have these 5 types of modifications

# Create two separate percolation curves for group 1 and 2 of modified proteins (proteoforms) ----

mm.g1.graph <- delete.vertices(mm.graph, g2$key)  # 2389 vertices
mm.g2.graph <- induced.subgraph(mm.graph, g2$key) # 480 vertices

w <- 14
h <- 7
factor <- 0.1
replicates <- 2
graphs <- list(mm.graph.g1, mm.graph.g2)
labels <- c("mm.graph.g1", "mm.graph.g2")
data.path <- "degreeDistribution/data/"
plots.path <- "degreeDistribution/plots/"

samples <- MakePercolationAnalysis(graphs, labels, data.path, plots.path, factor, replicates)

# Plot the degree distribution for groups 1 and 2 ----

## Calculate binned degree distribution plot

mm.g1.degrees <- degree(mm.g1.graph)
mm.g1.density <- GetBinnedDegreeProbabilities(mm.g1.degrees)
mm.g1.density$Entity <- "g1"

mm.g2.degrees <- degree(mm.g2.graph)
mm.g2.density <- GetBinnedDegreeProbabilities(mm.g2.degrees)
mm.g2.density$Entity <- "g2"

density.binned <- rbind(mm.g1.density, mm.g2.density)

## Calculate frequencies

mm.g1.frequencies <- as.data.frame(table(mm.g1.degrees), stringsAsFactors = F)
mm.g1.frequencies[, 1] <- as.numeric(mm.g1.frequencies[, 1])
mm.g1.frequencies$Fraction <- mm.g1.frequencies$Freq / gorder(mm.g1.graph)
names(mm.g1.frequencies) <- c("Degree", "Frequency", "Fraction")
mm.g1.frequencies$Entity <- "g1"

mm.g2.frequencies <- as.data.frame(table(mm.g2.degrees), stringsAsFactors = F)
mm.g2.frequencies[, 1] <- as.numeric(mm.g2.frequencies[, 1])
mm.g2.frequencies$Fraction <- mm.g2.frequencies$Freq / gorder(mm.g2.graph)
names(mm.g2.frequencies) <- c("Degree", "Frequency", "Fraction")
mm.g2.frequencies$Entity <- "g2"

degrees.frequencies <- rbind(mm.g1.frequencies, mm.g2.frequencies)

MakeDistributionPlot <- function(density.binned, degrees.frequencies, colors = c("blue3", "green3", "red3")) {
  plot <- ggplot() +
    geom_point(data = degrees.frequencies, aes(x = Degree, y = Frequency, color = Entity), alpha = 0.2) +
    #geom_line(data = density.binned, aes(x = degree, y = p, color = Entity), size = 1, linetype = "dashed") +
    scale_color_manual(values = colors) +
    scale_x_continuous("Degree", trans = "log2", breaks = 2^(0:20)) +
    scale_y_continuous("Frequency", trans = "log2") +
    geom_smooth(data = degrees.frequencies, aes(x = Degree, y = Frequency, color = Entity)) +
    ggtitle("Degree Distribution section networks (log-log)") +
    theme(axis.text.x = element_text(angle = 90, hjust = 1))
  return(plot)
}

plot <- MakeDistributionPlot(density.binned, degrees.frequencies)
plot
ggsave(paste(plots.path, "g1_g2_degree_distribution.png", sep = ""), width = w)
