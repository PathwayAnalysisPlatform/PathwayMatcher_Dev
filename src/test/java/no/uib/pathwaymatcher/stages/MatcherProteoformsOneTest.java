package no.uib.pathwaymatcher.stages;

import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.tools.Parser;
import no.uib.pathwaymatcher.tools.ParserProteoformSimple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class MatcherProteoformsOneTest {
    static Parser parser;
    static Matcher matcher;
    static Proteoform iP, rP;

    @BeforeAll
    static void setUp() {
        parser = new ParserProteoformSimple();
        matcher = FactoryMatcher.getMatcher("uniprotListAndModSites", "one");
        assertEquals(MatcherProteoformsOne.class, matcher.getClass());
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

            iP = parser.getProteoform("A2RUS2;00046:490");
            rP = parser.getProteoform("A2RUS2;00000:null,00046:490");
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

            iP = parser.getProteoform("A2RUS2-2;00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00048:null,00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00048:490");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            rP = parser.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = parser.getProteoform("A2RUS2-2;00048:490,00046:472");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
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

            iP = parser.getProteoform("A2RUS2-2;00048:10");
            rP = parser.getProteoform("A2RUS2-2;00046:472,00046:490");
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

            iP = parser.getProteoform("A2RUS2;00046:null");
            rP = parser.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

}