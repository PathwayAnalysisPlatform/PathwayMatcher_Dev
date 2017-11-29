library(ggplot2)

times <- read.csv("results.txt", sep = "\t", header = F)
names(times) <- c("size", "ms")

times$type <- sub('_.*','\\1',times$size)
times$size <- sub('.*_', '\\1', times$size)



df <- times[which(times$type != 'rsidList' & times$type != "peptideList" & times$size != "05000"),]
df[df=="uniprotList"] <- "Protein"
df[df=="uniprotListAndSites"] <- "Protein with PTM Sites"
df[df=="uniprotListAndModSites"] <- "Protein with PTM Sites and Types"

df <- times[which(times$type == 'peptideList'),]
df[df=="peptideList"] <- "Peptide list"

df <- times[which(times$type == 'rsidList'),]
df[df=="rsidList"] <- "SNP list"

ggplot(df, aes(x=size, y=ms, group=type, color=type)) +
  geom_point() + 
  geom_line() +
  theme(axis.text.x = element_text(angle = 90, hjust = 1))


 grep("uniprotList_",times$entries)

regexpr("_", times$entries)
