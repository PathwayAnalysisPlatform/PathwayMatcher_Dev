package no.uib.pathwaymatcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherProteinsTest {
    @Test
    void main() {
        String[] args = {"-t", "uniprotlist",
                         "-i", "src/test/resources/Generic/Proteins/Valid/correctList.txt"};
    }

}