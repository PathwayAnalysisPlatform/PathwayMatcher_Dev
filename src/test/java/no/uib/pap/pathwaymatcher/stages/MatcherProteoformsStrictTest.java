package no.uib.pap.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;

import no.uib.pap.model.Proteoform;
import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.Matching.Matcher;
import no.uib.pap.pathwaymatcher.Matching.MatcherFactory;
import no.uib.pap.pathwaymatcher.Matching.MatcherProteoformsStrict;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.jupiter.api.Assertions.*;

class MatcherProteoformsStrictTest {

    static ProteoformFormat pf;
    static Matcher matcher;
    static Proteoform iP, rP;
    static Set<Proteoform> entities;
    static SetMultimap<Proteoform, String> result;

    @BeforeAll
    static void setUp() {
        pf = ProteoformFormat.SIMPLE;
        matcher = MatcherFactory.getMatcher("proteoforms", "strict");
        assertEquals(MatcherProteoformsStrict.class, matcher.getClass());

        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        Conf.setDefaultValues();
    }


    @BeforeEach
    void setUpEach() {
        entities = new HashSet<>();
    }

    // Proteoforms simple
    @Test
    void matchesSameAllTest() {

        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("A2RUS2;");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("A2RUS2");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2");
            rP = pf.getProteoform("A2RUS2-2");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            rP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00000:null,00046:490");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:490,00000:null");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentUniProtAccTest() {

        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("P01308;");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("P01308");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("P01308;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:472,00046:490");
            rP = pf.getProteoform("P01308;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            rP = pf.getProteoform("P01308;00000:null,00046:490");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentIsoformTest() {

        try {
            iP = pf.getProteoform("A2RUS2-1;");
            rP = pf.getProteoform("A2RUS2;");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("A2RUS2-1");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-1");
            rP = pf.getProteoform("A2RUS2-2");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-1;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-1;00046:472,00046:490");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            rP = pf.getProteoform("A2RUS2-3;00000:null,00046:490");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentNumberOfPtmsTest() {

        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentPtmTypesTest() {

        try {
            iP = pf.getProteoform("A2RUS2;00048:472");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00000:472");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00048:490,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertFalse(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void noMatchDifferentPtmCoordinatesTest() {

        try {
            iP = pf.getProteoform("A2RUS2;00048:400");
            rP = pf.getProteoform("A2RUS2;00048:472");
            assertFalse(matcher.matches(iP, rP));

            // Matches because the null is a wildcard for any number
            iP = pf.getProteoform("A2RUS2;00046:null");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP));

            // Matches because the null is a wildcard for any number
            iP = pf.getProteoform("A2RUS2-2;00048:null,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00048:490");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00048:495,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00048:490");
            assertFalse(matcher.matches(iP, rP));

            // Matches because the null is a wildcard for any number
            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            assertTrue(matcher.matches(iP, rP));

            // Matches because the null is a wildcard for any number
            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:8");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00048:8");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00048:1,00046:null");
            assertFalse(matcher.matches(iP, rP));

            // Matches because the null is a wildcard for any number
            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchInNormalCase() {

        try {
            entities.add(pf.getProteoform("P01308;00087:53,00798:31,00798:43"));
            entities.add(pf.getProteoform("P01308;00798:95,00798:96,00798:100,00798:109"));
            result = matcher.match(entities);
            assertEquals(4, result.values().size());
            assertTrue(result.values().contains("R-HSA-429343"));
            assertTrue(result.values().contains("R-HSA-264971"));
            assertTrue(result.values().contains("R-HSA-429369"));
            assertTrue(result.values().contains("R-HSA-74672"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("P60880"));
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
            entities.add(pf.getProteoform("P02545-2"));
            result = matcher.match(entities);
            assertEquals(3, result.values().size());
            assertTrue(result.values().contains("R-HSA-2995387"));
            assertTrue(result.values().contains("R-HSA-264872"));
            assertTrue(result.values().contains("R-HSA-912378"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        // Keeping the previous proteoform it adds more
        try {
            entities.add(pf.getProteoform("Q9Y6W8-1;00048:180"));
            result = matcher.match(entities);
            assertEquals(4, result.values().size());
            assertTrue(result.values().contains("R-HSA-389927"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        // Add one that does not exist
        try {
            entities.add(pf.getProteoform("P00000"));
            result = matcher.match(entities);
            assertEquals(4, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

    }

    @Test
    void matchWithIsoformAndPtms() {

        try {
            entities.add(pf.getProteoform("P02545-1;00046:395"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        result = matcher.match(entities);

        assertEquals(1, result.values().size());

        assertTrue(result.values().contains("R-HSA-5228743"));
    }

    @Test
    void matchWithIsoformAndTwoPtms() {

        try {
            entities.add(pf.getProteoform("P02545-2;00046:22,00046:395"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        result = matcher.match(entities);

        assertEquals(1, result.values().size());

        assertTrue(result.values().contains("R-HSA-5229019"));
    }

    @Test
    void matchWithWithNullsPtms() {

        try {
            entities.add(pf.getProteoform("P01892"));
            result = matcher.match(entities);
            assertEquals(8, result.values().size());
            assertTrue(result.values().contains("R-HSA-1236884"));
            assertTrue(result.values().contains("R-HSA-8943104"));
            assertTrue(result.values().contains("R-HSA-182252"));
            assertTrue(result.values().contains("R-HSA-8863857"));
            assertTrue(result.values().contains("R-HSA-983389"));
            assertTrue(result.values().contains("R-HSA-182267"));
            assertTrue(result.values().contains("R-HSA-198892"));
            assertTrue(result.values().contains("R-HSA-983075"));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("P01892;01148:null"));
            result = matcher.match(entities);
            assertEquals(1, result.values().size());
            assertTrue(result.values().contains("R-HSA-8943073"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("Q9P218;00039:null,01914:null"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-2192910"));
            assertTrue(result.values().contains("R-HSA-2173179"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

    }

    @Test
    void coordinatesWithinMarginTest(){

        try {
            entities.add(pf.getProteoform("P60880;00115:87"));
            result = matcher.match(entities);
            assertEquals(0, result.values().size());
            assertFalse(result.values().contains("R-HSA-5244499"));
            assertFalse(result.values().contains("R-HSA-5244501"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("P60880;00115:87,00115:89,00115:91,00115:95"));
            result = matcher.match(entities);
            assertEquals(5, result.values().size());
            assertTrue(result.values().contains("R-HSA-3004546"));
            assertTrue(result.values().contains("R-HSA-6806142"));
            assertFalse(result.values().contains("R-HSA-5244501"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

}