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
        
        //Read list of exome proteins
        int cont = 0;
        for (String line; (line = br.readLine()) != null && cont < 20000; cont++) {
            System.out.println(cont + "\t\t" + line);
            //Connect to Reactome
            Session session = driver.session();
            
            //Query for neighbors
            String query = "WITH {id} AS UniprotId\n"
                    + "MATCH (nre:ReferenceEntity{databaseName:\"UniProt\"})<-[:referenceEntity]-(np:PhysicalEntity)<-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]-(rle:ReactionLikeEvent)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{identifier:UniprotId,databaseName:\"UniProt\"})\n"
                    + "WHERE nre.identifier <> UniprotId\n"
                    + "RETURN DISTINCT nre.identifier as Neighbor_Identifier";
            StatementResult queryResult = session.run(query, Values.parameters("id", line.trim()));

            //For each neighbor
            List<String> edges = new ArrayList<String>();

            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                //Create edge
                edges.add(line + "\tpp\t" + record.get("Neighbor_Identifier").asString());
            }
            if (edges.size() >= 5) {
                //Send them to table/file  
                for(String e : edges)
                    resultStream.write(e + "\n"); 
            }
            
            session.close();
        }
        
        resultStream.close();
        br.close();
        driver.close();
    }
}
