package no.uib.pap.pathwaymatcher.dsd;

import java.io.IOException;
import junit.framework.TestCase;
import no.uib.pap.pathwaymatcher.dsd.io.GraphPool;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Path;
import org.junit.Assert;

/**
 * This class tests that the distance estimation is the same as obtained with iGraph.
 *
 * @author Marc Vaudel
 */
public class DistanceTest extends TestCase {

    public void testDistances() throws IOException {

        Graph testGraph = GraphPool.getTestGraph();

        PathMatrix pathMatrix = new PathMatrix(testGraph);
        pathMatrix.computeMatrix();
        Path[][] shortestPath = pathMatrix.getShortestPaths();

        double[][] weights = getIgraphResults();

        for (int i = 0; i < testGraph.vertices.length; i++) {
            
            for (int j = 0; j < testGraph.vertices.length; j++) {

                Path path = shortestPath[i][j];

                if (i == j) {

                    Assert.assertTrue(path == null);

                } else {

                    double error = Math.abs(path.weight - weights[i][j]);
                    Assert.assertTrue(error < 0.001);

                }
            }
        }
    }

    /**
     * Returns the iGraph results.
     * 
     * @return the iGraph results
     */
    private double[][] getIgraphResults() {

        double[][] result = new double[5][5];

        result[0][1] = 0.6989700;
        result[0][2] = 1.477121;
        result[0][3] = 0.7781513;
        result[0][4] = 0.7781513;
        result[1][0] = 0.6989700;
        result[1][2] = 1.477121;
        result[1][3] = 0.7781513;
        result[1][4] = 0.7781513;
        result[2][0] = 1.4771213;
        result[2][1] = 1.4771213;
        result[2][3] = 0.6989700;
        result[2][4] = 0.6989700;
        result[3][0] = 0.7781513;
        result[3][1] = 0.7781513;
        result[3][2] = 0.698970;
        result[3][4] = 0.8450980;
        result[4][0] = 0.7781513;
        result[4][1] = 0.7781513;
        result[4][2] = 0.698970;
        result[4][3] = 0.8450980;

        return result;

    }

}
