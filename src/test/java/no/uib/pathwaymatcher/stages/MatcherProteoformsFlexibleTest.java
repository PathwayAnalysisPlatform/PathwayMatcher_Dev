package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.tools.Parser;
import no.uib.pathwaymatcher.tools.ParserProteoformSimple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.jupiter.api.Assertions.*;

class MatcherProteoformsFlexibleTest {

    static Parser parser;
    static Matcher matcher;
    static Proteoform iP, rP;
    static Set<Proteoform> entities;
    static SetMultimap<Proteoform, String> result;

    @BeforeAll
    static void setUp() {
        parser = new ParserProteoformSimple();
        matcher = FactoryMatcher.getMatcher("uniprotListAndModSites", "flexible");
        assertEquals(MatcherProteoformsFlexible.class, matcher.getClass());

        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        Conf.setDefaultValues();
    }

    @BeforeEach
    void setEachUp() {
        entities = new HashSet<>();
    }

    // Proteoforms simple
    @Test
    void matchesSameAllTest() {

        try {
            iP = parser.getProteoform("A2RUS2;");
            rP = parser.getProteoform("A2RUS2;");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2");
            rP = parser.getProteoform("A2RUS2");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2");
            rP = parser.getProteoform("A2RUS2-2");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00046:472");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00000:null,00046:490");
            rP = parser.getProteoform("A2RUS2;00000:null,00046:490");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;01234:12,00000:null,00046:490");
            rP = parser.getProteoform("A2RUS2;01234:12,00046:490,00000:null");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            rP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = parser.getProteoform("A2RUS2;01234:12");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = parser.getProteoform("A2RUS2");
            assertTrue(matcher.matches(iP, rP));

            // These pass because the input contains all the ptms of the reference
            iP = parser.getProteoform("A2RUS2;00046:472");
            rP = parser.getProteoform("A2RUS2;");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00046:472");
            rP = parser.getProteoform("A2RUS2");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            rP = parser.getProteoform("A2RUS2-2;00046:472");
            assertTrue(matcher.matches(iP, rP));

            // The input still contains the all the reference PTMs because a ptm is repeated 3 times
            iP = parser.getProteoform("A2RUS2;01234:12,00046:null");
            rP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

            // It has the 2 ptms needed and more
            iP = parser.getProteoform("A2RUS2-2;00046:472,00046:490,01674:null");
            rP = parser.getProteoform("A2RUS2-2;00046:472;00046:490");
            assertTrue(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentUniProtAccTest() {

        try {
            iP = parser.getProteoform("A2RUS2;");
            rP = parser.getProteoform("P01308;");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2");
            rP = parser.getProteoform("P01308");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00046:472");
            rP = parser.getProteoform("P01308;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00046:472,00046:490");
            rP = parser.getProteoform("P01308;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00000:null,00046:490");
            rP = parser.getProteoform("P01308;00000:null,00046:490");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentIsoformTest() {

        try {
            iP = parser.getProteoform("A2RUS2-1;");
            rP = parser.getProteoform("A2RUS2;");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2");
            rP = parser.getProteoform("A2RUS2-1");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-1");
            rP = parser.getProteoform("A2RUS2-2");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-1;00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-1;00046:472,00046:490");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00000:null,00046:490");
            rP = parser.getProteoform("A2RUS2-3;00000:null,00046:490");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentNumberOfPtmsTest() {

        // They fail because some of the PTMs in the input are not in the reference. They would match if it was the other way around
        try {
            iP = parser.getProteoform("A2RUS2;");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00046:472;00046:490");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490,01674:null");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentPtmTypesTest() {

        try {
            iP = parser.getProteoform("A2RUS2;00048:472");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;00000:472");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00048:490,00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentPtmCoordinatesTest() {

        try {
            iP = parser.getProteoform("A2RUS2;00048:400");
            rP = parser.getProteoform("A2RUS2;00048:472");
            assertFalse(matcher.matches(iP, rP));

            //This one matches because the null is a wildcard that matches the 472
            iP = parser.getProteoform("A2RUS2;00046:null");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00048:null,00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00048:490");
            assertTrue(matcher.matches(iP, rP));

            // This one matches because the null serves as wildcard to get the 00046:1
            iP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            rP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            assertTrue(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchInNormalCase() {

        try {
            entities.add(parser.getProteoform("P01308;00798:31,00087:53,00798:43"));
            result = matcher.match(entities);
            assertEquals(12, result.values().size());
            assertTrue(result.values().contains("R-HSA-429343"));
            assertTrue(result.values().contains("R-HSA-74673"));
            assertTrue(result.values().contains("R-HSA-141723"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("P01308;00798:31,00798:43"));
            result = matcher.match(entities);
            assertEquals(11, result.values().size());
            assertTrue(result.values().contains("R-HSA-74673"));
            assertFalse(result.values().contains("R-HSA-429343"));
            assertTrue(result.values().contains("R-HSA-265011"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("P01308"));
            result = matcher.match(entities);
            assertEquals(5, result.values().size());
            assertTrue(result.values().contains("R-HSA-141723"));
            assertTrue(result.values().contains("R-HSA-264893"));
            assertTrue(result.values().contains("R-HSA-265011"));
            assertTrue(result.values().contains("R-HSA-141720"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("P60880"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-5244499"));
            assertTrue(result.values().contains("R-HSA-5244501"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

    }

    @Test
    void matchWithIsoform() {
        try {
            entities.add(parser.getProteoform("Q9UBU3-2"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-422044"));
            assertTrue(result.values().contains("R-HSA-422090"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("Q9UPP1-3"));
            result = matcher.match(entities);
            assertEquals(1, result.values().size());
            assertTrue(result.values().contains("R-HSA-2245212"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchWithUniprotAccessionNoMatches() {
        try {
            entities.add(parser.getProteoform("Q9UBU3"));
            result = matcher.match(entities);
            assertEquals(0, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchWithUniprotAccession() {
        try {
            entities.add(parser.getProteoform("Q9UPP1"));
            result = matcher.match(entities);
            assertEquals(1, result.values().size());
            assertTrue(result.values().contains("R-HSA-5423096"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchWithMorePTMsThatAnnotated() {

        try {
            entities.add(parser.getProteoform("Q9UPP1-1;00046:69"));
            result = matcher.match(entities);
            assertEquals(1, result.values().size());
            assertTrue(result.values().contains("R-HSA-2172669"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("Q9UPP1-1;00046:69,00046:120"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-2245211"));
            assertTrue(result.values().contains("R-HSA-2172669"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("Q9UBU3-1;00390:26"));
            result = matcher.match(entities);
            assertEquals(12, result.values().size());
            assertTrue(result.values().contains("R-HSA-422027"));
            assertTrue(result.values().contains("R-HSA-422066"));
            assertFalse(result.values().contains("R-HSA-422039"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

    }

    @Test
    void matchWithNullTest(){
        try {
            entities.add(parser.getProteoform("Q9UPP1-1;00046:null"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-2245211"));
            assertTrue(result.values().contains("R-HSA-2172669"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchWithNullManyPtmsTest(){
        try {
            entities.add(parser.getProteoform("O95644;00046:null"));
            result = matcher.match(entities);
            assertEquals(3, result.values().size());
            assertTrue(result.values().contains("R-HSA-2025953"));
            assertTrue(result.values().contains("R-HSA-2685618"));
            assertTrue(result.values().contains("R-HSA-2025935"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("O95644;00046:175"));
            result = matcher.match(entities);
            assertEquals(0, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("O95644;00046:257"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-2685618"));
            assertTrue(result.values().contains("R-HSA-2025935"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(parser.getProteoform("O95644;00046:257,00046:null"));
            result = matcher.match(entities);
            assertEquals(3, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }


// Proteoforms PRO
//Todo

// Proteoforms Neo4j
//Todo

}