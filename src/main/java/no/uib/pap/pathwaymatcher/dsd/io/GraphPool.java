package no.uib.pap.pathwaymatcher.dsd.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Vertex;

/**
 * This class provides preset graphs.
 *
 * @author Marc Vaudel
 */
public class GraphPool {

    /**
     * Encoding, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    public static final String reactomeFile = "resources/networks/reactome/reactome_main_component.gz";
    public static final String biogridFile = "resources/networks/biogrid/biogrid_main_component.gz";
    public static final String mergedFile = "resources/networks/merged/merged_main_component.gz";

    /**
     * Returns a simple test graph.
     *
     * @return a simple test graph
     */
    public static Graph getTestGraph() {

        int[] edges1 = {1, 3, 4};
        double[] weights1 = {0.6989700, 0.7781513, 0.7781513};
        Vertex vertex1 = new Vertex("1", edges1, weights1);

        int[] edges2 = {0, 3, 4};
        double[] weights2 = {0.6989700, 0.7781513, 0.7781513};
        Vertex vertex2 = new Vertex("2", edges2, weights2);

        int[] edges3 = {3, 4};
        double[] weights3 = {0.6989700, 0.6989700};
        Vertex vertex3 = new Vertex("3", edges3, weights3);

        int[] edges4 = {0, 1, 2, 4};
        double[] weights4 = {0.7781513, 0.7781513, 0.6989700, 0.8450980};
        Vertex vertex4 = new Vertex("4", edges4, weights4);

        int[] edges5 = {0, 1, 2, 3};
        double[] weights5 = {0.7781513, 0.7781513, 0.6989700, 0.8450980};
        Vertex vertex5 = new Vertex("5", edges5, weights5);

        Vertex[] vertices = {vertex1, vertex2, vertex3, vertex4, vertex5};

        return new Graph(vertices);

    }

    /**
     * Returns a simple directed test graph.
     *
     * @return a simple directed test graph
     */
    public static Graph getTestGraphDirected() {

        int[] edges1 = {1, 3, 4};
        double[] weights1 = {0.6989700, 0.7781513, 0.7781513};
        Vertex vertex1 = new Vertex("1", edges1, weights1);

        int[] edges2 = {3, 4};
        double[] weights2 = {0.7781513, 0.7781513};
        Vertex vertex2 = new Vertex("2", edges2, weights2);

        int[] edges3 = {3, 4};
        double[] weights3 = {0.6989700, 0.6989700};
        Vertex vertex3 = new Vertex("3", edges3, weights3);

        int[] edges4 = {4};
        double[] weights4 = {0.8450980};
        Vertex vertex4 = new Vertex("4", edges4, weights4);

        int[] edges5 = {};
        double[] weights5 = {};
        Vertex vertex5 = new Vertex("5", edges5, weights5);

        Vertex[] vertices = {vertex1, vertex2, vertex3, vertex4, vertex5};

        return new Graph(vertices);

    }

    /**
     * Returns the Reactome graph.
     * 
     * @return the Reactome graph
     * 
     * @throws IOException Exception thrown if an error occurred while reading the file
     */
    public static Graph getReactomeGraph() throws IOException {

        return getGraphFromDataFrame(new File(reactomeFile), false);

    }

    /**
     * Returns the Biogrid graph.
     * 
     * @return the Biogrid graph
     * 
     * @throws IOException Exception thrown if an error occurred while reading the file
     */
    public static Graph getBiogridGraph() throws IOException {

        return getGraphFromDataFrame(new File(reactomeFile), false);

    }

    /**
     * Returns the Merged graph.
     * 
     * @return the Merged graph
     * 
     * @throws IOException Exception thrown if an error occurred while reading the file
     */
    public static Graph getMergedGraph() throws IOException {

        return getGraphFromDataFrame(new File(reactomeFile), false);

    }

    /**
     * Returns the a graph from an iGraph data frame.
     * 
     * @param file the file to import
     * @param directed boolean indicating whether the graph is directed
     * 
     * @return the graph as parsed from the file
     * 
     * @throws IOException Exception thrown if an error occurred while reading the file
     */
    private static Graph getGraphFromDataFrame(File file, boolean directed) throws IOException {

        InputStream fileStream = new FileInputStream(file);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            HashMap<String, Integer> verticesNames = new HashMap<>();
            HashMap<String, HashMap<Integer, Double>> edgesMap = new HashMap<>();
            int vertexCount = 0;

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split(" ");
                String from = lineSplit[0];
                String to = lineSplit[1];
                double weight = Double.parseDouble(lineSplit[2]);

                Integer fromI = verticesNames.get(from);

                if (fromI == null) {

                    fromI = vertexCount++;
                    verticesNames.put(from, fromI);

                }

                Integer toI = verticesNames.get(to);

                if (toI == null) {

                    toI = vertexCount++;
                    verticesNames.put(to, toI);

                }

                HashMap<Integer, Double> fromEdges = edgesMap.get(from);

                if (fromEdges == null) {

                    fromEdges = new HashMap<>(1);
                    edgesMap.put(from, fromEdges);

                }

                fromEdges.put(toI, weight);

                if (!directed) {

                    HashMap<Integer, Double> toEdges = edgesMap.get(to);

                    if (toEdges == null) {

                        toEdges = new HashMap<>(1);
                        edgesMap.put(to, toEdges);

                    }

                    toEdges.put(fromI, weight);

                }
            }

            final Vertex[] vertices = new Vertex[verticesNames.size()];

            verticesNames.entrySet().parallelStream()
                    .forEach(entry -> {

                        String vertexName = entry.getKey();
                        int index = entry.getValue();

                        TreeMap<Integer, Double> vertexEdges = new TreeMap(edgesMap.get(vertexName));

                        int[] edges = new int[vertexEdges.size()];
                        double[] weights = new double[vertexEdges.size()];

                        int i = 0;

                        for (Entry<Integer, Double> entry2 : vertexEdges.entrySet()) {

                            edges[i] = entry2.getKey();
                            weights[i] = entry2.getValue();
                            i++;

                        }

                        Vertex vertex = new Vertex(vertexName, edges, weights);
                        vertices[index] = vertex;

                    });

            return new Graph(vertices);

        }
    }
}
