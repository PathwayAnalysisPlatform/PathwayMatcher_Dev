# Scatter plot with y: Number of pathways mapped per proteoform
#                   x: Number of pathways mapped by the protein accession of the same proteoforms

###############################
# Load libraries 

library(ggplot2)
require(cowplot)
library(stats)

###############################
# First, load data

# For proteoforms the data comes: protein, ptms, reactionCount, pathwayCount
# For proteins the data comes: protein, reactionCount, pathwayCount

proteins <- read.csv("HitsPerProtein.csv", sep = ",", header = T)  
proteoforms <- read.csv("HitsPerProteoform.csv", sep = ",", header = T) 

colnames(proteins) <- c("protein", "reactionCount", "pathwayCount")
colnames(proteoforms) <- c("protein", "ptms", "reactionCount", "pathwayCount")

hits <- merge(proteins[,c(1,3)], proteoforms[,c(1,4)], by = "protein")
colnames(hits) <- c("protein", "ByAccession", "ByProteoform")

hits$Ratio <- log10(hits$ByAccession/hits$ByProteoform)

###############################
# Second, create plots

plot.scatter <- ggplot(hits, aes(x=ByAccession, y=ByProteoform, color=Ratio)) + geom_point() +
  scale_x_log10() +
  scale_y_log10() +
  scale_color_gradient(low="gray", high="blue3") +
  geom_abline(intercept = 0, slope = 1, linetype="dashed", color = "darkgray") +
  geom_hline(yintercept=mean(hits$ByProteoform), linetype="dashed", color = "black") +
  geom_vline(xintercept=mean(hits$ByAccession), linetype="dashed", color = "black") +
  theme_bw()
plot.scatter