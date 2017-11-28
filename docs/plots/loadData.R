load.data <- function(fileName = "times.csv", averageSdByType = F){
  # Read all times
  t <- read.csv(fileName, sep = ",", header = T)  
  
  # Convert to minutes
  t$Time <- t$ms / (1000.0*60.0)          
  
  # Keep only sizes multiple of 100
  t <- t[which((t$Size)%%100 == 0),]
  
  # Calculate mean and standard deviation for each size
  t <- aggregate(t$Time, list(t$Type,t$Size), function(x) {
    mean <- mean(x[!(abs(x - mean(x)) > 1*sd(x))])
    as.data.frame(list("mean" = mean, "sd" = sd(x)))
  })

  # Set the right names, after aggregate changed the names
  names(t) <- c("Type", "Size", "Time")
  
  # Restructure the dataframe so that columns are: Type, Size, Mean, sd, High, Low
  t$Mean <- as.numeric(t$Time[,1])  
  t$sd <- as.numeric(t$Time[,2])
  t$Time <- NULL
  t$High <- t$Mean + (1*t$sd)
  t$Low <- t$Mean - (1*t$sd)
  
  ##########################################
  # Optional sd average by Type
  
  if(averageSdByType == T){
    d <- aggregate(t$sd, list(t$Type), mean)
    names(d) <- c("Type", "sd")
    
    t <- merge.data.frame(t, d, by = "Type")
    t$High <- t$Mean + (1*t$sd.y)
    t$Low <- t$Mean - (1*t$sd.y)  
    t$sd <- t$sd.y
    t$sd.x <- NULL
    t$sd.y <- NULL
  }

  ##########################################
  # Return the dataframe with the times
  t
}
