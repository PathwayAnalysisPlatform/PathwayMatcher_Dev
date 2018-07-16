library(ggplot2)

df <- read.csv(file = "PTM_Frequencies.csv", header = T)
names(df) <- c("Frequency", "Type")
df$Type <- gsub("\"", "", df$Type) 
df$Type <- factor(df$Type, levels = df$Type)

plot <- ggplot(df, aes(x=Type, y=Frequency)) + geom_bar(stat="identity", fill="steelblue") +
  theme(axis.text.x = element_text(angle = 90, hjust = 1), legend.position = "none") 

plot
