package no.uib.pap.pathwaymatcher.dsd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import no.uib.pap.pathwaymatcher.dsd.io.PathFile;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Path;
import no.uib.pap.pathwaymatcher.dsd.model.Vertex;
import no.uib.pap.pathwaymatcher.dsd.model.paths.DoublePath;
import no.uib.pap.pathwaymatcher.dsd.model.paths.EdgePath;
import no.uib.pap.pathwaymatcher.dsd.model.paths.RecursivePath;
import no.uib.pap.pathwaymatcher.dsd.model.paths.SimplePath;

/**
 * This class navigates the graph in all directions and stores the shortest
 * paths. A file is used as back-end to store the paths.
 *
 * @author Marc Vaudel
 */
public class PathMatrix {

    /**
     * The maximal number of layers allowed in a path.
     */
    public final static int maxLayers = 1000;
    /**
     * The graph to compute the matrix from.
     */
    private final Graph graph;
    /**
     * Set of indexes already processed.
     */
    private final HashSet<Integer> processedIndexes;
    /**
     * The file storing the paths.
     */
    private final PathFile pathFile;
    /**
     * The number of vertices in the graph.
     */
    private final int nVertices;
    /**
     * Basic progress counter.
     */
    private int progress = 0;
    /**
     * Boolean indicating whether the process crashed.
     */
    private boolean crashed = false;

    /**
     * Constructor.
     *
     * @param graph The graph to compute the matrix from.
     * @param tempFile The file to use as back-end
     */
    public PathMatrix(Graph graph, File tempFile) {

        this.graph = graph;

        nVertices = graph.vertices.length;

        pathFile = new PathFile(tempFile, nVertices);

        processedIndexes = new HashSet<>(nVertices);

    }

    /**
     * Computes the matrix.
     *
     * @param nThreads the number of threads to use
     *
     * @throws java.lang.InterruptedException Exception thrown if a thread gets
     * interrupted
     * @throws java.util.concurrent.TimeoutException Exception thrown if the the
     * process times out
     */
    public void computeMatrix(int nThreads) throws InterruptedException, TimeoutException {

        ExecutorService pool = Executors.newFixedThreadPool(nThreads);

        for (int origin = 0; origin < nVertices; origin++) {

            SinglePath singlePath = new SinglePath(origin);
            pool.submit(singlePath);

        }

        pool.shutdown();

        if (!pool.awaitTermination(nVertices, TimeUnit.DAYS)) {

            throw new TimeoutException("Shortest path computation timed out.");

        }
    }

    /**
     * Exports the results as gzipped table.
     *
     * @param destinationFile the destination file
     *
     * @throws IOException exception thrown if an error occurred while reading
     * or writing a file
     * @throws DataFormatException exception thrown if the data format is not
     * supported
     */
    public void exportResults(File destinationFile) throws IOException, DataFormatException {

        if (!crashed) {

            pathFile.export(destinationFile);
            pathFile.close();

        }
    }

    /**
     * Convenience class finding the shortest paths to all vertices reachable
     * from a given vertex
     */
    private class SinglePath implements Runnable {

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

        @Override
        public void run() {

            if (crashed) {
                return;
            }

            try {

                System.out.print(origin + " ");

                computeShortestPaths();

                System.out.println(origin + " Saving to file.");

                for (int i = 0; i < singlePaths.length; i++) {

                    Path path = singlePaths[i];

                    if (path != null) {

                        pathFile.addPath(path);

                    }
                }

                if (crashed) {
                    return;
                }

                System.out.println(origin + " Saving completed.");

                processedIndexes.add(origin);

                System.gc();

                int tempProgress = (int) (1000.0 * ((double) processedIndexes.size()) / nVertices);
                if (tempProgress > progress) {
                    progress = tempProgress;
                    tempProgress /= 10;
                    System.out.println(tempProgress + "%");
                }

            } catch (Throwable e) {

                System.out.println(origin + " Crashed.");

                crashed = true;
                e.printStackTrace(System.out);

                throw new RuntimeException(e);
            }
        }

        /**
         * Computes all shortest paths from the origin index.
         */
        public void computeShortestPaths() {

            Vertex originVertice = graph.vertices[origin];
            int nVertices = originVertice.neighbors.length;

            ArrayList<Path> pathsToExpand = new ArrayList<>(nVertices);

            for (int i = 0; i < originVertice.neighbors.length && !crashed; i++) {

                int neighbor = originVertice.neighbors[i];
                double weight = originVertice.weights[i];

                Path singlePath = singlePaths[neighbor];

                if (singlePath == null
                        || singlePath.getWeight() > weight
                        || singlePath.getWeight() == weight && singlePath.length() > 2) {

                    Path startPath = new EdgePath(origin, neighbor, weight);
                    singlePaths[neighbor] = startPath;
                    pathsToExpand.add(startPath);

                }
            }
            
            while (!pathsToExpand.isEmpty()) {
                
                pathsToExpand = pathsToExpand.stream()
                        .flatMap(path -> expand(path).stream())
                        .collect(Collectors.toCollection(ArrayList::new));
                
            }
            
        }

        /**
         * Expands the given path to all next vertices.
         *
         * @param path the path to expand
         *
         * @return the paths left to expand
         */
        private ArrayList<Path> expand(Path path) {

            int lastIndex = path.getEnd();

            if (processedIndexes.contains(lastIndex)) {

                for (int j = 0; j < nVertices; j++) {

                    if (!path.contains(j)) {

                        Path pathExtension = pathFile.getPath(lastIndex, j);

                        if (pathExtension != null) {

                            double totalWeight = pathExtension.getWeight() + path.getWeight();
                            Path singlePath = singlePaths[j];

                            if (singlePath == null
                                    || singlePath.getWeight() > totalWeight
                                    || singlePath.getWeight() == totalWeight && singlePath.length() > pathExtension.length() + path.length()) {

                                DoublePath newPath = new DoublePath(path, pathExtension);
                                singlePaths[j] = newPath;

                            }
                        }
                    }
                }

                return new ArrayList<>(0);

            } else {

                Vertex lastVertice = graph.vertices[lastIndex];
                int nVertices = lastVertice.neighbors.length;

                ArrayList<Path> pathsToExpand = new ArrayList<>(nVertices);

                for (int i = 0; i < nVertices && !crashed; i++) {

                    int neighbor = lastVertice.neighbors[i];

                    if (!path.contains(neighbor)) {

                        double weight = lastVertice.weights[i];
                        double totalWeight = weight + path.getWeight();
                        Path singlePath = singlePaths[neighbor];

                        if (singlePath == null
                                || singlePath.getWeight() > totalWeight
                                || singlePath.getWeight() == totalWeight && singlePath.length() > path.length() + 1) {

                            Path newPath = new RecursivePath(path, neighbor, weight);

                            if (newPath.getLayer() > maxLayers) {

                                newPath = new SimplePath(newPath.getPath(), newPath.getWeight());

                            }

                            singlePaths[neighbor] = newPath;

                            pathsToExpand.add(newPath);

                        }
                    }
                }

                return pathsToExpand;

            }
        }
    }
}
