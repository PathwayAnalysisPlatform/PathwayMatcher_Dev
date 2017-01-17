package no.uib.Prototype1.Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import no.UiB.Prototype1.Configuration;
import no.uib.Prototype1.db.ConnectionNeo4j;
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

    private static ArrayList<no.uib.Prototype1.Tools.Node> nodes = new ArrayList<Node>();
    private static Set<String> availableProteins = new HashSet<String>(Configuration.maxNumProt);
    private static Set<String> addedNeighbors = new HashSet<String>(Configuration.maxNumProt);
    private static boolean firstEdge = true;
    private static FileWriter output;

    public static void main(String args[]) throws IOException {

        ConnectionNeo4j.driver = GraphDatabase.driver(ConnectionNeo4j.host, AuthTokens.basic(ConnectionNeo4j.username, ConnectionNeo4j.password));

        BufferedReader input = new BufferedReader(new FileReader(Configuration.inputListFile));
        output = new FileWriter(Configuration.outputGraphFilePath + "ProteinInteractions." + Configuration.outputGraphFileType);
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
