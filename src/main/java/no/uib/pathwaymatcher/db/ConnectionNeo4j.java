package no.uib.pathwaymatcher.db;

import no.uib.pathwaymatcher.Conf;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import static no.uib.pathwaymatcher.Conf.strMap;
import static no.uib.pathwaymatcher.model.Error.COULD_NOT_CONNECT_TO_NEO4j;
import static no.uib.pathwaymatcher.model.Error.sendError;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ConnectionNeo4j {

    public static Driver driver;
    public static Session session;

    /**
     * Create data structure to hold the proteoforms and initializeNeo4j the connection to Neo4j.
     * This method needs that the command line arguments have been parsed and set, also the variables from the
     * configuration file.
     *
     * @return
     */
    public static void initializeNeo4j() {

        if (strMap.get(Conf.StrVars.username).length() > 0) {
            ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(Conf.StrVars.host), AuthTokens.basic(strMap.get(Conf.StrVars.username), strMap.get(Conf.StrVars.password)));
        } else {
            ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(Conf.StrVars.host));
        }

        try {
            Session session = ConnectionNeo4j.driver.session();
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + strMap.get(Conf.StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.out.println(e);
            sendError(COULD_NOT_CONNECT_TO_NEO4j);
        }
    }
}
