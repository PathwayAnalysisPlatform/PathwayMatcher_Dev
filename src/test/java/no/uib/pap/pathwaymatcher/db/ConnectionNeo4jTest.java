package no.uib.pap.pathwaymatcher.db;

import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;

import org.junit.jupiter.api.Test;

import no.uib.pap.pathwaymatcher.Conf;

class ConnectionNeo4jTest {
    @Test
    void Neo4jDownNoAuTest() {
        Conf.setDefaultValues();
        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
    }

}