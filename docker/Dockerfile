# Base Image
FROM neo4j:3.2.1

# Metadata
LABEL base.image="neo4j:3.2.1"
LABEL version="3"
LABEL software="PathwayMatcher"
LABEL software.version="20170705"
LABEL description="An open source mapper from experimental data to Pathways and Reactions contained the Reactome knowledgebase"
LABEL website="https://github.com/LuisFranciscoHS/PathwayMatcher"
LABEL documentation="https://github.com/LuisFranciscoHS/PathwayMatcher/wiki"
LABEL license="Apache License 2.0: https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/LICENSE.txt"
LABEL tags="Pathway Analysis, Biomolecular Pathways, Proteomics, Genetics"

# Maintainer
MAINTAINER Luis Francisco Hernández Sánchez <Luis.Sanchez@uib.no>

USER root

WORKDIR /var/lib/neo4j

VOLUME /data

# RUN apt-get update && \
#     # apt-get -y install --no-install-recommends wget && \
#     apt-get -y install --no-install-recommends unzip

RUN apk add --no-cache --quiet \
    bash \
    curl

# Dowload PathwayMatcher executables
ARG PATHWAYMATCHER_URI=https://github.com/LuisFranciscoHS/PathwayMatcher/releases/download/v1.2/PathwayMatcher-1.2.tar
ARG PATHWAYMATCHER_TARBALL=PathwayMatcher-1.2.tar
ARG REACTOME_URI=http://reactome.org/download/current/reactome.graphdb.tgz
ARG REACTOME_TARBALL=reactome.graphdb.tgz

RUN curl --fail --silent --show-error --location --remote-name ${PATHWAYMATCHER_URI} && \
    tar --extract --file ${PATHWAYMATCHER_TARBALL} --directory /var/lib && \
    rm ${PATHWAYMATCHER_TARBALL}

RUN curl --fail --silent --show-error --location --remote-name ${REACTOME_URI} && \
    # tar --extract --file ${REACTOME_TARBALL} --directory /data/databases/ && \
    mv ${REACTOME_TARBALL} /data/databases/

RUN bin/neo4j-admin set-initial-password neo4j2 && \
    bin/neo4j-admin load --from=/data/databases/reactome.graphdb.tgz --force=true

RUN apk del curl

# RUN ZIP= && \
#     wget --no-check-certificate https://github.com/LuisFranciscoHS/PathwayMatcher/releases/download/v1.2/$ZIP -O /tmp/$ZIP && \
#     mkdir /home/pathwaymatcher/ && \
#     unzip /tmp/$ZIP -d /home/pathwaymatcher/

# Get VEP tables

# Set up Neo4j
# RUN wget -O - http://debian.neo4j.org/neotechnology.gpg.key | apt-key add -
# RUN echo 'deb http://debian.neo4j.org/repo stable/' > /etc/apt/sources.list.d/neo4j.list
# RUN apt-get update && \
#     apt-get -y install --no-install-recommends neo4j=3.0.0 && \
#     apt-get clean && \ 
#     apt-get purge
# RUN neo4j-admin set-initial-password neo4j2 && \
#     mkdir /var/run/neo4j/ && \
#     neo4j start

RUN bin/neo4j start