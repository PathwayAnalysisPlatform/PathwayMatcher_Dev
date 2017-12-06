package no.uib.pathwaymatcher.stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.Matching.MatcherFactory;
import no.uib.pathwaymatcher.Matching.Matcher;
import no.uib.pathwaymatcher.Search.Finder;
import no.uib.pathwaymatcher.model.Pathway;
import no.uib.pathwaymatcher.model.Proteoform;
import no.uib.pathwaymatcher.model.Reaction;
import no.uib.pathwaymatcher.Preprocessing.Parsing.Parser;
import no.uib.pathwaymatcher.Preprocessing.Parsing.ParserProteoformSimple;
import no.uib.pathwaymatcher.tools.PathwayStaticFactory;
import no.uib.pathwaymatcher.tools.ReactionStaticFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static org.junit.jupiter.api.Assertions.*;

/*
Query to check the examples.
// Get Reactions, Pathways and TopLevelPathways by Ewas
MATCH (tlp:TopLevelPathway)-[:hasEvent*]->(p:Pathway)-[:hasEvent*]->(rle:ReactionLikeEvent),
(rle)-[:input|output|catalystActivity|disease|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)
WHERE tlp.speciesName = "Homo sapiens" AND p.speciesName = "Homo sapiens" AND rle.speciesName = "Homo sapiens"
AND pe.stId = "R-HSA-74673"
RETURN DISTINCT pe.stId,  rle.stId AS Reaction, rle.displayName as ReactionDisplayName, p.stId AS Pathway, p.displayName AS PathwayDisplayName, tlp.stId as TopLevelPathwayStId, tlp.displayName as TopLevelPathwayDisplayName
ORDER BY Reaction
 */

class FinderTest {

    static Parser parser;
    static Matcher matcher;
    static Set<Proteoform> entities;
    static SetMultimap<Proteoform, String> mapping;
    private TreeMultimap<Proteoform, Reaction> result;


    @BeforeAll
    static void setUp() {
        parser = new ParserProteoformSimple();
        matcher = MatcherFactory.getMatcher("uniprotListAndModSites", "flexible");

        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        Conf.setDefaultValues();
    }

    @BeforeEach
    void setEachUp() {
        entities = new HashSet<>();
        mapping = HashMultimap.create();
        PathwayStaticFactory.clear();
        ReactionStaticFactory.clear();
    }

