package no.uib.pap.pathwaymatcher.dsd.cmd;

import java.io.File;
import java.io.IOException;
import no.uib.pap.pathwaymatcher.dsd.PathMatrix;
import no.uib.pap.pathwaymatcher.dsd.io.GraphPool;
import no.uib.pap.pathwaymatcher.dsd.io.PathExport;
import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Path;

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
            
            ExportShortestPathMatrix espm = new ExportShortestPathMatrix();
            espm.exportMatrices();

        } catch (Exception e) {
            e.printStackTrace();
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
     * @throws IOException Exception thrown if an error occurred while reading or writing a file
     */
    public void exportMatrices() throws IOException {
        
        // Reactome
        
        String mainTask = "    Reactome";
        progressHandler.start(mainTask);
        
        String task = "Reactome - Import";
        progressHandler.start(task);
        Graph graph = GraphPool.getReactomeGraph();
        progressHandler.end(task);
        
        task = "Reactome - Computing shortest path";
        progressHandler.start(task);
        PathMatrix pathMatrix = new PathMatrix(graph);
        pathMatrix.computeMatrix();
        Path[][] shortestPath = pathMatrix.getShortestPaths();
        progressHandler.end(task);
        
        
        task = "Reactome - Exporting shortest path";
        progressHandler.start(task);
        File destinationFile = new File("resources/networks/reactome/reactome_path_matrix.gz");
        PathExport.writeExport(shortestPath, graph, destinationFile);
        progressHandler.end(task);
        
        progressHandler.end(mainTask);
        
        
        // Biogrid
        
        mainTask = "    Biogrid";
        progressHandler.start(mainTask);
        
        task = "Biogrid - Import";
        progressHandler.start(task);
        graph = GraphPool.getBiogridGraph();
        progressHandler.end(task);
        
        task = "Biogrid - Computing shortest path";
        progressHandler.start(task);
        pathMatrix = new PathMatrix(graph);
        pathMatrix.computeMatrix();
        shortestPath = pathMatrix.getShortestPaths();
        progressHandler.end(task);
        
        
        task = "Biogrid - Exporting shortest path";
        progressHandler.start(task);
        destinationFile = new File("resources/networks/biogrid/biogrid_path_matrix.gz");
        PathExport.writeExport(shortestPath, graph, destinationFile);
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
        pathMatrix = new PathMatrix(graph);
        pathMatrix.computeMatrix();
        shortestPath = pathMatrix.getShortestPaths();
        progressHandler.end(task);
        
        
        task = "Merged - Exporting shortest path";
        progressHandler.start(task);
        destinationFile = new File("resources/networks/merged/merged_path_matrix.gz");
        PathExport.writeExport(shortestPath, graph, destinationFile);
        progressHandler.end(task);
        
        progressHandler.end(mainTask);
        
    }
    
}
