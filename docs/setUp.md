~~~~
docker run -it ubuntu /bin/bash
apt-get update
apt-get install wget

echo "deb http://httpredir.debian.org/debian jessie-backports main" | tee -a /etc/apt/sources.list.d/jessie-backports.list
apt-get update

wget -O - https://debian.neo4j.org/neotechnology.gpg.key | sudo apt-key add -
echo 'deb http://debian.neo4j.org/repo stable/' | tee -a /etc/apt/sources.list.d/neo4j.list
apt-get update

apt-get install neo4j

docker run -it --name C2 ubuntu-neo4j /bin/bash
neo4j-admin set-initial-password neo4j2
mkdir /var/run/neo4j/
neo4j start



docker cp Documents/PhD\ UiB/Projects/Tests/Docker/PathwayMatcher/reactome.graphdb.tgz  test1:/home/
docker exec -it test1 bash
tar -xvf /home/reactome.graphdb.tgzls 

 docker commit CONTAINER_ID ubuntu-neo4j
 docker run -it  ubuntu-neo4j /bin/bash

docker cp ./reactome.graphdb.tgz priceless_jones:/
docker exec -it priceless_jones /bin/bash

neo4j start

docker login
docker tag IMAGE_ID lfhs/tests:0.1
docker push lfhs/tests

neo4j-admin load --from=/reactome.graphdb.tgz --force=true
~~~~
Remove vim, wget


~~~~
java -jar PathwayMatcher-1.2.jar -t uniprotList -i input/uniprotList.txt
docker exec C2 java -jar /home/PathwayMatcher-1.0.jar -t uniprotList -i /home/input/uniprotList.txt
~~~~


#Using the official image of Neo4j for Docker
~~~~
docker run -it neo4j /bin/sh
bin/neo4j-admin set-initial-password neo4j
~~~~

docker run \
    --volume /c/Users/Francisco/Documents/phd/Projects/PathwayMatcher/resources/data:/data \
    --volume /c/Users/Francisco/Documents/phd/Projects/PathwayMatcher/shared:/home/data \
    --volume /c/Users/Francisco/Documents/phd/Projects/PathwayMatcher/logs:/logs \
lfhs/pathwaymatcher

docker exec pathwaymatcher_container \
     java -jar /var/lib/PathwayMatcher-1.3/PathwayMatcher-1.3.jar \
     -t uniprotList -i /home/data/input/uniprotList.txt \
     -o /home/data/output/output.csv

docker exec -it cranky_stallman --volume=/var/lib/neo4j/data/

# Running the container

docker run -it -v /c/Users/Francisco/Documents/phd/Projects/PathwayMatcher/shared:/var/lib/neo4j/data/ pathwaymatcher  java -jar PathwayMatcher-1.2.jar

docker run -v /Users/<path>:/<container path> ...
docker run -v c:\<path>:/<container path> ...

# Extract a column from a file
docker run -v /c/Users/Francisco/Documents/phd/Projects/PathwayMatcher/shared:/var/lib/neo4j/data/ pathwaymatcher java -cp /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar no.uib.pathwaymatcher.tools.ColumnExtractor -i /var/lib/neo4j/data/zBMIX-autosome-maf-above-0-005.result -c 0 -o /var/lib/neo4j/data/rsidX.csv

# Map rsids to pathways



## Execute the pathway matcher in the running container
docker exec <container name>
     java -d64 -Xmx10g -Xmx10g -jar /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -i /home/data/rsidX.csv \
     -o /home/data/tlpX.csv \
     -t rsidList \
     -v /home/data/vep/ -tlp

Example:
docker exec silly_booth \
     java -d64 -Xmx10g -Xmx10g -jar /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar -i /data/rsidX.csv -o /data/tlpX.csv -t rsidList -v /data/vep/ -tlp

docker exec zealous_davinci \
     java -d64 -Xmx10g -Xmx10g -jar /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t uniprotList -i data/uniprotList.txt -o data/output.csv \
     --reactionsFile data/reactions.csv --pathwaysFile data/pathways.csv

