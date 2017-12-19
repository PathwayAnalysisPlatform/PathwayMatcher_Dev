package no.uib.pathwaymatcher;

import no.uib.pathwaymatcher.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

class PathwayMatcherServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void matchProteinsTest() {

        List<String> input = FileUtils.getInput("src/main/resources/input/Proteins/UniProt/Hypoglycemia.txt");
        PathwayMatcherService service = new PathwayMatcherService();
        List<String> result = service.match(
                input,
                Conf.InputTypeEnum.uniprotList.toString(),
                3,
                true,
                Conf.MatchType.FLEXIBLE.toString());

        assertEquals(79, result.size());
    }
}