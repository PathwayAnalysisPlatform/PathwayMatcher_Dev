package no.uib.pathwaymatcher;

import org.junit.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherTest {

    @Test
    void mainNoArgumentsTest() {
        String[] args = new String[0];
        int rtnValue = PathwayMatcher.main(args);

        System.exit(0);
    }
}