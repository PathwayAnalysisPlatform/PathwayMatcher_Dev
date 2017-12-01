package no.uib.pathwaymatcher;

import org.junit.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherArgumentsTest {

    @Test
    void mainNoArgumentsTest() {
        String[] args = new String[0];
        PathwayMatcher.main(args);

        System.exit(0);
    }

}