    @Test
    void searchOneReactionWithoutTopLevelPathwaysTest() {

        Conf.setValue(Conf.BoolVars.showTopLevelPathways, false);
        try {
            Proteoform proteoform = parser.getProteoform("P60880");
            mapping.put(proteoform, "R-HSA-5244499");
            result = Finder.search(mapping);
            assertEquals(1, result.values().size());    // One reaction

            Reaction expectedReaction = new Reaction("R-HSA-194818", "BoNT/A LC cleaves target cell SNAP25");
            assertTrue(result.containsValue(expectedReaction));
            for (Reaction reaction : result.values()) {
                assertEquals(4, reaction.getPathwaySet().size());
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-5250968", "Toxicity of botulinum toxin type A (BoNT/A)")));
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-168799", "Neurotoxicity of clostridium toxins")));
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-5339562", "Uptake and actions of bacterial toxins")));
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-5663205", "Infectious disease")));
                for (Pathway pathway : reaction.getPathwaySet()) {
                    assertEquals(0, pathway.getTopLevelPathwaySet().size());
                }
            }

        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void searchOneReactionWithTopLevelPathwaysTest() {

        Conf.setValue(Conf.BoolVars.showTopLevelPathways, true);
        try {
            Proteoform proteoform = parser.getProteoform("P60880");
            mapping.put(proteoform, "R-HSA-5244499");
            result = Finder.search(mapping);
            assertEquals(1, result.values().size());    // One reaction

            Reaction expectedReaction = new Reaction("R-HSA-194818", "BoNT/A LC cleaves target cell SNAP25");
            assertTrue(result.containsValue(expectedReaction));
            for (Reaction reaction : result.values()) {
                assertEquals(4, reaction.getPathwaySet().size());
                Pathway tlp = new Pathway("R-HSA-1643685", "Disease");
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-5250968", "Toxicity of botulinum toxin type A (BoNT/A)")));
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-168799", "Neurotoxicity of clostridium toxins")));
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-5339562", "Uptake and actions of bacterial toxins")));
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-5663205", "Infectious disease")));
                for (Pathway pathway : reaction.getPathwaySet()) {
                    assertEquals(1, pathway.getTopLevelPathwaySet().size());
                    assertTrue(pathway.getTopLevelPathwaySet().contains(new Pathway("R-HSA-1643685", "Disease")));
                }
            }
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void searchWithTopLevelPathways2Test() {
        Conf.setValue(Conf.BoolVars.showTopLevelPathways, true);
        try {
            Proteoform proteoform = parser.getProteoform("P01308;00798:31,00798:43");
            mapping.put(proteoform, "R-HSA-429343");
            result = Finder.search(mapping);
            assertEquals(1, result.values().size());    // One reaction

            Reaction expectedReaction = new Reaction("R-HSA-977136", "Amyloid precursor proteins form ordered fibrils");
            assertTrue(result.containsValue(expectedReaction));
            for (Reaction reaction : result.values()) {
                assertEquals(1, reaction.getPathwaySet().size());
                Pathway tlp = new Pathway("R-HSA-392499", "Metabolism of proteins");
                assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-977225", "Amyloid fiber formation")));
                for (Pathway pathway : reaction.getPathwaySet()) {
                    assertEquals(1, pathway.getTopLevelPathwaySet().size());
                    assertTrue(pathway.getTopLevelPathwaySet().contains(new Pathway("R-HSA-392499", "Metabolism of proteins")));
                }
            }
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void searchWithBrokenStId() {
        Conf.setValue(Conf.BoolVars.showTopLevelPathways, true);
        try {
            Proteoform proteoform = parser.getProteoform("P01308;00798:31,00798:43");
            mapping.put(proteoform, "R-HSA-111111");
            result = Finder.search(mapping);
            assertEquals(0, result.values().size());
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void ewasMappingToMultipleReactions() {
        Conf.setValue(Conf.BoolVars.showTopLevelPathways, true);
        try {
            Proteoform proteoform = parser.getProteoform("P01308;00798:31,00798:43");
            mapping.put(proteoform, "R-HSA-74673");
            result = Finder.search(mapping);
            assertEquals(13, result.values().size());    // One reaction

            assertTrue(result.containsValue(new Reaction("R-HSA-74711", "Phosphorylation of IRS")));
            assertTrue(result.containsValue(new Reaction("R-HSA-110011", "Binding of Grb10 to the insulin receptor")));
            assertTrue(result.containsValue(new Reaction("R-HSA-265166", "Exocytosis of Insulin")));
            assertTrue(result.containsValue(new Reaction("R-HSA-422048", "Acyl Ghrelin and C-Ghrelin are secreted")));

            for (Reaction reaction : result.values()) {
                if (reaction.getStId().equals("R-HSA-422048")) {
                    assertEquals(2, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-392499", "Metabolism of proteins");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-422085", "Synthesis, secretion, and deacylation of Ghrelin")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }

                if (reaction.getStId().equals("R-HSA-74711")) {
                    assertEquals(4, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-162582", "Signal Transduction");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74713", "IRS activation")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74751", "Insulin receptor signalling cascade")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74752", "Signaling by Insulin receptor")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }

                if (reaction.getStId().equals("R-HSA-265166")) {
                    assertEquals(2, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-1430728", "Metabolism");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-422356", "Regulation of insulin secretion")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-163685", "Integration of energy metabolism")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }

                if (reaction.getStId().equals("R-HSA-110011")) {
                    assertEquals(4, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-162582", "Signal Transduction");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74749", "Signal attenuation")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74751", "Insulin receptor signalling cascade")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74752", "Signaling by Insulin receptor")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-9006934", "Signaling by Receptor Tyrosine Kinases")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }
            }
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }

    @Test
    void searchWithMultipleEwas() {
        Conf.setValue(Conf.BoolVars.showTopLevelPathways, true);
        try {
            entities.add(parser.getProteoform("P06213;00048:999,00048:1185,00048:1189,00048:1190,00048:1355,00048:1361"));
            mapping = matcher.match(entities);
            assertEquals(6, mapping.values().size());
            assertEquals(6, mapping.entries().size());

            result = Finder.search(mapping);
            Set<Reaction> uniqueReactions = new HashSet<>(result.values());
            assertEquals(14, uniqueReactions.size());

            assertTrue(result.containsValue(new Reaction("R-HSA-74707", "Binding of IRS to insulin receptor")));
            assertTrue(result.containsValue(new Reaction("R-HSA-74712", "Dissociation of IRS-P from insulin receptor")));
            assertTrue(result.containsValue(new Reaction("R-HSA-74715", "Autophosphorylation of insulin receptor")));

            for (Reaction reaction : result.values()) {
                if (reaction.getStId().equals("R-HSA-74742")) {
                    assertEquals(3, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-162582", "Signal Transduction");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74751", "Insulin receptor signalling cascade")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74752", "Signaling by Insulin receptor")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-9006934", "Signaling by Receptor Tyrosine Kinases")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }

                if (reaction.getStId().equals("R-HSA-8857925")) {
                    assertEquals(4, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-162582", "Signal Transduction");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-6811558", "PI5P, PP2A and IER3 Regulate PI3K/AKT Signaling")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-199418", "Negative regulation of the PI3K/AKT network")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-1257604", "PIP3 activates AKT signaling")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-9006925", "Intracellular signaling by second messengers")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }

                if (reaction.getStId().equals("R-HSA-74716")) {
                    assertEquals(2, reaction.getPathwaySet().size());

                    Pathway tlp = new Pathway("R-HSA-162582", "Signal Transduction");
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-74752", "Signaling by Insulin receptor")));
                    assertTrue(reaction.getPathwaySet().contains(new Pathway("R-HSA-9006934", "Signaling by Receptor Tyrosine Kinases")));
                    for (Pathway pathway : reaction.getPathwaySet()) {
                        assertEquals(1, pathway.getTopLevelPathwaySet().size());
                        assertTrue(pathway.getTopLevelPathwaySet().contains(tlp));
                    }
                }
            }
        } catch (ParseException e) {
            fail("Proteoforms should be parsed correctly.");
        }
    }
}