package no.uib.pap.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;

import no.uib.pap.model.ProteoformFormat;
import no.uib.pap.pathwaymatcher.Conf;
import no.uib.pap.pathwaymatcher.Matching.Matcher;
import no.uib.pap.pathwaymatcher.Matching.MatcherFactory;
import no.uib.pap.pathwaymatcher.Matching.MatcherProteoformsOne;
import no.uib.pap.model.Proteoform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static no.uib.pap.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Query to check the examples:
 * MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity)
 WHERE pe.speciesName = "Homo sapiens" AND re.databaseName = "UniProt" AND re.identifier = "P60880"
 WITH DISTINCT pe, re OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)
 WITH DISTINCT pe, (CASE WHEN size(re.variantIdentifier) > 0 THEN re.variantIdentifier ELSE re.identifier END) as proteinAccession, tm.coordinate as coordinate, mod.identifier as type ORDER BY type, coordinate
 WITH DISTINCT pe, proteinAccession, COLLECT(type + ":" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE "null" END) AS ptms
 RETURN DISTINCT proteinAccession,
 pe.stId as ewas,
 (CASE WHEN pe.startCoordinate IS NOT NULL AND pe.startCoordinate <> -1 THEN pe.startCoordinate ELSE "null" END) as startCoordinate,
 (CASE WHEN pe.endCoordinate IS NOT NULL AND pe.endCoordinate <> -1 THEN pe.endCoordinate ELSE "null" END) as endCoordinate,
 ptms ORDER BY proteinAccession, startCoordinate, endCoordinate
 */

class MatcherProteoformsOneTest {
	static ProteoformFormat pf;
    static Matcher matcher;
    static no.uib.pap.model.Proteoform iP, rP;
    static Set<Proteoform> entities;
    static SetMultimap<Proteoform, String> result;

    @BeforeAll
    static void setUp() {
        pf = ProteoformFormat.SIMPLE;
        matcher = MatcherFactory.getMatcher("proteoforms", "one");
        assertEquals(MatcherProteoformsOne.class, matcher.getClass());

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

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:490");
            rP = pf.getProteoform("A2RUS2;00000:null,00046:490");
            assertTrue(matcher.matches(iP, rP));

            // These pass because the input contains all the ptms of the reference
            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2;");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;00046:472");
            rP = pf.getProteoform("A2RUS2");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            rP = pf.getProteoform("A2RUS2-2;00046:472");
            assertTrue(matcher.matches(iP, rP));

            // The input still contains the all the reference PTMs because a ptm is repeated 3 times
            iP = pf.getProteoform("A2RUS2;01234:12,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00048:null,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00048:490");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:null,00046:null");
            rP = pf.getProteoform("A2RUS2;01234:12,00046:null,00046:1,00046:null");
            assertTrue(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00048:490,00046:472");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
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

        // They fail because some of the PTMs in the input are not in the reference. They would match if it was the other way around
        try {
            iP = pf.getProteoform("A2RUS2;");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertFalse(matcher.matches(iP, rP));

            iP = pf.getProteoform("A2RUS2-2;00048:10");
            rP = pf.getProteoform("A2RUS2-2;00046:472,00046:490");
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

            // This one matches because the null is a wild card for the 472
            iP = pf.getProteoform("A2RUS2;00046:null");
            rP = pf.getProteoform("A2RUS2;00046:472");
            assertTrue(matcher.matches(iP, rP));

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchInNormalCase() {

        try {
            entities.add(pf.getProteoform("P01308;00798:109"));
            result = matcher.match(entities);
            assertEquals(14, result.values().size());
            assertTrue(result.values().contains("R-HSA-141723"));
            assertTrue(result.values().contains("R-HSA-6808710"));
            assertTrue(result.values().contains("R-HSA-264971"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("P01308;00798:31,00798:43"));
            result = matcher.match(entities);
            assertEquals(18, result.values().size());
            assertTrue(result.values().contains("R-HSA-429343"));
            assertTrue(result.values().contains("R-HSA-264893"));
            assertTrue(result.values().contains("R-HSA-265075"));
            assertTrue(result.values().contains("R-HSA-429465"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("P01308"));
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
            entities.add(pf.getProteoform("P60880"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-5244499"));
            assertTrue(result.values().contains("R-HSA-5244501"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("P60880;00115:92"));
            result = matcher.match(entities);
            assertEquals(7, result.values().size());
            assertTrue(result.values().contains("R-HSA-3004546"));
            assertTrue(result.values().contains("R-HSA-5244501"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

    }

    @Test
    void matchWithIsoform() {
        try {
            entities.add(pf.getProteoform("Q9UBU3-2"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-422044"));
            assertTrue(result.values().contains("R-HSA-422090"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("Q9UPP1-3"));
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
            entities.add(pf.getProteoform("Q9UBU3"));
            result = matcher.match(entities);
            assertEquals(0, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchWithUniprotAccession() {
        try {
            entities.add(pf.getProteoform("Q9UPP1"));
            result = matcher.match(entities);
            assertEquals(1, result.values().size());
            assertTrue(result.values().contains("R-HSA-5423096"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void matchWithMorePTMsThanAnnotated() {

        try {
            entities.add(pf.getProteoform("Q9UPP1-1;00046:69"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-2172669"));
            assertTrue(result.values().contains("R-HSA-2245211"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("Q9UPP1-1;00046:69,00046:120"));
            result = matcher.match(entities);
            assertEquals(2, result.values().size());
            assertTrue(result.values().contains("R-HSA-2245211"));
            assertTrue(result.values().contains("R-HSA-2172669"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("Q9UBU3-1;00390:26"));
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
            entities.add(pf.getProteoform("Q9UPP1-1;00046:null"));
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
            entities.add(pf.getProteoform("O95644;00046:null"));
            result = matcher.match(entities);
            assertEquals(3, result.values().size());
            assertTrue(result.values().contains("R-HSA-2025953"));
            assertTrue(result.values().contains("R-HSA-2685618"));
            assertTrue(result.values().contains("R-HSA-2025935"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.add(pf.getProteoform("O95644;00046:257"));
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
            entities.add(pf.getProteoform("O95644;00046:175"));
            result = matcher.match(entities);
            assertEquals(1, result.values().size());
            assertTrue(result.values().contains("R-HSA-2025953"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }

        try {
            entities.clear();
            entities.add(pf.getProteoform("O95644;00046:257,00046:null"));
            result = matcher.match(entities);
            assertEquals(3, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void coordinatesWithinMarginTest(){

        try {
            entities.add(pf.getProteoform("P60880;00115:87"));
            result = matcher.match(entities);
            assertEquals(7, result.values().size());
            assertTrue(result.values().contains("R-HSA-3004546"));
            assertTrue(result.values().contains("R-HSA-6806142"));
            assertTrue(result.values().contains("R-HSA-5244501"));
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

}