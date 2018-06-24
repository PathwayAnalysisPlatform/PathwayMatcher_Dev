#!/usr/bin/env bash

##
# This script exports shortest path matrics for the different networks.
##


## Parameters

# Repository folder
repo=/mnt/work/marc/tools/PathwayMatcher

# Number of threads
nThreads=32


## Script

# Run the summary extraction command
echo "Extracting summary"
java -Xmx160G -Djava.util.concurrent.ForkJoinPool.common.parallelism=$nThreads -cp $repo/PathwayMatcher-1.7.jar no.uib.pap.pathwaymatcher.dsd.cmd.ExportShortestPathMatrix

