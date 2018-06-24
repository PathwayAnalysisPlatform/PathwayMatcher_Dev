package no.uib.pap.pathwaymatcher.dsd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Path;
import no.uib.pap.pathwaymatcher.dsd.model.Vertex;

/**
 * This class navigates the graph in all directions and stores the shortest
 * paths.
 *
 * @author Marc Vaudel
 */
public class PathMatrix {

    /**
     * The graph to compute the matrix from.
     */
    private final Graph graph;
    /**
     * Set of indexes already processed.
     */
    private final HashSet<Integer> processedIndexes;
    /**
     * The shortest paths between indexes of the vertices in the graph.
     */
    private final Path[][] shortestPaths;
    /**
     * The number of vertices in the graph.
     */
    private final int nVertices;
    /**
     * Basic progress counter.
     */
    private int progress = 0;

    /**
     * Constructor.
     *
     * @param graph The graph to compute the matrix from.
     */
    public PathMatrix(Graph graph) {

        this.graph = graph;

        nVertices = graph.vertices.length;

        shortestPaths = new Path[nVertices][nVertices];

        processedIndexes = new HashSet<>(nVertices);

    }

    /**
     * Computes the matrix.
     */
    public void computeMatrix() {

        IntStream.range(0, nVertices)
                .parallel()
                .forEach(i -> computeShortestPaths(i));

    }

    /**
     * Returns the matrix of shortest paths.
     *
     * @return the matrix of shortest paths
     */
    public Path[][] getShortestPaths() {
        return shortestPaths;
    }

    /**
     * Returns the shortest paths to all vertices reachable from the given
     * vertex.
     *
     * @param origin the index of the origin vertex
     */
    public void computeShortestPaths(int origin) {
        
        System.out.print(origin + " ");

        SinglePath singlePath = new SinglePath(origin);
        singlePath.computeShortestPaths();
        Path[] paths = singlePath.getShortestPaths();
        shortestPaths[origin] = paths;
        processedIndexes.add(origin);
        
        int tempProgress = (int) (1000.0 * ((double) processedIndexes.size()) / nVertices);
        if (tempProgress > progress) {
            progress = tempProgress;
            tempProgress /= 10;
            System.out.println(tempProgress  + "%");
        }

    }

    /**
     * Convenience class finding the shortest paths to all vertices reachable
     * from a given vertex
     */
    private class SinglePath {

        /**
         * The index of the origin vertex.
         */
        private final int origin;
        /**
         * Array of shortest paths to the vertices reachable from the origin.
         */
        private final Path[] singlePaths;

        /**
         * Constructor.
         *
         * @param origin The index of the origin vertex
         */
        public SinglePath(int origin) {

            this.origin = origin;

            singlePaths = new Path[nVertices];

        }

        /**
         * Computes all shortest paths from the origin index.
         */
        public void computeShortestPaths() {

            Vertex originVertice = graph.vertices[origin];

            for (int i = 0; i < originVertice.neighbors.length; i++) {

                int neighbor = originVertice.neighbors[i];
                double weight = originVertice.weights[i];

                Path tempPath = new Path(new int[]{origin, neighbor}, weight);

                if (singlePaths[neighbor] == null
                        || singlePaths[neighbor].weight > tempPath.weight
                        || singlePaths[neighbor].weight == tempPath.weight && tempPath.length() < singlePaths[neighbor].length()) {

                    singlePaths[neighbor] = tempPath;

                    expand(tempPath);

                }
            }
        }

        /**
         * Expands the given path to all next vertices.
         *
         * @param path the path to expand
         */
        private void expand(Path path) {

            int[] pathI = path.path;

            int lastIndex = pathI[pathI.length - 1];

            if (processedIndexes.contains(lastIndex)) {

                Path[] tempPaths = shortestPaths[lastIndex];

                for (int j = 0; j < tempPaths.length; j++) {

                    if (!path.contains(j)) {

                        Path tempPath = tempPaths[j];

                        if (tempPath != null) {

                            if (singlePaths[j] == null || singlePaths[j].weight > tempPath.weight + path.weight) {

                                Path newPath = Path.concat(path, tempPath);
                                singlePaths[j] = newPath;

                            }
                        }
                    }
                }
            } else {

                Vertex lastVertice = graph.vertices[lastIndex];

                for (int i = 0; i < lastVertice.neighbors.length; i++) {

                    int neighbor = lastVertice.neighbors[i];

                    if (!path.contains(neighbor)) {

                        double weight = lastVertice.weights[i];

                        int[] newPath = Arrays.copyOf(pathI, pathI.length + 1);
                        newPath[pathI.length] = neighbor;

                        Path tempPath = new Path(newPath, weight + path.weight);

                        if (singlePaths[neighbor] == null || singlePaths[neighbor].weight > tempPath.weight) {

                            singlePaths[neighbor] = tempPath;

                            expand(tempPath);

                        }
                    }
                }
            }
        }

        /**
         * Returns an array of shortest paths to the vertices reachable from the
         * origin.
         *
         * @return an array of shortest paths to the vertices reachable from the
         * origin
         */
        public Path[] getShortestPaths() {

            return singlePaths;

        }
    }
}
