load.hits <- function(fileProteins = "../data/ReactomeModifiedProteinFrequencies.csv", 
                      fileProteoforms = "../data/ReactomeModifiedProteinProteoformsFrequencies.csv"){
  
  # Read all times
  
  proteins <- read.csv(fileProteins, sep = ",", header = T)  
  proteoforms <- read.csv(fileProteoforms, sep = ",", header = T)  
  
  proteins$Type <- "Protein"
  proteoforms$Type <- "Proteoform"

  # Merge dataframes
  
  hits <- rbind(proteins[,c(2,3)], proteoforms[,c(3,4)])
  colnames(hits) <- c("Count", "Type")
  
  hits
}