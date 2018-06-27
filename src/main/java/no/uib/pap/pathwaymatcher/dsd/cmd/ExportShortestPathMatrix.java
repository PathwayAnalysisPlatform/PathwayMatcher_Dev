package no.uib.pap.pathwaymatcher.dsd.cmd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import no.uib.pap.pathwaymatcher.dsd.PathMatrix;
import no.uib.pap.pathwaymatcher.dsd.io.GraphPool;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;

/**
 * Exports the shortest path matrix.
 *
 * @author Marc Vaudel
 */
public class ExportShortestPathMatrix {

    /**
     * A simple progress handler.
     */
    private ProgressHandler progressHandler = new ProgressHandler();

    /**
     * Exports the shortest path matrix for the graphs available in the pool.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

            int nThreads = Integer.parseInt(args[0]);

            ExportShortestPathMatrix espm = new ExportShortestPathMatrix();
            espm.exportMatrices(nThreads);

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(System.out);
        }
    }

    /**
     * Constructor.
     */
    public ExportShortestPathMatrix() {

    }

    /**
     * Export the shortest paths matrices for all implemented graphs.
     *
     * @param nThreads the number of threads to use
     *
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file
     * @throws java.lang.InterruptedException Exception thrown if a thread gets
     * interrupted
     * @throws java.util.concurrent.TimeoutException Exception thrown if the the
     * process times out
     * @throws DataFormatException exception thrown if the data format is not
     * supported
     */
    public void exportMatrices(int nThreads) throws IOException, InterruptedException, TimeoutException, DataFormatException {

        // Reactome
        String mainTask = "    Reactome";
        progressHandler.start(mainTask);

        String task = "Reactome - Import";
        progressHandler.start(task);
        Graph graph = GraphPool.getReactomeGraph();
        progressHandler.end(task);

        task = "Reactome - Computing shortest path";
        progressHandler.start(task);
        File tempFile = new File("resources/networks/reactome/reactome_path_matrix.tmp");
        PathMatrix pathMatrix = new PathMatrix(graph, tempFile);
        pathMatrix.computeMatrix(nThreads);
        progressHandler.end(task);

        task = "Reactome - Exporting shortest path";
        progressHandler.start(task);
        File destinationFile = new File("resources/networks/reactome/reactome_path_matrix.gz");
        pathMatrix.exportResults(destinationFile);
        tempFile.delete();
        progressHandler.end(task);

        progressHandler.end(mainTask);
        
        if (true) {
            return;
        }

        // Biogrid
        mainTask = "    Biogrid";
        progressHandler.start(mainTask);

        task = "Biogrid - Import";
        progressHandler.start(task);
        graph = GraphPool.getBiogridGraph();
        progressHandler.end(task);

        task = "Biogrid - Computing shortest path";
        progressHandler.start(task);
        tempFile = new File("resources/networks/biogrid/biogrid_path_matrix.tmp");
        pathMatrix = new PathMatrix(graph, tempFile);
        pathMatrix.computeMatrix(nThreads);
        progressHandler.end(task);

        task = "Biogrid - Exporting shortest path";
        progressHandler.start(task);
        destinationFile = new File("resources/networks/biogrid/biogrid_path_matrix.gz");
        pathMatrix.exportResults(destinationFile);
        tempFile.delete();
        progressHandler.end(task);

        progressHandler.end(mainTask);

        // Biogrid
        mainTask = "    Merged";
        progressHandler.start(mainTask);

        task = "Merged - Import";
        progressHandler.start(task);
        graph = GraphPool.getMergedGraph();
        progressHandler.end(task);

        task = "Merged - Computing shortest path";
        progressHandler.start(task);
        tempFile = new File("resources/networks/merged/merged_path_matrix.tmp");
        pathMatrix = new PathMatrix(graph, tempFile);
        pathMatrix.computeMatrix(nThreads);
        progressHandler.end(task);

        task = "Merged - Exporting shortest path";
        progressHandler.start(task);
        destinationFile = new File("resources/networks/merged/merged_path_matrix.gz");
        pathMatrix.exportResults(destinationFile);
        tempFile.delete();
        progressHandler.end(task);

        progressHandler.end(mainTask);

    }

}
