library(igraph)
library(data.table)

source("graphs.R") 

# Library for percolation theory studies on pathway networks

GetMoment <- function(moment = 1, graph) {
  
  #' Get mth moment for the degree distribution of the graph.
  #' 
  #' Args:
  #'   moment: number of moment. Default is 1
  #'   graph: igraph object
  #' 
  #' Returns:
  #'   Double value of the mth moment
  
  degreeSequence <- degree(graph, v = V(graph), mode = c("all", "out", "in", "total"), loops = FALSE, normalized = FALSE)
  
  result = sum(sapply(as.data.frame.vector(degreeSequence), function (x) x**moment))/gorder(graph)
  
  return(result)
}

# TODO(LuisFranciscoHS): Get the right formula and update the code
GetPercolationThreshold <- function(graph) {
  
  #' Get percolation threshold probability (link percolation) 
  #' 
  #' It is calculated using the derivative of the quotiend of the first moment, and the difference between the second moment and the first. 
  #' The moments are calculated over the degree sequence of the graph.
  #' Only link removal is considered here, therefore nodes with degree 0 are allowed in the calculations. 
  #' 
  #' Args:
  #'   graph: igraph object
  #'  
  #' Returns: 
  #'   Double value of the percolation threshold probability
  #' 
  
  firstMoment <- GetMoment(1, graph)
  secondMoment <- GetMoment(2, graph)
  
  return(firstMoment/(secondMoment - firstMoment))
}

GetPT <- function (graph) {
  
  #' Get percolation threshold probability (node percolation background)
  #'
  #' Taking the shortcut on the bottom of the page.  There it says that
  #' <k> and <k^2> are first and second moments of the degree
  #' distribution. In statistics interpretation first and second moments are called mean and
  #' variance.
  #' 
  #' Args:
  #'   graph: igraph object
  #' 
  #' Returns:
  #'   Double value Percolation threshold
  #'
  
  degdist <- degree(graph, loops = FALSE)
  degdist <- degdist[degdist > 0]
  mean(degdist)/(var(degdist) - mean(degdist))
}

GetPercolationThresholdSimplified <- function(graph) {
  
  #' Get percolation threshold probability approximation for graphs with arbitrary degree distribution
  #' 
  #' @param graph igraph object
  #' @return Double value of the percolation threshold probability
  #'
  
  return(1/gsize(graph))
}

GetLog2Bins <- function(graph) {
  
  #' Get bin sizes dividing by two at each step.
  #' 
  #' Args:
  #'   graph: igraph object with the graph
  #'   measures: this argument is ignored, but needed in the plotting function
  #'   
  #' Returns:
  #'   vector with subsizes of the graph dividing by two until it is not possible
  
  bins.number <- ceiling(log2(gorder(graph)))
  
  bins.sizes <- integer(bins.number)
  max.size <- gorder(graph)
  
  for (i in 1:bins.number) {
    bins.sizes[i] <- max.size
    max.size <- ceiling(max.size / 2)
  }
  
  return(bins.sizes)
}

GetBinsByFactor <- function(graph, factor = 0.9, type = "link") {
  
  # Get bin sizes multiplying by a factor at each step ----
  #' 
  #' Args:
  #'   graph: igraph object with the graph
  #'   factor: numeric value in interval (0,1)
  #'   
  #' Returns:
  #'   integer vector with breaks
  #   ----
  
  if(factor >= 1)
    stop("Factor should be in the open interval (0,1)")
  
  value <- 0
  if(type == "link") {
    value <- gsize(graph)
  } else {
    value <- gorder(graph)
  }
  
  breaks <- c(value)
  while(TRUE) {
    value <- value * factor
    if(value < 1)
      break
    breaks <- c(breaks, value)
  }
  
  breaks <- as.integer(breaks)
  return(breaks[!duplicated(breaks)])
}

