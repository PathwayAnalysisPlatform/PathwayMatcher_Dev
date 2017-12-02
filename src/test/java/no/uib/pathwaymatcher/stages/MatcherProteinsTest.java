package no.uib.pathwaymatcher.stages;

import com.google.common.collect.SetMultimap;
import jdk.nashorn.internal.runtime.regexp.joni.MatcherFactory;
import no.uib.pathwaymatcher.Conf;
import no.uib.pathwaymatcher.model.Proteoform;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static no.uib.pathwaymatcher.db.ConnectionNeo4j.initializeNeo4j;
import static no.uib.pathwaymatcher.util.FileUtils.getInput;
import static org.junit.jupiter.api.Assertions.*;

class MatcherProteinsTest {

    /**
     *
     * Get ewas with a uniprot accession
     MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity)
     WHERE pe.speciesName = "Homo sapiens" AND re.databaseName = "UniProt" AND (re.identifier = "Q9Y6P5-3" OR re.variantIdentifier = "Q9Y6P5-3")
     RETURN (CASE WHEN re.variantIdentifier = "Q9Y6P5-3" THEN re.variantIdentifier ELSE re.identifier END) as protein, count(pe.stId) as count, collect(DISTINCT pe.stId) as ewas
     ORDER BY protein, ewas
     */

    @BeforeAll
    static void setUp() {
        initializeNeo4j("bolt://127.0.0.1:7687", "", "");
    }

    /**
     * Get ewas for all the isoform set
     *
     * MATCH (pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity)
     WHERE pe.speciesName = "Homo sapiens" AND re.databaseName = "UniProt"
     WITH (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as protein, count(pe.stId) as count, collect(DISTINCT pe.stId) as ewas
     WHERE protein in ["P42167-1","P42224","P42224-1","P42224-2","P42226","P42229","P42261","P42262","Q9Y6P5-1","Q9Y6P5-3","Q9Y6Q1"]
     RETURN protein, count, ewas
     ORDER BY protein, ewas
     */

    @Test
    void correctListTest() {
        // Read input
        Set<Proteoform> entities = null;
        Preprocessor preprocessor = FactoryPreprocessor.getPreprocessor("uniprotList");
        assertEquals(preprocessor.getClass(), PreprocessorProteins.class);

        try {
            // Create entity list with preprocessor
            entities = preprocessor.process(getInput("src/test/resources/Generic/Proteins/Valid/listWithIsoforms.txt"));
        } catch (java.text.ParseException e) {
            fail("File should be processed correctly.");
        }

        // Match
        Matcher matcher = FactoryMatcher.getMatcher("uniprotList", "protein");
        assertEquals(matcher.getClass(), MatcherProteins.class);
        SetMultimap<Proteoform, String> mapping = matcher.match(entities);
        assertEquals(11, mapping.keySet().size());
        Assert.assertEquals(36, mapping.entries().size());

        Assert.assertTrue(mapping.values().contains("R-HSA-2993885"));
        Assert.assertTrue(mapping.values().contains("R-HSA-8848635"));
        Assert.assertTrue(mapping.values().contains("R-HSA-909676"));
    }

}