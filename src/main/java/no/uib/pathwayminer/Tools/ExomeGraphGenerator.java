package no.uib.pathwayminer.Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import no.uib.pathwayminer.db.Connection;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ExomeGraphGenerator {

    private static Driver driver;

    public static void main(String args[]) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/csv/listFileUniprot.csv"));
        FileWriter resultStream = new FileWriter("./src/main/resources/sif/ExomeGraph5.sif");
        driver = GraphDatabase.driver(Connection.host, AuthTokens.basic(Connection.username, Connection.password));

        //Generate json for D3
        BufferedReader input = new BufferedReader(new FileReader("./src/main/resources/csv/listFileUniprot.csv"));
        FileWriter output = new FileWriter("C:/Users/Francisco/Documents/PhD UiB/Projects/PathwayAdventure/public/resources/ProteinInteractions.json");
        output.write("{\n\"nodes\": [\n");
        ArrayList<no.uib.pathwayminer.Tools.Node> nodes = new ArrayList<Node>();
        int maxNumProt = 100;
        boolean firstEdge = true;

        int cont = 0;
        //Print the nodes here
        System.out.println("Writing nodes...");
        for (String id; (id = input.readLine()) != null && cont < maxNumProt; cont++) {
            Node n = new Node(id, 2);
            //Check if it exists in reactome

            Session session = driver.session();
            String query = "MATCH (pe)-[:referenceEntity]->(re:ReferenceEntity{identifier:{id}})\n"
                    + "RETURN pe.stId";
            StatementResult queryResult = session.run(query, Values.parameters("id", id));

            if (queryResult.hasNext()) {
                n.group = 1;    //Set to group 1 of the proteins contained in Reactome.
            }
            session.close();

            System.out.println("Verified node " + cont + " : " + id);
            nodes.add(n);
        }

        for (int P = 0; P < maxNumProt && P < nodes.size(); P++) {
            if (P > 0) {
                output.write(",");
            }
            System.out.println("Write node " + (P + 1) + " : " + nodes.get(P).id);
            output.write(nodes.get(P).toString());
        }
        output.write("],\n  \"links\": [");
        //Print the edges here
        System.out.println("Writing edges...");
        for (int P = 0; P < maxNumProt && P < nodes.size(); P++) {

            //Connect to Reactome
            Session session = driver.session();

            /**
             * *** Get protein relations in the same complexes ****
             */
            String query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(c:Complex)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                    + "RETURN DISTINCT re.identifier as source, p.stId, p.displayName, c.displayName, nE.stId, nE.displayName, nP.identifier as target";
            StatementResult queryResult = session.run(query, Values.parameters("id", nodes.get(P).id));

            while (queryResult.hasNext()) {
                if (!firstEdge) {
                    output.write(",");
                }

                Record record = queryResult.next();
                output.write("{\"source\": \"" + record.get("source").asString() + "\", \"target\": \"" + record.get("target").asString() + "\", \"value\": " + 10 + "}");
                firstEdge = false;
            }
            System.out.println("Complex neighbors for node " + (P + 1) + " : " + nodes.get(P).id);

            /**
             * *** Get protein relations in the same reactions ****
             */
            query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]-(rle:ReactionLikeEvent)-[:hasComponent|hasMember|hasCandidate|repeatedUnit|input|output|catalystActivity|regulatedBy|physicalEntity|regulator*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                    + "RETURN DISTINCT re.identifier  as source, p.stId, p.displayName, rle.stId, rle.displayName, nE.stId, nE.displayName, nP.identifier as target";
            queryResult = session.run(query, Values.parameters("id", nodes.get(P).id));

            while (queryResult.hasNext()) {
                if (!firstEdge) {
                    output.write(",");
                }

                Record record = queryResult.next();
                output.write("{\"source\": \"" + record.get("source").asString() + "\", \"target\": \"" + record.get("target").asString() + "\", \"value\": " + 5 + "}");
                firstEdge = false;
            }
            System.out.println("Reaction neighbors for node " + (P+1) + " : " + nodes.get(P).id);
            session.close();
        }

        output.write("]\n}\n");

        input.close();
        output.close();

        resultStream.close();
        driver.close();
    }

    public static void getNeighborProteinsInSameComplexes(String id) //Relations at protein reference entity level, not considering subcellular location
    {

    }

    public static void getNeighborEWASInSameComplexes(String stId) //Relations at Entity with accessioned sequence level, considering subcellular location
    {
        //TODO
    }

    public static void getNeighborProteinsInSameReactions(String id) //Relations at protein reference entity level, not considering subcellular location
    {

    }

    public static void getNeighborEWASInSameReactions(String stId) //Relations at Entity with accessioned sequence level, considering subcellular location
    {
        //TODO
    }
}
