package no.uib.Prototype1.db;

import no.UiB.Prototype1.Configuration;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ConnectionNeo4j {
    
    public static String host = "bolt://localhost";
    public static String username = "neo4j";
    public static String password = "neo4j2";
    
    public static Driver driver = GraphDatabase.driver(Configuration.host, AuthTokens.basic(Configuration.username, Configuration.password));;
}
