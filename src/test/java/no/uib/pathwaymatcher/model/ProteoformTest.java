package no.uib.pathwaymatcher.model;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class ProteoformTest {
    @Test
    void equals() {
    }

    @Test
    void compareToEquivalentTest() {
        Parser parser = new ParserProteoformPRO();

        try {
            Proteoform p1 = parser.getProteoform("UniProtKB:P08572");
            Proteoform p2 = parser.getProteoform("UniProtKB:P08572");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P08572-1");
            p2 = parser.getProteoform("UniProtKB:P08572-1");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P08572,26-1712");
            p2 = parser.getProteoform("UniProtKB:P08572,26-1712");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P08572,?-?");
            p2 = parser.getProteoform("UniProtKB:P08572,null-NULL");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P08572,26-1712,Lys-?,MOD:01914");
            p2 = parser.getProteoform("UniProtKB:P08572,26-1712,Lys-null,MOD:01914");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149");
            p2 = parser.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P29590,1-882,Lys-160/Lys-65/Lys-490,MOD:01149");
            p2 = parser.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149");
            assertEquals(0, p1.compareTo(p2));

            p1 = parser.getProteoform("UniProtKB:P29590,1-882,Lys-/Lys-160/Lys-490,MOD:01149,Pro-null,MOD:00814|Cys-null,MOD:00831");
            p2 = parser.getProteoform("UniProtKB:P29590,1-882,Pro-null,MOD:00814|Cys-null,MOD:00831,Lys-65/Lys-160/Lys-490,MOD:01149");
            assertEquals(0, p1.compareTo(p2));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}