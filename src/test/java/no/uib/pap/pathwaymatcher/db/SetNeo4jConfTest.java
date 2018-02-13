package no.uib.pap.pathwaymatcher.db;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

class SetNeo4jConfTest {

    private static final String CONF_FILE = "src/test/resources/Generic/neo4j.conf";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void commentVariableTest() {

        try {

            SetNeo4jConf.commentVariable(CONF_FILE, "dbms.allow_upgrade");
            List<String> lines = Files.readLines(new File(CONF_FILE), Charset.defaultCharset());
            assertTrue(lines.contains("#dbms.allow_upgrade=true"));

            SetNeo4jConf.uncommentVariable(CONF_FILE, "dbms.allow_upgrade");
            lines = Files.readLines(new File(CONF_FILE), Charset.defaultCharset());
            assertTrue(lines.contains("dbms.allow_upgrade=true"));

        } catch (IOException e) {
            fail("File should be read correctly.");
        }
    }

    @Test
    void uncommentVariableTest() {

        try {

            SetNeo4jConf.uncommentVariable(CONF_FILE, "dbms.security.auth_enabled");
            List<String> lines = Files.readLines(new File(CONF_FILE), Charset.defaultCharset());
            assertTrue(lines.contains("dbms.security.auth_enabled=false"));

            SetNeo4jConf.commentVariable(CONF_FILE, "dbms.security.auth_enabled");
            lines = Files.readLines(new File(CONF_FILE), Charset.defaultCharset());
            assertTrue(lines.contains("#dbms.security.auth_enabled=false"));

        } catch (IOException e) {
            fail("File should be read correctly.");
        }
    }
}