GetPercolationCurvePoints <- function(graph, label, 
                                      factor = 0.2,
                                      replicates = 5, 
                                      type = "link",
                                      verbose = TRUE) {
  
  # Gets sample points for a node percolation curve of a graph ----
  #' 
  #' Tries different sizes of random subgraphs to check the completeness 
  #' and relative size of the largest connected component
  #' 
  #' Args:
  #'  graph: The graph in igraph format
  #'  label: name for the objects in the graph
  #'  factor: numeric value to define the breaks of the x-axis (size or order) depende on the percolation type argument
  #'  replicates: Number of replicate measurements for each size
  #'  entity: mm, pm, pp, proteins, proteoforms...
  #'  DefineMeasures: function to calculate breaks sequence
  #'  type: string with percolation type: Node|Link
  #'  relative: string with relative calculation of the lcc: Order|Size
  #'  verbose: if TRUE print messages at each step
  #' 
  #' Returns:
  #'   Data frame with three columns: (Sizes|Orders), Completeness and Relative(Size|Order)Lcc
  #'     (Sizes|Orders): the number of edges or vertices for the different subgraphs tested
  #'     Completeness: The product of fractions of vertices and edges in the subgraphs with respect to the original graph
  #'     Relative(Size|Order)Lcc: The quotient of the number of edges or vertices 
  #'     in the subgraphs and the number of edges or vertices in the original graph
  # ----
  
  breaks <- GetBinsByFactor(graph = graph, factor = factor, type = type)
  
  # Create an empty result data frame
  samples <- data.frame(Size=integer(),
                        Order=integer(),
                        Completeness=double(),
                        SizeLcc=integer(),
                        OrderLcc=integer(),
                        RelativeSizeLcc=double(),
                        RelativeOrderLcc=double(),
                        Entity=character())
  
  # Sample all sizes for each replicate
  for(r in 1:replicates) {
    for (b in breaks) {    
      if(b <= 10)
        break
      cat("\n***** Replicate: ", r, "\t Break: ", b, " *****\n\n")
      
      sg <- make_empty_graph(n = 0)
      
      # Reduce the graph to a subgraph
      if(type == "link") {
        sg <- RemoveNEdges(graph, gsize(graph) - b)                 
      } else {
        sg <- RemoveNVertices(graph, gorder(graph) - b)     
      }
      completeness <- (gorder(sg) / gorder(graph)) * (gsize(sg) / gsize(graph))
      if(verbose){
        cat("Subgraph size: ", gsize(sg), "\n")
        cat("Subgraph order: ", gorder(sg), "\n")
        cat("Completeness: ", completeness, "\n")
      }
      
      lcc <- GetLcc(sg)
      samples <- rbind(samples, c(gsize(sg), 
                                  gorder(sg), 
                                  completeness, 
                                  gsize(lcc),
                                  gorder(lcc),
                                  gsize(lcc)/gsize(graph), 
                                  gorder(lcc)/gorder(graph)))
      if(verbose){
        cat("lcc size: ", gsize(lcc), "\n")
        cat("lcc order: ", gorder(lcc), "\n")
        
      }
    }
  }
  
  samples$Entity <- label
  names(samples) <- c("Size", 
                      "Order",
                      "Completeness",
                      "SizeLcc",
                      "OrderLcc",
                      "RelativeSizeLcc",
                      "RelativeOrderLcc",
                      "Entity")
  
  return(samples)
}

GetSubcomponents <- function(graph, 
                                      factor = 0.2,
                                      replicates = 5, 
                                      entity = "Unknown",
                                      type = "link",
                                      verbose = TRUE) {
  
  breaks <- GetBinsByFactor(graph = graph, factor = factor, type = type)
  
  g1 <- new.env(hash = TRUE)
  g2 <- new.env(hash = TRUE)
  
  init1 <- function(s) { g1[[s]] <<- 0L }
  init2 <- function(s) { g2[[s]] <<- 0L }
  
  count1 <- function(s) { g1[[s]] <<- g1[[s]] <<- g1[[s]] + 1L }
  count2 <- function(s) { g2[[s]] <<- g2[[s]] <<- g2[[s]] + 1L }
  
  lapply(as_ids(V(graph)), init1)
  lapply(as_ids(V(graph)), init2)
    
  # Sample all sizes for each replicate
  for(r in 1:replicates) {
    for (b in breaks) {    
      if(b <= 10)
        break
      cat("\n***** Replicate: ", r, "\t Break: ", b, " *****\n\n")
      
      sg <- make_empty_graph(n = 0)
      
      # Reduce the graph to a subgraph
      if(type == "link") {
        sg <- RemoveNEdges(graph, gsize(graph) - b)                 
      } else {
        sg <- RemoveNVertices(graph, gorder(graph) - b)     
      }
      completeness <- (gorder(sg) / gorder(graph)) * (gsize(sg) / gsize(graph))
      if(verbose){
        cat("Subgraph size: ", gsize(sg), "\n")
        cat("Subgraph order: ", gorder(sg), "\n")
        cat("Completeness: ", completeness, "\n")
      }
      
      lcc <- GetLcc(sg)
      
      if(verbose){
        cat("lcc size: ", gsize(lcc), "\n")
        cat("lcc order: ", gorder(lcc), "\n")
      }
      
      # Separate the groups
      
      if(gsize(lcc)/gsize(graph) >= 0.25) {
        lapply( X = as_ids(V(lcc)), FUN = count1)
      } else {
        lapply( X = as_ids(V(lcc)), FUN = count2)
      }
    }
  }

  list1 <- unlist(as.list(g1))
  df1 <- data.frame(key = names(list1), value = list1, row.names = NULL)
  df1$Group <- "1"
  
  list2 <- unlist(as.list(g2))
  df2 <- data.frame(key = names(list2), value = list2, row.names = NULL)
  df2$Group <- "2"
  
  df <- rbind(df1, df2)

  return(df)
}

