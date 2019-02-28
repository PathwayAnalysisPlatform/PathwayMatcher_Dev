# Scatter plot with y: Number of pathways mapped per proteoform
#                   x: Number of pathways mapped by the protein accession of the same proteoforms

###############################
# Load libraries 

library(ggplot2)
library(gtable)
library(grid)
library(scico)


###############################
# Load data

# For proteoforms the data comes: protein, ptms, reactionCount, pathwayCount
# For proteins the data comes: protein, reactionCount, pathwayCount

proteins <- read.csv("docs/publication/tables/HitsPerProtein.csv.gz", sep = ",", header = T)
proteoforms <- read.csv("docs/publication/tables/HitsPerProteoform.csv.gz", sep = ",", header = T) 

colnames(proteins) <- c("protein", "reactionCount", "pathwayCount")
colnames(proteoforms) <- c("protein", "ptms", "reactionCount", "pathwayCount")

hits <- merge(proteins[,c(1,3)], proteoforms[,c(1,4)], by = "protein")
colnames(hits) <- c("protein", "ByAccession", "ByProteoform")


###############################
# Extract values to plot

hits$ByAccessionLog <- log10(hits$ByAccession)
hits$ByProteoformLog <- log10(hits$ByProteoform)
hits$RatioLog <- log10(hits$ByAccession/hits$ByProteoform)

medianAccessionLog <- median(hits$ByAccessionLog)
medianProteoformLog <- median(hits$ByProteoformLog)


###############################
# Plotting parameters

palette <- 'cork'
accessionColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
proteoformColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)


###############################
# Create plots

scatter.plot <- ggplot() + 
  geom_abline(intercept = 0, slope = 1, linetype="dotted", color = "darkgray") +
  geom_point(data = hits, aes(x=ByAccessionLog, y=ByProteoformLog, color=RatioLog), alpha=0.2) +
  scale_color_gradient(low="gray", high="black") +
  geom_hline(yintercept=medianProteoformLog, linetype="dashed", color = proteoformColor) +
  geom_vline(xintercept=medianAccessionLog, linetype="dashed", color = accessionColor) +
  scale_x_continuous(name = "Gene", breaks = c(medianAccessionLog, 0:2), labels = c(10^medianAccessionLog, 1, "", 100)) +
  scale_y_continuous(name = "Proteoform", breaks = c(medianProteoformLog, 0:2), labels = c(10^medianProteoformLog, 10^(0:2))) +
  theme_bw(base_size = 11) +
  theme(legend.position = "none",
        axis.text.x = element_text(color = c(accessionColor, rep("black", 3))),
        axis.ticks.x = element_line(color = c(accessionColor, rep("black", 3))),
        axis.text.y = element_text(color = c(proteoformColor, rep("black", 3))),
        axis.ticks.y = element_line(color = c(proteoformColor, rep("black", 3))),
        panel.grid.major = element_line(color = c(NA, rep("grey95", 3))),
        panel.grid.minor = element_blank(),
        panel.border = element_rect(color = "white"),
        axis.line = element_line(color = "black", size = 0.25))
scatter.grob <- ggplotGrob(scatter.plot)

yMax <- max(density(hits$ByAccessionLog)$y)
density.accession <- ggplot() +
    geom_density(data=hits, aes(x=ByAccessionLog), color=accessionColor, fill=accessionColor, alpha=0.2) +
    geom_vline(xintercept=medianAccessionLog, linetype="dashed", color = accessionColor) +
    scale_y_continuous(expand=c(0, 0), limits = c(0, 1.05 * yMax)) +
    theme_bw(base_size = 11) +
    theme(axis.title = element_blank(),
          axis.text = element_blank(),
          axis.ticks = element_blank(),
          panel.grid = element_blank(),
          panel.border = element_rect(color = "white"))
density.accession.grob <- ggplotGrob(density.accession)

yMax <- max(density(hits$ByProteoformLog)$y)
density.proteoform <- ggplot() +
    geom_density(data=hits, aes(x=ByProteoformLog), color=proteoformColor, fill=proteoformColor, alpha=0.2) +
    geom_vline(xintercept=medianProteoformLog, linetype="dashed", color = proteoformColor) +
    scale_y_continuous(expand=c(0, 0), limits = c(0, 1.05 * yMax)) +
    theme_bw(base_size = 11) +
    theme(axis.title = element_blank(),
          axis.text = element_blank(),
          axis.ticks = element_blank(),
          panel.grid = element_blank(),
          panel.border = element_rect(color = "white")) +
    coord_flip()
density.proteoform.grob <- ggplotGrob(density.proteoform)


###############################
# Arrange plots

merge.grob1 <- rbind(scatter.grob[1:5, ], density.accession.grob[7, ], size = "first")
merge.grob1 <- rbind(merge.grob1, scatter.grob[6:nrow(scatter.grob), ], size = "last")

merge.grob2 <- rbind(density.proteoform.grob[1:5, ], density.proteoform.grob[6, ], size = "first")
merge.grob2 <- rbind(merge.grob2, density.proteoform.grob[6:nrow(density.proteoform.grob), ], size = "last")

merge.grob <- cbind(merge.grob1[, 1:5], merge.grob2[, 5], size = "first")
merge.grob <- cbind(merge.grob, merge.grob1[, 6:ncol(merge.grob1)], size = "first")

merge.grob$widths[6] <- unit(0.15, "null")
merge.grob$heights[6] <- unit(0.15, "null")

###############################
# Export as figure

png("docs/publication/plots/fig_3B.png", height = 12, width = 12, units = "cm", res = 600)
grid.draw(merge.grob)
dummy <- dev.off()


