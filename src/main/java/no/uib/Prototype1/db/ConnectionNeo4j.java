package no.uib.Prototype1.db;

import org.neo4j.driver.v1.Driver;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ConnectionNeo4j {
    
    public static String host = "bolt://localhost";
    public static String username = "neo4j";
    public static String password = "neo4j2";
    
    public static Driver driver;
}
