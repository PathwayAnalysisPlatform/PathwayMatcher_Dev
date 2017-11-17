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

# Create plots

t <- aggregate(t$Time, list(t$Type,t$Size), function(x) {
  mean <- mean(x[!(abs(x - mean(x)) > 1*sd(x))])
  as.data.frame(list("mean" = mean, "sd" = sd(x)))
})
names(t) <- c("Type", "Size", "Time")
t$Mean <- as.numeric(t$Time[,1])
t$sd <- as.numeric(t$Time[,2])
t$Time <- NULL

t$High <- t$Mean + (1*t$sd)
t$Low <- t$Mean - (1*t$sd)

####
# Optional sd average by Type
d <- aggregate(q$sd, list(q$Type), mean)
names(d) <- c("Type", "sd")

t <- merge.data.frame(t, d, by = "Type")
t$High <- t$Mean + (1*t$sd.y)
t$Low <- t$Mean - (1*t$sd.y)
###

plot.snps <- ggplot(t[which(t$Type == "rsidList"),], aes(x = Size)) + 
  geom_ribbon(aes(ymin = Low, ymax = High), fill = "grey70") +
  geom_line(aes(y = Mean), color = "#de2d26", size = 1) +
  theme_bw() + ylab("Time [s]") + xlab("# SNPs") + 
  scale_x_continuous(breaks=c(600000,1200000,1800000)) + scale_y_continuous(limits = c(80, 130))
plot.snps

plot.proteins <- ggplot(t[which(t$Type == "uniprotList"),], aes(x = Size)) + 
  geom_ribbon(aes(ymin = Low, ymax = High), fill = "grey70") +
  geom_line( aes(y = Mean), color = "#2b8cbe", size = 1) +
  theme_bw() + ylab("Time [s]") + xlab("# Proteins")
plot.proteins

plot.peptides <- ggplot(t[which(t$Type == "peptideList"),], aes(x = Size)) + 
  geom_ribbon(aes(ymin = Low, ymax = High), fill = "grey70") +
  geom_line(aes(y = Mean), color = "#feb24c", size = 1) +
  theme_bw() + ylab("Time [s]") + xlab("# Peptides")
plot.peptides

plot.proteoforms <- ggplot(t[which(t$Type == "uniprotListAndModSites"),], aes(x = Size)) + 
  geom_ribbon(aes(ymin = Low, ymax = High), fill = "grey70") +
  geom_line(aes(y = Mean), color = "#31a354", size = 1) +
  theme_bw() + ylab("Time [s]") + xlab("# Proteoforms") 
plot.proteoforms

# Put the plots together in a grid

plot_grid(
  plot.snps, 
  plot.proteins, 
  plot.peptides,
  plot.proteoforms,
  labels = c("A", "B", "C", "D"))

##############################################
# Extra functions
#t <- aggregate(t$Time, list(t$Type,t$Size), function(x) {
#  mean(x[!(abs(x - mean(x)) > 1*sd(x))])
#})
#names(t) <- c("Type", "Size", "Time")

#############################################
# Other version of the plot

q <- aggregate(t$Time, list(t$Type,t$Size), function(x) { quantile(x, c(0.025, 0.5, 0.975)) })
q$Low <- q$Time[,1]
q$Mean <- q$Time[,2]
q$High <- q$Time[,3]

q <- aggregate(t$Time, list(t$Type,t$Size), function(x) {
  mean <- mean(x[!(abs(x - mean(x)) > 2*sd(x))])
  as.data.frame(list("mean" = mean, "sd" = sd(x)))
})
names(q) <- c("Type", "Size", "Time")
q$Mean <- as.numeric(q$Time[,1])
q$sd <- as.numeric(q$Time[,2])
d <- aggregate(q$sd, list(q$Type), mean)
q$Low <- q$Mean + mean
names(d) <- c("Type", "sd")

var <- d$sd[which(d$Type == "rsidList")]
t$High[which(t$Type == "rsidList")] <- t$Mean[which(t$Type == "rsidList")] + var
var <- d$sd[which(d$Type == "uniprotList")]
t$High[which(t$Type == "uniprotList")] <- t$Mean[which(t$Type == "uniprotList")] + var
var <- d$sd[which(d$Type == "peptideList")]
t$High[which(t$Type == "peptideList")] <- t$Mean[which(t$Type == "peptideList")] + var
var <- d$sd[which(d$Type == "uniprotListAndModSites")]
t$High[which(t$Type == "uniprotListAndModSites")] <- t$Mean[which(t$Type == "uniprotListAndModSites")] + var

type <- "rsidList"
plot.snps <- ggplot(q[which(q$Type == type),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#de2d26", size = 2, method = "loess") +
  geom_ribbon(x = Size, aes(ymin = Mean + var, ymax = Mean + var), fill = "blue", alpha = 0.5) +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# SNPs")
plot.snps

type <- "uniprotList"
var <- 2*d[which(d$Type == type),2]
plot.proteins <- ggplot(q[which(q$Type == type),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#2b8cbe", size = 2, se = F) +
  geom_ribbon(aes(ymin=Mean-(2*meanSd),ymax=Mean+(2*meanSd)), fill="#bdbdbd", alpha="0.5") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# Proteins")
plot.proteins

type <- "peptideList"
var <- 2*d[which(d$Type == type),2]
plot.proteins <- ggplot(t[which(t$Type == "peptideList" & t$Size >= 6000),], aes(x = Size, y = Time)) + 
  geom_smooth(method = "lm", level = 0.95, color = "#feb24c") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# Peptides")
plot.proteins

type <- "uniprotListAndModSites"
plot.proteoforms <- ggplot(q[which(q$Type == "uniprotListAndModSites"),], aes(x = Size)) + 
  geom_smooth(aes(y=Mean), color = "#31a354", size = 2, se = F) +
  geom_ribbon(aes(ymin=Low,ymax=High), fill="#bdbdbd", alpha="0.5") +
  theme_bw() +
  ylab("Time [s]") +
  xlab("# Proteoforms")
plot.proteoforms
