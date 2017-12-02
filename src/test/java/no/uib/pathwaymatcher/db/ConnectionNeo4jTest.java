package no.uib.pathwaymatcher.db;

import no.uib.pathwaymatcher.Conf;
import org.junit.jupiter.api.Test;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionNeo4jTest {
    @Test
    void Neo4jDownNoAuTest() {
        Conf.setDefaultValues();
        initializeNeo4j();
    }

}