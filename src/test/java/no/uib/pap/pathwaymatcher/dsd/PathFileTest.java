package no.uib.pap.pathwaymatcher.dsd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import junit.framework.TestCase;
import no.uib.pap.pathwaymatcher.dsd.io.GraphPool;
import no.uib.pap.pathwaymatcher.dsd.io.PathFile;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Path;
import org.junit.Assert;

/**
 * Test class for the path file.
 *
 * @author Marc Vaudel
 */
public class PathFileTest extends TestCase {

    /**
     * Tests that files written and parsed to the file get correctly parsed.
     * 
     * @throws IOException exception thrown if an error occurred while reading or writing the file
     * @throws InterruptedException exception thrown if a thread gets interrupted
     * @throws TimeoutException exception thrown if computing the shortest path matrix times out
     */
    public void testPathFile() throws IOException, InterruptedException, TimeoutException, DataFormatException {

        Graph testGraph = GraphPool.getTestGraph();

        PathMatrix pathMatrix = new PathMatrix(testGraph);
        pathMatrix.computeMatrix(2);
        Path[][] shortestPath = pathMatrix.getShortestPaths();

        File testFile = new File("output/pathFile");
        if (testFile.exists()) {
            testFile.delete();
        }
        PathFile pathFile = new PathFile(testFile, testGraph.vertices.length);

        for (int i = 0; i < testGraph.vertices.length; i++) {

            for (int j = 0; j < testGraph.vertices.length; j++) {

                Path path = shortestPath[i][j];

                if (path != null) {

                    pathFile.addPath(path);

                }
            }

            for (int j = 0; j < testGraph.vertices.length; j++) {

                Path path1 = shortestPath[i][j];
                Path path2 = pathFile.getPath(i, j);

                if (path1 == null) {

                    Assert.assertTrue(path2 == null);

                } else {

                    Assert.assertTrue(Math.abs(path1.weight - path2.weight) < 0.001);
                    Assert.assertTrue(path1.path.length == path2.path.length);

                    for (int k = 0; k < path1.path.length; k++) {

                        Assert.assertTrue(path1.path[k] == path2.path[k]);

                    }
                }
            }
        }
        
        pathFile.close();
        
    }
}
