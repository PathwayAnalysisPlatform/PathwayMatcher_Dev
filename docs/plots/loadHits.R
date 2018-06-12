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

load.hits.Merged <- function(fileProteins = "../data/ReactomeModifiedProteinFrequencies.csv", 
                      fileProteoforms = "../data/ReactomeModifiedProteinProteoformsFrequencies.csv"){
  
  # For proteoforms the data comes: protein, ptms, reactionCount, pathwayCount
  # For proteins the data comes: protein, reactionCount, pathwayCount
  
  # It has to be transformed to: Count, Type, Hit
  # Count is the number
  # Type is Protein|Proteoform
  # Hit is Reactions|Pathways
  
  proteins <- read.csv("HitsPerProtein.csv", sep = ",", header = T)  
  proteoforms <- read.csv("HitsPerProteoform.csv", sep = ",", header = T) 
  
  colnames(proteins) <- c("protein", "reactionCount", "pathwayCount")
  colnames(proteoforms) <- c("protein", "ptms", "reactionCount", "pathwayCount")
  
  reactionProteins <- data.frame(proteins$reactionCount, "Protein", "Reactions") 
  colnames(reactionProteins) <- c("Count", "Type", "Hit")
  
  reactionProteoforms <- data.frame(proteoforms$reactionCount, "Proteoform", "Reactions") 
  colnames(reactionProteoforms) <- c("Count", "Type", "Hit")
  
  pathwayProteins <- data.frame(proteins$pathwayCount, "Protein", "Pathway") 
  colnames(pathwayProteins) <- c("Count", "Type", "Hit")
  
  pathwayProteoforms <- data.frame(proteoforms$pathwayCount, "Proteoform", "Pathway") 
  colnames(pathwayProteoforms) <- c("Count", "Type", "Hit")

  # Merge dataframes
  
  hits <- rbind(reactionProteins, reactionProteoforms, pathwayProteins, pathwayProteoforms)
  
  hits
}