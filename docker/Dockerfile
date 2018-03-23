# Base Image
FROM java:8

# Metadata
LABEL base.image="ubuntu:16:04"
LABEL version="1.7"
LABEL software="PathwayMatcher"
LABEL software.version="20180323"
LABEL description="An open source mapper from omics data to Pathways and Reactions in Reactome"
LABEL website="https://github.com/LuisFranciscoHS/PathwayMatcher"
LABEL documentation="https://github.com/LuisFranciscoHS/PathwayMatcher/wiki"
LABEL license="Apache License 2.0: https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/LICENSE.txt"
LABEL tags="Pathway Analysis, Biomolecular Pathways, Proteomics, Genetics"

# Maintainer
MAINTAINER Luis Francisco Hernández Sánchez <Luis.Sanchez@uib.no>

ADD PathwayMatcher.jar PathwayMatcher.jar

CMD ["java","-jar","PathwayMatcher.jar"]