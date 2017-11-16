# Script to plot the performance for each type of input to PathwayMatcher

# INPUT: times.csv, it contains the time measures for the samples of each time in extended format (one measure, one line).
# OUTPUT: the grid plot of the four types of input

library(ggplot2)
require(cowplot)
library(stats)

# Read all times
t <- read.csv("times.csv", sep = ",", header = T)
t$Time <- t$ms / 1000.0 
t <- t[which((t$Size)%%100 == 0),]

q <- aggregate(t$Time, list(t$Type,t$Size), function(x) { quantile(x, c(0.025, 0.5, 0.975)) })
names(q) <- c("Type", "Size", "Time")
q$Low <- q$Time[,1]
q$Mean <- q$Time[,2]
q$High <- q$Time[,3]

# Create plots

plot.snps <- ggplot(q[which(q$Type == "rsidList"),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#de2d26", size = 2) +
  geom_ribbon(aes(ymin=Low,ymax=High), fill="#bdbdbd", alpha="0.5") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# SNPs")
plot.snps

plot.proteins <- ggplot(q[which(q$Type == "uniprotList"),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#2b8cbe", size = 2, se = F) +
  geom_ribbon(aes(ymin=Low,ymax=High), fill="#bdbdbd", alpha="0.5") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# Proteins")
plot.proteins

plot.peptides <- ggplot(q[which(q$Type == "peptideList" & q$Size >= 6000),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#feb24c", size = 2, se = F) +
  geom_ribbon(aes(ymin=Low,ymax=High), fill="#bdbdbd", alpha="0.5") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# Peptides")
plot.peptides

plot.proteoforms <- ggplot(q[which(q$Type == "uniprotListAndModSites"),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#31a354", size = 2, se = F) +
  geom_ribbon(aes(ymin=Low,ymax=High), fill="#bdbdbd", alpha="0.5") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# Proteoforms")
plot.proteoforms

# Put the plots together in a grid

plot_grid(
  plot.snps, 
  plot.proteins, 
  plot.peptides,
  plot.proteoforms,
  labels = c("A", "B", "C", "D"))

##############################################

#t <- aggregate(t$Time, list(t$Type,t$Size), function(x) {
#  mean(x[!(abs(x - mean(x)) > 1*sd(x))])
#})
#names(t) <- c("Type", "Size", "Time")
