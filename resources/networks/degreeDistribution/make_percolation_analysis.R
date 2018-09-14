# Make percolation curve sampling and plotting 

# Load libraries ----

library(igraph)
library(ggplot2)
source("graphs.R")
source("degreeDistribution/percolation.R")

# Load undirected graphs
# (Notice that they are handled as undirected graphs)

source("load_networks.R")


## Percolation Section networks (uniform sizes) -----

replicates <- 2
factor <- 0.3
type <- "link"

mm.samples.link <- GetPercolationCurvePoints(mm.graph, factor = factor, replicates = replicates, entity = "mm", type = type)
write.table(mm.samples.link, paste("mm.samples.", type, ".csv"))
mm.plot.link <- PlotPercolationCurve(mm.samples.link, colors = c("blue3"))
mm.plot.link
ggsave(paste("mm_", type, "_percolation_curve_approximation.png"), width=14, height=7)

pp.samples.link <- GetPercolationCurvePoints(pp.graph, factor = factor, replicates = replicates, entity = "pp", type = "link")
pp.samples.node <- GetPercolationCurvePoints(pp.graph, factor = factor, replicates = replicates, entity = "pp", type = "node")
write.table(pp.samples.link, paste("pp.samples.", type, ".csv"))
pp.plot.link <- PlotPercolationCurve(pp.samples.link, colors = c("red3"))
pp.plot.link
ggsave(paste("pp_", type, "_percolation_curve_approximation.png"), width=14, height=7)

pm.samples.link <- GetPercolationCurvePoints(pm.graph, factor = factor, replicates = replicates, entity = "pm", type = type)
write.table(pm.samples.link, paste("pm.samples.", type, ".csv"))
pm.plot.link <- PlotPercolationCurve(pm.samples.link, colors = c("green3"))
pm.plot.link
ggsave(paste("pm_", type, "_percolation_curve_approximation.png"), width=14, height=7)

sections.samples.link <- rbind(mm.samples.link, pp.samples.link, pm.samples.link)
write.csv(sections.samples.link, paste("sections_", type, "_percolation_curve_approximation_samples.csv"), row.names=FALSE, na="")
sections.plot.link <- PlotPercolationCurve(sections.samples.link)
sections.plot.link

type <- "node"
mm.samples.node <- GetPercolationCurvePoints(mm.graph, factor = factor, replicates = replicates, entity = "mm", type = type)
pp.samples.node <- GetPercolationCurvePoints(pp.graph, factor = factor, replicates = replicates, entity = "pp", type = type)
pm.samples.node <- GetPercolationCurvePoints(pm.graph, factor = factor, replicates = replicates, entity = "pm", type = type)
sections.samples.node <- rbind(mm.samples.node, pp.samples.node, pm.samples.node)
sections.plot.node <- PlotPercolationCurve(sections.samples.node, showRelSize = F)
sections.plot.node
sections.plot.node.link <- PlotPercolationCurve(sections.samples.link, showRelSize = T)
sections.plot.node.link
ggsave(paste("sections_", type, "_percolation_curve_approximation.png"), width=14, height=7)

# Full networks -------------- 

proteins.full.samples <- GetLinkPercolationCurvePoints(graph = proteins.full.graph, replicates = replicates, 
                                                       DefineSizes = GetLog2Bins, entity = "proteins.full")
proteins.full.plot <- PlotPercolationCurve(proteins.full.samples, c("orange3"))
proteins.full.plot
ggsave("plots/proteins_full_link_percolation_curve_approximation.png", width=14, height=7)

proteoforms.full.samples <- GetLinkPercolationCurvePoints(graph = proteoforms.full.graph, replicates = replicates, 
                                                          DefineSizes = GetLog2Bins, entity = "proteoforms.full")
proteoforms.full.plot <- PlotPercolationCurve(proteoforms.full.samples, c("turquoise3"))
proteoforms.full.plot
ggsave("plots/proteoforms_full_link_percolation_curve_approximation.png", width=14, height=7)

full.samples <- rbind(proteins.full.samples, proteoforms.full.samples)
write.csv(full.samples, "full_networks_link_percolation_curve_approximation_samples.csv", row.names=FALSE, na="")
full.plot <- PlotPercolationCurve(samples)
full.plot
ggsave("plots/full_networks_link_percolation_curve_approximation.png", width=14, height=7)
