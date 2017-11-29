# Script to create Time comparison plots for the Input processor for Reactome

library(ggplot2)

times_BufferedReader <- read.csv("Times_Proteoforms_Custom_v2_vs_v3.txt", sep = ",", header = T)
times_BufferedReader$Reader <- "BufferedReader"

times_channel <- read.csv("Times_channel.csv", sep = ",", header = T)
times_channel$Reader <- "Channel"

times_readAllBytes <- read.csv("Times_readAllBytes.csv", sep = ",", header = T)
times_readAllBytes$Reader <- "ReadAllBytes"

times <- rbind(times_BufferedReader, times_channel, times_readAllBytes)
times$ms <- as.numeric(times$ms)

times <- read.csv("Times_Proteoforms_Custom_v2_vs_v3.txt", sep = ",", header = T)
times <- times[which(times$Repetition > 1 & times$Version == "InputFormat_v3"),]
times <- aggregate(times$ms, list(times$Version,times$Size), function(x) {
  mean(x[!(abs(x - mean(x)) > 1*sd(x))])
})
names(times) <- c("Version", "Size", "ms")


ggplot(times, aes(x=Size, y=ms, color=Version)) +
  geom_line() +
  geom_smooth(se = FALSE) +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))


grep("uniprotList_",times$entries)

regexpr("_", times$entries)
