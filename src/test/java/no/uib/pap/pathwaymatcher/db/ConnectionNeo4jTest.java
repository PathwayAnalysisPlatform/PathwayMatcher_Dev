package no.uib.pap.pathwaymatcher.db;

import org.junit.jupiter.api.Test;

import no.uib.pap.pathwaymatcher.Conf;

import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionNeo4jTest {
    @Test
    void Neo4jDownNoAuTest() {
        Conf.setDefaultValues();
        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
    }

}