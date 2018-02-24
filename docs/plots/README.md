# Plots about proteins in Reactome

## Number of pathways where each protein is participant


## Number of pathways where each protein+PTMSet is participant
r <- read.csv("ReactionCountPerProtein+PTMSet.csv", header = T, sep = ",")
ggplot(r, aes(reactionCount)) + geom_density(adjust = 1/2) + xlim(0,25) + ylim(0, 0.7)

## Number of reactions where each protein is participant

## Number of reactions where each protein+PTMSet is participant

## Number of ptms per protein

## Number of unique ptms per protein

## Number of types of ptms per protein

## Number of unique PTMSets per protein
