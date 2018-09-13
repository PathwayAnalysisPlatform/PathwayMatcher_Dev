# Verify the general characteristics of the protein and proteoform networks
# Notice that they are handled as undirected graphs.

# Load libraries

library(igraph)
library(ggplot2)
source("graphs.R")
source("degreeDistribution/percolation.R")

# Load undirected graphs
source("load_networks.R")

################################################################################

# Get approximated percolation curve

write.csv(samples, "proteoformNetworkSamples.csv", row.names=FALSE, na="")

# Section networks -----

replicates <- 10
measures <- 50

mm.samples <- GetLinkPercolationCurvePoints(graph = mm.graph, measures = measures, replicates = reps, entity = "mm")
mm.plot <- PlotPercolationCurve(mm.samples, c("blue3"))
mm.plot
ggsave("mm__link_percolation_curve_approximation.png", width=14, height=7)

pp.samples <- GetLinkPercolationCurvePoints(graph = pp.graph, measures = measures, replicates = reps, entity = "pp")
pp.plot <- PlotPercolationCurve(pp.samples, c("red3"))
pp.plot
ggsave("pp__link_percolation_curve_approximation.png", width=14, height=7)

pm.samples <- GetLinkPercolationCurvePoints(graph = pm.graph, measures = measures, replicates = reps, entity = "pm")
pm.plot <- PlotPercolationCurve(pm.samples, c("green3"))
pm.plot
ggsave("pm__link_percolation_curve_approximation.png", width=14, height=7)

sections.samples <- rbind(mm.samples, pp.samples, pm.samples)
write.csv(sections.samples, "sections_percolation_curve_approximation_samples.csv", row.names=FALSE, na="")
sections.plot <- PlotPercolationCurve(sections.samples)
sections.plot
ggsave("sections__link_percolation_curve_approximation.png", width=14, height=7)

# Full networks -------------- 

proteins.full.samples <- GetLinkPercolationCurvePoints(graph = proteins.full.graph, replicates = reps, 
                                                       DefineSizes = GetLog2Bins, entity = "proteins.full")
proteins.full.plot <- PlotPercolationCurve(proteins.full.samples, c("orange3"))
proteins.full.plot
ggsave("plots/proteins_full_link_percolation_curve_approximation.png", width=14, height=7)

proteoforms.full.samples <- GetLinkPercolationCurvePoints(graph = proteoforms.full.graph, replicates = reps, 
                                                          DefineSizes = GetLog2Bins, entity = "proteoforms.full")
proteoforms.full.plot <- PlotPercolationCurve(proteoforms.full.samples, c("turquoise3"))
proteoforms.full.plot
ggsave("plots/proteoforms_full_link_percolation_curve_approximation.png", width=14, height=7)

full.samples <- rbind(proteins.full.samples, proteoforms.full.samples)
write.csv(full.samples, "full_networks_link_percolation_curve_approximation_samples.csv", row.names=FALSE, na="")
full.plot <- PlotPercolationCurve(samples)
full.plot
ggsave("plots/full_networks_link_percolation_curve_approximation.png", width=14, height=7)
