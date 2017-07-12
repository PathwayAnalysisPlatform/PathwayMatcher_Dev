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

VOLUME /home/data/

RUN apk add --no-cache --quiet \
    bash \
    curl

# Setup PathwayMatcher
ARG PATHWAYMATCHER_URI=https://github.com/LuisFranciscoHS/PathwayMatcher/releases/download/v1.2/PathwayMatcher-1.2.tar
ARG PATHWAYMATCHER_TARBALL=PathwayMatcher-1.2.tar

RUN curl --fail --show-error --location --remote-name ${PATHWAYMATCHER_URI} && \
    tar --extract --file ${PATHWAYMATCHER_TARBALL} --directory /var/lib && \
    rm ${PATHWAYMATCHER_TARBALL}

# Setup Reactome
ARG REACTOME_URI=http://reactome.org/download/current/reactome.graphdb.tgz
ARG REACTOME_TARBALL=reactome.graphdb.tgz

RUN curl --fail --show-error --location --remote-name ${REACTOME_URI} && \
    tar -xvf ${REACTOME_TARBALL} --directory /var/lib/neo4j/data/databases/ && \
    # mv /var/lib/neo4j/data/databases/reactome.graphdb.* data/databases/graph.db && \
    rm ${REACTOME_TARBALL} && \
    apk del curl
#RUN bin/neo4j-admin set-initial-password neo4j2
RUN java -cp /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar no.uib.pathwaymatcher.db.SetNeo4jConf conf/neo4j.conf dbms.allow_format_migration && \
    java -cp /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar no.uib.pathwaymatcher.db.SetNeo4jConf conf/neo4j.conf dbms.security.auth_enabled
