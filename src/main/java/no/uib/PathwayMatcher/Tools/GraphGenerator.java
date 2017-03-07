package no.uib.PathwayMatcher.Tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.PathwayMatcher.Configuration;
import no.uib.PathwayMatcher.Model.ModifiedProtein;
import no.uib.PathwayMatcher.PathwayMatcher;
import static no.uib.PathwayMatcher.PathwayMatcher.MPs;
import no.uib.PathwayMatcher.db.ConnectionNeo4j;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class GraphGenerator {

    private static ArrayList<no.uib.PathwayMatcher.Tools.Node> nodes = new ArrayList<Node>();
    private static Set<String> availableProteins = new HashSet<String>(Configuration.maxNumProt);
    private static Set<String> addedNeighbors = new HashSet<String>(Configuration.maxNumProt);
    private static boolean firstEdge = true;
    private static FileWriter output;

    public static void main(String args[]) throws IOException {

        if (initialize() == 1) {
            return;
        }

        ConnectionNeo4j.driver = GraphDatabase.driver(ConnectionNeo4j.host, AuthTokens.basic(ConnectionNeo4j.username, ConnectionNeo4j.password));

        BufferedReader input = new BufferedReader(new FileReader(Configuration.inputListFile));
        if (Configuration.verboseConsole) {
            System.out.println("The output graph will be in the file: " + Configuration.outputGraphFilePath + "ProteinInteractions." + Configuration.outputGraphFileType);
        }
        if (!Configuration.outputGraphFilePath.endsWith("/")) {
            Configuration.outputGraphFilePath += "/";
        }
        output = new FileWriter(Configuration.outputGraphFilePath + Configuration.outputFileName + "." + Configuration.outputGraphFileType);
        int index = 0;

        switch (Configuration.outputGraphFileType) {
            case json:                                  //Generate json for D3
                output.write("{\n\"nodes\": [\n");

                //Print the nodes here
                System.out.println("Writing nodes...");
                for (String id; (id = input.readLine()) != null && index < Configuration.maxNumProt;) {
                    Node n;

                    //Veryfy if the protein is in Reactome
                    Session session = ConnectionNeo4j.driver.session();
                    String query = "MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n"
                            + "RETURN pe.stId";         //Check if it exists in reactome
                    StatementResult queryResult = session.run(query, Values.parameters("id", id));

                    if (queryResult.hasNext()) {
                        n = new Node(id, 1);        //Set to group 1 of the proteins contained in Reactome.
                        availableProteins.add(id);
                    } else {
                        n = new Node(id, 0);
                    }
                    nodes.add(n);
                    session.close();

                    System.out.println("Verified node " + index + " : " + id);

                    if (!Configuration.showMissingProteins) {   //Avoid printing the proteins not contained in Reactome
                        if (n.group == 0) {
                            continue;
                        }
                    }
                    //Print to the file
                    if (index > 0) {
                        output.write(",");
                    }
                    index++;
                    System.out.println("Write node " + (index + 1) + " : " + n.id);
                    output.write(n.toString());
                }
                if (!Configuration.onlyNeighborsInList) {       //If necessary print neighbor nodes on the graph file
                    index = 0;
                    for (Node n : nodes) {          //Iterate over available proteins
                        if (n.group == 0) {
                            continue;
                        }

                        if (Configuration.complexNeighbors) {
                            for (String neighbor : n.complexNeighbors) {
                                if (availableProteins.contains(neighbor) || addedNeighbors.contains(neighbor)) {
                                    continue;
                                }
                                if (Configuration.verboseConsole) {
                                    System.out.println("Write neighbor: " + neighbor);
                                }
                                addedNeighbors.add(neighbor);
                                Node neighborNode = new Node(neighbor, 2, true);
                                output.write("," + neighborNode.toString());       //Print colon because there are always nodes before (the original list nodes)
                            }
                        }
                        if (Configuration.reactionNeighbors) {
                            for (String neighbor : n.reactionNeighbors) {
                                if (availableProteins.contains(neighbor) || addedNeighbors.contains(neighbor)) {    //If the node was previously added
                                    continue;
                                }
                                if (Configuration.verboseConsole) {
                                    System.out.println("Write neighbor: " + neighbor);
                                }
                                addedNeighbors.add(neighbor);
                                Node neighborNode = new Node(neighbor, 2, true);    //They are in group 2 because they are not in the original list.
                                output.write("," + neighborNode.toString());       //Print colon because there are always nodes before (the original list nodes)
                            }
                        }
                        index++;
                    }
                }

                output.write("],\n  \"links\": [");

                //Print the edges here
                System.out.println("Writing edges...");
                for (index = 0; index < nodes.size(); index++) {        //Iterate over available proteins / nodes
                    if (nodes.get(index).group == 1) {
                        if (Configuration.complexNeighbors) {
                            for (String neighbor : nodes.get(index).complexNeighbors) {
                                if (!(Configuration.onlyOrderedEdges && nodes.get(index).id.compareTo(neighbor) >= 0 && availableProteins.contains(neighbor))) {
                                    printEdge(nodes.get(index).id, neighbor, 10, "");
                                }
                            }
                            System.out.println("Complex neighbors for node " + (index + 1) + " : " + nodes.get(index).id);
                        }
                        if (Configuration.reactionNeighbors) {
                            for (String neighbor : nodes.get(index).reactionNeighbors) {
                                if (!(Configuration.onlyOrderedEdges && nodes.get(index).id.compareTo(neighbor) >= 0 && availableProteins.contains(neighbor))) {
                                    printEdge(nodes.get(index).id, neighbor, 10, "");
                                }
                            }
                            System.out.println("Reaction neighbors for node " + (index + 1) + " : " + nodes.get(index).id);
                        }
                    }
                }

                output.write("]\n}\n");
                break;

            case sif:
                break;
            case graphviz:
                break;
        }

        input.close();
        output.close();
        ConnectionNeo4j.driver.close();
    }

    private static int initialize() {

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Configuration.configGraphPath));

            //For every valid variable found in the config.txt file, the variable value gets updated
            String line;
            while ((line = configBR.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("//")) {
                    continue;
                }
                if (!line.contains("=")) {
                    continue;
                }
                String[] parts = line.split("=");
                if (parts[0].equals("verboseConsole")) {
                    Configuration.verboseConsole = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("unitType")) {
                    Configuration.unitType = Configuration.ProteinType.valueOf(parts[1]);
                } else if (parts[0].equals("configGraphPath")) {
                    Configuration.configGraphPath = parts[1].replace("\\", "/");
                } else if (parts[0].equals("maxNumProt")) {
                    Configuration.maxNumProt = Integer.valueOf(parts[1]);
                } else if (parts[0].equals("onlyNeighborsInList")) {
                    Configuration.onlyNeighborsInList = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("onlyOrderedEdges")) {
                    Configuration.onlyOrderedEdges = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("showMissingProteins")) {
                    Configuration.showMissingProteins = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("reactionNeighbors")) {
                    Configuration.reactionNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("complexNeighbors")) {
                    Configuration.complexNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("entityNeighbors")) {
                    Configuration.entityNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("candidateNeighbors")) {
                    Configuration.candidateNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("topLevelPathwayNeighbors")) {
                    Configuration.topLevelPathwayNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("pathwayNeighbors")) {
                    Configuration.pathwayNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("outputGraphFileType")) {
                    Configuration.outputGraphFileType = Configuration.GraphType.valueOf(parts[1]);
                } else if (parts[0].equals("outputGraphFilePath")) {
                    Configuration.outputGraphFilePath = parts[1].replace("\\", "/");
                } else if (parts[0].equals("outputFileName")) {
                    Configuration.outputFileName = parts[1];
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Configuration file not found at: " + Configuration.configPath);
            Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Configuration.configPath);
            Logger.getLogger(PathwayMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        MPs = new ArrayList<ModifiedProtein>(Configuration.maxNumberOfProteins);

        return 0;
    }

    private static void printEdge(String source, String target, int weight, String label) throws IOException {

        switch (Configuration.outputGraphFileType) {
            case json:
                if (!firstEdge) {
                    output.write(",");
                }
                output.write("{\"source\": \"" + source + "\", \"target\": \"" + target + "\", \"value\": " + weight + "}");
                break;
            case sif:
                break;
            case graphviz:
                break;
        }

        firstEdge = false;

    }
}
