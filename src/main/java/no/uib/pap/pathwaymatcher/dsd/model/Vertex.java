package no.uib.pap.pathwaymatcher.dsd.model;

/**
 * Simple model for a vertice.
 *
 * @author Marc Vaudel
 */
public class Vertex {
    
    /**
     * The name of the vertex.
     */
    public final String name;
    /**
     * Array of the indexes of the neighbors of the vertex.
     */
    public final int[] neighbors;
    /**
     * Array of weights for the edges to the neighbors.
     */
    public final double[] weights;
    
    /**
     * Constructor.
     * 
     * @param name the name of the vertex
     * @param neighbors the possible neighbors
     * @param weights the weights of the edges to neighbors
     */
    public Vertex(String name, int[] neighbors, double[] weights) {
        
        this.name = name;
        this.neighbors = neighbors;
        this.weights = weights;
        
    }

}
