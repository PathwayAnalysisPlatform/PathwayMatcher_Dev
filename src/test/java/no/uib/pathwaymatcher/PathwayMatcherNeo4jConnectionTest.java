package no.uib.pathwaymatcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import static no.uib.pathwaymatcher.model.Error.*;

public class PathwayMatcherNeo4jConnectionTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    // With dbms.security.auth_enabled=false
    @Test
    public void neo4jRunningAndCorrectHostTest(){
        exit.expectSystemExitWithStatus(0);
        String[] args = {"-t", "uniprotList", "-h", "bolt://127.0.0.1:7687", "-i" , "src/test/resources/Preprocessor/Proteins/Valid/singleProtein.txt"};
        PathwayMatcher.main(args);
    }

    // Neo4j running and incorrect host address
    @Test
    public void neo4jRunningAndIncorrectAddressTest(){
        exit.expectSystemExitWithStatus(4);
        String[] args = {"-t", "uniprotList", "-h", "bolt://127.0.0.7:7687", "-i" , "src/test/resources/Preprocessor/Proteins/Valid/singleProtein.txt"};
        PathwayMatcher.main(args);
    }

    // Neo4j running and incorrect port
    @Test
    public void neo4jRunningAndIncorrectPortTest(){
        exit.expectSystemExitWithStatus(4);
        String[] args = {"-t", "uniprotList", "-h", "bolt://127.0.0.1:7689", "-i" , "src/test/resources/Preprocessor/Proteins/Valid/singleProtein.txt"};
        PathwayMatcher.main(args);
    }

    // Neo4j down and correct host
//    @Test
//    public void neo4jDownAndCorrectHostTest(){
//
//        // Stop neo4j and test
//
//        exit.expectSystemExitWithStatus(4);
//        String[] args = {"-t", "uniprotList", "-h", "bolt://127.0.0.1:7687", "-i" , "/home/francisco/Documents/phd/Projects/PathwayMatcher/src/test/resources/Preprocessor/Proteins/Valid/singleProtein.txt"};
//        PathwayMatcher.main(args);
//    }

    // Incorrect host address
    // Incorrect port


    // With dbms.security.auth_enabled=true
}