docker exec blissful_minsky java -cp /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar no.uib.pathwaymatcher.db.Neo4jConfigurationSetter conf/neo4j.conf dbms.allow_format_migration
docker exec blissful_minsky java -cp /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar no.uib.pathwaymatcher.db.Neo4jConfigurationSetter conf/neo4j.conf dbms.security.auth_enabled

docker exec blissful_minsky curl --fail --show-error --location --remote-name https://github.com/LuisFranciscoHS/PathwayMatcher/releases/download/v1.2/PathwayMatcher-1.2.tar && \
    tar --extract --file PathwayMatcher-1.2.tar --directory /var/lib && \
    rm PathwayMatcher-1.2.tar

    # Save the docker image to docker cloud
    docker login
    docker tag image username/repository:tag ==> docker tag pathwaymatcher lfhs/pathwaymatcher:v1.2
    docker push username/repository:tag ==> docker push lfhs/pathwaymatcher:v1.2

# Run PathwayMatcher in Netbeats

### Uniprot list
-t uniprotList -i resources/input/uniprotList.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

### Uniprot list with sites
-t uniprotListAndSites -i resources/input/uniprotListAndSites.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

### Uniprot list with PTM sites and types
-t uniprotListAndModSites -i resources/input/uniprotListAndModSites.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

### Peptide list
-t peptideList -i resources/input/peptideList.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

### Peptide list and sites
-t peptideListAndSites -i resources/input/peptideListAndSites.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

### Peptide list with types and sites
-t peptideListAndSites -i resources/input/peptideListAndSites.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

# Run PathwayMatcher in Docker
-t peptideListAndModSites -i resources/input/peptideListAndModSites.txt -o resources/output/output.csv -u neo4j -p neo4j2 --reactionsFile resources/output/reactions.csv --pathwaysFile resources/output/pathways.csv

Located in the PathwayMatcher directory

## Run the container first to start neo4j and create the volumnes
docker run \
    --volume /c/Users/Francisco/Documents/phd/Projects/PathwayMatcher/shared:/data/ \
    --name pathwaymatcher_container \
    lfhs/pathwaymatcher

Then run the desired type of matching:

### Uniprot list
docker exec pathwaymatcher_container \
     java -d64 -Xmx10g -Xmx10g \
     -jar /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t uniprotList -i data/input/uniprotList.txt \
     -o data/output.csv \
     --reactionsFile data/reactions.csv \
     --pathwaysFile data/pathways.csv

### Uniprot list with sites
docker exec pathwaymatcher_container \
     java -d64 -Xmx10g -Xmx10g \
     -jar /var/lib/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t uniprotListAndSites -i data/input/uniprotListAndSites.txt \
     -o data/output.csv \
     --reactionsFile data/reactions.csv \
     --pathwaysFile data/pathways.csv

### Uniprot list with PTM sites and types
docker exec pathwaymatcher_container \
     java -d64 -Xmx10g -Xmx10g \
     -jar data/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t uniprotListAndModSites -i data/input/uniprotListAndModSites.txt \
     -o data/output.csv \
     --reactionsFile data/reactions.csv \
     --pathwaysFile data/pathways.csv

### Peptide list
docker exec pathwaymatcher_container \
     java -d64 -Xmx10g -Xmx10g \
     -jar data/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t peptideList -i resources/input/peptideList.txt \
     -o data/output.csv \
     --reactionsFile data/reactions.csv \
     --pathwaysFile data/pathways.csv

### Peptide list and sites
docker exec pathwaymatcher_container \
     java -d64 -Xmx10g -Xmx10g \
     -jar data/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t peptideListAndSites -i resources/input/peptideListAndSites.txt \
     -o data/output.csv \
     --reactionsFile data/reactions.csv \
     --pathwaysFile data/pathways.csv

     
### Peptide list with types and sites
docker exec pathwaymatcher_container \
     java -d64 -Xmx10g -Xmx10g \
     -jar data/PathwayMatcher-1.2/PathwayMatcher-1.2.jar \
     -t peptideListAndModSites -i resources/input/peptideListAndModSites.txt \
     -o data/output.csv \
     --reactionsFile data/reactions.csv \
     --pathwaysFile data/pathways.csv