PlotPercolationCurve <- function(samples, showRelSize = TRUE, colors = c("blue3", "green3", "red3"), showScaled = FALSE) {
  
  # Make percolation curve plot using point samples ----
  #' 
  #' Plots completeness (x-axis) vs relative size of the largest connected component (y-axis)
  #' Adds an adjustment curve with standard error for the points.
  #' 
  #' Args:
  #'  samples Data frame with three columns: Sizes(int), Completeness(num) and RelativeSizeLcc(num)
  #' 
  #' Returns:
  #'   ggplot2 object of the plot
  # ----
  
  theme_set(theme_bw())
  
  means <- aggregate(.~Size+Entity, samples, FUN = mean)
  
  p <- ggplot2::ggplot()
  if(showRelSize) {
    p <- p + 
      geom_point(data = samples, aes(x=Completeness, y=RelativeSizeLcc, color = Entity)) +
      geom_line(data = means, aes(x=Completeness, y=RelativeSizeLcc, color = Entity))
  } else {
    p <- p + 
      geom_point(data = samples, aes(x=Completeness, y=RelativeOrderLcc, color = Entity)) +
      geom_line(data = means, aes(x=Completeness, y=RelativeOrderLcc, color = Entity))
  }
  p <- p + scale_color_manual(values = colors) +
    ggtitle("Percolation curve approximation") +
    theme(axis.text.x = element_text(angle = 90, hjust = 1))
  
  if(showScaled)
    p <- p + scale_x_log10()
    
  return(p)
}

GetPercolationExtraData <- function(graph, measures, replicates, verbose = TRUE) {
  
  #' Gets sample points for a percolation curve of a graph
  #'
  #' Tries different sizes of random subgraphs to check the completeness
  #' and relative size of the largest connected component
  #'
  #' Args:
  #'  graph: The graph in igraph format
  #'  measures: Number of sizes of subgraphs to sample. The size is given by the number of edges
  #'  replicates: Number of replicate measurements for each size
  #'
  #' Returns:
  #'   Data table with columns nVertices, nEdges, subPT, subPT2,
  #'     lcc, completeness, and relativeLcc. nVertices, nEdges, and lcc
  #'     are absolute measurements of the sampled subgraph;
  #'     completeness, and relativeLcc are relative measures of sampled
  #'     subgraph vs the graph they were sampled from. subPT and subPT2
  #'     are ... whatever they are. (subPT is done with LFHS code,
  #'     subPT2 with BB code)
  #'
  
  ## Create the sequence of subgraph sizes
  ## subgraph the size of original is not a(n interesting) subgraph
  ## graph of size 0 is not interesting
  sizes <- as.integer(seq(gsize(graph)-1, 1, length.out = measures))
  
  samples <- rbindlist(lapply(sizes, function (s) {
    
    if(verbose)
      cat("Size: ", s)
    
    subVals <- replicate(replicates, GetMeasuresExtended(graph, s))
    
    data.table(nVertices = unlist(subVals["subOrder", ]),
               nEdges = s,
               subPT = unlist(subVals["subPT", ]),
               subPT2 = unlist(subVals["subPT2", ]),
               lcc = unlist(subVals["lcc", ]))
    
  }))
  totalV <- gorder(graph)
  totalE <- gsize(graph)
  samples[, `:=` (completeness = (nVertices/totalV)*(nEdges/totalE),
                  relativeLcc = lcc/totalV)]
  
  samples
}

