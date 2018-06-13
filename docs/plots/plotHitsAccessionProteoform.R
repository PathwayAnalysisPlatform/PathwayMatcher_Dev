# Scatter plot with y: Number of pathways mapped per proteoform
#                   x: Number of pathways mapped by the protein accession of the same proteoforms

###############################
# Load libraries 

library(ggplot2)
library(gtable)
library(grid)

###############################
# Load data

# For proteoforms the data comes: protein, ptms, reactionCount, pathwayCount
# For proteins the data comes: protein, reactionCount, pathwayCount

proteins <- read.csv("HitsPerProtein.csv", sep = ",", header = T)
proteoforms <- read.csv("HitsPerProteoform.csv", sep = ",", header = T) 

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
# Create plots

scatter.plot <- ggplot() + 
  geom_abline(intercept = 0, slope = 1, linetype="dotted", color = "darkgray") +
  geom_point(data = hits, aes(x=ByAccessionLog, y=ByProteoformLog, color=Ratio), alpha=0.2) +
  scale_color_gradient(low="gray", high="black") +
  geom_hline(yintercept=medianProteoformLog, linetype="dashed", color = "green3") +
  geom_vline(xintercept=medianAccessionLog, linetype="dashed", color = "blue3") +
  scale_x_continuous(name = "Accession", breaks = c(medianAccessionLog, 0:2), labels = c(10^medianAccessionLog, 10^(0:2))) +
  scale_y_continuous(name = "Proteoform", breaks = c(medianProteoformLog, 0:2), labels = c(10^medianProteoformLog, 10^(0:2))) +
  theme_bw(base_size = 11) +
  theme(legend.position = "none",
        axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5, color = c("blue3", rep("black", 3))),
        axis.ticks.x = element_line(color = c("blue3", rep("black", 3))),
        axis.text.y = element_text(color = c("green3", rep("black", 3))),
        axis.ticks.y = element_line(color = c("green3", rep("black", 3))),
        panel.grid.major = element_line(color = c(NA, rep("grey95", 3))),
        panel.grid.minor = element_blank())
scatter.grob <- ggplotGrob(scatter.plot)

yMax <- max(density(hits$ByAccessionLog)$y)
density.accession <- ggplot() +
    geom_density(data=hits, aes(x=ByAccessionLog), color="blue3", fill="blue3", alpha=0.2) +
    geom_vline(xintercept=medianAccessionLog, linetype="dashed", color = "blue3") +
    scale_y_continuous(expand=c(0, 0), limits = c(0, 1.05 * yMax)) +
    theme_bw(base_size = 11) +
    theme(axis.title = element_blank(),
          axis.text = element_blank(),
          axis.ticks = element_blank(),
          panel.grid = element_blank())
density.accession.grob <- ggplotGrob(density.accession)

yMax <- max(density(hits$ByProteoformLog)$y)
density.proteoform <- ggplot() +
    geom_density(data=hits, aes(x=ByProteoformLog), color="green3", fill="green3", alpha=0.2) +
    geom_vline(xintercept=medianProteoformLog, linetype="dashed", color = "green3") +
    scale_y_continuous(expand=c(0, 0), limits = c(0, 1.05 * yMax)) +
    theme_bw(base_size = 11) +
    theme(axis.title = element_blank(),
          axis.text = element_blank(),
          axis.ticks = element_blank(),
          panel.grid = element_blank()) +
    coord_flip()
density.proteoform.grob <- ggplotGrob(density.proteoform)

###############################
# Arrange plots

merge.grob1 <- rbind(scatter.grob[1:5, ], density.accession.grob[6, ], size = "first")
merge.grob1 <- rbind(merge.grob1, scatter.grob[6:nrow(scatter.grob), ], size = "last")

merge.grob2 <- rbind(density.proteoform.grob[1:5, ], density.proteoform.grob[5, ], size = "first")
merge.grob2 <- rbind(merge.grob2, density.proteoform.grob[6:nrow(density.proteoform.grob), ], size = "last")

merge.grob <- cbind(merge.grob1[, 1:4], merge.grob2[, 4], size = "first")
merge.grob <- cbind(merge.grob, merge.grob1[, 5:ncol(merge.grob1)], size = "first")

merge.grob$widths[5] <- unit(0.3, "null")
merge.grob$heights[6] <- unit(0.25, "null")

###############################
# Export as figure

png("PathwayProteoformAccession.jpg", height = 9, width = 9, units = "cm", res = 300)
grid.draw(merge.grob)
dummy <- dev.off()


