#
# Plot the degree distribution of the vertices in a network
#

# Libraries

library(igraph)
library(plyr)
library(ggplot2)

source("../load_networks.R")

theme_set(theme_bw())

MakeDistributionPlot <- function(density.binned, degrees.frequencies, colors = c("blue3", "green3", "red3")) {
  plot <- ggplot() +
    geom_point(data = degrees.frequencies, aes(x = Degree, y = Fraction, color = Entity), alpha = 0.2) +
    geom_line(data = density.binned, aes(x = degree, y = p, color = Entity), size = 1, linetype = "dashed") +
    scale_color_manual(values = colors) +
    scale_x_continuous("Degree", trans = "log2", breaks = 2^(0:20)) +
    scale_y_continuous("Fraction", trans = "log2") +
    geom_smooth() +
    ggtitle("Degree Distribution section networks (log-log)") +
    theme(axis.text.x = element_text(angle = 90, hjust = 1))
   return(plot)
}

# ----------- Full protein and proteoform networks 

# Calculate binned degree distribution plot

proteins.full.degrees <- degree(proteins.full.graph)
proteins.full.density <- GetBinnedDegreeProbabilities(proteins.full.degrees)
proteins.full.density$Entity <- "proteins.full"

proteoforms.full.degrees <- degree(proteoforms.full.graph)
proteoforms.full.density <- GetBinnedDegreeProbabilities(proteoforms.full.degrees)
proteoforms.full.density$Entity <- "proteoform.full"

density.binned <- rbind(proteins.full.density, proteoforms.full.density)

# Calculate frequencies

proteins.full.degrees.frequencies <- as.data.frame(table(proteins.full.degrees), stringsAsFactors = F)
proteins.full.degrees.frequencies[, 1] <- as.numeric(proteins.full.degrees.frequencies[, 1])
proteins.full.degrees.frequencies$Fraction <- proteins.full.degrees.frequencies$Freq / gorder(proteins.full.graph)
names(proteins.full.degrees.frequencies) <- c("Degree", "Frequency", "Fraction")
proteins.full.degrees.frequencies$Entity <- "proteins.full"

proteoforms.full.degrees.frequencies <- as.data.frame(table(proteoforms.full.degrees), stringsAsFactors = F)
proteoforms.full.degrees.frequencies[, 1] <- as.numeric(proteoforms.full.degrees.frequencies[, 1])
proteoforms.full.degrees.frequencies$Fraction <- proteoforms.full.degrees.frequencies$Freq / gorder(proteoforms.full.graph)
names(proteoforms.full.degrees.frequencies) <- c("Degree", "Frequency", "Fraction")
proteoforms.full.degrees.frequencies$Entity <- "proteoform.full"

degrees.frequencies <- rbind(proteins.full.degrees.frequencies, proteoforms.full.degrees.frequencies)

## Make the plots

MakeDistributionPlot(density.binned, degrees.frequencies)
ggsave("plots/full_networks_degree_distribution.png", width=14, height=7)

# ----------- Section networks

# Calculate binned degree distribution plot

pp.degrees <- degree(pp.graph)
pp.density <- GetBinnedDegreeProbabilities(pp.degrees)
pp.density$Entity <- "pp"

mm.degrees <- degree(mm.graph)
mm.density <- GetBinnedDegreeProbabilities(mm.degrees)
mm.density$Entity <- "mm"

pm.degrees <- degree(pm.graph)
pm.density <- GetBinnedDegreeProbabilities(pm.degrees)
pm.density$Entity <- "pm"

density.binned <- rbind(mm.density, pp.density, pm.density)

# Calculate frequencies

mm.frequencies <- as.data.frame(table(mm.degrees), stringsAsFactors = F)
mm.frequencies[, 1] <- as.numeric(mm.frequencies[, 1])
mm.frequencies$Fraction <- mm.frequencies$Freq / gorder(mm.graph)
names(mm.frequencies) <- c("Degree", "Frequency", "Fraction")
mm.frequencies$Entity <- "mm"

pp.frequencies <- as.data.frame(table(pp.degrees), stringsAsFactors = F)
pp.frequencies[, 1] <- as.numeric(pp.frequencies[, 1])
pp.frequencies$Fraction <- pp.frequencies$Freq / gorder(pp.graph)
names(pp.frequencies) <- c("Degree", "Frequency", "Fraction")
pp.frequencies$Entity <- "pp"

pm.frequencies <- as.data.frame(table(pm.degrees), stringsAsFactors = F)
pm.frequencies[, 1] <- as.numeric(pm.frequencies[, 1])
pm.frequencies$Fraction <- pm.frequencies$Freq / gorder(pm.graph)
names(pm.frequencies) <- c("Degree", "Frequency", "Fraction")
pm.frequencies$Entity <- "pm"

degrees.frequencies <- rbind(mm.frequencies, pp.frequencies, pm.frequencies)

## Make the plots

MakeDistributionPlot(density.binned, degrees.frequencies)
ggsave("plots/network_sections_degree_distribution.png", width=14, height=7)

# ------------- section to protein full plots

density.binned <- rbind(proteins.full.density, mm.density)
density.binned$Entity <- factor(density.binned$Entity, levels = c("proteins.full", "mm"))
density.binned <- density.binned[order(density.binned$Entity), ]
degrees.frequencies <- rbind(mm.frequencies, proteins.full.degrees.frequencies)
degrees.frequencies$Entity <- factor(degrees.frequencies$Entity, levels = c("proteins.full", "mm"))
degrees.frequencies <- degrees.frequencies[order(degrees.frequencies$Entity), ]
MakeDistributionPlot(density.binned, degrees.frequencies, colors = c("orange3", "blue3"))
ggsave("plots/mm_to_proteins_full_degree_distribution.png", width=14, height=7)


density.binned <- rbind(proteins.full.density, pp.density)
density.binned$Entity <- factor(density.binned$Entity, levels = c("proteins.full", "pp"))
density.binned <- density.binned[order(density.binned$Entity), ]
degrees.frequencies <- rbind(pp.frequencies, proteins.full.degrees.frequencies)
degrees.frequencies$Entity <- factor(degrees.frequencies$Entity, levels = c("proteins.full", "pp"))
degrees.frequencies <- degrees.frequencies[order(degrees.frequencies$Entity), ]
MakeDistributionPlot(density.binned, degrees.frequencies, colors = c("orange3", "red3"))
ggsave("plots/pp_to_proteins_full_degree_distribution.png", width=14, height=7)

density.binned <- rbind(proteins.full.density, pm.density)
density.binned$Entity <- factor(density.binned$Entity, levels = c("proteins.full", "pm"))
density.binned <- density.binned[order(density.binned$Entity), ]
degrees.frequencies <- rbind(pm.frequencies, proteins.full.degrees.frequencies)
degrees.frequencies$Entity <- factor(degrees.frequencies$Entity, levels = c("proteins.full", "pm"))
degrees.frequencies <- degrees.frequencies[order(degrees.frequencies$Entity), ]
MakeDistributionPlot(density.binned, degrees.frequencies, colors = c("orange3", "green3"))
ggsave("plots/pm_to_proteins_full_degree_distribution.png", width=14, height=7)