GetMeasures <- function (graph, size) {
  list(subOrder = sum(degree(graph) > 0),
       lcc = gorder(GetLcc(graph)))
}

GetMeasuresExtended <- function (graph, size) {
  sg <- RemoveNEdges(graph, gsize(graph) - size)
  list(subOrder = sum(degree(sg) > 0),
       subPT = GetPercolationThreshold(sg),
       subPT2 = GetPT(sg),
       lcc = gorder(GetLcc(sg)))
}

MakePercolationAnalysis <- function(graphs, labels, data.path = "data/", plots.path = "plots/", factor = 0.1, replicates = 2) {
  
  # Make different combinations of percolation plots for a set of networks.
  #
  # Creates random subgrams reducing the size (or order) of each network by the factor.
  # For each subgraph performs the measurements and stores the data to csv files. 
  # Then it makes many combinations of plots and stores it as png files.
  #
  # Args:
  #   graphs: list of igraph objects
  #   labels: atomic vector with a name for each graph
  #   data.path: where to store the csv files with the data
  #   plots.path: where to store the plots in png format
  #   factor: numeric factor to reduce the size (or order) of the graph
  #   replicates: integer number of replicates for a same subgraph
  #
  # Returns:
  #   Nothing in special... just kidding ;) it returns the data frame merging the percolation curve samples for all the graphs
  
  stopifnot(identical(length(graphs), length(labels)))
  
  stopifnot(factor < 1 && factor > 0)

  file.name <- paste(labels, collapse = "_")
    
  if(!dir.exists(data.path))
    stopif(!dir.create(data.path, showWarnings = FALSE, recursive = TRUE))

  if(!dir.exists(plots.path))
    stopif(!dir.create(plots.path, showWarnings = FALSE, recursive = TRUE))
  
  # Link percolation
  type <- "link"
  samples <- data.frame(Size=integer(),
                        Order=integer(),
                        Completeness=double(),
                        SizeLcc=integer(),
                        OrderLcc=integer(),
                        RelativeSizeLcc=double(),
                        RelativeOrderLcc=double(),
                        Entity=character()) 
  for(i in 1:length(graphs)) {
    sample <- GetPercolationCurvePoints(graphs[[i]], labels[i], factor = factor, replicates = replicates, type = type)
    samples <- rbind(samples, sample)
  }
  write.csv(samples, paste(data.path, file.name, "_", type, "_percolation_curve_approximation.csv", sep = ""), row.names=FALSE, na="")
  
  scale <- "log10"
  showScaled <- TRUE
  
  plot <- PlotPercolationCurve(samples, showRelSize = F, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_link_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  plot <- PlotPercolationCurve(samples, showRelSize = T, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_link_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  scale <- "linear"
  showScaled <- FALSE
  
  plot <- PlotPercolationCurve(samples, showRelSize = F, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_link_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  plot <- PlotPercolationCurve(samples, showRelSize = T, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_link_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  # Node percolation
  type <- "node"
  samples <- data.frame(Size=integer(),
                        Order=integer(),
                        Completeness=double(),
                        SizeLcc=integer(),
                        OrderLcc=integer(),
                        RelativeSizeLcc=double(),
                        RelativeOrderLcc=double(),
                        Entity=character()) 
  
  for(i in 1:length(graphs)) {
    sample <- GetPercolationCurvePoints(graphs[[i]], labels[i], factor = factor, replicates = replicates, type = type)
    samples <- rbind(samples, sample)
  }
  write.csv(samples, paste(data.path, file.name, "_", type, "_percolation_curve_approximation.csv", sep = ""), row.names=FALSE, na="")
  
  scale <- "log10"
  showScaled <- TRUE
  
  plot <- PlotPercolationCurve(samples, showRelSize = F, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_node_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  plot <- PlotPercolationCurve(samples, showRelSize = T, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_node_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  scale <- "linear"
  showScaled <- FALSE
  
  plot <- PlotPercolationCurve(samples, showRelSize = F, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_node_", "relOrder_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
  
  plot <- PlotPercolationCurve(samples, showRelSize = T, showScaled = showScaled)
  ggsave(paste(plots.path, file.name, "_node_", "relSize_", scale, "_percolation_curve_approximation.png", sep = ""), width = w)
    
  return(samples)
}