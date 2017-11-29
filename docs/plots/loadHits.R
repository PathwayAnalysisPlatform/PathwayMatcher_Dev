load.hits <- function(fileProteins = "../data/ReactomeModifiedProteinFrequencies.csv", 
                      fileProteoforms = "../data/ReactomeModifiedProteinProteoformsFrequencies.csv"){
  
  # Read all times
  
  proteins <- read.csv(fileProteins, sep = ",", header = T)  
  proteoforms <- read.csv(fileProteoforms, sep = ",", header = T)  
  
  proteins$Type <- "Proteins"
  proteoforms$Type <- "Proteoforms"

  # Merge dataframes
  
  hits <- rbind(proteins[,c(1,4,5)], proteoforms[,c(1,4,5)])
}