# This script is to compare the perfomance between different input types of pathway matcher

###############################
# Load libraries

library(ggplot2)
require(cowplot)
library(stats)
source("loadData.R")

###############################
# First, load data

t <- load.data(averageSdByType = F)

###############################
# Second, find out the sizes that where measured for all types
allSizes <- unique(t$Size)
allTypes <- c("uniprotList", "peptideList", "uniprotListAndModSites")

added <- 0
commonSizes <- NULL
for(s in allSizes){
  containsAll <- T
  foundTypes <- t$Type[which(t$Size == s)]
  for(e in allTypes){
    if(!(e %in% foundTypes)){
      containsAll <- F
      break
    }
  }
  
  if(containsAll){
    added <- added + 1
    commonSizes[[added]] <- s  
  }
  
}
commonSizes

###############################
# Third, compare the values between types

proteins <- t[which(t$Type == "uniprotList" & t$Size %in% commonSizes),]
proteoforms <- t[which(t$Type == "uniprotListAndModSites" & t$Size %in% commonSizes),]
peptides <- t[which(t$Type == "peptideList" & t$Size %in% commonSizes),]

ratios <- as.data.frame(cbind(commonSizes, proteoforms$Mean / proteins$Mean, peptides$Mean / proteins$Mean))
names(ratios) <- c("Size", "Protein_VS_Proteoform", "Protein_VS_Peptides")
