 docker run \
 -v /c/Users/Francisco/git/PathwayMatcher/resources/input/:/home/input/ \
 -v /c/Users/Francisco/git/PathwayMatcher/resources/output/:/home/output/ \
 --name c7 pathwaymatcher \
 -t uniprot \
 -i /home/input/Proteins/UniProt/CysticFibrosis.txt \
 -o output/ \
 -tlp \
 - g