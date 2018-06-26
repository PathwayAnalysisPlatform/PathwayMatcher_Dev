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
import no.uib.pap.pathwaymatcher.dsd.model.paths.SimplePath;
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
     * @throws IOException exception thrown if an error occurred while reading
     * or writing the file
     * @throws InterruptedException exception thrown if a thread gets
     * interrupted
     * @throws TimeoutException exception thrown if computing the shortest path
     * matrix times out
     * @throws DataFormatException exception thrown if the data format is not
     * supported
     */
    public void testPathFile() throws IOException, InterruptedException, TimeoutException, DataFormatException {

        Graph testGraph = GraphPool.getTestGraph();

        PathMatrixInMemory pathMatrix = new PathMatrixInMemory(testGraph);
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

                    Assert.assertTrue(Math.abs(path1.getWeight() - path2.getWeight()) < 0.001);

                    int[] path1Array = path1.getPath();
                    int[] path2Array = path2.getPath();

                    Assert.assertTrue(path1Array.length == path2Array.length);

                    for (int k = 0; k < path1.getPath().length; k++) {

                        Assert.assertTrue(path1Array[k] == path2Array[k]);

                    }
                }
            }
        }

        File gzFile = new File("output/pathFile.gz");
        pathFile.export(gzFile);

        pathFile.close();
        testFile.delete();

    }
}
