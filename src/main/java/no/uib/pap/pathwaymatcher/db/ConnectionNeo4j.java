package no.uib.pap.pathwaymatcher.db;

import static no.uib.pap.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pap.model.Error.sendError;

import java.util.List;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ConnectionNeo4j {

    public static Driver driver;
    public static Session session;

    /**
     * InitializeNeo4j the connection to Neo4j.
     * This method needs that the command line arguments have been parsed and set, also the variables from the
     * configuration file.
     *
     * @return
     */
    public static void initializeNeo4j(String host, String username, String password) {

        if (username.length() > 0) {
            ConnectionNeo4j.driver = GraphDatabase.driver(host, AuthTokens.basic(username, password));
        } else {
            ConnectionNeo4j.driver = GraphDatabase.driver(host);
        }

        try {
            Session session = ConnectionNeo4j.driver.session();
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + host + "\", ensure the database is running and that there is a working network connection to it.");
            System.out.println(e);
            sendError(COULD_NOT_CONNECT_TO_NEO4j);
        }
    }

    public static List<Record> query(String query, Value parameters) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();

        StatementResult queryResult = ConnectionNeo4j.session.run(query, parameters);

        ConnectionNeo4j.session.close();

        return queryResult.list();
    }

    public static int getSingleValue(String query, String attribute) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
        StatementResult queryResult = ConnectionNeo4j.session.run(query);
        Record r = queryResult.single();
        ConnectionNeo4j.session.close();

        return r.get(attribute).asInt();
    }

    public static int getSingleValue(String query, String attribute, Value parameters) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
        StatementResult queryResult = ConnectionNeo4j.session.run(query, parameters);
        Record r = queryResult.single();
        ConnectionNeo4j.session.close();

        return r.get(attribute).asInt();
    }
}
