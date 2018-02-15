package no.uib.pap.pathwaymatcher.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.ProteoformFormat;

class ProteoformTest {

    @Test
    void multiplePTMsWithSameAttributesTest(){
        Proteoform proteoform = new Proteoform("P01308");

        proteoform.addPtm("00048", null);
        proteoform.addPtm("00048", null);
        proteoform.addPtm("00048", null);

        assertEquals(3, proteoform.getPtms().entries().size());
    }

    @Test
    void equalsithAccessionTest() {
    	ProteoformFormat p = ProteoformFormat.SIMPLE;
        try {
            assertFalse(p.getProteoform("P10276").equals(p.getProteoform("Q15349")));
            assertTrue(p.getProteoform("P10276").equals(p.getProteoform("P10276")));
            assertFalse(p.getProteoform("Q15349").equals(p.getProteoform("P10276")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void compareToWithAccessionTest() {
    	ProteoformFormat p = ProteoformFormat.SIMPLE;

        try {
            assertTrue(0 > p.getProteoform("P10276").compareTo(p.getProteoform("Q15349")));
            assertTrue(0 == p.getProteoform("P10276").compareTo(p.getProteoform("P10276")));
            assertTrue(0 < p.getProteoform("Q15349").compareTo(p.getProteoform("P10276")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void compareToWithIsoformTest() {

    	ProteoformFormat p = ProteoformFormat.SIMPLE;

        try {
            // One with isoform and one without isoform
            assertTrue(0 > p.getProteoform("P02545").compareTo(p.getProteoform("P02545-1")));
            assertTrue(0 == p.getProteoform("P02545-1").compareTo(p.getProteoform("P02545-1")));
            assertTrue(0 < p.getProteoform("P02545-2").compareTo(p.getProteoform("P02545")));

            // Both with isoform
            assertTrue(0 > p.getProteoform("P02545-1").compareTo(p.getProteoform("P02545-2")));
            assertTrue(0 == p.getProteoform("P02545-1").compareTo(p.getProteoform("P02545-1")));
            assertTrue(0 < p.getProteoform("P02545-2").compareTo(p.getProteoform("P02545-1")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void compareToWithSubsequenceTest() {
    	ProteoformFormat p = ProteoformFormat.PRO;

        try {
            // One without isoform and one with subsequence
            assertTrue(0 > p.getProteoform("UniProtKB:P02545").compareTo(p.getProteoform("UniProtKB:P02545,26-1712")));
            assertTrue(0 < p.getProteoform("UniProtKB:P02545,26-1712").compareTo(p.getProteoform("UniProtKB:P02545")));

            assertTrue(0 > p.getProteoform("UniProtKB:P02545,26-1712").compareTo(p.getProteoform("UniProtKB:P02545-1")));
            assertTrue(0 == p.getProteoform("UniProtKB:P02545,26-1712").compareTo(p.getProteoform("UniProtKB:P02545,26-1712")));
            assertTrue(0 < p.getProteoform("UniProtKB:P02545-1").compareTo(p.getProteoform("UniProtKB:P02545,26-1712")));

            // One with isoform and one with subsequence
            assertTrue(0 > p.getProteoform("UniProtKB:P02545-1").compareTo(p.getProteoform("UniProtKB:P02545-1,26-1712")));
            assertTrue(0 == p.getProteoform("UniProtKB:P02545-1,26-1712").compareTo(p.getProteoform("UniProtKB:P02545-1,26-1712")));
            assertTrue(0 < p.getProteoform("UniProtKB:P02545-1,26-1712").compareTo(p.getProteoform("UniProtKB:P02545-1")));

            // Both with subsequences
            assertTrue(0 > p.getProteoform("UniProtKB:P01308,23-54").compareTo(p.getProteoform("UniProtKB:P01308,90-110")));
            assertTrue(0 == p.getProteoform("UniProtKB:P01308,23-54").compareTo(p.getProteoform("UniProtKB:P01308,23-54")));
            assertTrue(0 < p.getProteoform("UniProtKB:P01308,90-110").compareTo(p.getProteoform("UniProtKB:P01308,23-54")));

            // With null values
            assertTrue(0 > p.getProteoform("UniProtKB:P01308,?-54").compareTo(p.getProteoform("UniProtKB:P01308,90-110")));
            assertTrue(0 > p.getProteoform("UniProtKB:P01308,?-?").compareTo(p.getProteoform("UniProtKB:P01308,90-110")));
            assertTrue(0 > p.getProteoform("UniProtKB:P01308,null-54").compareTo(p.getProteoform("UniProtKB:P01308,90-110")));
            assertTrue(0 > p.getProteoform("UniProtKB:P01308,null-null").compareTo(p.getProteoform("UniProtKB:P01308,90-110")));
            assertTrue(0 == p.getProteoform("UniProtKB:P01308,?-?").compareTo(p.getProteoform("UniProtKB:P01308")));
            assertTrue(0 == p.getProteoform("UniProtKB:P01308,null-null").compareTo(p.getProteoform("UniProtKB:P01308")));
            assertTrue(0 < p.getProteoform("UniProtKB:P01308,90-110").compareTo(p.getProteoform("UniProtKB:P01308,?-54")));
            assertTrue(0 < p.getProteoform("UniProtKB:P01308,90-110").compareTo(p.getProteoform("UniProtKB:P01308,?-?")));
            assertTrue(0 < p.getProteoform("UniProtKB:P01308,90-110").compareTo(p.getProteoform("UniProtKB:P01308,null-54")));
            assertTrue(0 < p.getProteoform("UniProtKB:P01308,90-110").compareTo(p.getProteoform("UniProtKB:P01308,null-null")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void compareToWithPtmsTest(){
    	ProteoformFormat p = ProteoformFormat.SIMPLE;

        try {

            // By number of ptms : 0+
            assertTrue( 0 > p.getProteoform("Q49B96").compareTo(p.getProteoform("Q49B96;00798:30,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00798:30").compareTo(p.getProteoform("Q49B96;00798:30")));
            assertTrue( 0 < p.getProteoform("Q49B96;00798:30").compareTo(p.getProteoform("Q49B96")));

            // By number of ptms : 1+
            assertTrue( 0 > p.getProteoform("Q49B96;00798:30").compareTo(p.getProteoform("Q49B96;00798:30,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00798:30").compareTo(p.getProteoform("Q49B96;00798:30")));
            assertTrue( 0 < p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("Q49B96;00798:30")));

            // By inputType
            assertTrue( 0 > p.getProteoform("Q49B96;00048:30,00798:40").compareTo(p.getProteoform("Q49B96;00798:30,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("Q49B96;00798:30,00798:40")));
            assertTrue( 0 < p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("P01308;00048:30,00798:40")));

            // By inputType in the second ptm
            assertTrue( 0 > p.getProteoform("Q49B96;00048:30,00048:40").compareTo(p.getProteoform("Q49B96;00048:30,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00048:30,00798:40").compareTo(p.getProteoform("Q49B96;00048:30,00798:40")));
            assertTrue( 0 < p.getProteoform("Q49B96;00048:30,00798:40").compareTo(p.getProteoform("Q49B96;00048:30,00048:40")));

            // By coordinate
            assertTrue( 0 > p.getProteoform("Q49B96;00798:28,00798:40").compareTo(p.getProteoform("Q49B96;00798:30,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("Q49B96;00798:30,00798:40")));
            assertTrue( 0 < p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("P01308;00798:1,00798:43")));

            // By coordinate with nulls
            assertTrue( 0 > p.getProteoform("Q49B96;00798:null,00798:40").compareTo(p.getProteoform("Q49B96;00798:28,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00798:null,00798:40").compareTo(p.getProteoform("Q49B96;00798:null,00798:40")));
            assertTrue( 0 < p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("P01308;00798:null,00798:43")));

            // By coordinate with nulls in the second ptm
            assertTrue( 0 > p.getProteoform("Q49B96;00798:20,00798:null").compareTo(p.getProteoform("Q49B96;00798:20,00798:40")));
            assertTrue( 0 == p.getProteoform("Q49B96;00798:30,00798:null").compareTo(p.getProteoform("Q49B96;00798:30,00798:null")));
            assertTrue( 0 < p.getProteoform("Q49B96;00798:30,00798:40").compareTo(p.getProteoform("P01308;00798:30,00798:null")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void compareToWithSubsequencesAndPtms(){
    	ProteoformFormat p = ProteoformFormat.PRO;

        try {

            // By number of ptms : 0+
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590,1-882").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590").compareTo(p.getProteoform("UniProtKB:P29590")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,1-882").compareTo(p.getProteoform("UniProtKB:P29590,1-882")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882")));

            // By number of ptms : 1+
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590,Lys-65/Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590,1-882,Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,Lys-65/Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,Lys-65/Lys-160,MOD:01149")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,1-882,Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-160,MOD:01149")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160,MOD:01149")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,Lys-65/Lys-160,MOD:01149")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-160,MOD:01149")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160/Lys-490,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,1-882,Lys-65/Lys-160,MOD:01149")));

            // By inputType: Type has priority over coordinate
            assertTrue( 0 > p.getProteoform("UniProtKB:P29590,Lys-160,MOD:00048").compareTo(p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P29590,Lys-160,MOD:01149").compareTo(p.getProteoform("UniProtKB:P29590,Lys-160,MOD:00048")));

            // By inputType in the second ptm block
            assertTrue( 0 > p.getProteoform("UniProtKB:P02452,162-1218,Pro-null,MOD:00039|Lys-19,MOD:01914").compareTo(p.getProteoform("UniProtKB:P02452,162-1218,Pro-null,MOD:00039|Lys-19,MOD:02000")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P02452,162-1218,Pro-null,MOD:00039|Lys-19,MOD:01914").compareTo(p.getProteoform("UniProtKB:P02452,162-1218,Pro-null,MOD:00039|Lys-19,MOD:01914")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P02452,162-1218,Pro-null,MOD:00039|Lys-19,MOD:02000").compareTo(p.getProteoform("UniProtKB:P02452,162-1218,Pro-null,MOD:00039|Lys-19,MOD:01914")));

            // By coordinate
            assertTrue( 0 > p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));

            // By coordinate with nulls
            assertTrue( 0 > p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-5,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));

            // By coordinate with nulls in the second ptm
            assertTrue( 0 > p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 > p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-8/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-10/Glu-54,MOD:00041")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 == p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-8/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-8/Glu-54,MOD:00041")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-46/Glu-47/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-47/Glu-54,MOD:00041")));
            assertTrue( 0 < p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-10/Glu-54,MOD:00041").compareTo(p.getProteoform("UniProtKB:P00742,41-179,Pro-102,MOD:00036|Glu-5/Glu-8/Glu-54,MOD:00041")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}