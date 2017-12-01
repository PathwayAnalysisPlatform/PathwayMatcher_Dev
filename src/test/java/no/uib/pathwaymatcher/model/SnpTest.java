package no.uib.pathwaymatcher.model;

import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnpTest {
    @Test
    void equalsWithRsidTest() {
        Snp snp1 = new Snp("rs6054257");
        Snp snp2 = new Snp("rs6054257");
        assertTrue(snp1.equals(snp2));
        assertTrue(snp2.equals(snp1));

        snp1 = new Snp("rs6040355");
        snp2 = new Snp("rs6054257");
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));
    }

    @Test
    void equalsWithChrAndBpTest() {
        Snp snp1 = new Snp(10, 14370L);
        Snp snp2 = new Snp(20, 14370L);
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));

        snp1 = new Snp(20, 14370L);
        snp2 = new Snp(20, 14370L);
        assertTrue(snp1.equals(snp2));
        assertTrue(snp2.equals(snp1));

        snp1 = new Snp(20, 4370L);
        snp2 = new Snp(20, 14370L);
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));

        snp1 = new Snp(1, 4370L);
        snp2 = new Snp(2, 14370L);
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));
    }

    @Test
    void equalsWithThreeValuesTest() {
        // Snp1 is missing chr and bp
        Snp snp1 = new Snp("rs6054257");
        Snp snp2 = new Snp(20, 14370L, "rs6054257");
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));

        // Snp1 is missing rsid
        snp1 = new Snp(20, 14370L);
        snp2 = new Snp(20, 14370L, "rs6054257");
        assertTrue(snp1.equals(snp2));
        assertTrue(snp2.equals(snp1));

        // All equal
        snp1 = new Snp(20, 14370L, "rs6054257");
        snp2 = new Snp(20, 14370L, "rs6054257");
        assertTrue(snp1.equals(snp2));
        assertTrue(snp2.equals(snp1));

        // bp is different
        snp1 = new Snp(20, 14370L, "rs6054257");
        snp2 = new Snp(20, 146L, "rs6054257");
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));

        // chr is different
        snp1 = new Snp(1, 14370L, "rs6054257");
        snp2 = new Snp(2, 14370L, "rs6054257");
        assertFalse(snp1.equals(snp2));
        assertFalse(snp2.equals(snp1));

        // rsid is different. As long as the chr and bp are equal, then its taken as equivalent. This is for processing VCF files only with the chr and bp
        snp1 = new Snp(1, 14370L, "rs6054257");
        snp2 = new Snp(1, 14370L, "rs0054257");
        assertTrue(snp1.equals(snp2));
        assertTrue(snp2.equals(snp1));
    }

    @Test
    void compareToWithRsid() {
        Snp snp1 = new Snp("rs6040355");
        Snp snp2 = new Snp("rs6054257");
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        snp1 = new Snp("rs6040355");
        snp2 = new Snp("rs6040355");
        assertTrue(0 == snp1.compareTo(snp2));
        assertTrue(0 == snp2.compareTo(snp1));
    }

    @Test
    void compareToWithChrAndBpTest() {
        Snp snp1 = new Snp(10, 14370L);
        Snp snp2 = new Snp(20, 14370L);
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        snp1 = new Snp(20, 14370L);
        snp2 = new Snp(20, 14370L);
        assertTrue(0 == snp1.compareTo(snp2));
        assertTrue(0 == snp2.compareTo(snp1));

        snp1 = new Snp(20, 4370L);
        snp2 = new Snp(20, 14370L);
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        snp1 = new Snp(1, 4370L);
        snp2 = new Snp(2, 14370L);
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));
    }

    @Test
    void compareToWithThreeValuesTest() {
        // Snp1 is missing chr and bp
        Snp snp1 = new Snp("rs6054257");
        Snp snp2 = new Snp(20, 14370L, "rs6054257");
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        // Snp1 is missing rsid
        snp1 = new Snp(20, 14370L);
        snp2 = new Snp(20, 14370L, "rs6054257");
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        // All equal
        snp1 = new Snp(20, 14370L, "rs6054257");
        snp2 = new Snp(20, 14370L, "rs6054257");
        assertTrue(0 == snp1.compareTo(snp2));
        assertTrue(0 == snp2.compareTo(snp1));

        // bp is different
        snp1 = new Snp(20, 146L, "rs6054257");
        snp2 = new Snp(20, 14370L, "rs6054257");
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        // chr is different
        snp1 = new Snp(1, 14370L, "rs6054257");
        snp2 = new Snp(2, 14370L, "rs6054257");
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));

        // rsid is different: Even when they are considered equivalent, they can be sorted by rsid
        snp1 = new Snp(1, 14370L, "rs0054257");
        snp2 = new Snp(1, 14370L, "rs6054257");
        assertTrue(0 > snp1.compareTo(snp2));
        assertTrue(0 < snp2.compareTo(snp1));
    }

}