package no.uib.pap.pathwaymatcher.dsd.model.paths;

import no.uib.pap.pathwaymatcher.dsd.model.Path;

/**
 * Path of only one edge.
 *
 * @author Marc Vaudel
 */
public class EdgePath implements Path {
    
    /**
     * The start vertex.
     */
    private final int start;
    /**
     * The end vertex.
     */
    private final int end;
    /**
     * The weight of the edge.
     */
    private final double weight;

    /**
     * Constructor.
     * 
     * @param start The start vertex
     * @param end The end vertex
     * @param weight The weight of the edge.
     */
    public EdgePath(int start, int end, double weight) {
        
        this.start = start;
        this.end = end;
        this.weight = weight;
        
    }

    @Override
    public int[] getPath() {
        return new int[]{start, end};
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public boolean contains(int i) {
        return start == i || end == i;
    }

    @Override
    public boolean[] getTraversedVertices() {
        
        int length = end > start ? end : start;
        
        boolean[] result = new boolean[length+1];
        
        result[start] = true;
        result[end] = true;
        
        return result;
        
    }

    @Override
    public int getLayer() {
        return 0;
    }
    
}
