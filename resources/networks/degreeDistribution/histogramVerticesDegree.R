#
# Plotthe degree distribution of the vertices in a network
#

# Libraries

library(ggplot2)
library(igraph)
library(plyr)
library(ggplot2)

source("graphs.R")

# Load protein data

proteins.graph <- loadGraph("datasets/all_proteins.tsv.gz")
proteoforms.graph <- loadGraph("datasets/all_proteoforms.tsv.gz")
onlyProteins.graph <- loadGraph("datasets/protein_to_protein.tsv.gz")
onlyModified.graph <- loadGraph("datasets/proteoform_to_proteoform.tsv.gz")
mixed.graph <- loadGraph("datasets/protein_to_proteoform.tsv.gz")

proteoformDegSeq <- degree(proteoformGraph)                 # Calculate the degrees
proteoformDegSeq <- as.data.frame(proteoformDegSeq)         # Convert to data frame
names(proteoformDegSeq) <- c("degree")                      # Set the column name
proteoformDegSeq$entity <- "proteoform"

degSeq <- rbind(proteinDegSeq, proteoformDegSeq)

# Get mean of each entity type
cdat <- ddply(degSeq, "entity", summarise, degree.mean=mean(degree))
cdat

# Make count plot

plot <- ggplot(degSeq, aes(x=degree)) + 
  geom_histogram(binwidth=1, colour="black", fill="white") + 
  scale_x_discrete(limits = c(0, 1000)) +
  facet_grid(entity ~ .) +
  geom_vline(data=cdat, aes(xintercept=degree.mean), linetype="dashed", size=1, colour="red") +
  ggtitle("Degree counts")
plot

# Count frequencies
proteinDegFrequencies <- as.data.frame(table(proteinDegSeq))
proteoformDegFrequencies <- as.data.frame(table(proteoformDegSeq))

# Get fractions
totalProtein <- nrow(degSeq[degSeq$entity == "protein", ])
proteinDegFrequencies$fraction <- proteinDegFrequencies$Freq/totalProtein

totalProteoform <- nrow(degSeq[degSeq$entity == "proteoform", ])
proteoformDegFrequencies$fraction <- proteoformDegFrequencies$Freq/totalProteoform

degFrequencies <- rbind(proteinDegFrequencies, proteoformDegFrequencies)

# Make fraction plot
cdat <- ddply(degSeq, "entity", summarise, degree.mean=mean(degree))

degFrequencies$degree=as.numeric(levels(degFrequencies$degree))[degFrequencies$degree]

plot <- ggplot(degFrequencies, aes(x=degree)) +
  geom_col(aes(x=degree,y=fraction), position="identity") +
  scale_x_continuous(limits = c(0,4000)) +
  theme_bw() +
  facet_grid(entity ~ .) +
  geom_vline(data=cdat, aes(xintercept=degree.mean), linetype="dashed", size=1, colour="red") +
  ggtitle("Degree distribution")
plot

# Make log log degree distribution plot
plot <- ggplot(degFrequencies) + 
  geom_col(aes(x=degree,y=fraction), position="identity") +
  scale_x_log10(breaks=c(1, 10, 100, 1000)) +
  scale_y_continuous(limits=c(0, 0.1)) +
  facet_grid(entity ~ .) +
  geom_vline(data=cdat, aes(xintercept=degree.mean), linetype="dashed", size=1, colour="red") +
  ggtitle("Degree distribution")
plot

# Save the plot

png("plots/histogramDegreeDistribution.png", height = 9, width = 9, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()
