package no.uib.pap.pathwaymatcher.dsd.io;

import no.uib.pap.pathwaymatcher.dsd.model.Graph;
import no.uib.pap.pathwaymatcher.dsd.model.Vertex;

/**
 * This class provides preset graphs.
 *
 * @author Marc Vaudel
 */
public class GraphPool {
    
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

}
